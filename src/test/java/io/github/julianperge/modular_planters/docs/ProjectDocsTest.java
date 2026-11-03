package io.github.julianperge.modular_planters.docs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Checks the project's own docs: the CurseForge and Modrinth page, the README and the contributor files.
 * The page is pasted into CurseForge and Modrinth, so a link to a file that isn't in git, or that differs in
 * case, breaks there without any build noticing.
 */
class ProjectDocsTest {
    static final List<Path> DOCS = List.of(
            Path.of("docs/description.md"),
            Path.of("DEVELOPMENT.md"),
            Path.of("CONTRIBUTING.md"),
            Path.of(".github/ISSUE_TEMPLATE/bug_report.yml"),
            Path.of(".github/ISSUE_TEMPLATE/feature_request.yml"),
            Path.of(".github/ISSUE_TEMPLATE/config.yml"),
            Path.of(".github/PULL_REQUEST_TEMPLATE.md"),
            Path.of("CHANGELOG.md"),
            Path.of("README.md"));

    private static final int EM_DASH = 0x2014;
    private static final int EN_DASH = 0x2013;
    private static final Path PAGE = Path.of("docs/description.md");
    private static final String RAW = "https://raw.githubusercontent.com/julian-perge/ModularPlanters/";

    private static final Pattern BARE_BLOCK_NAME =
            Pattern.compile("(?<!Modular )Planter (Core|Block|Power|Fertilizer|Export|ME|Frame)");
    private static final Pattern MARKDOWN_TARGET = Pattern.compile("\\]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");
    private static final Pattern HTML_TARGET = Pattern.compile("(?:src|href)=\"([^\"]+)\"");
    private static final Pattern GITHUB_URL = Pattern.compile(
            "https://(raw\\.githubusercontent\\.com|github\\.com)/([^/\\s)\"'>]+)/([^/\\s)\"'>]+)/?([^\\s)\"'>#?]*)");

    @Test
    void readmeLinksGuideContributorDocsAndLogo() throws IOException {
        List<String> targets = targets(Files.readString(Path.of("README.md")));
        for (String expected : List.of("wiki/", "CONTRIBUTING.md", "DEVELOPMENT.md", "docs/media/logo.png")) {
            assertTrue(targets.contains(expected), "README.md does not link " + expected);
        }
    }

    @Test
    void noShotPlaceholdersLeft() throws IOException {
        for (Path doc : existing()) {
            assertTrue(!Files.readString(doc).contains("<!-- shot:"), doc + " still has a screenshot placeholder");
        }
    }

    @Test
    void filesExist() {
        DOCS.forEach(doc -> assertTrue(Files.isRegularFile(doc), doc + " does not exist"));
    }

    @Test
    void noUnicodeDashes() throws IOException {
        for (Path doc : existing()) {
            String text = Files.readString(doc);
            assertTrue(text.indexOf(EM_DASH) < 0 && text.indexOf(EN_DASH) < 0, doc + " has an em or en dash");
        }
    }

    @Test
    void blockNamesAreInGame() throws IOException {
        for (Path doc : existing()) {
            Matcher matcher = BARE_BLOCK_NAME.matcher(Files.readString(doc));
            if (matcher.find()) {
                fail(doc + " uses the short name \"" + matcher.group() + "\" instead of the in-game name");
            }
        }
    }

    @Test
    void linksResolve() throws IOException, InterruptedException {
        Optional<Set<String>> files = trackedFiles(Path.of("").toAbsolutePath());
        assumeTrue(files.isPresent(), "not a git clone, so the link check is skipped");
        Set<String> tracked = files.get();
        for (Path doc : existing()) {
            List<String> broken = brokenLinks(doc, Files.readString(doc), tracked);
            assertTrue(broken.isEmpty(), doc + " has broken links: " + broken);
        }
    }

    @Test
    void noTrackedFilesOutsideAGitClone(@TempDir Path dir) throws IOException, InterruptedException {
        assertEquals(Optional.empty(), trackedFiles(dir));
    }

    @Test
    void wrongCaseRawUrlIsBroken() {
        assertEquals(1, brokenLinks(PAGE, "![x](" + RAW + "main/docs/media/BANNER.JPG)", Set.of("docs/media/banner.jpg")).size());
    }

    @Test
    void untrackedFileIsBroken() {
        assertEquals(1, brokenLinks(PAGE, "![x](" + RAW + "main/docs/media/banner.jpg)", Set.of()).size());
    }

    @Test
    void otherBranchIsBroken() {
        assertEquals(1, brokenLinks(PAGE, "![x](" + RAW + "master/docs/media/banner.jpg)", Set.of("docs/media/banner.jpg")).size());
    }

    @Test
    void otherOwnerIsBroken() {
        String url = "https://raw.githubusercontent.com/julian-perg/ModularPlanters/main/docs/media/banner.jpg";
        assertEquals(1, brokenLinks(PAGE, "![x](" + url + ")", Set.of("docs/media/banner.jpg")).size());
    }

    @Test
    void wrongCaseRelativeLinkIsBroken() {
        assertEquals(1, brokenLinks(Path.of("README.md"), "[x](contributing.md)", Set.of("CONTRIBUTING.md")).size());
    }

    @Test
    void trackedLinksAreFine() {
        String text = "![x](" + RAW + "main/docs/media/banner.jpg) [g](https://github.com/julian-perge/ModularPlanters/tree/main/wiki)"
                + " [i](https://github.com/julian-perge/ModularPlanters/issues/new?template=bug_report.yml) [w](../wiki/)";
        assertEquals(List.of(), brokenLinks(PAGE, text, Set.of("docs/media/banner.jpg", "wiki/docs/overview.mdx")));
    }

    /**
     * Returns every link in the text that would break on GitHub, CurseForge or Modrinth. Relative links and links
     * into this repo must name a file or folder in the git index, with the exact case, because GitHub paths are
     * case-sensitive while the macOS file system is not. A link into the repo must also use the julian-perge owner
     * and the main branch, since that is what the published page loads.
     */
    private static List<String> brokenLinks(Path doc, String text, Set<String> tracked) {
        List<String> broken = new ArrayList<>();
        for (String target : targets(text)) {
            if (target.startsWith("http://") || target.startsWith("https://") || target.startsWith("#")
                    || target.startsWith("mailto:")) {
                continue;
            }
            Path base = doc.getParent() == null ? Path.of("") : doc.getParent();
            String path = base.resolve(target.replaceFirst("#.*$", "")).normalize().toString();
            if (!isTracked(path, tracked)) {
                broken.add(target);
            }
        }
        Matcher url = GITHUB_URL.matcher(text);
        while (url.find()) {
            String owner = url.group(2);
            String repo = url.group(3);
            String rest = url.group(4);
            if (!repo.equalsIgnoreCase("ModularPlanters")) {
                continue;
            }
            if (!owner.equals("julian-perge") || !repo.equals("ModularPlanters")) {
                broken.add(url.group());
                continue;
            }
            String branchAndPath;
            if (url.group(1).startsWith("raw.")) {
                branchAndPath = rest;
            } else if (rest.startsWith("blob/") || rest.startsWith("tree/")) {
                branchAndPath = rest.substring("blob/".length());
            } else {
                continue;
            }
            if (!branchAndPath.startsWith("main/") || !isTracked(branchAndPath.substring("main/".length()), tracked)) {
                broken.add(url.group());
            }
        }
        return broken;
    }

    private static boolean isTracked(String path, Set<String> tracked) {
        String file = path.replaceFirst("/$", "");
        return tracked.contains(file) || tracked.stream().anyMatch(entry -> entry.startsWith(file + "/"));
    }

    /**
     * Returns the files git tracks in the folder, or nothing when the folder isn't a git clone or git isn't
     * installed, as with source downloaded as a zip. The link check then skips instead of failing the build.
     */
    private static Optional<Set<String>> trackedFiles(Path dir) throws InterruptedException {
        try {
            Process git = new ProcessBuilder("git", "ls-files").directory(dir.toFile()).redirectErrorStream(true).start();
            String output = new String(git.getInputStream().readAllBytes());
            if (git.waitFor() != 0) {
                return Optional.empty();
            }
            return Optional.of(new TreeSet<>(Arrays.asList(output.split("\n"))));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static List<String> targets(String text) {
        List<String> targets = new ArrayList<>();
        for (Pattern pattern : List.of(MARKDOWN_TARGET, HTML_TARGET)) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                targets.add(matcher.group(1));
            }
        }
        return targets;
    }

    private static List<Path> existing() {
        return DOCS.stream().filter(Files::isRegularFile).toList();
    }
}

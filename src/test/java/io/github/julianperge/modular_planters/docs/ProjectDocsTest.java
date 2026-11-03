package io.github.julianperge.modular_planters.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Checks the project's own docs: the CurseForge and Modrinth page, the README and the contributor files.
 * The page is pasted into CurseForge and Modrinth, so a link to a file that isn't committed breaks there
 * without any build noticing.
 */
class ProjectDocsTest {
    static final List<Path> DOCS = List.of(
            Path.of("docs/description.md"),
            Path.of("DEVELOPMENT.md"),
            Path.of("CONTRIBUTING.md"),
            Path.of(".github/ISSUE_TEMPLATE/bug_report.yml"),
            Path.of(".github/ISSUE_TEMPLATE/feature_request.yml"),
            Path.of(".github/ISSUE_TEMPLATE/config.yml"),
            Path.of(".github/PULL_REQUEST_TEMPLATE.md"));

    private static final Pattern BARE_BLOCK_NAME =
            Pattern.compile("(?<!Modular )Planter (Core|Block|Power|Fertilizer|Export|ME|Frame)");
    private static final Pattern MARKDOWN_TARGET = Pattern.compile("\\]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");
    private static final Pattern HTML_TARGET = Pattern.compile("(?:src|href)=\"([^\"]+)\"");
    private static final Pattern REPO_URL = Pattern.compile(
            "https://(?:raw\\.githubusercontent\\.com/julian-perge/ModularPlanters/main/"
                    + "|github\\.com/julian-perge/ModularPlanters/(?:blob|tree)/main/)([^\\s)\"'>#]+)");

    @Test
    void filesExist() {
        DOCS.forEach(doc -> assertTrue(Files.isRegularFile(doc), doc + " does not exist"));
    }

    @Test
    void noUnicodeDashes() throws IOException {
        for (Path doc : existing()) {
            String text = Files.readString(doc);
            assertTrue(text.indexOf('—') < 0 && text.indexOf('–') < 0, doc + " has an em or en dash");
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
    void linksResolve() throws IOException {
        for (Path doc : existing()) {
            String text = Files.readString(doc);
            for (String target : targets(text)) {
                if (target.startsWith("http://") || target.startsWith("https://") || target.startsWith("#")
                        || target.startsWith("mailto:")) {
                    continue;
                }
                Path resolved = doc.toAbsolutePath().getParent().resolve(target.replaceFirst("#.*$", "")).normalize();
                assertTrue(Files.exists(resolved), doc + " links to missing " + target);
            }
            Matcher repo = REPO_URL.matcher(text);
            while (repo.find()) {
                assertTrue(Files.exists(Path.of(repo.group(1))), doc + " links to " + repo.group() + ", which is not in the repo");
            }
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

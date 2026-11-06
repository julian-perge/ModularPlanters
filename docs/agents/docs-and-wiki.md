# Docs, wiki and store pages

- `wiki/` pages are MDX, so a placeholder like `<problem>` parses as a JSX tag and breaks the page.
- ProjectDocsTest checks links against `git ls-files`, so a linked file must be tracked and match its exact case.
- `docs/description.md` loads its images from `main`, so push before pasting it into CurseForge or Modrinth.

# Domain Docs

This is a single-context repo: one `GLOSSARY.md` and `docs/adr/` at the root.

## Before exploring, read these

- `GLOSSARY.md`: the project's terms.
- `docs/adr/`: the ADRs that touch the area you are about to work in.

If a file does not exist, proceed silently.

## Use the glossary's vocabulary

Name domain concepts in issue titles, refactor proposals, hypotheses and test names with the terms `GLOSSARY.md` defines, and avoid the synonyms it lists. If a concept is missing from the glossary, either reconsider the name or note the gap for `/domain-modeling`.

## Flag ADR conflicts

When your output contradicts an ADR, say so explicitly:

> _Contradicts ADR-0003 (power and fertilizer are all or nothing), but worth reopening because…_

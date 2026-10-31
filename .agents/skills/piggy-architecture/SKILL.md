---
name: piggy-architecture
description: Guides changes to Piggy's Android architecture, including feature/module refactors, repository boundaries, domain and persistence models, mappers, dependency injection, and navigation.
---

# Piggy architecture workflow

Use this skill when a change affects architecture, module ownership, repository contracts or implementations, persistence mapping, dependency injection, or top-level navigation.

## Workflow

1. Read the canonical [`architecture guide`](../../../docs/architecture/ai-guidance.md). It is the single source of architecture rules; do not rely on a copied summary here.
2. Inspect the relevant module build files, contracts, implementations, bindings, and nearby tests before proposing abstractions or moving code.
3. Trace the call path from UI action or observation through feature state/use case, repository contract, implementation, and data source.
4. Identify the owner and dependency direction of every changed type. Make the smallest coherent change across contracts, implementations, mappings, DI, module dependencies, and callers.
5. Preserve existing cancellation and error behavior. Add focused tests for meaningful mapping or behavior changes when appropriate.
6. Validate with relevant Gradle tasks and report exactly which tasks ran.
7. If the user intentionally changes an architecture decision, update the canonical guide and its concise entry points in `AGENTS.md` and `.github/copilot-instructions.md`.

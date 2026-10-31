# Repository guidance for coding agents

Use this repository's architecture as the default. Before making architecture, data-model, mapping, or module-boundary changes, read [`docs/architecture/ai-guidance.md`](docs/architecture/ai-guidance.md). For tasks that add or move features, repositories, models, or mappings, follow [the Piggy architecture skill](.agents/skills/piggy-architecture/SKILL.md).

## Project invariants

- Keep Firebase and other persistence SDK types inside `core:data` (or their owning infrastructure module); `core:domain` remains platform- and backend-agnostic.
- Shared business models and repository contracts live in `core:domain`; their Firestore implementations live in `core:data`.
- Feature modules own feature UI, presentation state, and feature-specific use cases. They depend on domain contracts, never directly on Firestore.
- `app` is the composition root and owns the centralized Navigation 3 graph.
- Do not introduce Room or an app-owned local synchronization layer without an explicit product decision.
- Keep dependencies acyclic and declare module dependencies in the module build scripts. Shared Android build configuration belongs in `build-logic` convention plugins.

Follow the user's request and existing project conventions where this guidance does not apply. When the code and this document disagree, inspect the implementation and update the guidance if the architecture has intentionally changed.

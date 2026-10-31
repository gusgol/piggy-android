---
name: piggy-architecture
description: Applies Piggy's module boundaries, dependency direction, repository contract placement, persistence mapping, and navigation ownership when adding or refactoring Android features, repositories, domain models, mappers, or Gradle modules.
---

# Piggy architecture workflow

Use this skill when a change touches architectural boundaries, feature/module ownership, repository contracts or implementations, persistence models, mapping, dependency injection, or top-level navigation.

## Read first

Read [`../../../docs/architecture/ai-guidance.md`](../../../docs/architecture/ai-guidance.md) from this skill directory. Inspect the relevant module build files, contracts, and adjacent code before proposing a new abstraction.

## Apply the existing boundaries

1. Place feature UI, ViewModels/screen state, and feature behavior in the owning `feature:*` module.
2. Place Firebase-free business models and repository interfaces in `core:domain`.
3. Place repository implementations, Firestore data sources/document models, SDK interaction, and mapping to/from Firestore in `core:data`.
4. Keep authentication/session integration in `core:auth`, reusable visual components in `core:designsystem`, and app composition/navigation in `app`.
5. Keep dependencies acyclic. Features may depend on core contracts; core modules must not depend on features.
6. Add layer-specific models only when the representations have different semantics, ownership, or validation rules. Map at the boundary, keep conversion deterministic, and avoid pass-through mapper classes.
7. Keep Firestore types out of domain contracts and UI state. Do not introduce Room or app-owned local sync without an explicit product decision.

## Procedure

- Trace the current call path from UI action or observation through the feature state holder/use case, repository contract, implementation, and data source.
- State the intended owner and dependency direction for each changed type before editing.
- Make the smallest coherent change across the contract, implementation, mapper, DI binding, module dependency, and callers.
- Preserve cancellation and existing error semantics. Avoid silently treating malformed persisted data as valid domain data.
- Add focused tests for meaningful mapper and behavior changes where appropriate.
- Run the narrowest relevant Gradle validation and report exactly which tasks ran.
- If the request implies a change to the architectural policy itself, update `docs/architecture/ai-guidance.md` and `AGENTS.md` in the same change.

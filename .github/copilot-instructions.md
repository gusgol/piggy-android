# Repository guidance

Before changing architecture, persistence, model mapping, or Gradle module boundaries, read [`AGENTS.md`](../AGENTS.md) and [`docs/architecture/ai-guidance.md`](../docs/architecture/ai-guidance.md). For feature additions or changes to repository/model/mapping ownership, follow [the Piggy architecture skill](../.agents/skills/piggy-architecture/SKILL.md).

Keep `core:domain` backend-agnostic, keep Firestore implementation and serialization in `core:data`, keep feature presentation/use cases in their feature modules, and keep the Navigation 3 graph in `app`. Do not add Room or app-owned synchronization without an explicit product decision.

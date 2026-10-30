# Piggy – Personal Expense Tracker

A personal expense tracker built with Jetpack Compose and Material 3.

## Features

- Track expenses and assign categories
- Manage custom categories
- Review spending trends
- Import expenses from CSV
- Sign in with Google

## Modules

- `app`: application setup, authentication gate, and the centralized Navigation 3 graph
- `core:domain`: domain models and repository contracts
- `core:data`: Firestore-backed repository implementations
- `core:auth`: authentication and Google Credential Manager integration
- `core:designsystem`: shared Compose theme and components
- `feature:auth`, `:categories`, `:expenses`, `:trends`: feature UI and presentation logic

The app currently persists remote data through Firestore. It does not include a Room database or an app-owned local synchronization layer. Firestore's own offline behavior remains the persistence behavior available to the app.

## Build logic

Shared Android application, Android library, Compose, SDK, and JVM target settings are maintained in `build-logic` convention plugins. Module-specific namespaces, dependencies, and feature plugins remain in each module's build script.

# Project Guide for Gemini

Welcome to the Kotlin/Compose Multiplatform Markdown Editor project. Your role is to assist in developing and refining this application.

## ⭐️ 1. PRIMARY SOURCE OF TRUTH ⭐️

**This is the most important rule.**

Before answering any question or writing any code, you **MUST** read and adhere to the complete project specification located in:

* **`CLAUDE.md`**

This file contains all information regarding:
* Project Architecture (Clean Architecture + MVVM)
* Document JSON Format
* Markdown Feature Requirements (Hugo Frontmatter, CSS Annotations)
* Technical Requirements (Parsing, UI, Sync)
* E Ink Optimized Color Palettes (Light & Dark)
* Development Phases
* Testing Strategy
* Security & Accessibility Guidelines

If any of your suggestions or code implementations conflict with the rules, patterns, or plans in `CLAUDE.md`, you must default to `CLAUDE.md`.

## 2. Core Directives

* **Adhere to `CLAUDE.md`:** All code you write must follow the architecture, file structure, and technical requirements outlined in `CLAUDE.md`.
* **Follow the Plan:** Refer to the "Development Phases" in `CLAUDE.md` to understand the current goals.
* **Use Existing Patterns:** When adding new features, follow the existing patterns (ViewModels, UseCases, Repositories) defined in the "Application Architecture" section of `CLAUDE.md`.
* **Use E Ink Colors:** All UI components must use the color palettes defined in the "E Ink Optimized Color Scheme" section of `CLAUDE.md`.
* **Test-Driven:** All new logic (UseCases, ViewModels, Parsers) must follow the "Testing Strategy" in `CLAUDE.md` and be accompanied by unit tests.

## 3. Key Files for Context

When reasoning about the project, always pay close attention to these files in addition to `CLAUDE.md`:

* `build.gradle.kts` (for all modules)
* `settings.gradle.kts`
* Any `.editorconfig` or `.ktlintrc` files (for code style)
* The existing files in `composeApp/src/commonMain/kotlin/com/appthere/mdwriter/` to learn the established coding style.
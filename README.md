# AI Working Guidelines

## Project Purpose

This project is an Android application that provides multiple creative random selection systems, such as:

- Random Wheel
- Animal Race for Pickora
- Random Number Generator
- Future creative random mechanisms

The goal is to build a lightweight, scalable, and modular system where new random modes can be added easily.

---

## Tech Stack

- Language: Kotlin
- UI: XML Layout
- Architecture: MVVM
- UI Components: Activity / Fragment
- View Binding: Enabled
- Asynchronous tasks: Kotlin Coroutines

---

## Architecture Overview

The project follows the MVVM architecture:

View (Activity / Fragment + XML)
        ↓
ViewModel
        ↓
Repository
        ↓
Data / Random Engine

### Responsibilities

View:
- Handles UI rendering
- Observes ViewModel state
- Contains no business logic

ViewModel:
- Holds UI state
- Handles user actions
- Communicates with repository

Repository:
- Provides random generation logic
- Handles data sources if needed

Model:
- Defines data structures used in the app

---

## Feature-Based Organization

The project should be organized by feature.

Example structure:
animalrace/
    ui/
    viewmodel/
    model/

randomnumber/
    ui/
    viewmodel/
    model/


Each random system should be isolated as a feature module.

---

## Random System Design Principles

The app will contain multiple random systems.

Each system must:

- Be independent
- Reuse shared utilities
- Avoid duplicating logic
- Use shared random engine when possible

Possible random engines:

- Wheel spin logic
- Probability-based selection
- Animation-based race simulation
- Pure numeric random generator

---

## Resource Optimization

Because the app may contain many interactive UI screens, the following rules apply:

- Avoid heavy nested layouts
- Use ConstraintLayout when needed
- Reuse drawable resources
- Avoid duplicated animation files
- Use shared animation utilities

UI animations should be reusable across random systems when possible.

---

## XML Layout Rules

XML layouts should:

- Only describe UI structure
- Contain no logic
- Follow clear naming conventions

Example:
fragment_random_wheel.xml
fragment_animal_race.xml
fragment_random_number.xml

Avoid deep view hierarchies.

---

## Code Guidelines

When generating Kotlin code:

- Follow MVVM separation strictly
- Do not place business logic inside Activity or Fragment
- Keep ViewModel focused on UI state
- Extract reusable logic into utilities or engines

---

## Feature Expansion Rules

The project is expected to grow with additional random systems.

New random systems should:

- Follow the feature-based structure
- Reuse shared components
- Avoid modifying existing features unnecessarily

Example future features:

- Card draw random
- Dice roll system
- Probability picker
- Random challenge generator

---

## Editing Rules for AI

AI must NOT:

- Delete files
- Rename existing files
- Move files between modules
- Perform large architecture refactors
- Introduce new architectural patterns

AI may modify code only when:

- A specific file is provided
- The user explicitly asks for code changes

Default mode: ANALYZE AND SUGGEST.

---

## Code Generation Rules

When generating code:

- Return minimal required code
- Do not rewrite entire files
- Do not modify unrelated logic
- Follow existing naming conventions
- Maintain MVVM structure

---

## Dependency Rules

Do not introduce new libraries without explicit approval.

Examples:

- DI frameworks
- Networking libraries
- Database libraries

---

## Typical Tasks for AI

AI can help with:

- Reviewing MVVM implementation
- Suggesting feature structure
- Designing random algorithms
- Improving UI resource usage
- Debugging crashes
- Suggesting animation systems

---

## Forbidden Behavior

AI must NOT:

- Refactor the entire project
- Introduce a new architecture
- Mass-edit multiple files
- Redesign the entire UI system

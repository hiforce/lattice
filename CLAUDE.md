# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Lattice is a lightweight business extension framework for Java that enables complex business customization through a plugin architecture. It separates platform code from business-specific code and provides unified business identity management across the full chain.

## Build Commands

```bash
# Build all modules
mvn clean install

# Build a specific module
mvn clean install -pl lattice-runtime -am

# Skip tests during build
mvn clean install -DskipTests

# Run tests
mvn test
```

## Module Structure

- **lattice-model**: Core model classes, annotations, and SPI interfaces. Contains fundamental abstractions (`IAbility`, `IBusinessExt`, `ITemplate`) and all annotations.
- **lattice-runtime**: Runtime engine for ability registration, extension execution, caching, and session management. Contains `Lattice` singleton as the main entry point.
- **lattice-remote**: Remote invocation support with Dubbo integration for distributed extension execution.
- **lattice-tools**: Build tools including Maven plugin for generating `lattice.json` metadata and dynamic class loading utilities.

## Core Architecture

### Key Annotations

- `@Ability` - Marks a class as a domain ability with extension points
- `@Extension` - Marks methods as extension points within an ability interface
- `@Business` - Defines a business template with code, name, and priority
- `@Product` - Defines a product template (lower priority than business)
- `@UseCase` - Defines reusable use case implementations
- `@Realization` - Binds extension implementations to specific business/product codes

### Template Hierarchy (by priority, low to high)

1. **Product** (default priority 500) - Base product implementation
2. **UseCase** (default priority 100) - Reusable cross-business logic
3. **Business** (default priority 1000) - Business-specific customization

### Extension Execution Flow

1. `Lattice.getInstance().start()` initializes the framework
2. Abilities are created via `Lattice.getFirstMatchedAbility()` or `Lattice.getAllAbilities()`
3. Extension methods are invoked through `reduceExecute()` with a `Reducer` policy
4. Reducers determine how multiple realizations are combined

### Reducer Policies (in `Reducers` class)

- `Reducers.firstOf()` - Returns first matching result
- `Reducers.allMatch()` - Returns true if all match predicate
- `Reducers.anyMatch()` - Returns true if any match predicate
- `Reducers.none()` - Returns all results as a list

### Key Classes

- `Lattice` - Main singleton entry point; call `start()` to initialize
- `BaseLatticeAbility` - Base class for all abilities; provides `reduceExecute()` for extension invocation
- `IBusinessExt` - Interface for business extension implementations
- `BusinessConfig` - Runtime configuration mapping businesses to products and extension priorities

### SPI Mechanism

The framework uses Java SPI (META-INF/services) for discovering implementations of:
- `IAbility`, `IBusiness`, `IProduct`, `IUseCase`, `IBusinessExt`

### Maven Plugin

The `lattice-maven-build-plugin` generates `META-INF/lattice/lattice.json` containing metadata about all abilities, businesses, products, use cases, and realizations.

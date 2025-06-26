Goat Pattern
===

Goat is a Java dependency of the reactive-like developing pattern.
It provides a set of abstract tools to make it detached between systems / services.

## Getting Started

- To use Goat Pattern in your project, add the following dependencies to your Maven pom.xml:
- (It's not in the maven repository. You might have to build it yourself.)

```xml

<dependency>
    <groupId>com.hiro.goat</groupId>
    <artifactId>goat-api</artifactId>
    <version>0.0.1</version>
</dependency>
<dependency>
<groupId>com.hiro.goat</groupId>
<artifactId>goat-core</artifactId>
<version>0.0.1</version>
</dependency>
```

## Maven Commends

### Build Tool

1. Build dependency config (pom)

```shell

cd ./goat-dependencies
mvn clean install
```

2. Build project

```shell

mvn clean install -am -DskipTests
```

### Run Unit Tests

```shell

mvn clean test -am
# Or run verify
mvn clean verify -am
```

## Core Features

### Identity Generation

- Snowflake-based ID generation for distributed systems
- Support for multiple devices with unique device IDs
- High-performance, collision-free ID generation

### Component Lifecycle Management

- Standardized lifecycle for all components (start, stop, pause, resume, destroy)
- Abstract implementation with hooks for customization
- Thread-safe state transitions

### Task Processing

- Task-based execution model with success tracking
- Support for rollback operations
- Pre-processing and post-processing hooks
- Chainable tasks for complex workflows

### Worker Model

- Worker abstraction for background processing
- Queue-based dispatch workers for asynchronous processing
- Lifecycle management for workers

### Messaging System

- Postal system metaphor for message passing
- Support for direct, group, and broadcast messaging
- Secure message signing and verification
- Mailbox abstraction for message reception

### Platform Abstraction

- Abstract platform for building distributed systems
- Identity-based platform instances
- Extensible design for custom platforms

## Project Structure

- **goat-api**: Core interfaces and abstractions
- **goat-core**: Implementation of core components
- **goat-platform**: Platform-specific implementations
- **goat-dependencies**: Dependency management

## License

Goat-framework is licensed under GPL v3 with a linking exception. See LICENSE for details.

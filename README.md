# topsoil-seeder

**topsoil-seeder** is a lightweight Java library designed to simplify database seeding during development. It allows developers to populate their databases using human-readable YAML or JSON files, automatically handling entity relationships and persistence.

[![Maven Central](https://img.shields.io/badge/Maven--Central-v1.0.0-blue)](https://search.maven.org/)
[![License](https://img.shields.io/badge/License-Apache--2.0-green)](LICENSE)

## 🛠️ Requirements
- Java JDK 17 or higher.
- A JPA implementation (Hibernate, EclipseLink, etc.).

## ✨ Key Features

- **Declarative Seeding**: Define your initial state in YAML/JSON instead of verbose SQL scripts.
- **Entity Relationship Handling**: Smart resolution of Foreign Keys based on JPA annotations.
- **Spring Boot Starter**: Plug-and-play integration with Spring Boot projects.
- **Environment Aware**: Run seeds only in specific profiles (e.g., `dev`, `test`).
- **Reflection Based**: Automatically maps data to your JPA Entities without boilerplate code.

## 📦 Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.mgluizbrito</groupId>
    <artifactId>topsoil-seeder</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 🚀 Quick Start
1. Create a folder seeds (ex. `src/main/resources/seeds/`).
2. Create a file named like seed.yaml:

```yaml
- entity: User
  data:
    - id: 1
      name: "Admin"
      email: "admin@example.com"
- entity: Post
  data:
    - title: "My First Post"
      author: "@User:1" # Smart reference to the user above
```
3. Start your application. The library will scan and populate the database automatically.

## What is the difference for `Instancio` or `data.sql`?`
| Resource:     |    data.sql (Spring)     |     Instancio      |      topsoil-seeder       |
|:--------------|:------------------------:|:------------------:|:-------------------------:|
| Sintaxe       |    Pure SQL (verbose)    |     Java Code      |         YAML File         |
| Relationships | Need to know IDs on hand |    Auto/Randon     |  Reference (by name/id)   |
| Flexibility   |          Rigid           | Great for Testing  | Great for dev and mock-up |
| Portability   |  Depends on DB dialect   | N/A (object level) |      DB Independent       |


## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.
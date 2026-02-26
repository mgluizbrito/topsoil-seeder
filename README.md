# topsoil-seeder

**topsoil-seeder** is a lightweight Java library designed to simplify database seeding during development. It allows developers to populate their databases using human-readable YAML or JSON files, automatically handling entity relationships and persistence.

### **Recommended for development and test environments with clean DBs (ex: h2 database) or with the `create-drop` property set**

[![Maven Central](https://img.shields.io/badge/Maven--Central-v1.0.0-blue)](https://search.maven.org/)
[![License](https://img.shields.io/badge/License-Apache--2.0-green)](LICENSE)

## 🛠️ Requirements
- Java JDK 17 or higher.
- A JPA implementation (Hibernate, EclipseLink, etc.).

## ✨ Key Features

- **Declarative Seeding**: Define your initial state in YAML instead of verbose SQL scripts.
- **Base Package Support**: Configure a base package to use short entity names (e.g., `User` instead of `com.app.model.User`).
- **Transactional Integrity**: Automatic `rollback` on failure, ensuring your database remains consistent.
- **Reflection Based**: Automatically maps YAML keys to JPA Entity fields/setters.
- **H2 & Integration Ready**: Perfect for dev environments and integration tests.

## 📦 Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.mgluizbrito</groupId>
    <artifactId>topsoil-seeder-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

## 🚀 Quick Start
1. Create Department and Employee entities with the same attributes below (for ID you can use @Id + @GeneratedValue, doesn't have to be exactly a string or integer).
2. Create a folder seeds (ex. `src/main/resources/seeds/`).
3. Create a file named like 01-departments.yaml:

```yaml
entity: Department
data:
  - _id: dept-tech
    name: "Technology"
  - _id: dept-hr
    name: "Human Resources"
```
And other file like 02-employees.yaml:
```yaml
# If you don't define a setBasePackage as described below, you need to pass the entire package path here, for example...
entity: com.yourproject.model.Employee
data:
  - name: "Lucy Angkatell"
    department: "@dept-tech"
  - name: "John Christow"
    department: "@dept-hr"
  - name: "Henrietta Hardcastle"
    department: "@dept-hr"
```

4. Run the SeedEngine. The SeedEngine requires a JPA EntityManager to operate.

```java
EntityManager em = entityManagerFactory.createEntityManager();
SeedEngine engine = new SeedEngine(em);

// (Optional) Define where your entities are located to use short names in YAML 
engine.setBasePackage("com.yourproject.model");

// Execute seeding from a specific resource folder
engine.seed();
```

## 🛡️ Transaction Handling
The library follows a strict transactional flow to protect your data:
1. **Begin**: Opens a transaction before processing files.
2. **Process**: Maps and persists each entity block.
3. **Commit**: Saves changes if everything is successful.
4. **Rollback**: If an EntityClassNotFoundException or any RuntimeException occurs, the transaction is automatically rolled back and the exception is re-thrown.

## What is the difference for `Instancio` or `data.sql`?`
| Resource:     |    data.sql (Spring)     |     Instancio      |      topsoil-seeder       |
|:--------------|:------------------------:|:------------------:|:-------------------------:|
| Sintaxe       |    Pure SQL (verbose)    |     Java Code      |         YAML File         |
| Relationships | Need to know IDs on hand |    Auto/Randon     |  Reference (by name/id)   |
| Flexibility   |          Rigid           | Great for Testing  | Great for dev and mock-up |
| Portability   |  Depends on DB dialect   | N/A (object level) |      DB Independent       |


## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.
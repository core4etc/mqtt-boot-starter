# Core4etc MQTT Framework

**A comprehensive Java-based MQTT framework designed for building robust IoT and messaging applications with built-in support for Redis, database connectivity, and flexible configuration management.**

---

## Features

- MQTT Client Management: Simplified MQTT operations with automatic reconnection
- Redis Integration: Built-in support for Redis with key expiration listening
- Database Connectivity: JDBC connection pooling with automatic recovery
- Flexible Configuration: Support for YAML and properties files with nested structure
- Dependency Injection: Lightweight bean container for dependency management
- Modular Design: Extensible architecture with clear separation of concerns
- Licensed under Apache License 2.0

---

## Quick Start

### Prerequisites

- Java 8 or higher
- Maven or Gradle for dependency management
- An MQTT broker (e.g., [Eclipse Mosquitto](https://mosquitto.org/), HiveMQ or EMQX)

---

## Installation

### Maven

```bash
git clone https://github.com/your-org/core4etc-mqtt.git
cd core4etc-mqtt
mvn clean install

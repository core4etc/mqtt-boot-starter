# Core4etc MQTT Framework (mqtt-boot-starter)

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

```bash
git clone https://github.com/your-org/core4etc-mqtt.git
cd core4etc-mqtt
mvn clean install
```

## Basic Usage
```java
// Simple application bootstrap
Application.run();

// Or with custom configuration
Application.configure()
    .withSystem(new CustomSystemLoader())
        .withRedis(new CustomRedisLoader())
        .run();

// Access components
SystemConfig config = Application.getSystemConfig();
Optional<RedisClient> redis = Application.getRedisClient();
Optional<Connection> dbConnection = Application.getDatasourceConnection();
Optional<IMqttClient> mqttClient = Application.getMqttClient();
```

## Configuration
Create a configuration file in one of the supported formats:

### YAML Configuration Example (application.yaml)
```yaml
core4etc:
  application:
    name: "mqtt-service"
  mqtt:
    url: "broker.example.com"
    protocol: "tcp"
    port: "1883"
    username: "mqtt-user"
    password: "mqtt-password"
  database:
    url: "postgresql.example.com"
    port: "5432"
    name: "appdb"
    username: "dbuser"
    password: "dbpassword"
  redis:
    url: "redis.example.com"
    port: "6379"
    password: "redispass"
    subscribe: true
```

### Properties Configuration Example (application.properties)
```properties
core4etc.application.name=mqtt-service
core4etc.mqtt.url=broker.example.com
core4etc.mqtt.protocol=tcp
core4etc.mqtt.port=1883
core4etc.mqtt.username=mqtt-user
core4etc.mqtt.password=mqtt-password
core4etc.database.url=postgresql.example.com
core4etc.database.port=5432
core4etc.database.name=appdb
core4etc.database.username=dbuser
core4etc.database.password=dbpassword
core4etc.redis.url=redis.example.com
core4etc.redis.port=6379
core4etc.redis.password=redispass
core4etc.redis.subscribe=true
```

Place your configuration file in a directory and set the config.dir system property:
```bash
java -Dconfig.dir=/path/to/config -jar your-application.jar
```

## Core Components

### MQTT Template
The MqttTemplate class provides a high-level abstraction for MQTT operations:
```java
try (MqttTemplate mqttTemplate = new MqttTemplate()) {
        // Publish messages
        mqttTemplate.publish("sensors/temperature", "25.6");
        mqttTemplate.publish("sensors/humidity", "45.2", 1, true);

        // Subscribe with message handlers
        mqttTemplate.subscribe("sensors/#", message -> {
            System.out.println("Received: " + new String(message.getPayload()));
        });

        // Check connection status
        if (mqttTemplate.isConnected()) {
            System.out.println("Connected to MQTT broker");
        }
}
```

### Redis Template
The RedisTemplate class simplifies Redis operations:
```java
RedisTemplate redisTemplate = new RedisTemplate();

// Store data with expiration
redisTemplate.put("user:123", userObject, 3600L); // Expires in 1 hour

// Retrieve data
String value = redisTemplate.get("user:123");
User user = redisTemplate.getDisconnected("user:123");

// Close connection (typically at shutdown)
redisTemplate.close();
```

### Database Template
The DatasourceTemplate manages database connections:
```java
// Get a connection (automatically validates and recovers if needed)
Connection connection = DatasourceTemplate.getConnection();

try (Statement stmt = connection.createStatement();
ResultSet rs = stmt.executeQuery("SELECT * FROM table")) {

        while (rs.next()) {
        // Process results
        }
}

// Close all connections (at application shutdown)
DatasourceTemplate.close();
```

### Custom Callbacks
Extend the Callback class for MQTT event handling:
```java
// Get a connection (automatically validates and recovers if needed)
public class SensorCallback extends Callback {

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // Handle incoming messages
        String payload = new String(message.getPayload());
        System.out.println("Received on " + topic + ": " + payload);

        // Use the built-in MQTT template for responses
        getMqttTemplate().publish("ack/" + topic, "Message processed");
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Handle delivery confirmation
        System.out.println("Message delivered: " + token.getMessageId());
    }
}
```

## Advanced Usage

### Custom Loaders
Implement custom loaders for specialized component initialization:
```java
// Get a connection (automatically validates and recovers if needed)
public class CustomRedisLoader implements Loader<RedisClient> {
    @Override
    public RedisClient load() throws Exception {
        // Custom Redis client configuration
        return RedisClient.create("redis://custom-host:6379");
    }
}

// Use custom loader in application configuration
Application.configure().withRedis(new CustomRedisLoader()).run();
```

### Bean Management
Use the Bean container for dependency management:
```java
// Register a component
MyService service = new MyService();
Bean.create(service);

// Retrieve a component
MyService retrievedService = Bean.get(MyService.class);

// Check if a component exists
boolean exists = Bean.exists(MyService.class);

// Remove a component
Bean.remove(MyService.class);
```

## API Documentation

### Bean Container (com.core4etc.mqtt.bean.Bean)
Singleton bean container that manages object instances using a concurrent map.
```java
// Create or get a singleton instance
MyService service = Bean.create(myServiceInstance);

// Retrieve a singleton instance
MyService service = Bean.get(MyService.class);

// Check if a bean exists
boolean exists = Bean.exists(MyService.class);

// Remove a bean from the container
Bean.remove(MyService.class);
```

### Loader Interface (com.core4etc.mqtt.load.Loader)
Functional interface for loading resources with exception handling.
```java
Loader<String> fileLoader = () -> Files.readString(Path.of("config.txt"));

// With exception handling
String content = fileLoader.load(); // throws IOException

// Or with runtime exception wrapping
String content = fileLoader.get(); // throws RuntimeException if IOException occurs
```

### System Configuration (com.core4etc.mqtt.config.SystemConfig)
Record-based configuration structure containing all application settings.
```java
SystemConfig config = Application.getSystemConfig();

// Access configuration values
String appName = config.core4etc().application().name();
String mqttUrl = config.core4etc().mqtt().url();
String dbName = config.core4etc().database().name();
```

### Error Handling
The framework provides comprehensive error handling:
```java
try {
        // Framework operations
        Application.run();
} catch (Exception e) {
        // Handle initialization errors
        System.err.println("Application failed to start: " + e.getMessage());
        e.printStackTrace();
}

// MQTT operations throw MqttException
try {
        mqttTemplate.publish("topic", "message");
} catch (MqttException e) {
        System.err.println("MQTT operation failed: " + e.getMessage());
        // Handle reconnection or fallback logic
}
```

## Performance Considerations

- Connection Pooling: Database and Redis connections are managed efficiently
- Thread Safety: Most components are thread-safe for concurrent access
- Memory Management: Proper resource cleanup with AutoCloseable implementations
- Reconnection Logic: Automatic reconnection with configurable delays

## Contributing

1. Fork the repository
2. Create a feature branch (git checkout -b feature/amazing-feature)
3. Commit your changes (git commit -m 'Add amazing feature')
4. Push to the branch (git push origin feature/amazing-feature)
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the [LICENSE](https://license/) file for details.

## Support
For support and questions:
- Create an issue on GitHub
- Check the documentation
- Join our community forum

## Versioning
We use [SemVer](http://semver.org/) for versioning. For available versions, see the [tags](https://github.com/core4etc/mqtt-boot-starter/tags) on this repository.

## Acknowledgments
- Eclipse Paho for MQTT client implementation
- Lettuce for Redis client implementation
- Jackson for YAML and JSON processing
- All contributors who helped shape this framework
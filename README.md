# Biometric Test Application

Spring Boot application for connecting to ZKTeco biometric devices (e.g., iFace 702) and synchronizing biometric templates.

## Features

- Connect to ZKTeco devices via COM/ActiveX interface
- Sync fingerprint templates from the device to local database
- REST API for triggering synchronization
- H2 database for local storage

## Requirements

- Windows operating system (required for COM/ActiveX)
- Java 17 or higher
- Maven 3.6+
- ZKTeco SDK installed and registered on Windows
- ZKTeco device accessible on the network

## Configuration

Edit `src/main/resources/application.yml` to configure your device:

```yaml
zkteco:
  ip: 192.168.1.127        # Device IP address
  port: 4370               # Device port (default: 4370)
  password: ""             # Communication password (if set on device)
  machine-number: 1        # Machine number (usually 1)
```

## Device Setup

This application is configured to connect to:
- **Device**: ZKTeco iFace 702
- **IP Address**: 192.168.1.127
- **Port**: 4370
- **Server IP**: 192.168.1.109 (where this application runs)

## Building

```bash
./mvnw clean package
```

## Running

```bash
./mvnw spring-boot:run
```

Or run the JAR:

```bash
java -jar target/biometrictest-0.0.1-SNAPSHOT.jar
```

## API Endpoints

- `GET /api/biometric/sync` - Trigger template synchronization from device
- `GET /h2-console` - H2 database console (dev mode)

## Native Libraries

The application uses JACOB (Java-COM Bridge) which requires native Windows DLLs. These are located in the `libs/` directory and are automatically included in the build. See `libs/README.md` for details.

## Troubleshooting

### Maven Import Issues

If you encounter errors like `Could not find artifact net.sf.jacob-project:jacob:jar:1.20` when importing the project:

**Common Causes:**
- Outdated Maven metadata in your local `.m2` repository
- IDE cached configuration pointing to a different version
- Previous project configuration that used a different JACOB version

**Quick Solutions:**

1. **Force update Maven dependencies** (recommended first step):
   ```bash
   mvn clean install -U
   ```
   The `-U` flag forces Maven to update snapshots and releases from remote repositories.

2. **Clear your local Maven repository cache**:
   ```bash
   # On Windows
   rmdir /s /q %USERPROFILE%\.m2\repository\net\sf\jacob-project
   
   # On Linux/Mac
   rm -rf ~/.m2/repository/net/sf/jacob-project
   ```
   Then run `mvn clean install -U` again.

3. **IntelliJ IDEA**: Right-click on `pom.xml` → Maven → Reload Project

4. **Eclipse**: Right-click on project → Maven → Update Project (check "Force Update")

**📖 For detailed troubleshooting steps, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md)**

### JACOB-Specific Issues

See `libs/README.md` for JACOB-specific troubleshooting.

### Device Connection Issues

For device connection issues:
- Verify network connectivity to the device
- Check firewall settings
- Ensure ZKTeco SDK is properly installed
- Verify device IP and port in application.yml

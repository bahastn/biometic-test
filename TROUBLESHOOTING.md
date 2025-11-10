# Troubleshooting Guide

This document provides detailed troubleshooting steps for common issues with the Biometric Test Application.

## Maven Import / Dependency Issues

### Issue: "Could not find artifact net.sf.jacob-project:jacob:jar:1.20"

**Error Message:**
```
Could not find artifact net.sf.jacob-project:jacob:jar:1.20 at specified path C:\Users\<username>\IdeaProjects\biometrictest\libs\jacob-1.20.jar
```

**Root Cause:**
This error typically occurs when:
1. Your local Maven repository (`.m2/repository`) has cached metadata pointing to an incorrect version
2. Your IDE has cached project configuration from a previous setup
3. You previously had a different version of JACOB configured

**Solution Steps:**

### Step 1: Force Maven Update (Recommended First Step)

Run Maven with the `-U` flag to force update all dependencies:

```bash
# On Windows (Command Prompt)
mvnw clean install -U

# On Windows (PowerShell)
.\mvnw clean install -U

# On Linux/Mac
./mvnw clean install -U
```

The `-U` flag forces Maven to:
- Check remote repositories for updated releases and snapshots
- Download newer versions if available
- Refresh local repository metadata

### Step 2: Clear Maven Cache (If Step 1 Fails)

If the issue persists, clear the JACOB-specific Maven cache:

**Windows:**
```batch
rmdir /s /q %USERPROFILE%\.m2\repository\net\sf\jacob-project
```

**Linux/Mac:**
```bash
rm -rf ~/.m2/repository/net/sf/jacob-project
```

Then run:
```bash
mvn clean install -U
```

### Step 3: IDE-Specific Solutions

#### IntelliJ IDEA

1. **Reload Maven Project:**
   - Right-click on `pom.xml`
   - Select `Maven` → `Reload Project`

2. **Invalidate Caches:**
   - Go to `File` → `Invalidate Caches / Restart`
   - Check "Invalidate and Restart"

3. **Reimport:**
   - Right-click on `pom.xml`
   - Select `Maven` → `Reimport`

4. **Force re-download:**
   - Open Maven tool window (View → Tool Windows → Maven)
   - Click the refresh icon (🔄)
   - Or right-click on the project → Maven → Download Sources and Documentation

#### Eclipse

1. **Update Maven Project:**
   - Right-click on the project
   - Select `Maven` → `Update Project...`
   - Check "Force Update of Snapshots/Releases"
   - Click `OK`

2. **Clean Project:**
   - Select `Project` → `Clean...`
   - Choose the biometrictest project
   - Click `OK`

3. **Rebuild:**
   - Right-click on project → `Run As` → `Maven clean`
   - Then → `Run As` → `Maven install`

#### Visual Studio Code

1. **Clean Maven:**
   ```bash
   mvn clean
   ```

2. **Reload Java Projects:**
   - Open Command Palette (Ctrl+Shift+P or Cmd+Shift+P)
   - Type "Java: Clean Java Language Server Workspace"
   - Restart VS Code

3. **Reload Window:**
   - Command Palette → "Developer: Reload Window"

### Step 4: Verify pom.xml Configuration

Ensure your `pom.xml` has the correct JACOB dependency:

```xml
<dependency>
    <groupId>net.sf.jacob-project</groupId>
    <artifactId>jacob</artifactId>
    <version>1.14.3</version>
</dependency>
```

**Important:** The version should be `1.14.3`, not `1.20` or any other version.

### Step 5: Check for Local System-Scoped Dependencies

Search your `pom.xml` for any `<systemPath>` references to JACOB:

```bash
# On Windows (PowerShell)
Select-String -Path pom.xml -Pattern "systemPath"

# On Linux/Mac
grep -n "systemPath" pom.xml
```

If you find any `<systemPath>` entries for JACOB, remove them. The dependency should be resolved from Maven Central, not from a local file.

### Step 6: Verify Maven Settings

Check if you have a custom Maven `settings.xml` that might be causing issues:

**Location:**
- Windows: `%USERPROFILE%\.m2\settings.xml`
- Linux/Mac: `~/.m2/settings.xml`

Ensure there are no conflicting repository configurations or mirrors that might be blocking access to Maven Central.

## JACOB Runtime Issues

### Issue: UnsatisfiedLinkError

**Error:**
```
java.lang.UnsatisfiedLinkError: no jacob in java.library.path
```

**Solutions:**
1. Ensure you're using the correct JVM architecture (64-bit JVM for jacob-x64.dll)
2. The DLLs should be automatically copied to the classpath during build
3. Rebuild the project: `mvn clean package`

See `libs/README.md` for more details.

### Issue: Can't co-create object

**Error:**
```
Can't co-create object
```

**Solutions:**
1. Verify ZKTeco SDK is installed
2. Register the zkemkeeper.dll:
   ```batch
   regsvr32 zkemkeeper.dll
   ```
3. Run the application with administrator privileges

## Device Connection Issues

### Cannot Connect to Device

**Checklist:**
1. ✅ Device is powered on
2. ✅ Device is on the same network or accessible via network
3. ✅ IP address in `application.yml` matches device IP
4. ✅ Port 4370 is open (or custom port if changed)
5. ✅ Firewall allows connection to device
6. ✅ ZKTeco SDK is installed on the server machine

**Test Connection:**
```bash
# Test if device is reachable
ping 192.168.1.127

# Test if port is open (requires telnet client)
telnet 192.168.1.127 4370
```

## Build Issues

### Build Fails with Missing Dependencies

Run Maven with debug output:
```bash
mvn clean install -U -X
```

This will show detailed information about what Maven is trying to download and from where.

### Tests Fail

Skip tests during build if needed:
```bash
mvn clean package -DskipTests
```

Then run tests separately to diagnose:
```bash
mvn test
```

## Getting Help

If none of these solutions work:

1. **Check Maven version:**
   ```bash
   mvn --version
   ```
   Ensure you have Maven 3.6+ and Java 17+

2. **Check Java version:**
   ```bash
   java -version
   ```
   Ensure you have Java 17 or higher

3. **Collect diagnostic information:**
   ```bash
   mvn dependency:tree > dependencies.txt
   mvn help:effective-pom > effective-pom.xml
   ```

4. **Create an issue** with:
   - Full error message
   - Maven version
   - Java version
   - IDE and version
   - Operating system
   - Contents of `dependencies.txt` and `effective-pom.xml`

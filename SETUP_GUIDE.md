# Biometric Application Setup Guide

This guide will help you set up and use the biometric test application to connect to your ZKTeco device and monitor real-time attendance punches.

## Prerequisites

1. **Windows Operating System** - Required for COM/ActiveX integration with ZKTeco SDK
2. **Java 17 or higher** - [Download from Oracle](https://www.oracle.com/java/technologies/downloads/)
3. **Maven 3.6+** - [Download from Apache](https://maven.apache.org/download.cgi)
4. **ZKTeco SDK** - Must be installed and registered on Windows
5. **ZKTeco Biometric Device** - Accessible on your network (e.g., iFace 702)

## Configuration

### 1. Device Configuration

Edit `src/main/resources/application.yml` or `src/main/resources/application.properties`:

```yaml
zkteco:
  ip: 192.168.1.127        # Your device IP address
  port: 4370               # Device port (default: 4370)
  password: ""             # Communication password (if set on device)
  machine-number: 1        # Machine number (usually 1)
```

### 2. Database Configuration

The application uses H2 file-based database. No configuration needed - it will automatically create the database file at `./data/biometrictest.mv.db`.

## Building the Application

```bash
mvn clean package
```

This will create `target/biometrictest-0.0.1-SNAPSHOT.jar`

## Running the Application

### Option 1: Using Maven
```bash
mvn spring-boot:run
```

### Option 2: Using JAR file
```bash
java -jar target/biometrictest-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

## Accessing the Application

Once the application is running, open your web browser and navigate to:

### Home Page
**http://localhost:8080/**

The home page provides:
- Quick access to all features
- Real-time punch monitor dashboard
- H2 database console
- Connection testing tool
- API endpoint documentation

### Real-time Punch Monitor Dashboard
**http://localhost:8080/dashboard**

This dashboard provides:
- Real-time attendance monitoring via Server-Sent Events (SSE)
- Live punch notifications as they happen
- Statistics (check-ins, check-outs, session time)
- Verification mode indicators (fingerprint, card, face, etc.)

### H2 Database Console
**http://localhost:8080/h2-console**

Access the database console with these settings:
- **JDBC URL**: `jdbc:h2:file:./data/biometrictest`
- **Username**: `sa`
- **Password**: *(leave empty)*

## Using the Application

### Step 1: Test Device Connection

1. Go to the home page: http://localhost:8080/
2. Click on "Test Device Connection"
3. Confirm the test
4. Check the result:
   - ✅ **Success**: Connection working, ready to sync data
   - ❌ **Failed**: Check device IP, network, and SDK installation

### Step 2: Sync Biometric Templates

Sync fingerprint/face templates from the device to the database:

**Via Home Page:**
- Use the API endpoint: POST to `/zkteco/sync`

**Via Command Line:**
```bash
curl -X POST http://localhost:8080/zkteco/sync
```

### Step 3: Sync Attendance Punch Logs

Sync all punch records from the device:

**Via Dashboard:**
- Click the "Sync Punches" button

**Via Command Line:**
```bash
curl -X POST http://localhost:8080/zkteco/sync-punches
```

### Step 4: Monitor Real-time Punches

1. Open the dashboard: http://localhost:8080/dashboard
2. Click "Connect" to start real-time monitoring
3. The dashboard will automatically display new punches as they occur
4. View statistics and punch details in real-time

## Database Tables

The application creates two main tables:

### PUNCH_LOGS
Stores attendance punch records:
- `id` - Primary key
- `user_id` - Employee/user ID
- `name` - User name (from biometric templates)
- `punch_time` - Timestamp of the punch
- `verify_mode` - How they verified (0=password, 1=fingerprint, 2=card, 3=face)
- `in_out_mode` - Check-in/out status (0=check-in, 1=check-out)
- `work_code` - Work code
- `synced_at` - When the record was synced

### BIOMETRIC_TEMPLATES
Stores biometric templates:
- `id` - Primary key
- `user_id` - Employee/user ID
- `name` - User name
- `enabled` - Whether the user is enabled
- `finger_index` - Which finger (0-9)
- `template_data` - Fingerprint template data
- `algorithm_version` - Template algorithm version
- `synced_at` - When the template was synced

## API Endpoints

### Device Synchronization
- `POST /zkteco/sync` - Sync biometric templates from device
- `POST /zkteco/sync-punches` - Sync punch logs from device
- `GET /zkteco/test-connection` - Test device connectivity

### Punch Records
- `GET /zkteco/punches` - Get all punch records
- `GET /zkteco/punches/{userId}` - Get punch records for specific user
- `GET /zkteco/punches/realtime` - Real-time punch monitoring via SSE

## Troubleshooting

### Connection Issues

**Error: "Failed to connect to device"**
- Verify device IP and port in configuration
- Ensure device is powered on and on the network
- Check firewall settings
- Ping the device: `ping 192.168.1.127`

**Error: "no jacob in java.library.path"**
- Ensure ZKTeco SDK is properly installed
- Check that jacob-x64.dll is accessible
- Verify you're running on Windows OS

### Database Issues

**H2 Console won't connect**
- Use JDBC URL: `jdbc:h2:file:./data/biometrictest`
- Username: `sa`
- Password: (leave empty)
- Ensure application is running

**Database file not found**
- Database is created automatically on first use
- Check `./data/` directory
- Trigger creation by syncing data

### Dashboard Issues

**Real-time updates not working**
- Click "Connect" button in the dashboard
- Check browser console for errors
- Ensure Server-Sent Events (SSE) are supported
- Verify application is running

**No punches showing**
- First sync punches using "Sync Punches" button
- Ensure device has punch records
- Check database using H2 console

## Advanced Configuration

### Change Server Port

Edit `application.properties` or `application.yml`:
```properties
server.port=9090
```

### Use PostgreSQL Instead of H2

1. Add PostgreSQL dependency to `pom.xml`
2. Update datasource configuration:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/biometric
spring.datasource.username=postgres
spring.datasource.password=yourpassword
```

### Enable SQL Logging

Already enabled in default configuration:
```properties
spring.jpa.properties.hibernate.format_sql=true
```

## Security Considerations

1. **Production Deployment**: 
   - Disable H2 console in production
   - Use proper database (PostgreSQL, MySQL)
   - Add authentication/authorization
   - Use HTTPS

2. **Network Security**:
   - Use VPN for remote device access
   - Restrict H2 console access
   - Configure firewall rules

## Support

For issues or questions:
1. Check logs in application console
2. Review troubleshooting section
3. Verify device and SDK installation
4. Check network connectivity

## License

This project is licensed under the terms specified in the LICENSE file.

# Biometric Test Application

Spring Boot application for connecting to ZKTeco biometric devices (e.g., iFace 702) and managing real-time attendance monitoring.

## 🎯 Quick Start

1. **Build the application**:
   ```bash
   mvn clean package
   ```

2. **Run the application**:
   ```bash
   java -jar target/biometrictest-0.0.1-SNAPSHOT.jar
   ```

3. **Access the home page**:
   Open your browser to **http://localhost:8080/**

## 📋 Features

- ✅ **Home Page Dashboard** - Central hub with access to all features
- ✅ **Real-time Punch Monitoring** - Live attendance tracking via Server-Sent Events (SSE)
- ✅ **Device Connection Testing** - Verify connectivity to ZKTeco devices
- ✅ **Biometric Template Sync** - Sync fingerprint/face templates from device
- ✅ **Attendance Log Sync** - Import punch records from device
- ✅ **H2 Database Console** - Direct database access and management
- ✅ **REST API** - Complete API for data access and synchronization
- ✅ **Database Persistence** - Automatic table creation and data storage

## 📊 Application Pages

### 🏠 Home Page - http://localhost:8080/
Central dashboard providing:
- Quick access to all features
- Device connection testing
- API endpoint documentation
- Database connection details
- Navigation to dashboard and H2 console

### 📺 Real-time Punch Monitor - http://localhost:8080/dashboard
Live attendance monitoring with:
- Real-time punch notifications
- Check-in/check-out tracking
- Verification mode indicators (fingerprint, card, face)
- Session statistics and timing

### 💾 H2 Database Console - http://localhost:8080/h2-console
Direct database access:
- **JDBC URL**: `jdbc:h2:file:./data/biometrictest`
- **Username**: `sa`
- **Password**: *(leave empty)*

## 🔧 Requirements

- **Windows OS** - Required for COM/ActiveX integration
- **Java 17+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.6+** - [Download](https://maven.apache.org/download.cgi)
- **ZKTeco SDK** - Must be installed and registered
- **ZKTeco Device** - Accessible on network (e.g., iFace 702)

## ⚙️ Configuration

Edit `src/main/resources/application.yml` or `application.properties`:

```yaml
zkteco:
  ip: 192.168.1.127        # Device IP address
  port: 4370               # Device port (default: 4370)
  password: ""             # Communication password (if set on device)
  machine-number: 1        # Machine number (usually 1)
```

## 🏗️ Building

```bash
mvn clean package
```

## 🚀 Running

### Using Maven:
```bash
mvn spring-boot:run
```

### Using JAR:
```bash
java -jar target/biometrictest-0.0.1-SNAPSHOT.jar
```

## 📡 API Endpoints

### Device Synchronization
- `POST /zkteco/sync` - Sync biometric templates from device
- `POST /zkteco/sync-punches` - Sync punch logs from device
- `GET /zkteco/test-connection` - Test device connectivity

### Punch/Attendance Management
- `GET /zkteco/punches` - Get all punch records
- `GET /zkteco/punches/{userId}` - Get punch records for specific user
- `GET /zkteco/punches/realtime` - Real-time punch monitoring via SSE

### Database & Console
- `GET /h2-console` - H2 database console

## 💾 Database Tables

### PUNCH_LOGS
Stores attendance records with:
- User ID and name
- Punch timestamp
- Verification method (fingerprint, card, face, etc.)
- Check-in/check-out status
- Sync timestamp

### BIOMETRIC_TEMPLATES
Stores biometric data with:
- User ID and name
- Finger index (0-9)
- Template data
- Algorithm version
- Enabled status

## 🔍 Using the Application

1. **Start the application** and navigate to http://localhost:8080/
2. **Test connection** to your ZKTeco device using the connection test button
3. **Sync templates** to import biometric data: `POST /zkteco/sync`
4. **Sync punches** to import attendance logs: `POST /zkteco/sync-punches`
5. **Monitor real-time** by opening the dashboard and clicking "Connect"
6. **View data** using the H2 console or API endpoints

## 📚 Documentation

- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - Comprehensive setup and configuration guide
- **[REALTIME_PUNCH_MONITORING.md](REALTIME_PUNCH_MONITORING.md)** - Real-time monitoring details
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Troubleshooting guide

## 🐛 Troubleshooting

### Connection Issues
- Verify device IP and port in configuration
- Ensure ZKTeco SDK is installed and registered
- Check network connectivity and firewall settings
- Use the connection test feature from home page

### Database Issues
- Use H2 console with JDBC URL: `jdbc:h2:file:./data/biometrictest`
- Database is created automatically in `./data/` directory
- Check application logs for errors

### Dashboard Issues
- Ensure application is running
- Click "Connect" button in dashboard
- Check browser console for SSE errors
- Sync punches first using the sync button

For detailed troubleshooting, see **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)**

## 🔒 Security Notes

For production deployment:
- Disable H2 console
- Use production database (PostgreSQL, MySQL)
- Add authentication/authorization
- Enable HTTPS
- Restrict database access

## 📄 Native Libraries

The application uses JACOB (Java-COM Bridge) for Windows COM/ActiveX integration. Native DLLs are located in `libs/` and automatically included in the build. See `libs/README.md` for details.

## 📖 Additional Documentation

- **[SETUP_GUIDE.md](SETUP_GUIDE.md)** - Comprehensive setup and configuration guide
- **[REALTIME_PUNCH_MONITORING.md](REALTIME_PUNCH_MONITORING.md)** - Real-time monitoring details
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Troubleshooting guide

## 📝 License

This project is licensed under the terms specified in the LICENSE file.


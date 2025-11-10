# Real-Time Biometric Punch Monitoring

This document explains how to use the real-time punch monitoring and synchronization features.

## Overview

The application now supports:
1. **Punch Log Synchronization** - Sync attendance/punch records from ZKTeco devices to the database
2. **Real-Time Monitoring** - Monitor punch events as they happen in real-time using Server-Sent Events (SSE)
3. **Query Punch Records** - Retrieve historical punch data from the database

## API Endpoints

### 1. Sync Templates (Existing)
**Endpoint:** `POST /zkteco/sync`

Synchronizes user biometric templates (fingerprints) from the device to the database.

```bash
curl -X POST http://localhost:8080/zkteco/sync
```

**Response:**
```json
"Imported templates: 10"
```

---

### 2. Sync Punch Logs
**Endpoint:** `POST /zkteco/sync-punches`

Synchronizes all attendance/punch records from the device to the database.

```bash
curl -X POST http://localhost:8080/zkteco/sync-punches
```

**Response:**
```json
"Imported punch logs: 150"
```

---

### 3. Get All Punches
**Endpoint:** `GET /zkteco/punches`

Retrieves all punch records from the database, ordered by punch time (most recent first).

```bash
curl http://localhost:8080/zkteco/punches
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": "100",
    "name": "John Doe",
    "punchTime": "2025-11-10T08:30:00+00:00",
    "verifyMode": 1,
    "inOutMode": 0,
    "workCode": "0",
    "syncedAt": "2025-11-10T11:40:00+00:00"
  },
  {
    "id": 2,
    "userId": "101",
    "name": "Jane Smith",
    "punchTime": "2025-11-10T08:32:00+00:00",
    "verifyMode": 1,
    "inOutMode": 0,
    "workCode": "0",
    "syncedAt": "2025-11-10T11:40:00+00:00"
  }
]
```

**Field Descriptions:**
- `userId` - User enrollment number from the biometric device
- `name` - User name (if available from template data)
- `punchTime` - Date and time when the punch occurred
- `verifyMode` - Authentication method used:
  - `0` = Password
  - `1` = Fingerprint
  - `2` = Card
  - `3` = Face
- `inOutMode` - Punch type:
  - `0` = Check-in
  - `1` = Check-out
  - `2` = Break-out
  - `3` = Break-in
  - `4` = Overtime-in
  - `5` = Overtime-out
- `workCode` - Work code (if configured on device)
- `syncedAt` - When this record was synced to the database

---

### 4. Get Punches by User
**Endpoint:** `GET /zkteco/punches/{userId}`

Retrieves punch records for a specific user, ordered by punch time (most recent first).

```bash
curl http://localhost:8080/zkteco/punches/100
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": "100",
    "name": "John Doe",
    "punchTime": "2025-11-10T17:30:00+00:00",
    "verifyMode": 1,
    "inOutMode": 1,
    "workCode": "0",
    "syncedAt": "2025-11-10T11:40:00+00:00"
  },
  {
    "id": 1,
    "userId": "100",
    "name": "John Doe",
    "punchTime": "2025-11-10T08:30:00+00:00",
    "verifyMode": 1,
    "inOutMode": 0,
    "workCode": "0",
    "syncedAt": "2025-11-10T11:40:00+00:00"
  }
]
```

---

### 5. Real-Time Punch Monitoring (SSE)
**Endpoint:** `GET /zkteco/punches/realtime`

Establishes a Server-Sent Events (SSE) connection to receive real-time punch notifications as they occur on the device.

**JavaScript Example:**
```javascript
const eventSource = new EventSource('http://localhost:8080/zkteco/punches/realtime');

eventSource.addEventListener('punch', (event) => {
  console.log('New punch:', event.data);
  // event.data contains: "User: 100 (John Doe), Time: 2025-11-10T08:30:00Z, Mode: 1, InOut: 0"
});

eventSource.onerror = (error) => {
  console.error('SSE Error:', error);
  eventSource.close();
};

// Close connection when done
// eventSource.close();
```

**cURL Example (for testing):**
```bash
curl -N http://localhost:8080/zkteco/punches/realtime
```

The connection will remain open and you'll receive events like:
```
event: punch
data: User: 100 (John Doe), Time: 2025-11-10T08:30:00+00:00, Mode: 1, InOut: 0

event: punch
data: User: 101 (Jane Smith), Time: 2025-11-10T08:32:00+00:00, Mode: 1, InOut: 0
```

**How it Works:**
1. When a client connects to this endpoint, the service starts monitoring the device
2. The device is polled every 2 seconds for new attendance records
3. New punches (within the last 5 minutes) are automatically saved to the database
4. All connected clients receive the punch data in real-time via SSE
5. When all clients disconnect, monitoring automatically stops to save resources

---

## Usage Workflow

### Initial Setup
1. Start the application
2. Sync user templates first (so names appear in punch logs):
   ```bash
   curl -X POST http://localhost:8080/zkteco/sync
   ```
3. Sync existing punch logs:
   ```bash
   curl -X POST http://localhost:8080/zkteco/sync-punches
   ```

### Real-Time Monitoring
1. Open a browser or create a web page with JavaScript
2. Connect to the SSE endpoint to start receiving real-time updates
3. As employees punch in/out on the device, you'll see events immediately

### Query Historical Data
- Get all punches: `GET /zkteco/punches`
- Get user-specific punches: `GET /zkteco/punches/{userId}`

---

## HTML Example for Real-Time Monitoring

Create a simple HTML page to monitor punches in real-time:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Real-Time Punch Monitor</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        #punches { max-height: 400px; overflow-y: auto; border: 1px solid #ccc; padding: 10px; }
        .punch { padding: 5px; border-bottom: 1px solid #eee; }
        .status { margin-bottom: 10px; padding: 10px; background: #f0f0f0; }
    </style>
</head>
<body>
    <h1>Real-Time Biometric Punch Monitor</h1>
    
    <div class="status">
        Status: <span id="status">Disconnected</span>
    </div>
    
    <button onclick="connect()">Connect</button>
    <button onclick="disconnect()">Disconnect</button>
    <button onclick="clearPunches()">Clear</button>
    
    <h2>Recent Punches</h2>
    <div id="punches"></div>
    
    <script>
        let eventSource = null;
        
        function connect() {
            if (eventSource) {
                alert('Already connected');
                return;
            }
            
            document.getElementById('status').textContent = 'Connecting...';
            eventSource = new EventSource('http://localhost:8080/zkteco/punches/realtime');
            
            eventSource.onopen = () => {
                document.getElementById('status').textContent = 'Connected';
                document.getElementById('status').style.color = 'green';
            };
            
            eventSource.addEventListener('punch', (event) => {
                const punchesDiv = document.getElementById('punches');
                const punchDiv = document.createElement('div');
                punchDiv.className = 'punch';
                punchDiv.textContent = new Date().toLocaleTimeString() + ' - ' + event.data;
                punchesDiv.insertBefore(punchDiv, punchesDiv.firstChild);
            });
            
            eventSource.onerror = (error) => {
                console.error('SSE Error:', error);
                document.getElementById('status').textContent = 'Error/Disconnected';
                document.getElementById('status').style.color = 'red';
                eventSource.close();
                eventSource = null;
            };
        }
        
        function disconnect() {
            if (eventSource) {
                eventSource.close();
                eventSource = null;
                document.getElementById('status').textContent = 'Disconnected';
                document.getElementById('status').style.color = 'black';
            }
        }
        
        function clearPunches() {
            document.getElementById('punches').innerHTML = '';
        }
    </script>
</body>
</html>
```

Save this as `punch-monitor.html` and open it in a browser. Click "Connect" to start monitoring real-time punches.

---

## Configuration

The device connection is configured in `application.yml`:

```yaml
zkteco:
  ip: 192.168.1.127        # Device IP address
  port: 4370               # Device port (default: 4370)
  password: ""             # Communication password (if set on device)
  machine-number: 1        # Machine number (usually 1)
```

---

## Database Schema

### punch_logs Table
```sql
CREATE TABLE punch_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL,
    name VARCHAR(64),
    punch_time TIMESTAMP WITH TIME ZONE NOT NULL,
    verify_mode INTEGER,
    in_out_mode INTEGER,
    work_code VARCHAR(16),
    synced_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

---

## Troubleshooting

### Real-time monitoring not working
1. Ensure the device is accessible from the server
2. Check that the device IP and port are correct in `application.yml`
3. Verify the ZKTeco SDK is properly installed and registered
4. Check application logs for connection errors

### Duplicate punch records
- The sync endpoint imports all records from the device each time
- Consider clearing old records periodically or implementing deduplication logic

### SSE connection drops
- Browser limits the number of concurrent SSE connections
- Check network stability
- The connection will auto-disconnect if no clients are connected

### Time zone issues
- Punch times are stored with timezone information (OffsetDateTime)
- Ensure your server's timezone is configured correctly

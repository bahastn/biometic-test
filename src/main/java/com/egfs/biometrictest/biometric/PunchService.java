package com.egfs.biometrictest.biometric;

import com.egfs.biometrictest.ZktecoProperties;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PunchService {
    private static final Logger log = LoggerFactory.getLogger(PunchService.class);

    private final ZktecoProperties props;
    private final PunchLogRepository punchRepo;
    private final BiometricTemplateRepository templateRepo;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean monitoring = false;

    public PunchService(ZktecoProperties props, PunchLogRepository punchRepo, 
                        BiometricTemplateRepository templateRepo) {
        this.props = props;
        this.punchRepo = punchRepo;
        this.templateRepo = templateRepo;
    }

    @Transactional
    public int syncPunches() {
        ActiveXComponent zk = null;
        int imported = 0;
        try {
            zk = new ActiveXComponent("zkemkeeper.CZKEM");
            boolean connected = Dispatch.call(zk, "Connect_Net", props.getIp(), props.getPort()).getBoolean();
            if (!connected) {
                log.error("Failed to connect to device {}:{}", props.getIp(), props.getPort());
                return 0;
            }

            // Set communication password if configured
            if (props.getPassword() != null && !props.getPassword().isEmpty()) {
                boolean ok = Dispatch.call(zk, "SetCommPassword", Integer.parseInt(props.getPassword())).getBoolean();
                if (!ok) log.warn("SetCommPassword returned false");
            }

            // Read all attendance logs
            boolean readSuccess = Dispatch.call(zk, "ReadGeneralLogData", props.getMachineNumber()).getBoolean();
            if (!readSuccess) {
                log.warn("ReadGeneralLogData failed");
                return 0;
            }

            // Iterate through all attendance logs using GetAllGLogData
            Variant vEnrollNo = new Variant("", true);
            Variant vVerifyMode = new Variant(0, true);
            Variant vInOutMode = new Variant(0, true);
            Variant vYear = new Variant(0, true);
            Variant vMonth = new Variant(0, true);
            Variant vDay = new Variant(0, true);
            Variant vHour = new Variant(0, true);
            Variant vMinute = new Variant(0, true);
            Variant vSecond = new Variant(0, true);
            Variant vWorkCode = new Variant(0, true);

            // GetAllGLogData uses 8 parameters: machineNo, enrollNo, verifyMode, inOutMode, year, month, day, hour
            // We need to split the call into two parts
            while (Dispatch.call(zk, "GetAllGLogData",
                    new Variant(props.getMachineNumber()),
                    vEnrollNo, vVerifyMode, vInOutMode,
                    vYear, vMonth, vDay, vHour).getBoolean()) {

                // Get additional time info using separate calls
                Variant vMin = new Variant(0, true);
                Variant vSec = new Variant(0, true);
                
                String userId = vEnrollNo.getStringRef();
                int verifyMode = vVerifyMode.getIntRef();
                int inOutMode = vInOutMode.getIntRef();
                int year = vYear.getIntRef();
                int month = vMonth.getIntRef();
                int day = vDay.getIntRef();
                int hour = vHour.getIntRef();
                
                // For minute and second, we'll use a separate call or default to 0
                // Most ZKTeco devices support SSR_GetGeneralLogData but we need to call it properly
                int minute = 0;
                int second = 0;
                
                try {
                    // Try to get detailed time if available
                    if (Dispatch.call(zk, "SSR_GetGeneralExtLogData",
                            new Variant(props.getMachineNumber()),
                            vMin, vSec).getBoolean()) {
                        minute = vMin.getIntRef();
                        second = vSec.getIntRef();
                    }
                } catch (Exception e) {
                    // Ignore if not supported
                }

                // Build timestamp
                OffsetDateTime punchTime = OffsetDateTime.of(
                    year, month, day, hour, minute, second, 0,
                    ZoneId.systemDefault().getRules().getOffset(
                        java.time.LocalDateTime.of(year, month, day, hour, minute, second)
                    )
                );

                // Get user name from template if exists
                String name = templateRepo.findByUserIdAndFingerIndex(userId, 0)
                        .map(BiometricTemplate::getName)
                        .orElse(null);

                // Create punch log
                PunchLog punch = new PunchLog();
                punch.setUserId(userId);
                punch.setName(name);
                punch.setPunchTime(punchTime);
                punch.setVerifyMode(verifyMode);
                punch.setInOutMode(inOutMode);
                punch.setWorkCode("0");
                punchRepo.save(punch);
                imported++;

                // Reset variants for next iteration
                vEnrollNo = new Variant("", true);
                vVerifyMode = new Variant(0, true);
                vInOutMode = new Variant(0, true);
                vYear = new Variant(0, true);
                vMonth = new Variant(0, true);
                vDay = new Variant(0, true);
                vHour = new Variant(0, true);
            }

            log.info("Successfully synced {} punch logs", imported);
            return imported;

        } catch (Throwable t) {
            log.error("Error syncing punch logs", t);
            return imported;
        } finally {
            if (zk != null) {
                try {
                    Dispatch.call(zk, "Disconnect");
                } catch (Exception ignore) {}
            }
        }
    }

    public List<PunchLog> getAllPunches() {
        return punchRepo.findAllByOrderByPunchTimeDesc();
    }

    public List<PunchLog> getPunchesByUser(String userId) {
        return punchRepo.findByUserIdOrderByPunchTimeDesc(userId);
    }

    public SseEmitter registerForRealtimeUpdates() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        
        // Start monitoring if not already started
        if (!monitoring && !emitters.isEmpty()) {
            startRealtimeMonitoring();
        }
        
        return emitter;
    }

    private void startRealtimeMonitoring() {
        if (monitoring) {
            return;
        }
        
        monitoring = true;
        executor.submit(() -> {
            log.info("Starting real-time punch monitoring");
            ActiveXComponent zk = null;
            
            try {
                zk = new ActiveXComponent("zkemkeeper.CZKEM");
                boolean connected = Dispatch.call(zk, "Connect_Net", props.getIp(), props.getPort()).getBoolean();
                
                if (!connected) {
                    log.error("Failed to connect to device for real-time monitoring");
                    monitoring = false;
                    return;
                }

                // Set communication password if configured
                if (props.getPassword() != null && !props.getPassword().isEmpty()) {
                    Dispatch.call(zk, "SetCommPassword", Integer.parseInt(props.getPassword()));
                }

                // Enable real-time events
                Dispatch.call(zk, "RegEvent", props.getMachineNumber(), 65535);
                
                log.info("Real-time monitoring enabled");
                
                // Keep connection alive and listen for events
                while (monitoring && !emitters.isEmpty()) {
                    // Check for new attendance records periodically
                    checkForNewPunches(zk);
                    Thread.sleep(2000); // Poll every 2 seconds
                }
                
            } catch (Exception e) {
                log.error("Error in real-time monitoring", e);
            } finally {
                monitoring = false;
                if (zk != null) {
                    try {
                        Dispatch.call(zk, "Disconnect");
                    } catch (Exception ignore) {}
                }
                log.info("Real-time monitoring stopped");
            }
        });
    }

    private void checkForNewPunches(ActiveXComponent zk) {
        try {
            // Read latest general log data
            boolean readSuccess = Dispatch.call(zk, "ReadGeneralLogData", props.getMachineNumber()).getBoolean();
            if (!readSuccess) {
                return;
            }

            List<PunchLog> newPunches = new ArrayList<>();
            
            // Get latest attendance records using GetAllGLogData
            Variant vEnrollNo = new Variant("", true);
            Variant vVerifyMode = new Variant(0, true);
            Variant vInOutMode = new Variant(0, true);
            Variant vYear = new Variant(0, true);
            Variant vMonth = new Variant(0, true);
            Variant vDay = new Variant(0, true);
            Variant vHour = new Variant(0, true);

            while (Dispatch.call(zk, "GetAllGLogData",
                    new Variant(props.getMachineNumber()),
                    vEnrollNo, vVerifyMode, vInOutMode,
                    vYear, vMonth, vDay, vHour).getBoolean()) {

                String userId = vEnrollNo.getStringRef();
                int verifyMode = vVerifyMode.getIntRef();
                int inOutMode = vInOutMode.getIntRef();
                int year = vYear.getIntRef();
                int month = vMonth.getIntRef();
                int day = vDay.getIntRef();
                int hour = vHour.getIntRef();
                
                // Default minute and second to 0
                int minute = 0;
                int second = 0;

                OffsetDateTime punchTime = OffsetDateTime.of(
                    year, month, day, hour, minute, second, 0,
                    ZoneId.systemDefault().getRules().getOffset(
                        java.time.LocalDateTime.of(year, month, day, hour, minute, second)
                    )
                );

                // Only process recent punches (within last 5 minutes)
                if (punchTime.isAfter(OffsetDateTime.now().minusMinutes(5))) {
                    String name = templateRepo.findByUserIdAndFingerIndex(userId, 0)
                            .map(BiometricTemplate::getName)
                            .orElse(null);

                    PunchLog punch = new PunchLog();
                    punch.setUserId(userId);
                    punch.setName(name);
                    punch.setPunchTime(punchTime);
                    punch.setVerifyMode(verifyMode);
                    punch.setInOutMode(inOutMode);
                    punch.setWorkCode("0");
                    
                    // Save to database
                    punchRepo.save(punch);
                    newPunches.add(punch);
                }

                // Reset variants
                vEnrollNo = new Variant("", true);
                vVerifyMode = new Variant(0, true);
                vInOutMode = new Variant(0, true);
                vYear = new Variant(0, true);
                vMonth = new Variant(0, true);
                vDay = new Variant(0, true);
                vHour = new Variant(0, true);
            }

            // Send new punches to all connected clients
            if (!newPunches.isEmpty()) {
                sendToAllEmitters(newPunches);
            }

        } catch (Exception e) {
            log.error("Error checking for new punches", e);
        }
    }

    private void sendToAllEmitters(List<PunchLog> punches) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                for (PunchLog punch : punches) {
                    emitter.send(SseEmitter.event()
                        .name("punch")
                        .data(formatPunch(punch)));
                }
            } catch (Exception e) {
                deadEmitters.add(emitter);
            }
        }
        
        // Remove dead emitters
        emitters.removeAll(deadEmitters);
        
        // Stop monitoring if no more emitters
        if (emitters.isEmpty()) {
            monitoring = false;
        }
    }

    private String formatPunch(PunchLog punch) {
        return String.format("User: %s (%s), Time: %s, Mode: %d, InOut: %d",
                punch.getUserId(),
                punch.getName() != null ? punch.getName() : "Unknown",
                punch.getPunchTime(),
                punch.getVerifyMode(),
                punch.getInOutMode());
    }

    public void stopMonitoring() {
        monitoring = false;
        emitters.clear();
    }
}

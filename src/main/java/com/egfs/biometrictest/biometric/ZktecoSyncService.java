package com.egfs.biometrictest.biometric;

import com.egfs.biometrictest.ZktecoProperties;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ZktecoSyncService {
    private static final Logger log = LoggerFactory.getLogger(ZktecoSyncService.class);

    private final ZktecoProperties props;
    private final BiometricTemplateRepository repo;

    public ZktecoSyncService(ZktecoProperties props, BiometricTemplateRepository repo) {
        this.props = props;
        this.repo = repo;
    }

    @Transactional
    public int syncTemplates() {
        ActiveXComponent zk = null;
        int imported = 0;
        try {
            // Ensure jacob-x64.dll is on java.library.path or in working dir.
            zk = new ActiveXComponent("zkemkeeper.CZKEM");
            boolean connected = Dispatch.call(zk, "Connect_Net", props.getIp(), props.getPort()).getBoolean();
            if (!connected) {
                log.error("Failed to connect to device {}:{}", props.getIp(), props.getPort());
                return 0;
            }

            // If a device communication password is configured:
            if (props.getPassword() != null && !props.getPassword().isEmpty()) {
                boolean ok = Dispatch.call(zk, "SetCommPassword", Integer.parseInt(props.getPassword())).getBoolean();
                if (!ok) log.warn("SetCommPassword returned false");
            }

            // Read all user info first
            Dispatch.call(zk, "ReadAllUserID", props.getMachineNumber());

            // Iterate through all users: SSR_GetAllUserInfo(MachineNo, out enrollNo, out name, out password, out privilege, out enabled)
            // Using JACOB out params:
            Variant vEnroll = new Variant("", true);
            Variant vName = new Variant("", true);
            Variant vPwd = new Variant("", true);
            Variant vPriv = new Variant(0, true);
            Variant vEnabled = new Variant(false, true);

            while (Dispatch.call(zk, "SSR_GetAllUserInfo",
                    new Variant(props.getMachineNumber()),
                    vEnroll, vName, vPwd, vPriv, vEnabled).getBoolean()) {

                String userId = vEnroll.getStringRef();
                String name = vName.getStringRef();
                boolean enabled = vEnabled.getBooleanRef();

                // For each finger index 0..9, try to read template
                for (int fingerIndex = 0; fingerIndex <= 9; fingerIndex++) {
                    Variant vFlag = new Variant(0, true);
                    Variant vTmp = new Variant("", true);
                    Variant vLen = new Variant(0, true);

                    // GetUserTmpExStr(MachineNo, EnrollNumber, FingerIndex, out Flag, out TmpData, out TmpLength)
                    boolean hasTemplate = Dispatch.call(zk, "GetUserTmpExStr",
                            new Variant(props.getMachineNumber()),
                            new Variant(userId),
                            new Variant(fingerIndex),
                            vFlag, vTmp, vLen).getBoolean();

                    if (hasTemplate) {
                        String template = vTmp.getStringRef(); // ZK 10.0 format string
                        // Upsert
                        BiometricTemplate entity = repo.findByUserIdAndFingerIndex(userId, fingerIndex)
                                .orElseGet(BiometricTemplate::new);
                        entity.setUserId(userId);
                        entity.setName(name);
                        entity.setEnabled(enabled);
                        entity.setFingerIndex(fingerIndex);
                        entity.setTemplateData(template);
                        entity.setAlgorithmVersion("10"); // adjust if needed
                        repo.save(entity);
                        imported++;
                    }
                }

                // Reset out params for next loop
                vEnroll = new Variant("", true);
                vName = new Variant("", true);
                vPwd = new Variant("", true);
                vPriv = new Variant(0, true);
                vEnabled = new Variant(false, true);
            }

            // Optional: faces on iFace702 -> Use GetUserFaceStr if supported
            // Example:
            // Variant vFaceData = new Variant("", true);
            // boolean hasFace = Dispatch.call(zk, "GetUserFaceStr", props.getMachineNumber(), userId, 50, vFaceData, new Variant(0, true)).getBoolean();

            return imported;
        } catch (Throwable t) {
            log.error("ZKTeco sync error", t);
            return imported;
        } finally {
            try {
                if (zk != null) {
                    Dispatch.call(zk, "Disconnect");
                }
            } catch (Exception ignore) { }
        }
    }

    public Map<String, Object> testConnection() {
        Map<String, Object> result = new HashMap<>();
        ActiveXComponent zk = null;
        try {
            zk = new ActiveXComponent("zkemkeeper.CZKEM");
            boolean connected = Dispatch.call(zk, "Connect_Net", props.getIp(), props.getPort()).getBoolean();
            
            if (!connected) {
                result.put("success", false);
                result.put("error", "Failed to connect to device at " + props.getIp() + ":" + props.getPort());
                result.put("deviceInfo", "");
                return result;
            }

            // Get device information
            String deviceInfo = "ZKTeco Device at " + props.getIp() + ":" + props.getPort();
            
            // Try to get firmware version
            try {
                Variant vFirmwareVersion = new Variant("", true);
                boolean gotVersion = Dispatch.call(zk, "GetFirmwareVersion", 
                    new Variant(props.getMachineNumber()), vFirmwareVersion).getBoolean();
                if (gotVersion) {
                    deviceInfo += " (FW: " + vFirmwareVersion.getStringRef() + ")";
                }
            } catch (Exception e) {
                log.debug("Could not get firmware version", e);
            }

            result.put("success", true);
            result.put("error", "");
            result.put("deviceInfo", deviceInfo);
            
            log.info("Successfully tested connection to device at {}:{}", props.getIp(), props.getPort());
            return result;

        } catch (Throwable t) {
            log.error("Connection test error", t);
            result.put("success", false);
            result.put("error", "Exception: " + t.getMessage() + ". Please ensure ZKTeco SDK is installed and device is accessible.");
            result.put("deviceInfo", "");
            return result;
        } finally {
            try {
                if (zk != null) {
                    Dispatch.call(zk, "Disconnect");
                }
            } catch (Exception ignore) { }
        }
    }
}


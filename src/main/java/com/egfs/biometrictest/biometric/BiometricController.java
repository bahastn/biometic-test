package com.egfs.biometrictest.biometric;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/zkteco")
public class BiometricController {

    private final ZktecoSyncService templateService;
    private final PunchService punchService;

    public BiometricController(ZktecoSyncService templateService, PunchService punchService) {
        this.templateService = templateService;
        this.punchService = punchService;
    }

    @PostMapping("/sync")
    public ResponseEntity<String> sync() {
        int count = templateService.syncTemplates();
        return ResponseEntity.ok("Imported templates: " + count);
    }

    @PostMapping("/sync-punches")
    public ResponseEntity<String> syncPunches() {
        int count = punchService.syncPunches();
        return ResponseEntity.ok("Imported punch logs: " + count);
    }

    @GetMapping("/punches")
    public ResponseEntity<List<PunchLog>> getAllPunches() {
        List<PunchLog> punches = punchService.getAllPunches();
        return ResponseEntity.ok(punches);
    }

    @GetMapping("/punches/{userId}")
    public ResponseEntity<List<PunchLog>> getPunchesByUser(@PathVariable String userId) {
        List<PunchLog> punches = punchService.getPunchesByUser(userId);
        return ResponseEntity.ok(punches);
    }

    @GetMapping(value = "/punches/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamRealtimePunches() {
        return punchService.registerForRealtimeUpdates();
    }
}


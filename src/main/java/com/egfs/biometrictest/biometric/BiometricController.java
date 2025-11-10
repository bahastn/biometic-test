package com.egfs.biometrictest.biometric;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/zkteco")
public class BiometricController {

    private final ZktecoSyncService service;

    public BiometricController(ZktecoSyncService service) {
        this.service = service;
    }

    @PostMapping("/sync")
    public ResponseEntity<String> sync() {
        int count = service.syncTemplates();
        return ResponseEntity.ok("Imported templates: " + count);
    }
}


package com.learnSpringBoot.File.Processor.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock virus scanner. Enabled only when app.virus-scan.enabled=true
 */
@Component
@ConditionalOnProperty(name = "app.virus-scan.enabled", havingValue = "true")
public class VirusScanner {

    /**
     * Simple mock: treat files containing string "virus" (in filename) as infected.
     */
    public boolean scan(java.nio.file.Path file) {
        String name = file.getFileName().toString().toLowerCase();
        // pretend heavier logic — here it's instant check
        return !name.contains("virus");
    }
}

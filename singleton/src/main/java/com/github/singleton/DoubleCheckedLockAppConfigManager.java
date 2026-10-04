package com.github.singleton;

import java.util.HashMap;
import java.util.Map;

public final class DoubleCheckedLockAppConfigManager {

    // 🚨 CRUCIAL: The volatile keyword is mandatory here!
    private static volatile DoubleCheckedLockAppConfigManager instance;
    private final Map<String, String> configData;

    private DoubleCheckedLockAppConfigManager() {
        // Simulating heavy I/O configuration parsing
        this.configData = new HashMap<>();
        this.configData.put("env", "production");
        this.configData.put("db.url", "jdbc:postgresql://localhost:5432/prod_db");
        this.configData.put("api.timeout", "5000");
    }

    /**
     * High-Performance Thread-Safe Singleton using Double-Checked Locking.
     */
    public static DoubleCheckedLockAppConfigManager getInstance() {
        // Check 1: No locking. If initialized, return immediately (Fast Path)
        DoubleCheckedLockAppConfigManager result = instance;
        if (result == null) {
            
            // Acquire lock on the class object (Slow Path)
            synchronized (DoubleCheckedLockAppConfigManager.class) {
                result = instance;
                
                // Check 2: Re-check because a thread could have initialized it 
                // while this thread was waiting for the lock.
                if (result == null) {
                    instance = result = new DoubleCheckedLockAppConfigManager();
                }
            }
        }
        return result;
    }

    public String getConfig(String key) {
        return configData.get(key);
    }
}
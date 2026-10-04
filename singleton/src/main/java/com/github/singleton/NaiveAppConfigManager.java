package com.github.singleton;

import java.util.HashMap;
import java.util.Map;

public final class NaiveAppConfigManager {
    private static NaiveAppConfigManager instance;
    private final Map<String, String> configData;

    // Private constructor prevents instantiation from other classes
    private NaiveAppConfigManager() {
        // Simulating heavy I/O or file loading
        this.configData = new HashMap<>();
        this.configData.put("env", "production");
        this.configData.put("db.url", "jdbc:postgresql://localhost:5432/prod_db");
        this.configData.put("api.timeout", "5000");
    }

    /**
     * Naive Thread-Safe Method.
     * Synchronizing the entire method guarantees only one 
     * thread can initialize the instance, but it introduces 
     * severe performance degradation, because threads will be blocked 
     * just to check if the instance is already created, 
     * even after it has been initialized.
     */
    public static synchronized NaiveAppConfigManager getInstance() {
        if (instance == null) {
            instance = new NaiveAppConfigManager();
        }
        return instance;
    }

    public String getConfig(String key) {
        return configData.get(key);
    }
}

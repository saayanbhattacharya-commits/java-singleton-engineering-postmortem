package com.github.singleton;

public enum EnumTypeAppConfigManager {
    INSTANCE; // The single global instance managed entirely by the JVM

    private final java.util.Map<String, String> configData;

    // Enums constructors are inherently private and run exactly once
    EnumTypeAppConfigManager() {
        this.configData = new java.util.HashMap<>();
        this.configData.put("env", "production");
        this.configData.put("db.url", "jdbc:postgresql://localhost:5432/prod_db");
    }

    public String getConfig(String key) {
        return configData.get(key);
    }
}
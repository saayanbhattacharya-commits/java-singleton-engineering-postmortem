package com.github.singleton;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class ReflectionAndSerializationSafeInitializationHolderAppConfigManager implements Serializable {

    private final Map<String, String> configData;

    private ReflectionAndSerializationSafeInitializationHolderAppConfigManager() {
        // REFLECTION GUARD: Check if the JVM has already initialized the Singleton instance
        if (Holder.INSTANCE != null) {
            throw new IllegalStateException(
                "CRITICAL SECURITY VIOLATION: ReflectionSafeInitializationHolderAppConfigManager instance already exists. " +
                "Instantiation via Reflection is strictly prohibited."
            );
        }

        // Simulating heavy I/O configuration parsing
        this.configData = new HashMap<>();
        this.configData.put("env", "production");
        this.configData.put("db.url", "jdbc:postgresql://localhost:5432/prod_db");
        this.configData.put("api.timeout", "5000");
    }

    private static class Holder {
        private static final ReflectionAndSerializationSafeInitializationHolderAppConfigManager INSTANCE = new ReflectionAndSerializationSafeInitializationHolderAppConfigManager();
    }

    public static ReflectionAndSerializationSafeInitializationHolderAppConfigManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * SERIALIZATION_DEFENCE :
     * This special hook intercepts Java's deserialization engine. 
     * The JVM will still construct the duplicate object in memory from the stream, 
     * but this method forces it to throw away that duplicate and return our 
     * legitimate, active Single Source of Truth instead.
     */
    protected Object readResolve() {
        return getInstance();
    }

    public String getConfig(String key) {
        return configData.get(key);
    }
}

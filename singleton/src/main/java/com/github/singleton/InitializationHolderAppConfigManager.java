package com.github.singleton;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class InitializationHolderAppConfigManager implements Serializable {

    private final Map<String, String> configData;

    private InitializationHolderAppConfigManager() {
        // Simulating heavy I/O configuration parsing
        this.configData = new HashMap<>();
        this.configData.put("env", "production");
        this.configData.put("db.url", "jdbc:postgresql://localhost:5432/prod_db");
        this.configData.put("api.timeout", "5000");
    }

    /**
     * WHY THIS IS BETTER THAN DOUBLE-CHECKED LOCKING (DCL):
     *
     * 1. ZERO BOILERPLATE / CLEANER CODE:
     *    Double-checked locking requires nested if-statements, 
     *    a synchronized block, local variable optimizations, 
     *    and the tricky 'volatile' keyword. This implementation 
     *    only needs a standard static nested class.
     *
     * 2. NO VOLATILE REGISTRATION / HARDWARE TAX:
     *    DCL relies on 'volatile' to prevent instruction reordering, which forces 
     *    the CPU to invalidate its local caches and read from main memory. This implementation 
     *    completely avoids that hardware tax, resulting in marginally faster execution. 
     *
     * 3. GUARANTEED LAZY-LOADING BY JVM SPECIFICATION:
     *    The static inner class 'Holder' is completely ignored by the JVM when 
     *    the outer class is first loaded into memory. It is only loaded 
     *    and initialized when someone calls 'getInstance()'.
     *
     * 4. IMPLICIT THREAD SAFETY VIA CLASS LOADERS:
     *    Java guarantees that class loading is fundamentally thread-safe. The JVM 
     *    handles all internal synchronization when initializing 'INSTANCE'. We leverage 
     *    the rock-solid internals of the JVM rather than writing our own lock mechanisms.
     */
    private static class Holder {
        private static final InitializationHolderAppConfigManager INSTANCE = new InitializationHolderAppConfigManager();
    }

    public static InitializationHolderAppConfigManager getInstance() {
        return Holder.INSTANCE;
    }

    public String getConfig(String key) {
        return configData.get(key);
    }
}

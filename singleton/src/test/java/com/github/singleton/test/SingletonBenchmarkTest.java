package com.github.singleton.test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.singleton.DoubleCheckedLockAppConfigManager;
import com.github.singleton.EnumTypeAppConfigManager;
import com.github.singleton.InitializationHolderAppConfigManager;
import com.github.singleton.NaiveAppConfigManager;

class SingletonBenchmarkTest {

    private static final int TOTAL_THREADS = 100;
    private static final int ITERATIONS_PER_THREAD = 300_000;

    @Test 
    @DisplayName ("Benchmark Naive vs DCL vs Initialization Holder vs Enum-type")
    void benchmark() throws InterruptedException {
        System.out.println("=== Benchmarking Naive Singleton Implementation ===");
        benchmark(NaiveAppConfigManager::getInstance);

        System.out.println("\n=== Benchmarking Double-Checked Locking Singleton Implementation ===");
        benchmark(DoubleCheckedLockAppConfigManager::getInstance);

        System.out.println("\n=== Benchmarking Initialization-on-Demand Holder Singleton Implementation ===");
        benchmark(InitializationHolderAppConfigManager::getInstance);

        System.out.println("\n=== Benchmarking Enum-type Singleton Implementation ===");
        benchmark(() -> EnumTypeAppConfigManager.INSTANCE);
    }

    private static void benchmark(Supplier<Object> instanceSupplier) throws InterruptedException {
        System.out.println("Starting Benchmark: " + TOTAL_THREADS + " threads performing " + ITERATIONS_PER_THREAD + " lookups each...");

        ExecutorService executor = Executors.newFixedThreadPool(TOTAL_THREADS);
        CountDownLatch dynamicStartLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(TOTAL_THREADS);
        AtomicBoolean isTestRunning = new AtomicBoolean(true);

        // Step 1: Start the Watcher Thread to monitor BLOCKED states
        Thread monitorThread = new Thread(() -> {
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            System.out.println("Monitor active. Sampling thread states every 5ms...");
            long totalTimesMonitorRanAfterThreadsStart = 0;
            long sumOfBlockedCountAtMonitoring = 0;
            
            while (isTestRunning.get()) {
                long[] threadIds = threadMXBean.getAllThreadIds();
                ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds);
                
                int blockedCount = 0;
                int runningCount = 0;

                for (ThreadInfo info : threadInfos) {
                    if (info != null && info.getThreadName().contains("pool-")) { // Filter for our executor threads
                        if (info.getThreadState() == Thread.State.BLOCKED) {
                            blockedCount++;
                        } else if (info.getThreadState() == Thread.State.RUNNABLE) {
                            runningCount++;
                        }
                    }
                }
                
                // Only print if threads have actually broken past the starting gate
                if (blockedCount > 0 || runningCount > 0) {
                    totalTimesMonitorRanAfterThreadsStart++;
                    sumOfBlockedCountAtMonitoring += blockedCount;
                }

                try {
                    Thread.sleep(5); // Sample every 5 milliseconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // After the test is done, print the summary
            System.out.println("Benchmark completed. Monitor thread summary: Avg blocked count : " 
            + sumOfBlockedCountAtMonitoring / (double) totalTimesMonitorRanAfterThreadsStart 
            + " over " + totalTimesMonitorRanAfterThreadsStart + " samples.");
        });
        monitorThread.start();

        long startTime = System.nanoTime();

        for (int i = 0; i < TOTAL_THREADS; i++) {
            executor.submit(() -> {
                try {
                    // Wait for the gun to fire so all threads execute concurrently
                    dynamicStartLatch.await(); 
                    
                    for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
                        var config = instanceSupplier.get();
                        // Prevent JVM from optimizing away the loop by doing a quick operational read
                        if (config == null) {
                            throw new IllegalStateException("Singleton is null!");
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Release all threads simultaneously
        dynamicStartLatch.countDown(); 
        
        // Wait for all threads to finish their work
        finishLatch.await();
        long endTime = System.nanoTime();

        isTestRunning.set(false);
        executor.shutdown();

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.printf("Benchmark Completed in: %.4f seconds%n", durationInSeconds);
        monitorThread.join(); // Ensure the monitor thread has finished before exiting
    }
}


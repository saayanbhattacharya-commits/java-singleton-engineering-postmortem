package com.github.singleton.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.singleton.InitializationHolderAppConfigManager;

import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

public class SingletonSerializationLoopholeTest {
    @Test
    @DisplayName("Exploit: Use Serialization/Deserialization to duplicate a Singleton")
    void testSerializationCreatesNewInstance() throws Exception {
        // Step 1: Grab the legitimate active configuration instance
        InitializationHolderAppConfigManager instanceOne = InitializationHolderAppConfigManager.getInstance();

        // Step 2: Serialize the instance into a byte array (simulating network/disk write)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(instanceOne);
        oos.close();

        // Step 3: Deserialize the byte array back into a Java object
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        InitializationHolderAppConfigManager instanceTwo = (InitializationHolderAppConfigManager) ois.readObject();
        ois.close();

        // Step 4: Verify if it is the same object or not
        System.out.println("🔍 Instance One HashCode: " + instanceOne.hashCode());
        System.out.println("🔍 Instance Two HashCode: " + instanceTwo.hashCode());

        // Assert that they are now two completely separate objects in memory
        assertNotSame(instanceOne, instanceTwo, 
            "CRITICAL ARCHITECTURAL BREAKDOWN: Deserialization bypassed the Singleton contract and created a duplicate!");
    }
}

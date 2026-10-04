package com.github.singleton.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.singleton.EnumTypeAppConfigManager;
import com.github.singleton.ReflectionAndSerializationSafeInitializationHolderAppConfigManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SingletonReflectionAndSerializationSafeTest {
    @Test
    @DisplayName("Verify duplicate instance creation not allowed via reflection for Reflection And Serialization safe impl")
    void testReflectionCannotCreateDuplicateInstancesOfReflectionAndSerialzationSafeImpl() throws Exception {
        // Step 1: Grab the legitimate operational instance via the public API
        ReflectionAndSerializationSafeInitializationHolderAppConfigManager instanceOne = ReflectionAndSerializationSafeInitializationHolderAppConfigManager.getInstance();

        // Step 2: Use Java Reflection to dig into the class layout
        Class<ReflectionAndSerializationSafeInitializationHolderAppConfigManager> clazz = ReflectionAndSerializationSafeInitializationHolderAppConfigManager.class;
        
        // Retrieve the private constructor hidden from standard compilation rules
        Constructor<ReflectionAndSerializationSafeInitializationHolderAppConfigManager> declaredConstructor = clazz.getDeclaredConstructor();

        // THE CRITICAL EXPLOIT: Force the JVM to override the 'private' visibility modifier
        declaredConstructor.setAccessible(true);

        // Step 3: Instantiate a completely separate object in memory using the backdoor, but this will result in an exception now
        InvocationTargetException invocationTargetException = assertThrows(InvocationTargetException.class, () -> declaredConstructor.newInstance());
        assertTrue(invocationTargetException.getCause() instanceof IllegalStateException);
    }

    @Test
    @DisplayName("Verify serialization defense successfully returns the exact same instance for Reflection And Serialization safe impl")
    void testSerializationReturnsSameInstanceOfReflectionAndSerializationSafeImpl() throws Exception {
        ReflectionAndSerializationSafeInitializationHolderAppConfigManager instanceOne = 
        ReflectionAndSerializationSafeInitializationHolderAppConfigManager.getInstance();

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(instanceOne);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ReflectionAndSerializationSafeInitializationHolderAppConfigManager instanceTwo = (ReflectionAndSerializationSafeInitializationHolderAppConfigManager) ois.readObject();
        ois.close();

        // Success Check: Verify that the identity contract is preserved
        assertSame(instanceOne, instanceTwo, "SUCCESS: readResolve() intercepted the leak!");
        System.out.println("Success: Serialization leak plugged. Both hashcodes match: " + instanceOne.hashCode());
    }

    @Test
    @DisplayName("Verify duplicate instance creation not allowed via reflection for Enum type Impl")
    void testReflectionCannotCreateDuplicateInstancesOfEnumTypeImpl() throws Exception {
        // Step 1: Grab the legitimate operational instance via the public API
        EnumTypeAppConfigManager instanceOne = EnumTypeAppConfigManager.INSTANCE;

        // Step 2: Use Java Reflection to dig into the class layout
        Class<EnumTypeAppConfigManager> clazz = EnumTypeAppConfigManager.class;
        
        // Retrieve the private constructor hidden from standard compilation rules
        Constructor<EnumTypeAppConfigManager> declaredConstructor = clazz.getDeclaredConstructor(String.class, int.class);

        // THE CRITICAL EXPLOIT: Force the JVM to override the 'private' visibility modifier
        declaredConstructor.setAccessible(true);

        // Step 3: Instantiate a completely separate object in memory using the backdoor, but this will result in an exception now
        IllegalArgumentException iae = assertThrows(IllegalArgumentException.class, () -> declaredConstructor.newInstance("SPY_INSTANCE", 1));
        assertEquals("Cannot reflectively create enum objects", iae.getMessage());
    }

    @Test
    @DisplayName("Verify serialization defense successfully returns the exact same instance for Enum type Impl")
    void testSerializationReturnsSameInstanceOfEnumTypeImpl() throws Exception {
        EnumTypeAppConfigManager instanceOne = EnumTypeAppConfigManager.INSTANCE;

        // Serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(instanceOne);
        oos.close();

        // Deserialize
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        EnumTypeAppConfigManager instanceTwo = (EnumTypeAppConfigManager) ois.readObject();
        ois.close();

        // Success Check: Verify that the identity contract is preserved
        assertSame(instanceOne, instanceTwo, "SUCCESS: readResolve() intercepted the leak!");
        System.out.println("Success: Serialization leak plugged. Both hashcodes match: " + instanceOne.hashCode());
    }
}
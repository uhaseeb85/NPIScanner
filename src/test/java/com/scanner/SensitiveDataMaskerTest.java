package com.scanner;

import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveDataMaskerTest {

    @Test
    void testMaskAndSerialize_SimpleObject() {
        User user = new User("john_doe", "password123", "john@example.com");
        String json = SensitiveDataMasker.maskAndSerialize(user);

        assertTrue(json.contains("\"username\": \"john_doe\""));
        assertTrue(json.contains("\"email\": \"john@example.com\""));
        assertTrue(json.contains("\"password\": \"***\""));
        assertFalse(json.contains("password123"));
    }

    @Test
    void testMaskAndSerialize_NestedObject() {
        Address address = new Address("123 Main St", "City", "12345");
        UserProfile profile = new UserProfile("jane_doe", "secretToken", address);
        String json = SensitiveDataMasker.maskAndSerialize(profile);

        assertTrue(json.contains("\"username\": \"jane_doe\""));
        assertTrue(json.contains("\"token\": \"***\""));
        assertFalse(json.contains("secretToken"));
        assertTrue(json.contains("\"street\": \"123 Main St\""));
    }

    @Test
    void testMaskAndSerialize_Collections() {
        List<String> secrets = Arrays.asList("secret1", "secret2");
        Map<String, String> config = new HashMap<>();
        config.put("apiKey", "12345-key");
        config.put("publicValue", "public");

        ConfigContainer container = new ConfigContainer(secrets, config);
        String json = SensitiveDataMasker.maskAndSerialize(container);

        assertTrue(json.contains("\"apiKey\": \"***\""));
        assertFalse(json.contains("12345-key"));
        assertTrue(json.contains("\"publicValue\": \"public\""));
    }

    @Test
    void testMaskAndSerialize_CircularReference() {
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        nodeA.next = nodeB;
        nodeB.next = nodeA;

        String json = SensitiveDataMasker.maskAndSerialize(nodeA);

        assertTrue(json.contains("\"name\": \"A\""));
        assertTrue(json.contains("\"name\": \"B\""));
        assertTrue(json.contains("[Circular Reference]"));
    }

    @Test
    void testMaskAndSerialize_CustomPatterns() {
        CustomData data = new CustomData("value1", "value2");
        List<String> patterns = Arrays.asList("field1");

        String json = SensitiveDataMasker.maskAndSerialize(data, patterns);

        assertTrue(json.contains("\"field1\": \"***\""));
        assertFalse(json.contains("value1"));
        assertTrue(json.contains("\"field2\": \"value2\""));
    }

    @Test
    void testMaskAndSerialize_Null() {
        assertEquals("null", SensitiveDataMasker.maskAndSerialize(null));
    }

    // Helper classes for testing
    static class User {
        String username;
        String password;
        String email;

        User(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }
    }

    static class Address {
        String street;
        String city;
        String zip;

        Address(String street, String city, String zip) {
            this.street = street;
            this.city = city;
            this.zip = zip;
        }
    }

    static class UserProfile {
        String username;
        String token;
        Address address;

        UserProfile(String username, String token, Address address) {
            this.username = username;
            this.token = token;
            this.address = address;
        }
    }

    static class ConfigContainer {
        List<String> secrets;
        Map<String, String> config;

        ConfigContainer(List<String> secrets, Map<String, String> config) {
            this.secrets = secrets;
            this.config = config;
        }
    }

    static class Node {
        String name;
        Node next;

        Node(String name) {
            this.name = name;
        }
    }

    static class CustomData {
        String field1;
        String field2;

        CustomData(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }
}

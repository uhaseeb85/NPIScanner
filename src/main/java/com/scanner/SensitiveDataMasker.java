package com.scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Utility class for masking sensitive data in Java objects before
 * serialization.
 * Uses reflection to inspect object fields and masks values that match
 * sensitive patterns.
 */
public class SensitiveDataMasker {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Default list of sensitive field name patterns (case-insensitive).
     */
    private static final List<String> DEFAULT_SENSITIVE_PATTERNS = Arrays.asList(
            "password", "passwd", "pwd",
            "ssn", "social_security", "socialsecurity",
            "credit_card", "creditcard", "debit_card", "debitcard",
            "api_key", "apikey", "secret",
            "token", "auth_token", "authtoken",
            "pin", "cvv", "cvc",
            "dob", "date_of_birth", "dateofbirth");

    private static final String MASK_VALUE = "***";

    /**
     * Masks sensitive data in the given object and returns it as a JSON string.
     *
     * @param obj the object to mask and serialize
     * @return JSON string with masked sensitive fields
     */
    public static String maskAndSerialize(Object obj) {
        return maskAndSerialize(obj, DEFAULT_SENSITIVE_PATTERNS);
    }

    /**
     * Masks sensitive data in the given object using custom patterns and returns it
     * as a JSON string.
     *
     * @param obj               the object to mask and serialize
     * @param sensitivePatterns list of sensitive field name patterns
     * @return JSON string with masked sensitive fields
     */
    public static String maskAndSerialize(Object obj, List<String> sensitivePatterns) {
        if (obj == null) {
            return "null";
        }

        Set<Integer> processedObjects = new HashSet<>();
        Object maskedObj = maskObject(obj, sensitivePatterns, processedObjects);
        return GSON.toJson(maskedObj);
    }

    /**
     * Recursively masks sensitive fields in an object.
     *
     * @param obj               the object to mask
     * @param sensitivePatterns list of sensitive patterns
     * @param processedObjects  set of already processed object identities (for
     *                          circular reference detection)
     * @return the masked object
     */
    private static Object maskObject(Object obj, List<String> sensitivePatterns, Set<Integer> processedObjects) {
        if (obj == null) {
            return null;
        }

        // Prevent circular references
        int objectId = System.identityHashCode(obj);
        if (processedObjects.contains(objectId)) {
            return "[Circular Reference]";
        }
        processedObjects.add(objectId);

        Class<?> clazz = obj.getClass();

        // Handle primitives and wrapper types
        if (isPrimitiveOrWrapper(clazz) || clazz == String.class) {
            return obj;
        }

        // Handle arrays
        if (clazz.isArray()) {
            return maskArray(obj, sensitivePatterns, processedObjects);
        }

        // Handle collections
        if (obj instanceof Collection) {
            return maskCollection((Collection<?>) obj, sensitivePatterns, processedObjects);
        }

        // Handle maps
        if (obj instanceof Map) {
            return maskMap((Map<?, ?>) obj, sensitivePatterns, processedObjects);
        }

        // Handle custom objects using reflection
        return maskCustomObject(obj, sensitivePatterns, processedObjects);
    }

    /**
     * Masks sensitive fields in a custom object using reflection.
     */
    private static Map<String, Object> maskCustomObject(Object obj, List<String> sensitivePatterns,
            Set<Integer> processedObjects) {
        Map<String, Object> result = new LinkedHashMap<>();
        Class<?> clazz = obj.getClass();

        // Get all fields including inherited ones
        List<Field> allFields = getAllFields(clazz);

        for (Field field : allFields) {
            // Skip static fields
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String fieldName = field.getName();

            try {
                Object fieldValue = field.get(obj);

                // Check if field name matches sensitive pattern
                if (isSensitiveField(fieldName, sensitivePatterns)) {
                    result.put(fieldName, MASK_VALUE);
                } else if (fieldValue != null) {
                    // Recursively mask nested objects
                    result.put(fieldName, maskObject(fieldValue, sensitivePatterns, processedObjects));
                } else {
                    result.put(fieldName, null);
                }
            } catch (IllegalAccessException e) {
                result.put(fieldName, "[Inaccessible]");
            }
        }

        return result;
    }

    /**
     * Gets all fields from a class including inherited fields.
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * Checks if a field name matches any sensitive pattern.
     */
    private static boolean isSensitiveField(String fieldName, List<String> sensitivePatterns) {
        String lowerFieldName = fieldName.toLowerCase();
        for (String pattern : sensitivePatterns) {
            if (lowerFieldName.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Masks elements in an array.
     */
    private static Object maskArray(Object array, List<String> sensitivePatterns, Set<Integer> processedObjects) {
        int length = java.lang.reflect.Array.getLength(array);
        List<Object> result = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            Object element = java.lang.reflect.Array.get(array, i);
            result.add(maskObject(element, sensitivePatterns, processedObjects));
        }

        return result;
    }

    /**
     * Masks elements in a collection.
     */
    private static Collection<Object> maskCollection(Collection<?> collection, List<String> sensitivePatterns,
            Set<Integer> processedObjects) {
        List<Object> result = new ArrayList<>();

        for (Object element : collection) {
            result.add(maskObject(element, sensitivePatterns, processedObjects));
        }

        return result;
    }

    /**
     * Masks values in a map.
     */
    private static Map<Object, Object> maskMap(Map<?, ?> map, List<String> sensitivePatterns,
            Set<Integer> processedObjects) {
        Map<Object, Object> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();

            // Check if key is a sensitive field name
            if (key instanceof String && isSensitiveField((String) key, sensitivePatterns)) {
                result.put(key, MASK_VALUE);
            } else {
                result.put(key, maskObject(value, sensitivePatterns, processedObjects));
            }
        }

        return result;
    }

    /**
     * Checks if a class is a primitive or wrapper type.
     */
    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == Boolean.class ||
                clazz == Byte.class ||
                clazz == Character.class ||
                clazz == Short.class ||
                clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class;
    }
}

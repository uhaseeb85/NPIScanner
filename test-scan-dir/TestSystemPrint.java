package com.example;

public class TestSystemPrint {
    public void testMethod(String ssn, String password) {
        System.out.println("User SSN: " + ssn);
        System.err.println("Password: " + password);
        logger.info("Using logger: " + ssn);
    }
}

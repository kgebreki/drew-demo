package com.demo.testing;

/**
 * Represents the result of a single test case.
 */
public class TestResult {

    private final String name;
    private final boolean passed;
    private final String errorMessage;

    public TestResult(String name, boolean passed, String errorMessage) {
        this.name = name;
        this.passed = passed;
        this.errorMessage = errorMessage;
    }

    public String getName() {
        return name;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

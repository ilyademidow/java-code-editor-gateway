package ru.idemidov.interviewgateway;

public class InterviewException extends RuntimeException {
    private String message;

    public InterviewException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}

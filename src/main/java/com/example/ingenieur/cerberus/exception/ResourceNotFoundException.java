package com.example.ingenieur.cerberus.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final String field;
    private final Object resourceId;

    public ResourceNotFoundException(String message, String field, Object resourceId) {
        super(message);
        this.field = field;
        this.resourceId = resourceId;
    }

    public String getField() {
        return field;
    }

    public Object getResourceId() {
        return resourceId;
    }
}


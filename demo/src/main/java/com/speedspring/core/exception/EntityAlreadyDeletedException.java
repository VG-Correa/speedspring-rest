package com.speedspring.core.exception;


public class EntityAlreadyDeletedException extends RuntimeException{
    public EntityAlreadyDeletedException(String message) {
        super(message);
    }
}

package com.spec.speedspring.core.exception;

public class UserUnauthorizedException extends RuntimeException{
    
    public UserUnauthorizedException(String mensagem) {
        super(mensagem);
    }

}

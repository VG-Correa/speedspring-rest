package com.spec.speedspring.core.exception;

public class UserUnauthorizedException extends RuntimeException{
    
    UserUnauthorizedException(String mensagem) {
        super(mensagem);
    }

}

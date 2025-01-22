package com.santanna.serviceorder.handler.model;

public class BadRequestException extends RuntimeException {

        public BadRequestException(String message) {
            super(message);
        }
}

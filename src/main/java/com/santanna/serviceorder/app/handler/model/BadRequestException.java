package com.santanna.serviceorder.app.handler.model;

public class BadRequestException extends RuntimeException {

        public BadRequestException(String message) {
            super(message);
        }
}

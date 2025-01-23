package com.santanna.serviceorder.app.handler.model;

public class NotFoundException extends RuntimeException {

        public NotFoundException(String message) {
            super(message);
        }
}

package com.santanna.serviceorder.handler.model;

public class NotFoundException extends RuntimeException {

        public NotFoundException(String message) {
            super(message);
        }
}

package com.elecciones.senado.apigateway.exceptions;

public class InvalidHeaderException extends Exception {

    public InvalidHeaderException(String message) {
        super(message);
    }
}

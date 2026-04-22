package br.com.teste.exception;

public class GarageConfigException extends RuntimeException {
    public GarageConfigException(String message) {
        super(message);
    }

    public GarageConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}

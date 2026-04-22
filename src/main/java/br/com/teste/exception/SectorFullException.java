package br.com.teste.exception;

public class SectorFullException extends RuntimeException {
    public SectorFullException(String message) {
        super(message);
    }

    public SectorFullException(String message, Throwable cause) {
        super(message, cause);
    }
}

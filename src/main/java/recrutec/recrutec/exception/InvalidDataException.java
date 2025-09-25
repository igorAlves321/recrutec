package recrutec.recrutec.exception;

/**
 * Exceção lançada quando dados inválidos são fornecidos
 */
public class InvalidDataException extends RuntimeException {

    public InvalidDataException(String message) {
        super(message);
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
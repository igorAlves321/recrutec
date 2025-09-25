package recrutec.recrutec.exception;

/**
 * Exceção lançada quando regras de negócio são violadas
 */
public class BusinessLogicException extends RuntimeException {

    public BusinessLogicException(String message) {
        super(message);
    }

    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}
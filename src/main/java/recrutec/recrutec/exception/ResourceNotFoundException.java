package recrutec.recrutec.exception;

/**
 * Exceção lançada quando um recurso não é encontrado
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s não encontrado com %s: '%s'", resource, field, value));
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
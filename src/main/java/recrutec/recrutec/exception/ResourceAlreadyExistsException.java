package recrutec.recrutec.exception;

/**
 * Exceção lançada quando se tenta criar um recurso que já existe
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resource, String field, Object value) {
        super(String.format("%s já existe com %s: '%s'", resource, field, value));
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
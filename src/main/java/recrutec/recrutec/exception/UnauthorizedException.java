package recrutec.recrutec.exception;

/**
 * Exceção lançada quando o usuário não tem autorização para acessar um recurso
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
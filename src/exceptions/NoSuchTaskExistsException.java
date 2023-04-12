package exceptions;

public class NoSuchTaskExistsException extends RuntimeException {

    public NoSuchTaskExistsException(String message) {
        super(message);
    }
}

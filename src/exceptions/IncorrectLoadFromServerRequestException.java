package exceptions;

public class IncorrectLoadFromServerRequestException extends RuntimeException {
    public IncorrectLoadFromServerRequestException(String message) {
        super(message);
    }
}

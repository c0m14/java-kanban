package exceptions;

public class NoSuchTaskExists extends RuntimeException {

    public NoSuchTaskExists(String message) {
        super(message);
    }
}

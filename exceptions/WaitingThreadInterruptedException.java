package bg.sofia.uni.fmi.mjt.battleships.exceptions;

public class WaitingThreadInterruptedException extends RuntimeException {

    public WaitingThreadInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

}

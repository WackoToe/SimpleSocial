package Exceptions;

/**
 * Created by Paolo on 16/05/2016.
 */
public class WrongLoginException extends Exception {
    public WrongLoginException(String message)
    {
        super(message);
    }
}

package bfst20.mapdrawer.exceptions;

public class NoPointChosenException extends Exception {
    public NoPointChosenException() {
        super("Du skal først sætte et punkt på kortet (via. højreklik) for at kunne gemme det!");
    }
}

package bfst20.mapdrawer.Exceptions;

public class noPointChosenException extends Exception {

    public noPointChosenException() {
        super("Du skal først sætte et punkt på kortet (via. højreklik) for at kunne gemme det!");
    }
}

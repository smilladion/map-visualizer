package bfst20.mapdrawer.Exceptions;

public class noSavedPointsException extends Exception {

    public noSavedPointsException() {
        super("Du har ikke nogle gemte punkter. Højreklik på kortet, og tryk på 'Gem punkt', for at gemme et punkt");
    }
}

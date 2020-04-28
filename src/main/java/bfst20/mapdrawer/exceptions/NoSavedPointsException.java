package bfst20.mapdrawer.exceptions;

public class NoSavedPointsException extends Exception {

    public NoSavedPointsException() {
        super("Du har ikke nogle gemte punkter. Højreklik på kortet, og tryk på 'Gem punkt', for at gemme et punkt");
}
}

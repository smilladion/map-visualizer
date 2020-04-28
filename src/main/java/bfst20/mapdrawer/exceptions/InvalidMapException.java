package bfst20.mapdrawer.exceptions;

public class InvalidMapException extends Exception {
    public InvalidMapException() {
        super("Forkert filtype! \n\n Programmet understøtter OSM, ZIP og BIN.");
    }
}

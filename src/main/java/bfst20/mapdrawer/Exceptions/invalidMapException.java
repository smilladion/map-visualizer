package bfst20.mapdrawer.Exceptions;

public class invalidMapException extends Exception {
    public invalidMapException() {
        super("Forkert filtype! \n\n Programmet understøtter OSM, ZIP og BIN.");
    }
}

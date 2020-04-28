package bfst20.mapdrawer.exceptions;

public class NoAddressMatchException extends Exception {

    public NoAddressMatchException() {
        super("Vi kunne desværre ikke finde en adresse der matchede din søgning, prøv venligst igen.");
    }
}

package bfst20.mapdrawer.Exceptions;

public class noAddressMatchException extends Exception {

    public noAddressMatchException() {
        super("Vi kunne desværre ikke finde en adresse der matchede din søgning, prøv venligst igen.");
    }
}

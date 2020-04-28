package bfst20.mapdrawer.exceptions;

public class NoRouteException extends Exception {

    public NoRouteException(String vehicleAlternative1, String vehicleAlternative2) {
        super("Vi kunne desværre ikke finde en rute mellem dine ønskede adresser. Prøv at skift til "
                + vehicleAlternative1 + " eller " + vehicleAlternative2 + ", eller søg på en anden adresse.");

    }
}

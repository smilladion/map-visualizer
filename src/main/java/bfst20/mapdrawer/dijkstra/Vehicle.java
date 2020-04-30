package bfst20.mapdrawer.dijkstra;

/**
 *Interface for the three different types of route you can take.
 * The classes implementing it (car, bike, walk) sets the corresponding boolean method to true.
*/
public interface Vehicle {

    boolean isCar();

    boolean isBike();

    boolean isWalk();

    boolean isSameVehicleAs(Vehicle vehicle);
    
}

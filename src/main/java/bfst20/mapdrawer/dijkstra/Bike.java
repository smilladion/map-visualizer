package bfst20.mapdrawer.dijkstra;

/** Routes of type bike. */
public class Bike implements Vehicle {

    @Override
    public boolean isCar() {
        return false;
    }
    
    @Override
    public boolean isBike() {
        return true;
    }

    @Override
    public boolean isWalk() {
        return false;
    }

    @Override
    public boolean isSameVehicleAs(Vehicle vehicle) {
        return vehicle.isBike();
    }
}

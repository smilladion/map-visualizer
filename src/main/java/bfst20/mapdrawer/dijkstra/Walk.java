package bfst20.mapdrawer.dijkstra;

/** Routes of type walk. */
public class Walk implements Vehicle {

    @Override
    public boolean isCar() {
        return false;
    }
    
    @Override
    public boolean isBike() {
        return false;
    }

    @Override
    public boolean isWalk() {
        return true;
    }

    @Override
    public boolean isSameVehicleAs(Vehicle vehicle) {
        return vehicle.isWalk();
    }
}

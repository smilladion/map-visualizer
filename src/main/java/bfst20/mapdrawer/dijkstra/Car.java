package bfst20.mapdrawer.dijkstra;

public class Car implements Vehicle {

    @Override
    public boolean isCar() {
        return true;
    }
    
    @Override
    public boolean isBike() {
        return false;
    }
    
    @Override
    public boolean isWalk() {
        return false;
    }

    @Override
    public boolean isSameVehicleAs(Vehicle vehicle) {
        if (vehicle.isCar()) {
            return true;
        }
        return false;
    }
}

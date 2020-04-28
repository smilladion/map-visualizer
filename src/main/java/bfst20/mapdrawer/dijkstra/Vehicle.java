package bfst20.mapdrawer.dijkstra;

public interface Vehicle {

    public boolean isCar();

    public boolean isBike();

    public boolean isWalk();

    public boolean isSameVehicleAs(Vehicle vehicle);
}

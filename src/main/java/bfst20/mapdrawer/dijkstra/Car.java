package bfst20.mapdrawer.dijkstra;

public class Car implements Vehicle {

    @Override
    public boolean isCar() {
        return true;
    }

    public boolean isBike() {
        return false;
    }

    public boolean isWalk() {
        return false;
    }

}

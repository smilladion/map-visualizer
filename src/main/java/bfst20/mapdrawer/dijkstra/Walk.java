package bfst20.mapdrawer.dijkstra;

public class Walk implements Vehicle {

    @Override
    public boolean isCar() {
        return false;
    }

    public boolean isBike() {
        return false;
    }
    public boolean isWalk() {
        return true;
    }
}

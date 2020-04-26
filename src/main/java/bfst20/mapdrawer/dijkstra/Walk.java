package bfst20.mapdrawer.dijkstra;

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
}

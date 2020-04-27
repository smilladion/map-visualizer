package bfst20.mapdrawer.dijkstra;

public class Bike implements Vehicle {

    @Override
    public boolean isCar() {
        return false;
    }

    public boolean isBike() {return true;}

    public boolean isWalk() {return false;}
}

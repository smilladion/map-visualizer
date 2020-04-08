package bfst20.mapdrawer.Rutevejledning;

public class DirectedEdge {

    int from;
    int to;
    int weight;
    boolean bike;
    boolean walk;
    boolean car;

    public DirectedEdge(int from, int to, int weight, boolean bike, boolean walk, boolean car) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.bike = bike;
        this.walk = walk;
        this.car = car;
    }

}

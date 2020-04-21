package bfst20.mapdrawer.dijkstra;

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

    public int from() {
        return from;
    }

    public int to() {
        return to;
    }

    public int getWeight() {
        return weight;
    }

   public String toString() {
        return String.format("from: " + from + ", to: " + to);
   }

}

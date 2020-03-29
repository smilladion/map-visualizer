package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.osm.OSMWay;

import java.util.Comparator;
import java.util.List;

/*
This tree takes in OSMWays and finds the average coordinate from its list of OSMNodes.
This average coordinate represents the way, and is what the KdTree contains (and makes splitting lines from).
That way we can see whether the majority of a way is outside/inside of a bounding box.
Because a way would not get drawn if less than its average is on screen, this method may need a "border" bigger than the current zoom level.
Other situations like this will need similar fixes, but overall this implementation should hopefully work.
 */
public class KdTree {

    // Stores the root. Allows "travel" down the tree through references to KdNode's left and right child.
    private final KdNode root;

    public KdTree(List<OSMWay> nodes) {
        root = build(nodes, 0);

        //draw(root, 0, 2); // TODO: For testing in console
        System.out.flush(); // Bugfix for when it doesn't system print
    }

    private static KdNode build(List<OSMWay> nodes, int depth) { // Builds the tree (is called in the constructor)
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        if (nodes.size() == 1) { // If there is only one way left (this terminates the recursive call)
            return new KdNode(nodes.get(0)); // ... add as a leaf node
        } else if (depth % 2 == 0) { // If depth is even...
            nodes.sort(Comparator.comparing(OSMWay::getAvgX)); // ... then we sort the list by x-coordinate
        } else {
            nodes.sort(Comparator.comparing(OSMWay::getAvgY)); // Otherwise if odd, sort by y-coordinate
        }

        // Split the list down the middle and make two new lists, represents the left and right side of the splitting line
        List<OSMWay> left = nodes.subList(0, nodes.size() / 2);
        List<OSMWay> right = nodes.subList(nodes.size() / 2, nodes.size());

        // Call method again with new lists to determine right and left child of the node
        KdNode vLeft = build(left.subList(0, left.size() - 1), depth + 1); // Removes/ignores the median way to avoid duplicates in the tree
        KdNode vRight = build(right, depth + 1);

        OSMWay median = left.get(left.size() - 1); // Stores the median way, which represents the coordinates for the splitting line

        return new KdNode(median, vLeft, vRight);
    }

    // TODO: Method purely used for testing in console, remove when not needed anymore
    public void draw(KdNode node, int indentation, int lrn) {
        drawIndent(indentation);

        if (node == null) {
            if (lrn == 0) {
                System.out.println("L EMPTY");
            } else if (lrn == 1) {
                System.out.println("R EMPTY");
            }

            return;
        }

        if (lrn == 0) {
            System.out.print("L ");
        } else if (lrn == 1) {
            System.out.print("R ");
        }

        System.out.print(node);
        System.out.print("\n");

        draw(node.left, indentation + 1, 0);
        draw(node.right, indentation + 1, 1);
    }

    // Also testing
    private void drawIndent(int indentation) {
        for (int i = 0; i < indentation; i++) {
            System.out.print("-");
        }
    }

    public KdNode getRoot() {
        return root;
    }

    private static class KdPoint {

        private final float x;
        private final float y;

        private KdPoint(OSMWay way) {
            this.x = way.getAvgX(); // Creates the average point for a specified way
            this.y = way.getAvgY();
        }

        @Override
        public String toString() { // TODO: Testing purposes
            return "KdPoint{" +
                "x=" + x +
                ", y=" + y +
                '}';
        }
    }

    private static class KdNode {

        private final OSMWay way;
        private final KdPoint point;

        private final KdNode left;
        private final KdNode right;

        private KdNode(OSMWay way, KdNode left, KdNode right) { // This is a normal node
            this.way = way;
            this.point = new KdPoint(way);

            this.left = left;
            this.right = right;
        }

        private KdNode(OSMWay way) { // This is a leaf node with no children
            this.way = way;
            this.point = new KdPoint(way);

            this.left = null;
            this.right = null;
        }

        @Override
        public String toString() { // TODO: Testing purposes
            return "KdNode{" +
                "point=" + point +
                '}';
        }
    }
}

package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.osm.OSMMap;
import bfst20.mapdrawer.osm.OSMWay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/*
This tree takes in OSMWays and finds the average coordinate from its list of OSMNodes.
This average coordinate represents the way, and is what the KdTree contains (and makes splitting lines from).
That way we can see whether the majority of a way is outside/inside of a bounding box.
Because a way would not get drawn if less than its average is on screen, this method may need a "border" bigger than the current zoom level.
Other situations like this will need similar fixes, but overall this implementation should hopefully work.
 */
public class KdTree { // TODO Only works for distinct points right now (supposedly)

    // Stores the root. Allows "travel" down the tree through references to KdNode's left and right child.
    private final KdNode root;
    private final Rect rootBounds; // The coordinates for the whole canvas

    public KdTree(List<OSMWay> nodes, OSMMap model) {
        rootBounds = new Rect(model.getMinLon(), model.getMinLat(), model.getMaxLon(), model.getMaxLat());
        root = build(nodes, 0, rootBounds);

        //draw(root, 0, 2); // TODO: For testing in console
        System.out.flush(); // Bugfix for when it doesn't system print
    }

    // Builds the tree (is called in the constructor)
    private static KdNode build(List<OSMWay> nodes, int depth, Rect nodeArea) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        if (nodes.size() == 1) { // If there is only one way left (this terminates the recursive call)
            return new KdNode(nodes.get(0), nodeArea); // ... add as a leaf node
        } else if (depth % 2 == 0) { // If depth is even...
            nodes.sort(Comparator.comparing(OSMWay::getAvgX)); // ... then we sort the list by x-coordinate
        } else {
            nodes.sort(Comparator.comparing(OSMWay::getAvgY)); // Otherwise if odd, sort by y-coordinate
        }

        // Split the list down the middle and make two new lists, represents the left and right side of the splitting line
        List<OSMWay> left = nodes.subList(0, nodes.size() / 2);
        List<OSMWay> right = nodes.subList(nodes.size() / 2, nodes.size());

        OSMWay median = left.get(left.size() - 1); // Stores the median way, which represents the coordinates for the splitting line

        // The section below defines the area of the left and right children
        Rect areaLeft, areaRight;

        if (depth % 2 == 0) {
            areaLeft = new Rect(nodeArea.xmin, nodeArea.ymin, median.getAvgX(), nodeArea.ymax);
            areaRight = new Rect(median.getAvgX(), nodeArea.ymin, nodeArea.xmax, nodeArea.ymax);
        } else {
            areaLeft = new Rect(nodeArea.xmin, nodeArea.ymin, nodeArea.xmax, median.getAvgY());
            areaRight = new Rect(nodeArea.xmin, median.getAvgY(), nodeArea.xmax, nodeArea.ymax);
        }

        // Call method again with new lists to determine right and left child of the node
        KdNode vLeft = build(left.subList(0, left.size() - 1), depth + 1, areaLeft); // Removes/ignores the median way to avoid duplicates in the tree
        KdNode vRight = build(right, depth + 1, areaRight);

        return new KdNode(median, nodeArea, vLeft, vRight);
    }

    // Searches the tree with the specified range, returns a list of ways in the range
    public Collection<OSMWay> search(KdNode node, float minx, float miny, float maxx, float maxy) {

        Rect range = new Rect(minx, miny, maxx, maxy);
        List<OSMWay> results = new ArrayList<>();

        if (node.left == null && node.right == null) { // If node is a leaf
            if (range.containsPoint(node.point)) { // Is this point in the range?
                results.add(node.way); // Add to results (confirmed in range)
            }
        }
        if (node.left != null && range.intersects(node.left.area)) { // If the range and node's area intersect
            if (range.containsRect(node.left.area)) { // If the node's area is fully contained within range
                results.addAll(getKdNodesFrom(node.left)); // Add it and all of its children
            } else {
                results.addAll(search(node.left, minx, miny, maxx, maxy)); // Run method again and add its results to the current result list
            }
        }
        if (node.right != null && range.intersects(node.right.area)) {
            if (range.containsRect(node.right.area)) {
                results.addAll(getKdNodesFrom(node.right));
            } else {
                results.addAll(search(node.right, minx, miny, maxx, maxy));
            }
        }

        return results;
    }

    public Collection<OSMWay> getKdNodesFrom(KdNode root) {
        List<OSMWay> results = new ArrayList<>();
        results.add(root.way);

        if (root.left != null) {
            results.addAll(getKdNodesFrom(root.left));
        }
        if (root.right != null) {
            results.addAll(getKdNodesFrom(root.right));
        }

        return results;
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
        private final Rect area; // The bounds the splitting node defines

        private final KdNode left;
        private final KdNode right;

        private KdNode(OSMWay way, Rect area, KdNode left, KdNode right) { // This is a normal node
            this.way = way;
            this.point = new KdPoint(way);
            this.area = area;

            this.left = left;
            this.right = right;
        }

        private KdNode(OSMWay way, Rect area) { // This is a leaf node with no children
            this.way = way;
            this.point = new KdPoint(way);
            this.area = area;

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

    private static class Rect {

        private final double xmin;
        private final double ymin;
        private final double xmax;
        private final double ymax;

        public Rect(double xmin, double ymin, double xmax, double ymax) {
            this.xmin = xmin;
            this.ymin = ymin;
            this.xmax = xmax;
            this.ymax = ymax;
        }

        // Returns true for normal intersections (including bounds) and if one is fully contained within the other
        public boolean intersects(Rect rect) {
            return xmax >= rect.xmin && ymax >= rect.ymin && rect.xmax >= xmin && rect.ymax >= ymin;
        }

        // Returns true only if one rectangle is fully contained within another
        public boolean containsRect(Rect rect) {
            return rect.xmin >= xmin && rect.xmax <= xmax && rect.ymin >= ymin && rect.ymax <= ymax;
        }

        public boolean containsPoint(KdPoint p) {
            return p.x >= xmin && p.x <= xmax && p.y >= ymin && p.y <= ymax;
        }
    }
}

package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.geometry.Point2D;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
The tree takes in a list of NodeProviders, meaning classes that contain a drawable and a bounding box.
This makes it possible to have both ways and relations in the KdTree.
Comparisons are made not using splitting lines like normal, but bounding boxes around each element.
Each KdNode in the tree contains a bounding box encompassing all of its children's bounding boxes.
 */

public class KdTree implements Serializable{

    private static final long serialVersionUID = 1L;

    private final KdNode root;

    public KdTree(List<NodeProvider> nodes) {
        root = build(nodes, 0);
    }

    // Builds the tree (is called in the constructor)
    private static KdNode build(List<NodeProvider> nodes, int depth) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        if (nodes.size() == 1) { // If there is only one node left (this terminates the recursive call)
            return new KdNode(nodes.get(0)); // ... add as a leaf node
        } else if (depth % 2 == 0) { // If depth is even...
            nodes.sort(Comparator.comparing(NodeProvider::getAvgX)); // ... then we sort the list by x-coordinate
        } else {
            nodes.sort(Comparator.comparing(NodeProvider::getAvgY)); // Otherwise if odd, sort by y-coordinate
        }

        // Split the list down the middle and make two new lists
        List<NodeProvider> left = nodes.subList(0, nodes.size() / 2);
        List<NodeProvider> right = nodes.subList(nodes.size() / 2, nodes.size());

        NodeProvider median = left.get(left.size() - 1); // Stores the median way, which represents the current (root) node

        // Call method again with new lists to determine right and left child of the node
        KdNode vLeft = build(left.subList(0, left.size() - 1), depth + 1); // Removes/ignores the median way to avoid duplicates in the tree
        KdNode vRight = build(right, depth + 1);

        return new KdNode(median, vLeft, vRight);
    }
    
    public ArrayList<NodeProvider> search(Rectangle range) {
        ArrayList<NodeProvider> results = new ArrayList<>();
        search(results, root, range);
        
        return results;
    }
    
    /** Searches the tree with the specified range, returns a set of provider IDs (ways/relations) in the range. */
    private void search(ArrayList<NodeProvider> results, KdNode node, Rectangle range) {
        results.add(node.provider);

        if (node.left != null && range.intersects(node.left.boundingBox)) {
            search(results, node.left, range);
        }

        if (node.right != null && range.intersects(node.right.boundingBox)) {
            search(results, node.right, range);
        }
    }

    /** Finds the nearest road to a specific point. */
    public OSMWay nearest(double x, double y) {
        return nearest(root, new Point2D(x, y), null);
    }
    
    // Only returns/checks ways that contain a road name - will need another method if we want to include all nodeproviders
    private OSMWay nearest(KdNode node, Point2D point, OSMWay nearest) {
        if (node.provider instanceof OSMWay && ((OSMWay) node.provider).getRoad() != null) {
            OSMWay current = (OSMWay) node.provider;
            
            if (distance(point, nearest) > distance(point, current)) {
                nearest = current;
            }
        }

        if (node.left != null && node.left.boundingBox.containsPoint(point)) {
            nearest = nearest(node.left, point, nearest);
        }

        if (node.right != null && node.right.boundingBox.containsPoint(point)) {
            nearest = nearest(node.right, point, nearest);
        }

        return nearest;
    }

    private double distance(Point2D point, OSMWay way) {
        if (way == null) {
            return Double.MAX_VALUE; // Distance is so big anything it is compared to will be smaller
        }
        
        // Keeps track of the current best distance
        double bestDistance = Double.MAX_VALUE;
        
        for (OSMNode node : way.getNodes()) {
            double distance = node.distanceSq(point);
            
            if (bestDistance > distance) {
                bestDistance = distance;
            }
        }

        return bestDistance;
    }

    public KdNode getRoot() {
        return root;
    }

    public static class KdNode implements Serializable{

        private static final long serialVersionUID = 1L;
        
        private final NodeProvider provider;
        private final Rectangle boundingBox;
        private final KdNode left;
        private final KdNode right;

        private KdNode(NodeProvider provider, KdNode left, KdNode right) { // This is a normal node
            this.provider = provider;
            this.left = left;
            this.right = right;
            this.boundingBox = createBoxFromChildren(this);
        }

        private KdNode(NodeProvider way) { // This is a leaf node with no children
            this.provider = way;
            this.left = null;
            this.right = null;
            this.boundingBox = provider.getBoundingBox();
        }

        private Rectangle createBoxFromChildren(KdNode node) {
            Rectangle midBox = node.provider.getBoundingBox();
            Rectangle leftBox = node.left == null ? midBox : node.left.boundingBox; // If left is null, set to midBox, otherwise set to box of left node
            Rectangle rightBox = node.right == null ? midBox : node.right.boundingBox;

            // Compares all 3 boxes to each other and grabs the coordinates that will contain them all within
            double xmin = Math.min(midBox.getXmin(), Math.min(leftBox.getXmin(), rightBox.getXmin()));
            double xmax = Math.max(midBox.getXmax(), Math.max(leftBox.getXmax(), rightBox.getXmax()));
            double ymin = Math.min(midBox.getYmin(), Math.min(leftBox.getYmin(), rightBox.getYmin()));
            double ymax = Math.max(midBox.getYmax(), Math.max(leftBox.getYmax(), rightBox.getYmax()));

            return new Rectangle(xmin, ymin, xmax, ymax);
        }
    }
}

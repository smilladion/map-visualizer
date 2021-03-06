package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.osm.NodeProvider;
import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.geometry.Point2D;
import javafx.scene.transform.Affine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class is used to filter out all elements outside the bounds of the screen, to increase performance.
 * The tree takes in a list of NodeProviders, meaning classes that contain a drawable and a bounding box.
 * This makes it possible to have both ways and relations in the KdTree.
 * Comparisons are made not using splitting lines like normal, but bounding boxes around each element.
 * Each KdNode in the tree contains a bounding box encompassing all of its children's bounding boxes.
 */
public class KdTree implements Serializable {

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

    /** Searches the tree with the specified range, returns a list of the providers (ways/relations) in the range. */
    public ArrayList<NodeProvider> search(Rectangle range) {
        ArrayList<NodeProvider> results = new ArrayList<>();
        search(results, root, range);

        return results;
    }
    
    private void search(ArrayList<NodeProvider> results, KdNode node, Rectangle range) {
        results.add(node.provider);

        if (node.left != null && range.intersects(node.left.boundingBox)) {
            search(results, node.left, range);
        }

        if (node.right != null && range.intersects(node.right.boundingBox)) {
            search(results, node.right, range);
        }
    }

    /** 
     * Finds the nearest road to a specific point. Option to include or ignore 
     * the current zoom level, to filter out invisible ways. 
     */
    public OSMWay nearest(double x, double y, boolean ignoreCurrentZoom, Affine transform) {
        return nearest(root, new Point2D(x, y), new OSMWay(), ignoreCurrentZoom, transform);
    }

    // Only returns/checks ways that contain a road name.
    private OSMWay nearest(KdNode node, Point2D point, OSMWay nearest, boolean ignoreCurrentZoom, Affine transform) {
        if (node.provider instanceof OSMWay && ((OSMWay) node.provider).getRoad() != null) {
            
            if (!ignoreCurrentZoom) {
                if (node.provider.getType().shouldPaint(transform.getMxx())) {
                    OSMWay current = (OSMWay) node.provider;

                    if (distance(point, nearest) > distance(point, current)) {
                        nearest = current;
                    }
                }
            } else {
                OSMWay current = (OSMWay) node.provider;

                if (distance(point, nearest) > distance(point, current)) {
                    nearest = current;
                }
            }
        }

        if (node.left != null) {
            nearest = nearest(node.left, point, nearest, ignoreCurrentZoom, transform);
        }

        if (node.right != null) {
            nearest = nearest(node.right, point, nearest, ignoreCurrentZoom, transform);
        }

        return nearest;
    }

    // Calculates the distance from a point to a way, based on the way's nodes.
    private float distance(Point2D point, OSMWay way) {
        if (way == null) {
            return Float.MAX_VALUE; // Distance is so big anything it is compared to will be smaller
        }

        // Keeps track of the current best distance
        float bestDistance = Float.MAX_VALUE;

        for (OSMNode node : way.getNodes()) {
            float distance = node.distanceSq(point);

            if (bestDistance > distance) {
                bestDistance = distance;
            }
        }

        return bestDistance;
    }

    /** Returns the node in a way that is closest to the given point. */
    public OSMNode nodeDistance(Point2D point, OSMWay way) {
        if (way == null) {
            return null;
        }

        // Keeps track of the current best distance
        float bestDistance = Float.MAX_VALUE;
        OSMNode bestNode = null;

        for (OSMNode node : way.getNodes()) {
            float distance = node.distanceSq(point);

            if (bestDistance > distance) {
                bestDistance = distance;
                bestNode = node;
            }
        }
        
        return bestNode;
    }

    // KdTree contains elements of this class, and does calculations based on its contents.
    private static class KdNode implements Serializable {

        private static final long serialVersionUID = 1L;

        private final NodeProvider provider;
        private final Rectangle boundingBox;
        private final KdNode left;
        private final KdNode right;

        // This is a normal node
        private KdNode(NodeProvider provider, KdNode left, KdNode right) {
            this.provider = provider;
            this.left = left;
            this.right = right;
            this.boundingBox = createBoxFromChildren(this);
        }

        // This is a leaf node with no children
        private KdNode(NodeProvider way) {
            this.provider = way;
            this.left = null;
            this.right = null;
            this.boundingBox = provider.getBoundingBox();
        }

        // Creates a new bounding box encompassing all the node's children.
        private Rectangle createBoxFromChildren(KdNode node) {
            Rectangle midBox = node.provider.getBoundingBox();
            Rectangle leftBox = node.left == null ? midBox : node.left.boundingBox; // If left is null, set to midBox, otherwise set to box of left node
            Rectangle rightBox = node.right == null ? midBox : node.right.boundingBox;

            // Compares all 3 boxes to each other and grabs the coordinates that will contain them all within
            float xmin = Math.min(midBox.getXmin(), Math.min(leftBox.getXmin(), rightBox.getXmin()));
            float xmax = Math.max(midBox.getXmax(), Math.max(leftBox.getXmax(), rightBox.getXmax()));
            float ymin = Math.min(midBox.getYmin(), Math.min(leftBox.getYmin(), rightBox.getYmin()));
            float ymax = Math.max(midBox.getYmax(), Math.max(leftBox.getYmax(), rightBox.getYmax()));

            return new Rectangle(xmin, ymin, xmax, ymax);
        }
    }
}

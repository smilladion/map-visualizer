package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.osm.OSMNode;
import bfst20.mapdrawer.osm.OSMWay;
import javafx.geometry.Point2D;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/*
The tree takes in a list of NodeProviders, meaning classes that contain a drawable and a bounding box.
This makes it possible to have both ways and relations in the KdTree.
Comparisons are made not using splitting lines like normal, but bounding boxes around each element.
Each KdNode in the tree contains a bounding box encompassing all of its children's bounding boxes.
 */

public class KdTree {

    private final KdNode root;

    public KdTree(List<NodeProvider> nodes) {
        root = build(nodes, 0);

        //draw(root, 0, 2); // TODO: For testing in console
        System.out.flush();
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

    // Searches the tree with the specified range, returns a list of providers (ways/relations) in the range
    public void search(List<NodeProvider> results, KdNode node, Rectangle range) {
        results.add(node.provider);

        if (node.left != null && range.intersects(node.left.boundingBox)) {
            search(results, node.left, range);
        }

        if (node.right != null && range.intersects(node.right.boundingBox)) {
            search(results, node.right, range);
        }
    }

    public NodeProvider nearest(double x, double y, Predicate<NodeProvider> predicate) {
        return nearest(root, new Point2D(x, y), null, predicate);
    }

    // TODO Middle nodes may be closer, right now only checking leaves
    private NodeProvider nearest(KdNode node, Point2D p, NodeProvider nearest, Predicate<NodeProvider> predicate) {
        OSMNode closestNode = new OSMNode(0, 0, 0);
        
        if (nearest == null) {
            nearest = predicate.test(node.provider) ? node.provider : null;
        }
        
        if (node.left != null && node.left.boundingBox.containsPoint(p)) {
            nearest = nearest(node.left, p, nearest, predicate);
        }

        if (node.right != null && node.right.boundingBox.containsPoint(p)) {
            nearest = nearest(node.right, p, nearest, predicate);
        }
        
        if (node.left == null && node.right == null) {
            if (predicate.test(node.provider)) {
                for (OSMNode n : ((OSMWay) node.provider).getNodes()) {
                    if (p.distance(new Point2D(n.getLon(), n.getLat())) < p.distance(new Point2D(closestNode.getLon(), closestNode.getLat()))) {
                        nearest = node.provider;
                        closestNode = n;
                    }
                }
            }
            
            /*if (p.distance(node.boundingBox.getCenterPoint()) < p.distance(nearest.getBoundingBox().getCenterPoint())) {
                    nearest = node.provider;*/
        }

        return nearest;
    }

    public KdNode getRoot() {
        return root;
    }

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

        System.out.print(node.provider.getAvgX() + " " + node.provider.getAvgY());
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

    public static class KdNode {

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

    private static class KdPoint {

        private final float x;
        private final float y;

        private KdPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}

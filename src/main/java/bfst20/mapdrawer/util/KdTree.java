package bfst20.mapdrawer.util;

import bfst20.mapdrawer.osm.OSMNode;
import edu.princeton.cs.algs4.BST;

import java.util.ArrayList;
import java.util.Comparator;

public class KdTree {

    // Keys are coordinates (even depth = x-coordinate, odd depth = y-coordinate) and values are the corresponding node id
    // Tree contains the coordinate for each splitting line that occurs on the canvas
    private BST<Float, Long> tree;
    private ArrayList<OSMNode> nodesX; // Nodes sorted by x-coordinate
    private ArrayList<OSMNode> nodesY; // Nodes sorted by y-coordinate

    public KdTree(ArrayList<OSMNode> nodes) throws EmptyKdTreeException {
        tree = new BST<>();
        nodesX = nodes;
        nodesY = nodes;

        nodesX.sort(Comparator.comparing(OSMNode::getLat)); // Sorting method by latitude...
        nodesY.sort(Comparator.comparing(OSMNode::getLon)); // ... then by longitude

        buildKdTree(nodesX, nodesY, 0); // Depth is only used for the recursive calls, so always 0 at start
    }

    public void buildKdTree(ArrayList<OSMNode> nodesX, ArrayList<OSMNode> nodesY, int depth) throws EmptyKdTreeException {

        ArrayList<OSMNode> leftTree = new ArrayList<>();
        ArrayList<OSMNode> rightTree = new ArrayList<>();

        if (nodesX.size() == 0) { // nodesX and nodesY are equally long, so could check either of them here
            throw new EmptyKdTreeException();
        } else if (nodesX.size() == 1) { // TODO which node?
            tree.put(nodesX.get(0).getLat(), nodesX.get(0).getAsLong()); // Get the first node's x-coordinate (latitute) and id, and add it to the tree
        } else if (depth % 2 == 0) {
            //tree.put(pointsX.get(pointsX.size() / 2), pointsY.get(pointsY.size() / 2));
            tree.put(nodesX.get(nodesX.size() / 2).getLat(), nodesX.get(nodesX.size() / 2).getAsLong()); // Get the median node from nodesX and add it to the tree

            for (int i = 0; i < nodesX.size() / 2; i++) {
                leftTree.add(nodesX.get(i)); // Add every node from the first half of the nodesX list to a new list called leftTree
            }

            for (int i = nodesX.size() / 2; i < nodesX.size(); i++) {
                rightTree.add(nodesX.get(i)); // Add every node from the second half of the nodesX list to a new list called rightTree
            }

            buildKdTree(leftTree, nodesY, depth + 1); // Do everything again with the new, halved list of nodes
            buildKdTree(rightTree, nodesY, depth + 1); // ... For both the left and right side of the splitting line
        } else {
            tree.put(nodesY.get(nodesY.size() / 2).getLon(), nodesY.get(nodesY.size() / 2).getAsLong()); // Get the median node from nodesY and add it to the tree

            for (int i = 0; i < nodesY.size() / 2; i++) {
                leftTree.add(nodesY.get(i)); // Add every node from the first half of the nodesY list to leftTree
            }

            for (int i = nodesY.size() / 2; i < nodesY.size(); i++) {
                rightTree.add(nodesY.get(i)); // Add every node from the second half of the nodesY list to rightTree
            }

            buildKdTree(nodesX, leftTree, depth + 1); // TODO is up/down either right or left?
            buildKdTree(nodesX, rightTree, depth + 1);
        }
    }

    public BST<Float, Long> getTree() {
        return tree;
    }

    public static final class EmptyKdTreeException extends Exception {}
}

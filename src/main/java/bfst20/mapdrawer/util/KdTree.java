package bfst20.mapdrawer.util;

import edu.princeton.cs.algs4.BST;

import java.util.ArrayList;

public class KdTree {

    private ArrayList<Float> pointsX;
    private ArrayList<Float> pointsY;
    private BST<Float, Float> tree;

    private int depth;
    private Float root;

    public void buildKdTree(ArrayList<Float> pointsX, ArrayList<Float> pointsY, int depth) throws EmptyKdTreeException {
        this.pointsX = pointsX;
        this.pointsY = pointsY;
        this.depth = depth;

        ArrayList<Float> leftTree = new ArrayList<Float>();
        ArrayList<Float> rightTree = new ArrayList<Float>();

        if (pointsX.size() == 1) { // Check if list is 0 also
            root = pointsX.get(0);
        } else if (pointsX.size() == 0) {
            throw new EmptyKdTreeException();
        } else if (depth % 2 == 0) {
            root = pointsX.get(pointsX.size() / 2);
            tree.put(root, pointsY.get(pointsY.size() / 2));

            for (int i = 0; i < (pointsX.size()) / 2; i++) {
                Float right = pointsX.get(i);
                rightTree.add(right);
            }

            for (int i = (pointsX.size()) / 2; i < pointsX.size(); i++) {
                Float left = pointsX.get(i);
                leftTree.add(left);
            }

            buildKdTree(rightTree, pointsY, depth + 1);
            buildKdTree(leftTree, pointsY, depth + 1);
        } else {
            root = pointsY.get(pointsY.size() / 2);
            tree.put(root, pointsX.get(pointsX.size() / 2));

            for (int i = 0; i < (pointsY.size()) / 2; i++) {
                Float right = pointsY.get(i);
                rightTree.add(right);
            }

            for (int i = (pointsY.size()) / 2; i < pointsY.size(); i++) {
                Float left = pointsY.get(i);
                leftTree.add(left);
            }

            buildKdTree(pointsX, rightTree, depth + 1);
            buildKdTree(pointsX, leftTree, depth + 1);
        }
    }

    public Float getRoot() {
        return root;
    }

    public static final class EmptyKdTreeException extends Exception {}
}

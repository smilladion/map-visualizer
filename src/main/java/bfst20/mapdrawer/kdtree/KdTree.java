package bfst20.mapdrawer.kdtree;

import bfst20.mapdrawer.osm.OSMWay;

import java.util.Comparator;
import java.util.List;

public class KdTree {

    private final KdNode root;

    public KdTree(List<OSMWay> nodes) {
        root = build(nodes.subList(0, 10), 0);

        draw(root, 0, 2);
        System.out.flush();
    }

    private static KdNode build(List<OSMWay> nodes, int depth) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        if (nodes.size() == 1) {
            return new KdNode(nodes.get(0));
        } else if (depth % 2 == 0) {
            nodes.sort(Comparator.comparing(OSMWay::getAvgX));
        } else {
            nodes.sort(Comparator.comparing(OSMWay::getAvgY));
        }

        List<OSMWay> left = nodes.subList(0, nodes.size() / 2);
        List<OSMWay> right = nodes.subList(nodes.size() / 2, nodes.size());

        KdNode vLeft = build(left.subList(0, left.size() - 1), depth + 1);
        KdNode vRight = build(right, depth + 1);

        OSMWay median = left.get(left.size() - 1);

        return new KdNode(median, vLeft, vRight);
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

        System.out.print(node);
        System.out.print("\n");

        draw(node.left, indentation + 1, 0);
        draw(node.right, indentation + 1, 1);
    }

    private void drawIndent(int indentation) {
        for (int i = 0; i < indentation; i++) {
            System.out.print("-");
        }
    }

    private static class KdPoint {

        private final float x;
        private final float y;

        private KdPoint(OSMWay way) {
            this.x = way.getAvgX();
            this.y = way.getAvgY();
        }

        @Override
        public String toString() {
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

        private KdNode(OSMWay way, KdNode left, KdNode right) {
            this.way = way;
            this.point = new KdPoint(way);

            this.left = left;
            this.right = right;
        }

        private KdNode(OSMWay way) {
            this.way = way;
            this.point = new KdPoint(way);

            // This is a leaf node with no children
            this.left = null;
            this.right = null;
        }

        @Override
        public String toString() {
            return "KdNode{" +
                "point=" + point +
                '}';
        }
    }
}

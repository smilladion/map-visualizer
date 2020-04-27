package bfst20.mapdrawer.map;

import javafx.geometry.Point2D;
import org.junit.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MapViewTest {

    @Test
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int N = sc.nextInt();

        System.out.println("N: " + N);

        for (int i = 0; i < N; i++) {

            double x1 = sc.nextDouble();
            double y1 = sc.nextDouble();
            double x2 = sc.nextDouble();
            double y2 = sc.nextDouble();
            double x22 = sc.nextDouble();
            double y22 = sc.nextDouble();

            Point2D vec1 = new Point2D((x2 - x1),(y2 - y1));
            Point2D vec2 = new Point2D((x22 - x2), (y22-y2));

            calculateAngle1(vec1, vec2);

            System.out.println(ccw(new Point2D(x1, y1), new Point2D(x2, y2), new Point2D(x22, y22)));

            /*double x1 = sc.nextDouble();
            double y1 = sc.nextDouble();
            double x2 = sc.nextDouble();
            double y2 = sc.nextDouble();

            double x22 = sc.nextDouble();
            double y22 = sc.nextDouble();

            Point2D vec1 = new Point2D(x2 - x1, y2 - y1);
            Point2D vec2 = new Point2D(x22 - x2, y22 - y2);

            double angleFrom = Math.atan2(vec1.getX(), vec1.getY());
            double angleTo = Math.atan2(vec2.getX(), vec2.getY());
            double angle = angleTo - angleFrom;

            if (angle > Math.PI) {
                angle = -(angle - Math.PI);
            } else if (angle < -Math.PI) {
                angle = -(angle + Math.PI);
            }

            angle *= 180 / Math.PI;

            System.out.println(angle);*/
        }

    }

    public static int ccw(Point2D a, Point2D b, Point2D c) {
        double area2 = (b.getX()-a.getX())*(c.getY()-a.getY()) - (b.getY()-a.getY())*(c.getX()-a.getX());
        if      (area2 < 0) return -1;
        else if (area2 > 0) return +1;
        else                return  0;
    }


    public static double calculateAngle1(Point2D vectorFrom, Point2D vectorTo) {

        double dot = vectorFrom.dotProduct(vectorTo);
        double lengthFrom = (Math.sqrt(((vectorFrom.getX())*(vectorFrom.getX()))+((vectorFrom.getY())*(vectorFrom.getY()))));
        double lengthTo = (Math.sqrt(((vectorTo.getX())*(vectorTo.getX()))+((vectorTo.getY())*(vectorTo.getY()))));

        double cosv = (dot / (lengthFrom * lengthTo));

        double angle = Math.acos(cosv);
        System.out.println("angle: " + angle);
        double angle1 = Math.toDegrees(angle);
        System.out.println(angle1);

        double realAngle = 180 - angle1;
        System.out.println("real: " + realAngle);

        return realAngle;
    }
}
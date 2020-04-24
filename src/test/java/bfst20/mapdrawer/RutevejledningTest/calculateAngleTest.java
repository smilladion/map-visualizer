package java.bfst20.mapdrawer.RutevejledningTest;

import bfst20.mapdrawer.drawing.Point;
import javafx.geometry.Point2D;

import java.util.Scanner;

public class calculateAngleTest {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        double x1 = sc.nextDouble();
        double y1 = sc.nextDouble();
        double x2 = sc.nextDouble();
        double y2 = sc.nextDouble();
        double x22 = sc.nextDouble();
        double y22 = sc.nextDouble();

        Point2D vec1 = new Point2D((x2 - x1),(y2 - y1));
        Point2D vec2 = new Point2D((x22 - x2), (y22-y2));

        calculateAngle1(vec1, vec2);

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

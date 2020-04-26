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

            System.out.println(angle);
        }

    }

    public double calculateAngle(Point2D p1, Point2D p2) {
        double dotProduct = (p1.getX() * p2.getX()) + (p1.getY() * p2.getY());
        double length = ((Math.sqrt((p1.getX()*p1.getX()) + (p1.getY()*p1.getY()))) * (Math.sqrt((p2.getX()*p2.getX())) + (p2.getY()*p2.getY())));

        double cosv = dotProduct / length;

        double angle = Math.acos(cosv);

        System.out.println(angle);

        return angle;
    }
}
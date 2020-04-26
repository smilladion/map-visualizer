package java.bfst20.mapdrawer.RutevejledningTest;

import java.util.Scanner;

public class calculateAngleTest {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        double x1 = sc.nextDouble();
        double y1 = sc.nextDouble();
        double x2 = sc.nextDouble();
        double y2 = sc.nextDouble();

        double dotProduct = (x1 * x2) + (y1 * y2);
        double length = ((Math.sqrt((x1*x1) + (y1*y1))) * (Math.sqrt((x2*x2)) + (y2*y2)));

        double cosv = dotProduct / length;

        double angle = Math.acos(cosv);

        System.out.println(angle);

    }

    public void calculateAngle(double x1, double x2, double y1, double y2) {

    }
}

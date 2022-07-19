package shortestpath.utils;

import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    public static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while (true) {
            int read = in.read(buffer, 0, buffer.length);

            if (read == -1) {
                return result.toByteArray();
            }

            result.write(buffer, 0, read);
        }
    }

    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {
        }
    }

    public static String worldPointToString(final WorldPoint point) {
        return String.format("WorldPoint(%d, %d, %d)", point.getX(), point.getY(), point.getPlane());
    }

    public static boolean isPointInsideRectangle(final WorldPoint rectangleCorner,
                                                 final WorldPoint rectangleOppositeCorner,
                                                 final WorldPoint point) {
        if (rectangleCorner.getPlane() != rectangleOppositeCorner.getPlane()) {
            throw new RuntimeException("Rectangle corners not in same plane - " + rectangleCorner + ", " + rectangleOppositeCorner);
        }

        final WorldPoint upperLeftRectangleCorner = new WorldPoint(Math.min(rectangleCorner.getX(), rectangleOppositeCorner.getX()),
                Math.max(rectangleCorner.getY(), rectangleOppositeCorner.getY()),
                rectangleCorner.getPlane());

        final WorldPoint lowerRightRectangleCorner = new WorldPoint(Math.max(rectangleCorner.getX(), rectangleOppositeCorner.getX()),
                Math.min(rectangleCorner.getY(), rectangleOppositeCorner.getY()),
                rectangleCorner.getPlane());

        return (point.getX() >= upperLeftRectangleCorner.getX() && point.getX() <= lowerRightRectangleCorner.getX()) &&
                (point.getY() >= lowerRightRectangleCorner.getY() && point.getY() <= upperLeftRectangleCorner.getY()) &&
                point.getPlane() == upperLeftRectangleCorner.getPlane();
    }
}

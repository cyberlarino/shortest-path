package shortestpath.utils;

import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.pathfindertask.PathfinderTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {
    public static byte[] readAllBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        final byte[] buffer = new byte[4096];

        for (int numberOfBytesRead; (numberOfBytesRead = in.read(buffer, 0, buffer.length)) != -1;) {
            result.write(buffer, 0, numberOfBytesRead);
        }
        result.flush();
        return result.toByteArray();
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
}

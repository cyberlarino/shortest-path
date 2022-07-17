package shortestpath;

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
}

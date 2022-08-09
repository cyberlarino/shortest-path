package shortestpath.utils;

import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.lang3.StringUtils;
import shortestpath.pathfinder.pathfindertask.PathfinderTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static String pathToResourcePath(final Path path) {
        final Path resourceDirectoryPath = Paths.get("src/main/resources");
        if (!path.startsWith(resourceDirectoryPath)) {
            throw new RuntimeException("Invalid resource path, doesn't lead to 'resources' directory.");
        }
        return StringUtils.removeStart(path.toString(), resourceDirectoryPath.toString()).replace('\\', '/');
    }

    public static String worldPointToString(final WorldPoint point) {
        return String.format("WorldPoint(%d, %d, %d)", point.getX(), point.getY(), point.getPlane());
    }
}

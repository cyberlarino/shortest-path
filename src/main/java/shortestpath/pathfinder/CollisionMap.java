package shortestpath.pathfinder;

import net.runelite.api.coords.WorldPoint;
import shortestpath.ShortestPathPlugin;
import shortestpath.Util;

import javax.print.attribute.standard.OrientationRequested;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CollisionMap extends SplitFlagMap {
    public CollisionMap(int regionSize, Map<Position, byte[]> compressedRegions) {
        super(regionSize, compressedRegions, 2);
    }

    public boolean checkDirection(final WorldPoint point, final OrdinalDirection dir) {
        return checkDirection(point.getX(), point.getY(), point.getPlane(), dir);
    }

    public boolean checkDirection(int x, int y, final int z, final OrdinalDirection dir) {
        Point direction = dir.toPoint();
        if (Math.abs(direction.x) + Math.abs(direction.y) > 1) {
            // Diagonal cases
            final boolean horizontalPossible = checkDirection(x, y, z, OrdinalDirection.fromPoint(new Point(direction.x, 0)));
            final boolean verticalPossible = checkDirection(x, y, z, OrdinalDirection.fromPoint(new Point(0, direction.y)));
            if (!horizontalPossible && !verticalPossible) {
                return false;
            }
        }

        int flag = 0;
        if (dir == OrdinalDirection.EAST || dir == OrdinalDirection.WEST) {
            flag = 1;
        }

        if (dir == OrdinalDirection.WEST) {
            x -= 1;
        }
        else if (dir == OrdinalDirection.SOUTH) {
            y -= 1;
        }
        return get(x, y, z, flag);
    }

    public boolean isBlocked(final WorldPoint point) {
        return isBlocked(point.getX(), point.getY(), point.getPlane());
    }

    public boolean isBlocked(final int x, final int y, final int z) {
        return Stream.of(OrdinalDirection.values()).noneMatch(dir -> checkDirection(x, y, z, dir));
    }

    public static CollisionMap fromFile(final String filepath) {
        Map<SplitFlagMap.Position, byte[]> compressedRegions = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(filepath)) {
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream in = new ZipInputStream(bis);

            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String[] n = entry.getName().split("_");

                compressedRegions.put(
                        new SplitFlagMap.Position(Integer.parseInt(n[0]), Integer.parseInt(n[1])),
                        Util.readAllBytes(in)
                );
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new CollisionMap(64, compressedRegions);
    }
}

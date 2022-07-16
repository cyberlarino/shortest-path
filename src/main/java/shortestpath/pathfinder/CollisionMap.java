package shortestpath.pathfinder;

import net.runelite.api.coords.WorldPoint;
import shortestpath.ShortestPathPlugin;
import shortestpath.Util;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CollisionMap extends SplitFlagMap {
    public CollisionMap(int regionSize, Map<Position, byte[]> compressedRegions) {
        super(regionSize, compressedRegions, 2);
    }

    public boolean check(final WorldPoint point) {
        return check(point.getX(), point.getY(), point.getPlane());
    }

    public boolean check(final int x, final int y, final int z) {
        return get(x, y, z, 0);
    }

    public boolean checkDirection(final WorldPoint point, final OrdinalDirection dir) {
        return checkDirection(point.getX(), point.getY(), point.getPlane(), dir);
    }

    public boolean checkDirection(final int x, final int y, final int z, final OrdinalDirection dir) {
        Point direction = dir.toPoint();
        if (Math.abs(direction.x) + Math.abs(direction.y) > 1) {
            // Diagonal cases
            final boolean horizontalPossible = check(x + direction.x, y, z);
            final boolean verticalPossible = check(x, y + direction.y, z);
            if (!horizontalPossible && !verticalPossible) {
                return false;
            }
        }
        return check(x + direction.x, y + direction.y, z);
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

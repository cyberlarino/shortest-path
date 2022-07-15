package shortestpath.pathfinder;

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

    public boolean check(int x, int y, int z) {
        return get(x, y, z, 0);
    }

    public boolean checkDirection(int x, int y, int z, OrdinalDirection dir) {
        Point direction = dir.toPoint();
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

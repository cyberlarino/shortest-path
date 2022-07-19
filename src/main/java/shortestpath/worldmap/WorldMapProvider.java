package shortestpath.worldmap;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.WorldListLoad;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class WorldMapProvider {
    private static final Path DEFAULT_COLLISION_MAP_PATH = Paths.get("src/main/resources/collision-map.zip");
    private static final Path DEFAULT_TRANSPORTS_PATH = Paths.get("src/main/resources/transports.txt");

    @Getter
    final private WorldMap worldMap;

    public WorldMapProvider() {
        this(DEFAULT_COLLISION_MAP_PATH, DEFAULT_TRANSPORTS_PATH);
    }

    public WorldMapProvider(final Path collisionMapPath, final Path transportsPath) {
        final CollisionMap map = CollisionMap.fromFile(collisionMapPath);
        final Map<WorldPoint, List<Transport>> transports = Transport.fromFile(transportsPath);
        this.worldMap = new WorldMap(map, transports);
    }

    public CollisionMap getCollisionMap() {
        return worldMap.getCollisionMap();
    }

    public Map<WorldPoint, List<Transport>> getTransports() {
        return worldMap.getTransports();
    }
}

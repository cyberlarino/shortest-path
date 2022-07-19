package shortestpath.worldmap;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.List;
import java.util.Map;

public class WorldMap {
    @Getter
    private final CollisionMap collisionMap;
    @Getter
    private final Map<WorldPoint, List<Transport>> transports;

    WorldMap(final CollisionMap map, final Map<WorldPoint, List<Transport>> transports) {
        this.collisionMap = map;
        this.transports = transports;
    }
}

package shortestpath.pathfinder;

import net.runelite.api.coords.WorldPoint;
import shortestpath.Transport;

import java.util.List;
import java.util.Map;

public class PathfinderConfig {
    public CollisionMap map;
    public Map<WorldPoint, List<Transport>> transports;
    public boolean avoidWilderness = true;
    public boolean useAgilityShortcuts = false;
    public boolean useGrappleShortcuts = false;
    public int agilityLevel = 1;
    public int rangedLevel = 1;
    public int strengthLevel = 1;

    public PathfinderConfig(CollisionMap map) {
        this.map = map;
        this.transports = null;
    }

    public PathfinderConfig(CollisionMap map, Map<WorldPoint, List<Transport>> transports) {
        this.map = map;
        this.transports = transports;
    }
}

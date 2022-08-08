package shortestpath.worldmap;

import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.Node;
import shortestpath.utils.OrdinalDirection;
import shortestpath.pathfinder.path.Transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class WorldMap {
    private final CollisionMap collisionMap;
    private final Map<WorldPoint, List<Transport>> transports;
    private final List<Transport> allTransports;

    WorldMap(final CollisionMap map, final Map<WorldPoint, List<Transport>> transports) {
        this.collisionMap = map;
        this.transports = transports;

        this.allTransports = new ArrayList<>();
        for (List<Transport> list : transports.values()) {
            allTransports.addAll(list);
        }
    }

    public List<Transport> getTransports() {
        return allTransports;
    }

    public List<Transport> getTransports(final WorldPoint point) {
        return transports.getOrDefault(point, new ArrayList<>());
    }

    public void addTransport(final Transport transport) {
        if (allTransports.contains(transport)) {
            return;
        }

        allTransports.add(transport);
        transports.computeIfAbsent(transport.getOrigin(), k -> new ArrayList<>()).add(transport);
    }

    public boolean isBlocked(final WorldPoint point) {
        return collisionMap.isBlocked(point);
    }

    public boolean checkDirection(final WorldPoint point, final OrdinalDirection dir) {
        return collisionMap.checkDirection(point, dir);
    }

    public List<Node> getNeighborNodes(final Node node, final Predicate<Node> nodePredicate) {

        return new ArrayList<>();
    }
}

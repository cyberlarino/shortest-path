package shortestpath.pathfinder;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.Transport;

import java.util.*;
import java.util.function.Predicate;

public class NodeGraph {
    private final CollisionMap map;
    private final Map<WorldPoint, List<Transport>> transports;
    @Getter
    private List<Node> boundary = new LinkedList<>();
    @Getter
    private Set<WorldPoint> visited = new HashSet<>();

    public NodeGraph(final CollisionMap map, final Map<WorldPoint, List<Transport>> transports) {
        this.map = map;
        this.transports = transports;
    }

    public void addBoundaryNode(final Node node) {
        boundary.add(node);
    }

    public void evaluateBoundaryNode(final int index) {
        evaluateBoundaryNode(index, x -> true);
    }

    public void evaluateBoundaryNode(final int index, final Predicate<WorldPoint> predicate) {
        final Node node = boundary.remove(index);
        addNeighbors(node, predicate);
    }

    private void addNeighbor(final Node node, final WorldPoint neighbor, final Predicate<WorldPoint> predicate) {
        if (!predicate.test(neighbor)) {
            return;
        }
        if (!visited.add(neighbor)) {
            return;
        }
        boundary.add(new Node(neighbor, node));
    }

    private void addNeighbors(final Node node, final Predicate<WorldPoint> predicate) {
        for (OrdinalDirection direction : OrdinalDirection.values()) {
            if (map.checkDirection(node.position.getX(), node.position.getY(), node.position.getPlane(), direction)) {
                addNeighbor(node, new WorldPoint(node.position.getX() + direction.toPoint().x, node.position.getY() + direction.toPoint().y, node.position.getPlane()), predicate);
            }
        }

        for (Transport transport : transports.getOrDefault(node.position, new ArrayList<>())) {
            addNeighbor(node, transport.getDestination(), predicate);
        }
    }
}

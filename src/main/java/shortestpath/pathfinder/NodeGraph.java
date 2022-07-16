package shortestpath.pathfinder;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.Transport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        evaluateBoundaryNode(index, x -> true, x -> true);
    }

    public void evaluateBoundaryNode(final int index, final Predicate<WorldPoint> neighborPredicate, final Predicate<Transport> transportPredicate) {
        final Node node = boundary.remove(index);
        addNeighbors(node, neighborPredicate, transportPredicate);
    }

    private void addNeighbor(final Node node, final WorldPoint neighbor) {
        if (!visited.add(neighbor)) {
            return;
        }
        boundary.add(new Node(neighbor, node));
    }

    private void addNeighbors(final Node node, final Predicate<WorldPoint> neighborPredicate, final Predicate<Transport> transportPredicate) {
        for (OrdinalDirection direction : OrdinalDirection.values()) {
            if (map.checkDirection(node.getPosition(), direction)) {
                final WorldPoint neighbor = new WorldPoint(node.getPosition().getX() + direction.toPoint().x, node.getPosition().getY() + direction.toPoint().y, node.getPosition().getPlane());
                if (neighborPredicate.test(neighbor)) {
                    addNeighbor(node, neighbor);
                }
            }
        }

        for (Transport transport : transports.getOrDefault(node.getPosition(), new ArrayList<>())) {
            if (transportPredicate.test(transport)) {
                addNeighbor(node, transport.getDestination());
            }
        }
    }
}

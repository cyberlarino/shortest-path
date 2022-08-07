package shortestpath.pathfinder;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.path.Walk;
import shortestpath.worldmap.WorldMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class NodeGraph {
    private static final List<OrdinalDirection> directionPriority = Arrays.asList(
            OrdinalDirection.NORTH,
            OrdinalDirection.EAST,
            OrdinalDirection.SOUTH,
            OrdinalDirection.WEST,
            OrdinalDirection.NORTH_EAST,
            OrdinalDirection.SOUTH_EAST,
            OrdinalDirection.SOUTH_WEST,
            OrdinalDirection.NORTH_WEST);

    private final WorldMap worldMap;
    @Getter
    private List<Node> boundary = new LinkedList<>();
    @Getter
    private Set<WorldPoint> visited = new HashSet<>();

    public NodeGraph(final WorldMap worldMap) {
        this.worldMap = worldMap;
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

    private void addNeighbor(final Node node, final Movement neighborMovement) {
        if (!visited.add(neighborMovement.getDestination())) {
            return;
        }
        boundary.add(new Node(neighborMovement, node));
    }

    private void addNeighbors(final Node node, final Predicate<WorldPoint> neighborPredicate, final Predicate<Transport> transportPredicate) {
        final WorldPoint currentPoint = node.getMovement().getDestination();
        for (OrdinalDirection direction : directionPriority) {
            if (worldMap.checkDirection(currentPoint, direction)) {
                final WorldPoint neighbor = new WorldPoint(currentPoint.getX() + direction.toPoint().getX(), currentPoint.getY() + direction.toPoint().getY(), currentPoint.getPlane());
                final Walk walkMovement = new Walk(currentPoint, neighbor);
                if (neighborPredicate.test(neighbor)) {
                    addNeighbor(node, walkMovement);
                }
            }
        }

        for (Transport transport : worldMap.getTransports(currentPoint)) {
            if (transportPredicate.test(transport)) {
                addNeighbor(node, transport);
            }
        }
    }
}

package shortestpath.pathfinder;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Walk;

import java.util.LinkedList;
import java.util.List;

public class Node {
    @Getter
    private final Movement movement;
    @Getter
    private final Node previous;

    public Node(final Movement movement, final Node previous) {
        this.movement = movement;
        this.previous = previous;
    }

    public static Node createInitialNode(final WorldPoint point) {
        return new Node(new Walk(point, point), null);
    }

    public Path getPath() {
        List<Movement> movements = new LinkedList<>();

        Node nodeIterator = this;
        while (nodeIterator != null) {
            movements.add(0, nodeIterator.getMovement());
            nodeIterator = nodeIterator.previous;
        }

        return new Path(movements);
    }
}

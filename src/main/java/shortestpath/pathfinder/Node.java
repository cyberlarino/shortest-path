package shortestpath.pathfinder;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.LinkedList;

public class Node {
    @Getter
    private final WorldPoint position;
    @Getter
    private final Node previous;

    public Node(final WorldPoint position, final Node previous) {
        this.position = position;
        this.previous = previous;
    }

    public Path getPath() {
        Path path = new Path(new LinkedList<>());

        Node nodeIterator = this;
        while (nodeIterator != null) {
            path.getPoints().add(0, nodeIterator.position);
            nodeIterator = nodeIterator.previous;
        }

        return path;
    }
}

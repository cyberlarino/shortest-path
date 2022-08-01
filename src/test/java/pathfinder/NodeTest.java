package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import shortestpath.pathfinder.Node;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Walk;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NodeTest {
    @Test
    public void testNodeGetPath() {
        final List<WorldPoint> points = new ArrayList<WorldPoint>() {{
            add(new WorldPoint(0, 0, 0));
            add(new WorldPoint(1, 0, 0));
            add(new WorldPoint(2, 0, 0));
            add(new WorldPoint(3, 0, 0));
        }};

        final Node node0 = Node.createInitialNode(points.get(0));
        final Node node1 = new Node(new Walk(points.get(0), points.get(1)), node0);
        final Node node2 = new Node(new Walk(points.get(1), points.get(2)), node1);
        final Node node3 = new Node(new Walk(points.get(2), points.get(3)), node2);

        final Path path = node3.getPath();
        assertEquals(path.getMovements().get(0), node0.getMovement());
        assertEquals(path.getMovements().get(points.size() - 1), node3.getMovement());
        assertEquals(path.getMovements().size(), points.size());
    }
}

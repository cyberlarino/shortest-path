package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Test;
import shortestpath.pathfinder.Node;
import shortestpath.pathfinder.Path;

import java.util.ArrayList;
import java.util.List;

public class NodeTest {
    @Test
    public void testNodeGetPath() {
        final List<WorldPoint> points = new ArrayList<WorldPoint>() {{
            add(new WorldPoint(0, 0, 0));
            add(new WorldPoint(1, 0, 0));
            add(new WorldPoint(2, 0, 0));
            add(new WorldPoint(3, 0, 0));
        }};

        final Node node0 = new Node(points.get(0), null);
        final Node node1 = new Node(points.get(1), node0);
        final Node node2 = new Node(points.get(2), node1);
        final Node node3 = new Node(points.get(3), node2);

        final Path path = node3.getPath();
        Assert.assertEquals(path.getPoints().get(0), node0.getPosition());
        Assert.assertEquals(path.getPoints().get(points.size() - 1), node3.getPosition());
        Assert.assertEquals(path.getPoints().size(), points.size());
    }
}

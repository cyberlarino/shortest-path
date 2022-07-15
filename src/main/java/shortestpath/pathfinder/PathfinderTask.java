package shortestpath.pathfinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.sun.org.apache.xpath.internal.axes.OneStepIterator;
import lombok.Getter;
import net.runelite.api.World;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import shortestpath.Path;
import shortestpath.Transport;

public class PathfinderTask implements Runnable {
    private static final WorldArea WILDERNESS_ABOVE_GROUND = new WorldArea(2944, 3523, 448, 448, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND = new WorldArea(2944, 9918, 320, 442, 0);

    @Getter
    private final WorldPoint start;
    @Getter
    private final WorldPoint target;
    private final PathfinderConfig config;
    private final Predicate<WorldPoint> neighborPredicate;

    @Getter
    private Path path;
    private boolean done = false;

    public PathfinderTask(PathfinderConfig config, WorldPoint start, WorldPoint target) {
        this.config = config;
        this.start = start;
        this.target = target;

        final boolean isStartOrTargetInWilderness = isInWilderness(start) || isInWilderness(target);
        this.neighborPredicate = (point) -> {
                if (config.avoidWilderness && !isStartOrTargetInWilderness && isInWilderness(point)) {
                    return false;
                }
                return true;
        };

        new Thread(this).start();
    }

    public static class PathfinderConfig {
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

    private static boolean isInWilderness(WorldPoint p) {
        return WILDERNESS_ABOVE_GROUND.distanceTo(p) == 0 || WILDERNESS_UNDERGROUND.distanceTo(p) == 0;
    }

    public boolean isDone() {
        return this.done;
    }

    public WorldPoint getStart() {
        return this.start;
    }

    public WorldPoint getTarget() {
        return this.target;
    }

    public Path getPath() {
        return this.path;
    }

    @Override
    public void run() {
        NodeGraph graph = new NodeGraph(config.map, config.transports);
        graph.addBoundaryNode(new Node(start, null));

        int bestDistance = Integer.MAX_VALUE;
        while (!graph.getBoundary().isEmpty()) {
            final int indexToEvaluate = 0;
            final Node node = graph.getBoundary().get(indexToEvaluate);

            if (node.position.equals(target)) {
                this.path = node.getPath();
                break;
            }

            int distance = node.position.distanceTo(target);
            if (this.path == null || distance < bestDistance) {
                this.path = node.getPath();
                bestDistance = distance;
            }

            graph.evaluateBoundaryNode(indexToEvaluate, this.neighborPredicate);
        }

        this.done = true;
    }
}

package shortestpath.pathfinder.pathfindertask;

import java.util.function.Predicate;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.Node;
import shortestpath.pathfinder.NodeGraph;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMap;

@Slf4j
public class SimplePathfinderTask implements PathfinderTask {
    private static final WorldArea WILDERNESS_ABOVE_GROUND = new WorldArea(2944, 3523, 448, 448, 0);
    private static final WorldArea WILDERNESS_UNDERGROUND = new WorldArea(2944, 9918, 320, 442, 0);

    private static boolean isInWilderness(WorldPoint p) {
        return WILDERNESS_ABOVE_GROUND.distanceTo(p) == 0 || WILDERNESS_UNDERGROUND.distanceTo(p) == 0;
    }

    @Getter
    private final WorldPoint start;
    @Getter
    private final WorldPoint target;
    @Getter
    private Path path;
    @Getter
    private PathfinderTaskStatus status = PathfinderTaskStatus.CALCULATING;
    private final long visitedNodes = 0;

    private final WorldMap worldMap;
    private final Predicate<WorldPoint> neighborPredicate;
    private final Predicate<Transport> transportPredicate;
    private boolean shouldCancelTask = false;

    SimplePathfinderTask(final WorldMap worldMap, final PathfinderConfig config, final WorldPoint start, final WorldPoint target, final Predicate<Transport> transportPredicate) {
        this.worldMap = worldMap;
        this.start = start;
        this.target = target;

        final boolean isStartOrTargetInWilderness = isInWilderness(start) || isInWilderness(target);
        this.neighborPredicate = (point) -> {
            if (config.avoidWilderness && !isStartOrTargetInWilderness && isInWilderness(point)) {
                return false;
            }
            return true;
        };
        this.transportPredicate = transportPredicate;

        new Thread(this).start();
    }

    public SimplePathfinderTask(final WorldMap worldMap, final PathfinderConfig config, final WorldPoint start, final WorldPoint target) {
        this(worldMap, config, start, target, config.getCanPlayerUseTransportPredicate());
    }

    public void cancelTask() {
        shouldCancelTask = true;
    }

    @Override
    public void run() {
        NodeGraph graph = new NodeGraph(worldMap);
        graph.addBoundaryNode(Node.createInitialNode(start));

        int bestDistance = Integer.MAX_VALUE;
        while (!graph.getBoundary().isEmpty() && !shouldCancelTask) {
            final int indexToEvaluate = 0;
            final Node node = graph.getBoundary().get(indexToEvaluate);

            if (node.getMovement().getDestination().equals(target)) {
                this.path = node.getPath();
                break;
            }

            final int distance = node.getMovement().getDestination().distanceTo(target);
            if (this.path == null || distance < bestDistance) {
                this.path = node.getPath();
                bestDistance = distance;
            }

            graph.evaluateBoundaryNode(indexToEvaluate, this.neighborPredicate, this.transportPredicate);
        }

        if (shouldCancelTask) {
            status = PathfinderTaskStatus.CANCELLED;
        }
        else {
            status = PathfinderTaskStatus.DONE;
        }
    }
}

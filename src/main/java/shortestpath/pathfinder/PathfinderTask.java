package shortestpath.pathfinder;

import java.util.function.Predicate;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.path.Walk;
import shortestpath.worldmap.WorldMap;

@Slf4j
public class PathfinderTask implements Runnable {
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
    private boolean isDone = false;
    @Getter
    private final long visitedNodes = 0;

    private final WorldMap worldMap;
    private final Predicate<WorldPoint> neighborPredicate;
    private final Predicate<Transport> transportPredicate;
    private boolean shouldAbortTask = false;

    public PathfinderTask(final WorldMap worldMap, final PathfinderConfig config, final WorldPoint start, final WorldPoint target) {
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
        this.transportPredicate = config.getCanPlayerUseTransportPredicate();

        new Thread(this).start();
    }

    public void abortTask() {
        shouldAbortTask = true;
    }

    @Override
    public void run() {
        NodeGraph graph = new NodeGraph(worldMap);
        graph.addBoundaryNode(Node.createInitialNode(start));

        int bestDistance = Integer.MAX_VALUE;
        while (!graph.getBoundary().isEmpty() && !shouldAbortTask) {
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

        final String taskStatusInfo = (shouldAbortTask ? "aborted" : "finished calculating");
        log.debug("PathfinderTask done (" + taskStatusInfo + "); " + graph.getVisited().size() + " visited nodes, "
                + graph.getBoundary().size() + " boundary nodes");

        this.isDone = true;
    }
}

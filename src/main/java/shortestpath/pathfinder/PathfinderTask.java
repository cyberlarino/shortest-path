package shortestpath.pathfinder;

import java.util.function.Predicate;

import lombok.Getter;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import shortestpath.worldmap.Transport;
import shortestpath.utils.Util;
import shortestpath.worldmap.WorldMap;

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

    private final WorldMap worldMap;
    private final Predicate<WorldPoint> neighborPredicate;
    private final Predicate<Transport> transportPredicate;

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

    @Override
    public void run() {
        NodeGraph graph = new NodeGraph(worldMap);
        graph.addBoundaryNode(new Node(start, null));

        int bestDistance = Integer.MAX_VALUE;
        while (!graph.getBoundary().isEmpty()) {
            final int indexToEvaluate = 0;
            final Node node = graph.getBoundary().get(indexToEvaluate);

            if (node.getPosition().equals(target)) {
                this.path = node.getPath();
                break;
            }

            int distance = node.getPosition().distanceTo(target);
            if (this.path == null || distance < bestDistance) {
                this.path = node.getPath();
                bestDistance = distance;
            }

            graph.evaluateBoundaryNode(indexToEvaluate, this.neighborPredicate, this.transportPredicate);
        }

        this.isDone = true;
    }
}

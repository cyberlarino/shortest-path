package shortestpath.pathfinder;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import shortestpath.ClientInfoProvider;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.pathfindertask.PathfinderTask;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskHandler;
import shortestpath.utils.Util;
import shortestpath.worldmap.WorldMap;
import shortestpath.worldmap.WorldMapProvider;

import java.awt.Point;

@Slf4j
public class PathfinderRequestHandler {
    private final ClientInfoProvider clientInfoProvider;
    private final WorldMapProvider worldMapProvider;
    final PathfinderTaskHandler pathfinderTaskHandler;

    private PathfinderTask activeTask = null;
    private WorldPoint start = null;
    private WorldPoint target = null;
    private boolean isStartExplicitlySet = false;

    public PathfinderRequestHandler(final ClientInfoProvider clientInfoProvider,
                                    final WorldMapProvider worldMapProvider,
                                    final PathfinderTaskHandler pathfinderTaskHandler) {
        this.clientInfoProvider = clientInfoProvider;
        this.worldMapProvider = worldMapProvider;
        this.pathfinderTaskHandler = pathfinderTaskHandler;
    }

    public void setTarget(final WorldPoint target) {
        if (!isStartExplicitlySet) {
            this.start = clientInfoProvider.getPlayerLocation();
        }
        this.target = target;
        updatePath();
    }

    public void setStart(final WorldPoint start) {
        if (target == null) {
            return;
        }
        this.start = start;
        isStartExplicitlySet = true;
        updatePath();
    }

    public void clearPath() {
        this.activeTask = null;
        this.start = null;
        this.target = null;
        this.isStartExplicitlySet = false;
    }

    public WorldPoint getStart() {
        return start;
    }

    public WorldPoint getTarget() {
        return target;
    }

    public Path getActivePath() {
        return (activeTask == null ? null : activeTask.getPath());
    }

    public boolean hasActivePath() {
        return activeTask != null;
    }

    public boolean isActivePathDone() {
        if (activeTask == null) {
            return true;
        }

        return activeTask.getStatus() != PathfinderTaskStatus.CALCULATING;
    }

    private void updatePath() {
        final WorldPoint originalStart = start;
        final WorldPoint originalTarget = target;
        start = findClosestNonBlockedPoint(start);
        target = findClosestNonBlockedPoint(target);
        if (start == null || target == null) {
            final String invalidPointInfo = (start == null ?
                    "start (" + originalStart + ")" : "target (" + originalTarget + ")");
            log.debug("No unblocked point close to " + invalidPointInfo + " found, cancelling path");
            clearPath();
            return;
        }

        activeTask = pathfinderTaskHandler.newTask(start, target);
        log.debug("New PathfinderTask started: " + Util.worldPointToString(start) + " to " + Util.worldPointToString(target));
    }

    private final static int RADIUS_TO_CHECK = 10;
    private WorldPoint findClosestNonBlockedPoint(WorldPoint point) {
        final WorldMap map = worldMapProvider.getWorldMap();

        int cardinalDirectionIterator = 0;
        for (int radius = 1; radius < RADIUS_TO_CHECK; ++cardinalDirectionIterator) {
            final int cardinalDirectionIndex = cardinalDirectionIterator % OrdinalDirection.CARDINAL_DIRECTIONS.size();
            final Point direction = OrdinalDirection.CARDINAL_DIRECTIONS.get(cardinalDirectionIndex).toPoint();

            final int directionTraverseLength = Math.floorDiv(cardinalDirectionIterator, 2);
            point = point.dx(direction.x * directionTraverseLength).dy(direction.y * directionTraverseLength);

            if (!map.isBlocked(point)) {
                return point;
            }

            if (cardinalDirectionIterator % OrdinalDirection.CARDINAL_DIRECTIONS.size() == 0) {
                ++radius;
            }
        }

        return null;
    }
}

package shortestpath.pathfinder;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.ClientInfoProvider;
import shortestpath.pathfinder.PathfinderTaskGenerator;
import shortestpath.worldmap.WorldMapProvider;

public class PathfinderRequestHandler {
    private final ClientInfoProvider clientInfoProvider;
    private final WorldMapProvider worldMapProvider;
    private final PathfinderTaskGenerator pathfinderTaskGenerator;

    private PathfinderTask activeTask = null;
    private WorldPoint start = null;
    private WorldPoint target = null;

    public PathfinderRequestHandler(final ClientInfoProvider clientInfoProvider,
                                    final WorldMapProvider worldMapProvider,
                                    final PathfinderTaskGenerator pathfinderTaskGenerator) {
        this.clientInfoProvider = clientInfoProvider;
        this.worldMapProvider = worldMapProvider;
        this.pathfinderTaskGenerator = pathfinderTaskGenerator;
    }

    public void setTarget(final WorldPoint target) {
        this.start = clientInfoProvider.getPlayerLocation();
        this.target = target;
        updatePath();
    }

    public void clearPath() {
        this.activeTask = null;
        this.start = null;
        this.target = null;
    }

    public void setStart(final WorldPoint start) {
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
        return (activeTask == null ? true : activeTask.isDone());
    }

    private void updatePath() {
        activeTask = pathfinderTaskGenerator.generate(start, target);
    }
}

package shortestpath.pathfinder;

import net.runelite.api.coords.WorldPoint;
import shortestpath.ConfigProvider;
import shortestpath.worldmap.WorldMapProvider;

public class PathfinderTaskGenerator {
    private final ConfigProvider configProvider;
    private final WorldMapProvider worldMapProvider;
    private final PathfinderTaskHandler pathfinderTaskHandler;

    public PathfinderTaskGenerator(final ConfigProvider configProvider,
                                   final WorldMapProvider worldMapProvider,
                                   final PathfinderTaskHandler pathfinderTaskHandler) {
        this.configProvider = configProvider;
        this.worldMapProvider = worldMapProvider;
        this.pathfinderTaskHandler = pathfinderTaskHandler;
    }

    public PathfinderTask generate(final WorldPoint start, final WorldPoint target) {
        final PathfinderTask newTask = new PathfinderTask(worldMapProvider.getWorldMap(), configProvider.getPathFinderConfig(), start, target);
        pathfinderTaskHandler.add(newTask);
        return newTask;
    }
}

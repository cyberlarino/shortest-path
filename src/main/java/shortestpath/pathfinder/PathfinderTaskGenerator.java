package shortestpath.pathfinder;

import net.runelite.api.coords.WorldPoint;
import shortestpath.ConfigProvider;
import shortestpath.worldmap.WorldMapProvider;

public class PathfinderTaskGenerator {
    private final ConfigProvider configProvider;
    private final WorldMapProvider worldMapProvider;

    public PathfinderTaskGenerator(final ConfigProvider configProvider, final WorldMapProvider worldMapProvider) {
        this.configProvider = configProvider;
        this.worldMapProvider = worldMapProvider;
    }

    public PathfinderTask generate(final WorldPoint start, final WorldPoint target) {
        return new PathfinderTask(worldMapProvider.getWorldMap(), configProvider.getPathFinderConfig(), start, target);
    }
}

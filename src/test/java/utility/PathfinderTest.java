package utility;

import net.runelite.api.coords.WorldPoint;
import org.mockito.Mockito;
import shortestpath.ClientInfoProvider;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.pathfinder.PathfinderTaskHandler;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.pathfindertask.ComplexPathfinderTask;
import shortestpath.utils.PathfinderUtil;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionPathfinderTask;
import shortestpath.worldmap.sections.SectionMapper;
import shortestpath.worldmap.sections.SectionRoute;

import java.util.List;

public class PathfinderTest {
    // Plugin
    private static ClientInfoProvider clientInfoProvider;
    private static ConfigProvider configProvider;

    // WorldMap
    private static WorldMapProvider worldMapProvider;
    private static SectionMapper sectionMapper;

    // Pathfinder
    private static PathfinderTaskHandler pathfinderTaskHandler;
    private static PathfinderRequestHandler pathfinderRequestHandler;

    public static void main(String[] args) {
        // Providers
        clientInfoProvider = Mockito.mock(ClientInfoProvider.class);
        configProvider = Mockito.mock(ConfigProvider.class);
        worldMapProvider = new WorldMapProvider();
        sectionMapper = SectionMapper.fromFile(worldMapProvider);
        // Pathfinder
        pathfinderTaskHandler = new PathfinderTaskHandler(configProvider, worldMapProvider, sectionMapper);
        pathfinderRequestHandler = new PathfinderRequestHandler(clientInfoProvider, worldMapProvider, pathfinderTaskHandler);

        final PathfinderConfig pathfinderConfig = new PathfinderConfig();
        pathfinderConfig.agilityLevel = 99;

        // Start timer
        final long startTime = System.nanoTime();

        final int TASKS_TO_CALCULATE = 50;
        final WorldPoint start = new WorldPoint(3161, 3482, 0);
        final WorldPoint target = new WorldPoint(3653, 3353, 0);
        for (int i = 0; i < TASKS_TO_CALCULATE; ++i) {
            final ComplexPathfinderTask task = new ComplexPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, pathfinderConfig, start, target);
            final boolean finishedInTime = PathfinderUtil.waitForTaskCompletion(task, 10);
            if (!finishedInTime) {
                throw new RuntimeException("Path used more than 5 seconds.");
            }
        }

        // Check execution time
        final long endTime = System.nanoTime();
        final long averageTaskCompletionTime = (endTime - startTime) / TASKS_TO_CALCULATE;
        final double elapsedTimeInSeconds = (double) averageTaskCompletionTime / 1_000_000_000.0;
        System.out.println("Average time per task: " + elapsedTimeInSeconds);
    }
}

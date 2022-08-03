package utility;

import net.runelite.api.coords.WorldPoint;
import org.mockito.Mockito;
import shortestpath.ClientInfoProvider;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskHandler;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionPathfinderTask;
import shortestpath.worldmap.sections.SectionMapper;
import shortestpath.worldmap.sections.SectionRoute;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PathfinderTest {
    private static ClientInfoProvider clientInfoProvider;
    private static ConfigProvider configProvider;
    private static WorldMapProvider worldMapProvider;

    private static SectionMapper sectionMapper;
    private static SectionPathfinderTask sectionPlanner;

    private static PathfinderTaskHandler pathfinderTaskHandler;
    private static PathfinderRequestHandler pathfinderRequestHandler;

    public static void main(String[] args) {
        // Providers
        clientInfoProvider = Mockito.mock(ClientInfoProvider.class);
        configProvider = Mockito.mock(ConfigProvider.class);
        worldMapProvider = new WorldMapProvider();

        sectionMapper = new SectionMapper(worldMapProvider);
        sectionMapper.findSections();
        System.out.println("Done mapping sections.\n");

        final WorldPoint start = new WorldPoint(3232, 3401, 0);
        final WorldPoint target = new WorldPoint(3089, 3523, 0);

        sectionPlanner = new SectionPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, start, target);
        waitForPathfinderTaskCompletion(sectionPlanner);
        List<SectionRoute> routes = sectionPlanner.getRoutes();

        for (final SectionRoute route : routes) {
            System.out.println("\nNew route:");
            for (final Transport transport : route.getTransports()) {
                System.out.println(transport.getOrigin() + " to " + transport.getDestination());
            }
            System.out.println("Route length: " + route.length() + " points.");
        }
        System.out.println("Routes found: " + routes.size());

        // Pathfinder
        pathfinderTaskHandler = new PathfinderTaskHandler(configProvider, worldMapProvider, sectionMapper);
        pathfinderRequestHandler = new PathfinderRequestHandler(clientInfoProvider, worldMapProvider, pathfinderTaskHandler);
    }

    private static boolean waitForPathfinderTaskCompletion(final SectionPathfinderTask task) {
        long startTime = System.nanoTime();
        while (task.getStatus() == PathfinderTaskStatus.CALCULATING) {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (Exception ignore) {
            }
        }
        return true;
    }
}

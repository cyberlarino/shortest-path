package utility;

import jogamp.common.os.elf.Section;
import net.runelite.api.coords.WorldPoint;
import org.mockito.Mockito;
import shortestpath.ClientInfoProvider;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.pathfinder.PathfinderTaskHandler;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionPathfinder;
import shortestpath.worldmap.sections.SectionMapper;
import shortestpath.worldmap.sections.SectionRoute;

import java.util.List;

public class PathfinderTest {
    private static ClientInfoProvider clientInfoProvider;
    private static ConfigProvider configProvider;
    private static WorldMapProvider worldMapProvider;

    private static SectionMapper sectionMapper;
    private static SectionPathfinder sectionPlanner;

    private static PathfinderTaskHandler pathfinderTaskHandler;
    private static PathfinderRequestHandler pathfinderRequestHandler;

    public static void main(String[] args) {
        // Providers
        clientInfoProvider = Mockito.mock(ClientInfoProvider.class);
        configProvider = Mockito.mock(ConfigProvider.class);
        worldMapProvider = new WorldMapProvider();

        sectionMapper = new SectionMapper(worldMapProvider);
        sectionPlanner = new SectionPathfinder(worldMapProvider, sectionMapper);
        sectionMapper.findSections();

        System.out.println("Done mapping sections.\n");

        final WorldPoint start = new WorldPoint(3232, 3401, 0);
        final WorldPoint target = new WorldPoint(3089, 3523, 0);
        List<SectionRoute> routes = sectionPlanner.getPossibleRoutes(start, target);

        for (final SectionRoute route : routes) {
            System.out.println("\nNew route:");
            for (final Transport transport : route.getTransports()) {
                System.out.println(transport.getOrigin() + " to " + transport.getDestination());
            }
        }
        System.out.println("Routes found: " + routes.size());

        // Pathfinder
        pathfinderTaskHandler = new PathfinderTaskHandler(configProvider, worldMapProvider, sectionMapper);
        pathfinderRequestHandler = new PathfinderRequestHandler(clientInfoProvider, worldMapProvider, pathfinderTaskHandler);
    }
}

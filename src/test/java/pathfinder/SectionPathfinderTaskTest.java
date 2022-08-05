package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.utils.Util;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;
import shortestpath.worldmap.sections.SectionPathfinderTask;
import shortestpath.worldmap.sections.SectionRoute;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class SectionPathfinderTaskTest {
    private static final long TIMEOUT_SECONDS = 100;

    private WorldMapProvider worldMapProvider;
    private SectionMapper sectionMapper;
    private PathfinderConfig pathfinderConfig;

    @Before
    public void setup() {
        this.worldMapProvider = new WorldMapProvider();
        this.sectionMapper = SectionMapper.fromFile(worldMapProvider);
        this.pathfinderConfig = new PathfinderConfig();
    }

    // Utility functions
    private static boolean waitForPathfinderTaskCompletion(final SectionPathfinderTask task) {
        long startTime = System.nanoTime();
        while (task.getStatus() == PathfinderTaskStatus.CALCULATING) {
            if ((System.nanoTime() - startTime) >= TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS)) {
                return false;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    private boolean looseRailingShortcutUsed(final SectionRoute route) {
        final WorldPoint upperLeftCorner = new WorldPoint(3418, 3480, 0);
        final WorldPoint bottomRightCorner = new WorldPoint(3424, 3475, 0);
        final Predicate<WorldPoint> pointInsideShortcutBoundary = (point) -> Util.isPointInsideRectangle(upperLeftCorner, bottomRightCorner, point);
        return route.getTransports().stream().anyMatch((transport) -> pointInsideShortcutBoundary.test(transport.getOrigin()) || pointInsideShortcutBoundary.test(transport.getDestination()));
    }

    private boolean allRoutesValid(final List<SectionRoute> routes, final WorldPoint start, final WorldPoint target) {
        final Integer startSection = sectionMapper.getSectionId(start);
        final Integer targetSection = sectionMapper.getSectionId(target);

        final Predicate<SectionRoute> isRouteValid = route -> {
            if (route.getTransports().isEmpty()) {
                return false;
            }

            final Predicate<Transport> isTransportValid = transport -> {
                return transport.getOrigin() != null && transport.getDestination() != null;
            };
            if (!route.getTransports().stream().allMatch(isTransportValid)) {
                return false;
            }

            final Integer routeStartSection = sectionMapper.getSectionId(route.getOrigin());
            final Integer routeTargetSection = sectionMapper.getSectionId(route.getDestination());
            if (!Objects.equals(routeStartSection, startSection) || !Objects.equals(routeTargetSection, targetSection)) {
                return false;
            }

            final Integer firstTransportSection = sectionMapper.getSectionId(route.getTransports().get(0).getOrigin());
            if (!Objects.equals(routeStartSection, firstTransportSection)) {
                return false;
            }

            final Integer lastTransportSection = sectionMapper.getSectionId(
                    route.getTransports().get(route.getTransports().size() - 1).getDestination());
            if (!Objects.equals(routeTargetSection, lastTransportSection)) {
                return false;
            }

            for (int i = 0; i < route.getTransports().size() - 1; ++i) {
                final Integer currentTransportDestinationSection = sectionMapper.getSectionId(route.getTransports().get(i).getDestination());
                final Integer nextTransportOriginSection = sectionMapper.getSectionId(route.getTransports().get(i + 1).getOrigin());
                if (!Objects.equals(currentTransportDestinationSection, nextTransportOriginSection)) {
                    return false;
                }
            }

            return true;
        };
        return routes.stream().allMatch(isRouteValid);
    }

    @Test
    public void simpleSectionTraverseTest_GrandExchange() {
        final WorldPoint start = new WorldPoint(3232, 3401, 0);
        final WorldPoint target = new WorldPoint(3089, 3523, 0);

        SectionPathfinderTask sectionPathfinderTask = new SectionPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, start, target);
        final boolean taskCompletedInTime = waitForPathfinderTaskCompletion(sectionPathfinderTask);
        Assert.assertTrue(taskCompletedInTime);

        final List<SectionRoute> routes = sectionPathfinderTask.getRoutes();
        Assert.assertTrue(allRoutesValid(routes, start, target));
        Assert.assertTrue(routes.size() > 0);
    }

    @Test
    public void simpleSectionTraverseTest_LumbridgeCastle() {
        final WorldPoint start = new WorldPoint(3212, 3218, 2);
        final WorldPoint target = new WorldPoint(3224, 3218, 0);

        SectionPathfinderTask sectionPathfinderTask = new SectionPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, start, target);
        final boolean taskCompletedInTime = waitForPathfinderTaskCompletion(sectionPathfinderTask);
        Assert.assertTrue(taskCompletedInTime);

        final List<SectionRoute> routes = sectionPathfinderTask.getRoutes();
        Assert.assertTrue(allRoutesValid(routes, start, target));
        Assert.assertTrue(routes.size() > 0);
    }

    @Test
    public void agilityShortcutTest_meetsRequirements() {
        final WorldPoint start = new WorldPoint(3395, 3485, 0);
        final WorldPoint target = new WorldPoint(3478, 9839, 0);
        pathfinderConfig.agilityLevel = 65;

        SectionPathfinderTask sectionPathfinderTask = new SectionPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, start, target, pathfinderConfig.getCanPlayerUseTransportPredicate());
        final boolean taskCompletedInTime = waitForPathfinderTaskCompletion(sectionPathfinderTask);
        Assert.assertTrue(taskCompletedInTime);

        final List<SectionRoute> routes = sectionPathfinderTask.getRoutes();
        Assert.assertTrue(allRoutesValid(routes, start, target));
        Assert.assertTrue(routes.size() > 0);

        final boolean looseRailingShortcutUsedInAnyRoute = routes.stream().anyMatch(this::looseRailingShortcutUsed);
        Assert.assertTrue(looseRailingShortcutUsedInAnyRoute);
    }

    @Test
    public void agilityShortcutTest_doesNotMeetRequirements() {
        final WorldPoint start = new WorldPoint(3395, 3485, 0);
        final WorldPoint target = new WorldPoint(3478, 9839, 0);

        SectionPathfinderTask sectionPathfinderTask = new SectionPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, start, target, pathfinderConfig.getCanPlayerUseTransportPredicate());
        final boolean taskCompletedInTime = waitForPathfinderTaskCompletion(sectionPathfinderTask);
        Assert.assertTrue(taskCompletedInTime);

        final List<SectionRoute> routes = sectionPathfinderTask.getRoutes();
        Assert.assertTrue(allRoutesValid(routes, start, target));
        Assert.assertTrue(routes.size() > 0);

        final boolean looseRailingShortcutUsedInAnyRoute = routes.stream().anyMatch(this::looseRailingShortcutUsed);
        Assert.assertFalse(looseRailingShortcutUsedInAnyRoute);
    }
}

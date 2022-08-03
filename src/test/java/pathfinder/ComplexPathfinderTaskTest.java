package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.pathfindertask.ComplexPathfinderTask;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class ComplexPathfinderTaskTest {
    private static final long TIMEOUT_SECONDS = 10;

    private WorldMapProvider worldMapProvider;
    private SectionMapper sectionMapper;
    private PathfinderConfig defaultConfig;

    @Before
    public void setup() {
        this.worldMapProvider = new WorldMapProvider();
        this.sectionMapper = SectionMapper.fromFile(worldMapProvider);
        this.defaultConfig = new PathfinderConfig();
    }

    // Utility functions
    private static boolean waitForPathfinderTaskCompletion(final ComplexPathfinderTask task) {
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

    private boolean isPathValid(final Path path) {
        final Predicate<Movement> movementTransportOrNotBlocked = movement -> {
            final WorldPoint movementOrigin = movement.getOrigin();
            final WorldPoint movementDestination = movement.getDestination();
            if (!worldMapProvider.getWorldMap().isBlocked(movementDestination)) {
                return true;
            }

            return worldMapProvider.getWorldMap().getTransports(movementOrigin).size() != 0;
        };

        if (!path.getMovements().stream().allMatch(movementTransportOrNotBlocked)) {
            return false;
        }

        for (int i = 0; i < path.getMovements().size() - 1; ++i) {
            if (!path.getMovements().get(i).getDestination().equals(path.getMovements().get(i + 1).getOrigin())) {
                return false;
            }

            final WorldPoint point = path.getMovements().get(i).getDestination();
            final WorldPoint nextPoint = path.getMovements().get(i + 1).getDestination();

            if (point.distanceTo(nextPoint) > 1) {
                // A 'jump' in the path, either transport was used, or path isn't connected properly
                boolean pathTransportUsed = false;
                for (Transport transport : worldMapProvider.getWorldMap().getTransports(point)) {
                    if (transport.getDestination().equals(nextPoint)) {
                        pathTransportUsed = true;
                        break;
                    }
                }
                if (!pathTransportUsed) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    public void pathTest_differentSection() {
        final WorldPoint start = new WorldPoint(2979, 3819, 0);
        final WorldPoint target = new WorldPoint(2854, 3969, 0);

        ComplexPathfinderTask task = new ComplexPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, defaultConfig, start, target);
        final boolean finishedInTime = waitForPathfinderTaskCompletion(task);
        Assert.assertTrue(finishedInTime);

        Assert.assertTrue(isPathValid(task.getPath()));
    }
}
package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Test;
import shortestpath.pathfinder.Path;
import shortestpath.worldmap.Transport;
import shortestpath.pathfinder.Node;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.PathfinderTask;
import shortestpath.worldmap.WorldMapProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class PathfinderTest {
    private static final long TIMEOUT_SECONDS = 5;

    private final WorldMapProvider worldMapProvider;
    private final PathfinderConfig defaultConfig;
    private final List<WorldPoint> varrockSteppingStoneShortcutPoints;

    public PathfinderTest() {
        this.worldMapProvider = new WorldMapProvider();
        this.defaultConfig = new PathfinderConfig();

        this.varrockSteppingStoneShortcutPoints = new ArrayList<>();
        this.varrockSteppingStoneShortcutPoints.add(new WorldPoint(3150, 3363, 0));
        this.varrockSteppingStoneShortcutPoints.add(new WorldPoint(3151, 3363, 0));
        this.varrockSteppingStoneShortcutPoints.add(new WorldPoint(3152, 3363, 0));
        this.varrockSteppingStoneShortcutPoints.add(new WorldPoint(3153, 3363, 0));
    }

    private static boolean waitForPathfinderTaskCompletion(final PathfinderTask task) {
        long startTime = System.nanoTime();
        while (!task.isDone()) {
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
        final Predicate<WorldPoint> pointTransportOrNotBlocked = worldPoint -> {
            if (!worldMapProvider.getCollisionMap().isBlocked(worldPoint)) {
                return true;
            }

            if (worldMapProvider.getTransports().getOrDefault(worldPoint, new ArrayList<>()).size() != 0) {
                return true;
            }
            return false;
        };

        if (!path.getPoints().stream().allMatch(pointTransportOrNotBlocked)) {
            return false;
        }

        for (int i = 0; i < path.getPoints().size() - 1; ++i) {
            final WorldPoint point = path.getPoints().get(i);
            final WorldPoint nextPoint = path.getPoints().get(i + 1);

            if (point.distanceTo(nextPoint) > 1) {
                // A 'jump' in the path, either transport was used, or path isn't connected properly
                boolean pathTransportUsed = false;
                for (Transport transport : worldMapProvider.getTransports().getOrDefault(point, new ArrayList<>())) {
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

    private static boolean anyOfThePointsInPath(final List<WorldPoint> points, final Path path) {
        return path.getPoints().stream().anyMatch(pathPoint -> {
            for (WorldPoint listPoint : points) {
                if (listPoint.equals(pathPoint)) {
                    return true;
                }
            }
            return false;
        });
    }

    private boolean varrockSteppingStoneShortcutInPath(final Path path) {
        return anyOfThePointsInPath(varrockSteppingStoneShortcutPoints, path);
    }

    @Test
    public void testStraightPath() {
        // Test if a single straight generated path is traversable
        final PathfinderConfig config = defaultConfig;

        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final PathfinderTask task = new PathfinderTask(worldMapProvider.getWorldMap(), config, start, target);

        final boolean calculatedPathInTime = waitForPathfinderTaskCompletion(task);
        Assert.assertTrue(calculatedPathInTime);

        Path path = task.getPath();
        Assert.assertEquals(path.getPoints().get(0), start);
        Assert.assertEquals(path.getPoints().get(path.getPoints().size() - 1), target);
        Assert.assertTrue(isPathValid(path));
    }

    @Test
    public void testPathAdheresCollisionMap() {
        // Test if a path adheres to the collision map, and goes around instead of through
        //   illustrations/testPathAdheresCollisionMap.png
        final PathfinderConfig config = defaultConfig;

        final WorldPoint start = new WorldPoint(3147, 3338, 0);
        final WorldPoint target = new WorldPoint(3175, 3323, 0);
        final PathfinderTask task = new PathfinderTask(worldMapProvider.getWorldMap(), config, start, target);

        final boolean calculatedPathInTime = waitForPathfinderTaskCompletion(task);
        final boolean isPathValid = isPathValid(task.getPath());
        Assert.assertTrue(calculatedPathInTime);
        Assert.assertTrue(isPathValid);

        // Gate required to pass through to get into area
        final List<WorldPoint> fenceGate = new ArrayList<>();
        fenceGate.add(new WorldPoint(3176, 3316, 0));
        fenceGate.add(new WorldPoint(3177, 3315, 0));

        final boolean fanceGatePassedThroughDuringPath = anyOfThePointsInPath(fenceGate, task.getPath());
        Assert.assertTrue(fanceGatePassedThroughDuringPath);
    }

    @Test
    public void testInvalidPath() {
        // Test if 'isPathValid' detects not traversable path
        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        Node startNode = new Node(start, null);
        Node targetNode = new Node(target, startNode);

        Path path = targetNode.getPath();
        Assert.assertFalse(isPathValid(path));
    }

    @Test
    public void testShortcut_NotMeetingRequirements() {
        // Test that the Varrock stepping stone shortcut isn't used, if required level isn't met
        //   illustrations/testShortcut.png
        final PathfinderConfig config = defaultConfig;
        config.useTransports = true;
        config.useAgilityShortcuts = true;
        config.agilityLevel = 30;

        final WorldPoint start = new WorldPoint(3161, 3364, 0);
        final WorldPoint target = new WorldPoint(3143, 3364, 0);
        final PathfinderTask task = new PathfinderTask(worldMapProvider.getWorldMap(), config, start, target);

        final boolean calculatedPathInTime = waitForPathfinderTaskCompletion(task);
        Assert.assertTrue(calculatedPathInTime);

        Path path = task.getPath();
        Assert.assertEquals(path.getPoints().get(0), start);
        Assert.assertEquals(path.getPoints().get(path.getPoints().size() - 1), target);
        Assert.assertTrue(isPathValid(path));

        Assert.assertFalse(varrockSteppingStoneShortcutInPath(path));
    }

    @Test
    public void testShortcut_MeetingRequirements() {
        // Test that the Varrock stepping stone shortcut is used when required level met
        //   illustrations/testShortcut.png
        final PathfinderConfig config = defaultConfig;
        config.useTransports = true;
        config.useAgilityShortcuts = true;
        config.agilityLevel = 31;

        final WorldPoint start = new WorldPoint(3161, 3364, 0);
        final WorldPoint target = new WorldPoint(3143, 3364, 0);
        final PathfinderTask task = new PathfinderTask(worldMapProvider.getWorldMap(), config, start, target);

        final boolean calculatedPathInTime = waitForPathfinderTaskCompletion(task);
        Assert.assertTrue(calculatedPathInTime);

        Path path = task.getPath();
        Assert.assertEquals(path.getPoints().get(0), start);
        Assert.assertEquals(path.getPoints().get(path.getPoints().size() - 1), target);
        Assert.assertTrue(isPathValid(path));

        Assert.assertTrue(varrockSteppingStoneShortcutInPath(path));
    }
}

package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Test;
import shortestpath.Path;
import shortestpath.Transport;
import shortestpath.pathfinder.CollisionMap;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.PathfinderTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PathfinderTest {
    private static final long TIMEOUT_SECONDS = 5;

    private final CollisionMap map;
    private final Map<WorldPoint, List<Transport>> transports;

    public PathfinderTest() {
        this.map = CollisionMap.fromFile("src/main/resources/collision-map.zip");
        this.transports = Transport.fromFile("src/main/resources/transports.txt");

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
        if (!path.getPoints().stream().allMatch(worldPoint -> {
            return map.get(worldPoint.getX(), worldPoint.getY(), worldPoint.getPlane(), 0);
        })) {
            return false;
        }

        for (int i = 0; i < path.getPoints().size() - 1; ++i) {
            final WorldPoint point = path.getPoints().get(i);
            final WorldPoint nextPoint = path.getPoints().get(i + 1);

            if (point.distanceTo(nextPoint) > 1) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testStraightPath() {
        final PathfinderConfig config = new PathfinderConfig(map, transports);

        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final PathfinderTask task = new PathfinderTask(config, start, target);

        final boolean calculatedPathInTime = waitForPathfinderTaskCompletion(task);
        Assert.assertTrue(calculatedPathInTime);

        Path path = task.getPath();
        Assert.assertEquals(path.getPoints().get(0), start);
        Assert.assertEquals(path.getPoints().get(path.getPoints().size() - 1), target);
        Assert.assertTrue(isPathValid(path));
    }

    @Test
    public void testShortcut_NotMeetingRequirements() {
        final PathfinderConfig config = new PathfinderConfig(map, transports);
        config.useAgilityShortcuts = true;
        config.useTransports = true;
        config.agilityLevel = 30;
        final boolean varrockSteppingStoneShortcutUseable = (config.agilityLevel >= 31);

        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final PathfinderTask task = new PathfinderTask(config, start, target);

        final boolean calculatedPathInTime = waitForPathfinderTaskCompletion(task);
        Assert.assertTrue(calculatedPathInTime);

        Path path = task.getPath();
        Assert.assertEquals(path.getPoints().get(0), start);
        Assert.assertEquals(path.getPoints().get(path.getPoints().size() - 1), target);
        Assert.assertTrue(isPathValid(path));

        final List<WorldPoint> varrockSteppingStoneShortcutPoints = new ArrayList<WorldPoint>() {{
            new WorldPoint(3150, 3363, 0);
            new WorldPoint(3151, 3363, 0);
            new WorldPoint(3152, 3363, 0);
            new WorldPoint(3153, 3363, 0);
        }};
        final boolean varrockSteppingStoneShortcutInPath = varrockSteppingStoneShortcutPoints.stream().anyMatch(worldPoint -> {
            return path.getPoints().contains(worldPoint);
        });
        Assert.assertTrue(varrockSteppingStoneShortcutInPath == varrockSteppingStoneShortcutUseable);
    }

    @Test
    public void testShortcut_MeetingRequirements() {
        final PathfinderConfig config = new PathfinderConfig(map, transports);
        config.useAgilityShortcuts = true;
        config.useTransports = true;
        config.agilityLevel = 31;
        final boolean varrockSteppingStoneShortcutUseable = (config.agilityLevel >= 31);

        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final PathfinderTask task = new PathfinderTask(config, start, target);

        final boolean calculatedPathInTime = waitForPathfinderTaskCompletion(task);
        Assert.assertTrue(calculatedPathInTime);

        Path path = task.getPath();
        Assert.assertEquals(path.getPoints().get(0), start);
        Assert.assertEquals(path.getPoints().get(path.getPoints().size() - 1), target);
        Assert.assertTrue(isPathValid(path));

        final List<WorldPoint> varrockSteppingStoneShortcutPoints = new ArrayList<WorldPoint>() {{
            new WorldPoint(3150, 3363, 0);
            new WorldPoint(3151, 3363, 0);
            new WorldPoint(3152, 3363, 0);
            new WorldPoint(3153, 3363, 0);
        }};
        final boolean varrockSteppingStoneShortcutInPath = varrockSteppingStoneShortcutPoints.stream().anyMatch(worldPoint -> {
            return path.getPoints().contains(worldPoint);
        });
        Assert.assertTrue(varrockSteppingStoneShortcutInPath == varrockSteppingStoneShortcutUseable);
    }
}

package unittests.pathfinder.pathfindertask;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.pathfindertask.ComplexPathfinderTask;
import shortestpath.utils.PathfinderUtil;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;

import java.util.List;
import java.util.Set;

public class ComplexPathfinderTaskTest {
    private static final long TIMEOUT_SECONDS = 10;

    private WorldMapProvider worldMapProvider = new WorldMapProvider();
    private SectionMapper sectionMapper = SectionMapper.fromFile(worldMapProvider);
    private PathfinderConfig defaultConfig;

    @Before
    public void setup() {
        this.defaultConfig = new PathfinderConfig();
    }

    // Utility functions
    private boolean isLooseRailingShortcutUsed(final Path path) {
        // Find transports in loose railing shortcut
        final WorldPoint upperLeftCorner = new WorldPoint(3418, 3480, 0);
        final WorldPoint bottomRightCorner = new WorldPoint(3424, 3475, 0);
        final Set<Transport> looseRailingShortcut =
                PathfinderUtil.getTransportsInsideRectangle(upperLeftCorner, bottomRightCorner, worldMapProvider.getWorldMap());

        // Check if any of those transports are in path
        final List<Transport> transportsInPath = path.getMovementOfType(Transport.class);
        return looseRailingShortcut.stream().anyMatch(transportsInPath::contains);
    }

    public Path completeComplexPathfinderTask(final WorldPoint start, final WorldPoint target, final PathfinderConfig config) {
        final ComplexPathfinderTask task = new ComplexPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, config, start, target);
        final boolean finishedInTime = PathfinderUtil.waitForTaskCompletion(task, TIMEOUT_SECONDS);
        Assert.assertTrue(finishedInTime);

        final Path path = task.getPath();
        Assert.assertNotNull(path);
        Assert.assertTrue(PathfinderUtil.isPathValid(worldMapProvider.getWorldMap(), path));
        return path;
    }

    @Test
    @Ignore
    public void pathTest_GrandExchange() {
        final WorldPoint start = new WorldPoint(2979, 3819, 0);
        final WorldPoint target = new WorldPoint(2854, 3969, 0);
        completeComplexPathfinderTask(start, target, defaultConfig);
    }

    @Test
    public void pathTest_CanifisLooseRailing_requirementMet() {
        // Verify the loose railing shortcut in Canifis is used if high enough agility level
        final WorldPoint start = new WorldPoint(3395, 3485, 0);
        final WorldPoint target = new WorldPoint(3478, 9839, 0);
        defaultConfig.agilityLevel = 65;

        final Path path = completeComplexPathfinderTask(start, target, defaultConfig);
        Assert.assertTrue(isLooseRailingShortcutUsed(path));
    }

    @Test
    public void pathTest_CanifisLooseRailing_requirementNotMet() {
        // Verify the loose railing shortcut in Canifis is used if high enough agility level
        final WorldPoint start = new WorldPoint(3395, 3485, 0);
        final WorldPoint target = new WorldPoint(3478, 9839, 0);
        defaultConfig.agilityLevel = 63;

        final Path path = completeComplexPathfinderTask(start, target, defaultConfig);
        Assert.assertFalse(isLooseRailingShortcutUsed(path));
    }

    @Test
    public void pathTest_SteppingStoneShortcutUsed() {
        // Set-up, find stepping stone shortcut
        final WorldPoint boundaryCorner = new WorldPoint(3156, 3360, 0);
        final WorldPoint boundaryOppositeCorner = new WorldPoint(3147, 3365, 0);
        final Set<Transport> steppingStoneShortcut =
                PathfinderUtil.getTransportsInsideRectangle(boundaryCorner, boundaryOppositeCorner, worldMapProvider.getWorldMap());

        // Verify stepping-stone shortcut in path
        final WorldPoint start = new WorldPoint(3206, 3215, 1);
        final WorldPoint target = new WorldPoint(3159, 3364, 0);
        defaultConfig.agilityLevel = 32;

        final Path path = completeComplexPathfinderTask(start, target, defaultConfig);
        final List<Transport> transportsInPath = path.getMovementOfType(Transport.class);
        final boolean isSteppingStoneShortcutUsed = steppingStoneShortcut.stream().anyMatch(transportsInPath::contains);
        Assert.assertTrue(isSteppingStoneShortcutUsed);
    }

    @Test
    @Ignore
    public void pathTest_WildernessLeverUsed() {
        // Set-up, find Edgeville Wilderness Lever transport
        final WorldPoint boundaryCorner = new WorldPoint(3087, 3477, 0);
        final WorldPoint boundaryOppositeCorner = new WorldPoint(3093, 3473, 0);
        final Set<Transport> wildernessLever =
                PathfinderUtil.getTransportsInsideRectangle(boundaryCorner, boundaryOppositeCorner, worldMapProvider.getWorldMap());

        // Verify stepping-stone shortcut in path
        final WorldPoint start = new WorldPoint(3095, 3509, 1);
        final WorldPoint target = new WorldPoint(2566, 3310, 0);

        final Path path = completeComplexPathfinderTask(start, target, defaultConfig);
        final List<Transport> transportsInPath = path.getMovementOfType(Transport.class);
        final boolean isWildernessLeverUsed = wildernessLever.stream().anyMatch(transportsInPath::contains);
        Assert.assertTrue(isWildernessLeverUsed);
    }
}
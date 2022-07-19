package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import shortestpath.ClientInfoProvider;
import shortestpath.pathfinder.Path;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.pathfinder.PathfinderTask;
import shortestpath.pathfinder.PathfinderTaskGenerator;
import shortestpath.worldmap.WorldMapProvider;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class PathfinderRequestHandlerTest {
    private ClientInfoProvider clientInfoProviderMock;
    private PathfinderTaskGenerator pathfinderTaskGeneratorMock;
    private PathfinderTask pathfinderTaskMock;

    private WorldMapProvider worldMapProvider;
    private PathfinderRequestHandler pathfinderRequestHandler;

    public PathfinderRequestHandlerTest() {
        this.clientInfoProviderMock = mock(ClientInfoProvider.class);
        this.pathfinderTaskGeneratorMock = mock(PathfinderTaskGenerator.class);
        this.pathfinderTaskMock = mock(PathfinderTask.class);

        this.worldMapProvider = new WorldMapProvider();
        this.pathfinderRequestHandler = new PathfinderRequestHandler(clientInfoProviderMock, worldMapProvider, pathfinderTaskGeneratorMock);
    }

    // Utility functions
    private Path expectGeneratePath(final WorldPoint start, final WorldPoint target) {
        // Test helper function, sets up mocks to generate start-target PathfinderTask
        final Path path = new Path(Arrays.asList(start, target));

        when(clientInfoProviderMock.getPlayerLocation()).thenReturn(start);
        when(pathfinderTaskGeneratorMock.generate(start, target)).thenReturn(pathfinderTaskMock);
        when(pathfinderTaskMock.getPath()).thenReturn(path);
        return path;
    }

    @Test
    public void testValidStartStopPoints() {
        // Simple and valid start/stop points of a path, should generate a task with exactly those points.
        // Also verify that getActivePath() returns the generated path.
        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final Path expectedPath = expectGeneratePath(start, target);

        // Initially no path
        Assert.assertNull(pathfinderRequestHandler.getActivePath());
        Assert.assertTrue(pathfinderRequestHandler.isActivePathDone());

        // Set target, should request path from player location to target
        pathfinderRequestHandler.setTarget(target);
        verify(clientInfoProviderMock).getPlayerLocation();
        verify(pathfinderTaskGeneratorMock).generate(start, target);

        // With path task in place, getActivePath() and isActivePathDone() should return proper values
        final Path activePath = pathfinderRequestHandler.getActivePath();
        verify(pathfinderTaskMock).getPath();
        Assert.assertEquals(expectedPath, activePath);

        //   Task not yet done
        when(pathfinderTaskMock.isDone()).thenReturn(false);
        boolean isPathTaskDone = pathfinderRequestHandler.isActivePathDone();
        Assert.assertFalse(isPathTaskDone);

        //   Task done
        when(pathfinderTaskMock.isDone()).thenReturn(true);
        isPathTaskDone = pathfinderRequestHandler.isActivePathDone();
        Assert.assertTrue(isPathTaskDone);
        verify(pathfinderTaskMock, times(2)).isDone(); // two because happened twice
    }

    @Test
    public void testGetActivePathStart_GetActivePathTarget() {
        // Test functions getStart() and getTarget()
        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final Path expectedPath = expectGeneratePath(start, target);

        pathfinderRequestHandler.setTarget(target);
        Assert.assertEquals(start, pathfinderRequestHandler.getStart());
        Assert.assertEquals(target, pathfinderRequestHandler.getTarget());
    }

    @Test
    public void testHasActivePath() {
        // Test function hasActivePath()
        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final Path expectedPath = expectGeneratePath(start, target);

        Assert.assertFalse(pathfinderRequestHandler.hasActivePath());
        pathfinderRequestHandler.setTarget(target);
        Assert.assertTrue(pathfinderRequestHandler.hasActivePath());
    }

    @Test
    public void testClearPath() {
        // Tests that the function clearPath(), clears the path
        final WorldPoint start = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final Path expectedPath = expectGeneratePath(start, target);

        // Ensure Handler has path first
        pathfinderRequestHandler.setTarget(target);
        Assert.assertTrue(pathfinderRequestHandler.hasActivePath());
        Assert.assertNotNull(pathfinderRequestHandler.getActivePath());
        Assert.assertNotNull(pathfinderRequestHandler.getStart());
        Assert.assertNotNull(pathfinderRequestHandler.getTarget());

        // Clear path
        pathfinderRequestHandler.clearPath();
        Assert.assertFalse(pathfinderRequestHandler.hasActivePath());
        Assert.assertNull(pathfinderRequestHandler.getActivePath());
        Assert.assertNull(pathfinderRequestHandler.getStart());
        Assert.assertNull(pathfinderRequestHandler.getTarget());
    }

    @Test
    public void testSetStart() {
        // Tests explicitly setting start

        // Set-up, make initial player-target path
        final WorldPoint playerPosition = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final Path playerToTargetPath = expectGeneratePath(playerPosition, target);
        pathfinderRequestHandler.setTarget(target);
        reset(clientInfoProviderMock); // reset for 'never()' condition below

        // Expect a new task to be generated, and output the start-target path
        final WorldPoint startPoint = new WorldPoint(3161, 3364, 0);
        final Path startToTargetPath = new Path(Arrays.asList(startPoint, target));

        final PathfinderTask pathfinderTaskMock1 = mock(PathfinderTask.class);
        when(pathfinderTaskGeneratorMock.generate(startPoint, target)).thenReturn(pathfinderTaskMock1);
        when(pathfinderTaskMock1.getPath()).thenReturn(startToTargetPath);


        // When setting new target, the path outputted should be the startToTargetPath, as player position is
        // not involved.
        pathfinderRequestHandler.setStart(startPoint);
        Assert.assertEquals(pathfinderTaskMock1.getPath(), startToTargetPath);
        verify(clientInfoProviderMock, never()).getPlayerLocation();
        verify(pathfinderTaskMock1).getPath();
    }
}

package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import shortestpath.ClientInfoProvider;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.pathfinder.pathfindertask.SimplePathfinderTask;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskHandler;
import shortestpath.pathfinder.path.Walk;
import shortestpath.utils.Util;
import shortestpath.worldmap.WorldMapProvider;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PathfinderRequestHandlerTest {
    @Mock
    private ClientInfoProvider clientInfoProviderMock;
    @Mock
    private PathfinderTaskHandler pathfinderTaskHandlerMock;
    @Mock
    private SimplePathfinderTask simplePathfinderTaskMock;

    @Captor
    private ArgumentCaptor<WorldPoint> worldPointArgumentCaptor;

    private WorldMapProvider worldMapProvider;
    private PathfinderRequestHandler pathfinderRequestHandler;

    @Before
    public void setup() {
        this.worldMapProvider = new WorldMapProvider();
        this.pathfinderRequestHandler =
                new PathfinderRequestHandler(clientInfoProviderMock, worldMapProvider, pathfinderTaskHandlerMock);
    }

    private final WorldPoint blockedGeTile = new WorldPoint(3166, 3491, 0);

    // Utility functions
    private boolean isPointOnGeBorder(final WorldPoint point) {
        // Tests whether point is on the green border as described in 'testFilteringBlockedPoints.png'
        final WorldPoint outsideBorderTopLeft = new WorldPoint(3162, 3492, 0);
        final WorldPoint outsideBorderBottomRight = new WorldPoint(3167, 3487, 0);
        final boolean pointWithinGeCenter =
                Util.isPointInsideRectangle(outsideBorderTopLeft, outsideBorderBottomRight, point);

        final WorldPoint blockedRectangleTopLeft = new WorldPoint(3163, 3491, 0);
        final WorldPoint blockedRectangleBottomRight = new WorldPoint(3166, 3488, 0);
        final boolean pointWithinBlockedArea =
                Util.isPointInsideRectangle(blockedRectangleTopLeft, blockedRectangleBottomRight, point);

        return pointWithinGeCenter && !pointWithinBlockedArea;
    }


    private static Path createFakePath(final WorldPoint start, final WorldPoint target) {
        final Walk origin = new Walk(start, start);
        final Walk destination = new Walk(start, target);
        return new Path(Arrays.asList(origin, destination));
    }

    private Path expectGeneratePath(final WorldPoint start, final WorldPoint target) {
        // Test helper function, sets up mocks to generate start-target PathfinderTask
        final Path path = createFakePath(start, target);

        when(clientInfoProviderMock.getPlayerLocation()).thenReturn(start);
        when(pathfinderTaskHandlerMock.newTask(start, target)).thenReturn(simplePathfinderTaskMock);
        when(simplePathfinderTaskMock.getPath()).thenReturn(path);
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
        verify(pathfinderTaskHandlerMock).newTask(start, target);

        // With path task in place, getActivePath() and isActivePathDone() should return proper values
        final Path activePath = pathfinderRequestHandler.getActivePath();
        verify(simplePathfinderTaskMock).getPath();
        Assert.assertEquals(expectedPath, activePath);

        //   Task not yet done
        when(simplePathfinderTaskMock.getStatus()).thenReturn(PathfinderTaskStatus.CALCULATING);
        boolean isPathTaskDone = pathfinderRequestHandler.isActivePathDone();
        Assert.assertFalse(isPathTaskDone);

        //   Task done
        when(simplePathfinderTaskMock.getStatus()).thenReturn(PathfinderTaskStatus.DONE);
        isPathTaskDone = pathfinderRequestHandler.isActivePathDone();
        Assert.assertTrue(isPathTaskDone);
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
        final Path startToTargetPath = createFakePath(startPoint, target);

        final SimplePathfinderTask simplePathfinderTaskMock1 = mock(SimplePathfinderTask.class);
        when(pathfinderTaskHandlerMock.newTask(startPoint, target)).thenReturn(simplePathfinderTaskMock1);
        when(simplePathfinderTaskMock1.getPath()).thenReturn(startToTargetPath);


        // When setting new target, the path outputted should be the startToTargetPath, as player position is
        // not involved.
        pathfinderRequestHandler.setStart(startPoint);
        Assert.assertEquals(simplePathfinderTaskMock1.getPath(), startToTargetPath);
        verify(clientInfoProviderMock, never()).getPlayerLocation();
        verify(simplePathfinderTaskMock1).getPath();
    }

    @Test
    public void testRememberExplicitlySettingStart() {
        // When using 'Set Start' path has been explicitly set, and a new path generated with 'Set Target' should
        // keep this start point. Remember this until function 'clearPath()' has been called.

        // Set-up, make initial player-target path
        final WorldPoint playerPosition = new WorldPoint(3171, 3383, 0);
        final WorldPoint target = new WorldPoint(3171, 3404, 0);
        final Path playerToTargetPath = expectGeneratePath(playerPosition, target);
        pathfinderRequestHandler.setTarget(target);

        // Set-up, Explicitly run 'Set Start' with new StartPoint
        final WorldPoint startPoint = new WorldPoint(3161, 3364, 0);
        pathfinderRequestHandler.setStart(startPoint);


        // 'Set Target', explicitly set startPoint should be kept
        final WorldPoint newTarget = new WorldPoint(3143, 3364, 0);
        final Path startToNewTargetPath = createFakePath(startPoint, newTarget);

        final SimplePathfinderTask simplePathfinderTaskMock2 = mock(SimplePathfinderTask.class);
        when(pathfinderTaskHandlerMock.newTask(startPoint, newTarget)).thenReturn(simplePathfinderTaskMock2);
        when(simplePathfinderTaskMock2.getPath()).thenReturn(startToNewTargetPath);

        pathfinderRequestHandler.setTarget(newTarget);
        Assert.assertEquals(startToNewTargetPath, pathfinderRequestHandler.getActivePath());


        // Run 'clearPath()' and 'Set Target' again, start should be the point of the player
        // as in the initial 'setTarget()' call.
        pathfinderRequestHandler.clearPath();
        pathfinderRequestHandler.setTarget(target);
        Assert.assertEquals(playerToTargetPath, pathfinderRequestHandler.getActivePath());
    }

    @Test
    public void testFilteringBlockedPoints_setTarget() {
        // 'Set Target' on a blocked point should move the request point to a close but walkable tile instead.
        //   testFilteringBlockedPoints.png
        final WorldPoint somePlayerPosition = new WorldPoint(3166, 3479, 0);
        when(clientInfoProviderMock.getPlayerLocation()).thenReturn(somePlayerPosition);

        pathfinderRequestHandler.setTarget(blockedGeTile);
        verify(pathfinderTaskHandlerMock).newTask(any(), worldPointArgumentCaptor.capture());

        final boolean requestedTargetOnGeBorder = isPointOnGeBorder(worldPointArgumentCaptor.getValue());
        Assert.assertTrue(requestedTargetOnGeBorder);
    }

    @Test
    public void testFilteringBlockedPoints_setStart() {
        // 'Set Start' on a blocked point should move the request point to a close but walkable tile instead.
        //   testFilteringBlockedPoints.png
        final WorldPoint somePlayerPosition = new WorldPoint(3166, 3479, 0);
        when(clientInfoProviderMock.getPlayerLocation()).thenReturn(somePlayerPosition);
        final WorldPoint someTarget = new WorldPoint(3166, 3481, 0);
        pathfinderRequestHandler.setTarget(someTarget);
        reset(pathfinderTaskHandlerMock); // only setStart() related calls are relevant

        pathfinderRequestHandler.setStart(blockedGeTile);
        verify(pathfinderTaskHandlerMock).newTask(worldPointArgumentCaptor.capture(), any());

        final boolean requestedStartOnGeBorder = isPointOnGeBorder(worldPointArgumentCaptor.getValue());
        Assert.assertTrue(requestedStartOnGeBorder);
    }

    @Test
    public void testFilteringBlockedPoints_noValidTargetPoint() {
        // 'Set Target' on a point far away from any unblocked points should not generate a path.
        final WorldPoint somePlayerPosition = new WorldPoint(3166, 3479, 0);
        when(clientInfoProviderMock.getPlayerLocation()).thenReturn(somePlayerPosition);
        final WorldPoint unreachablePoint = new WorldPoint(3510, 3735, 0);

        pathfinderRequestHandler.setTarget(unreachablePoint);
        verify(pathfinderTaskHandlerMock, never()).newTask(any(), any());
    }

    @Test
    public void testCancelCurrentTaskOnNewRequest() {
        final WorldPoint somePlayerPosition = new WorldPoint(3166, 3479, 0);
        when(clientInfoProviderMock.getPlayerLocation()).thenReturn(somePlayerPosition);
        when(pathfinderTaskHandlerMock.newTask(any(), any())).thenReturn(simplePathfinderTaskMock);
        when(simplePathfinderTaskMock.getStatus()).thenReturn(PathfinderTaskStatus.CALCULATING);

        // Make a request
        final WorldPoint someTarget = new WorldPoint(3166, 3481, 0);
        pathfinderRequestHandler.setTarget(someTarget);

        // Make a new request, other task which is not done should be cancelled
        final WorldPoint someOtherTarget = new WorldPoint(3171, 3404, 0);
        pathfinderRequestHandler.setTarget(someOtherTarget);
        verify(simplePathfinderTaskMock).cancelTask();
    }
}

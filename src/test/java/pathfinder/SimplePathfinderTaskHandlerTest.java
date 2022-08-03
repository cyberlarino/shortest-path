package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.pathfinder.pathfindertask.SimplePathfinderTask;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskHandler;
import shortestpath.pathfinder.path.Walk;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;

import java.util.Arrays;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimplePathfinderTaskHandlerTest {
    @Mock
    private SimplePathfinderTask simplePathfinderTaskMock;
    @Mock
    private ConfigProvider configProviderMock;
    @Mock
    private WorldMapProvider worldMapProviderMock;
    @Mock
    private SectionMapper sectionMapperMock;

    private final int TICKS_WITHOUT_PROGRESS_BEFORE_CANCEL = 5;
    private PathfinderTaskHandler pathfinderTaskHandler;

    @Before
    public void setup() {
        this.pathfinderTaskHandler = new PathfinderTaskHandler(configProviderMock, worldMapProviderMock, sectionMapperMock);

        when(configProviderMock.ticksWithoutProgressBeforeCancelingTask()).thenReturn(TICKS_WITHOUT_PROGRESS_BEFORE_CANCEL);
    }

    // Utility functions
    private static Path createFakePath(final WorldPoint start, final WorldPoint target) {
        final Walk origin = new Walk(start, start);
        final Walk destination = new Walk(start, target);
        return new Path(Arrays.asList(origin, destination));
    }

    @Test
    public void testCancelTask_noProgress() {
        // When path calculation has no progress after threshold amount of ticks, cancel task
        final WorldPoint start = new WorldPoint(0, 0, 0);
        final WorldPoint target = new WorldPoint(10, 0, 0);
        when(simplePathfinderTaskMock.getStart()).thenReturn(start);
        when(simplePathfinderTaskMock.getTarget()).thenReturn(target);
        when(simplePathfinderTaskMock.getStatus()).thenReturn(PathfinderTaskStatus.CALCULATING);

        final Path noProgressPath = createFakePath(start, start);

        // Add task to list of tasks to manage
        pathfinderTaskHandler.add(simplePathfinderTaskMock);
        Assert.assertEquals(1, pathfinderTaskHandler.numberOfTasks());

        // No progress on the path, but threshold not reached yet, shouldn't cancel
        when(simplePathfinderTaskMock.getPath()).thenReturn(noProgressPath);
        for (int i = 0; i < TICKS_WITHOUT_PROGRESS_BEFORE_CANCEL - 1; ++i) {
            pathfinderTaskHandler.evaluateTasks();
        }
        verify(simplePathfinderTaskMock, never()).cancelTask();

        // Set-up for next step
        reset(simplePathfinderTaskMock);
        when(simplePathfinderTaskMock.getStart()).thenReturn(start);
        when(simplePathfinderTaskMock.getTarget()).thenReturn(target);
        when(simplePathfinderTaskMock.getPath()).thenReturn(noProgressPath);

        // Threshold reached, this time cancel task
        pathfinderTaskHandler.evaluateTasks();
        verify(simplePathfinderTaskMock).cancelTask();
        Assert.assertEquals(0, pathfinderTaskHandler.numberOfTasks());
    }

    @Test
    public void testCancelTask_progress() {
        // Do not cancel task if it has progress
        final WorldPoint start = new WorldPoint(0, 0, 0);
        final WorldPoint someProgressPoint = new WorldPoint(5, 0, 0);
        final WorldPoint target = new WorldPoint(10, 0, 0);

        final Path noProgressPath = createFakePath(start, start);
        final Path someProgressPath = createFakePath(start, someProgressPoint);

        // Add task to list of tasks to manage
        pathfinderTaskHandler.add(simplePathfinderTaskMock);
        Assert.assertEquals(1, pathfinderTaskHandler.numberOfTasks());

        // No progress on the path, but threshold not reached yet, shouldn't cancel
        when(simplePathfinderTaskMock.getPath()).thenReturn(noProgressPath);
        for (int i = 0; i < TICKS_WITHOUT_PROGRESS_BEFORE_CANCEL - 1; ++i) {
            pathfinderTaskHandler.evaluateTasks();
        }

        // Some progress on the path, counter should be reset, do not abort task
        when(simplePathfinderTaskMock.getPath()).thenReturn(someProgressPath);
        pathfinderTaskHandler.evaluateTasks();

        verify(simplePathfinderTaskMock, never()).cancelTask();
        Assert.assertEquals(1, pathfinderTaskHandler.numberOfTasks());
    }
}
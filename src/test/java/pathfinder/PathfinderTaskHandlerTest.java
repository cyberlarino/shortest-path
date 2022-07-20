package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.Path;
import shortestpath.pathfinder.PathfinderTask;
import shortestpath.pathfinder.PathfinderTaskHandler;

import java.util.Arrays;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PathfinderTaskHandlerTest {
    @Mock
    private PathfinderTask pathfinderTaskMock;
    @Mock
    private ConfigProvider configProviderMock;

    private final int TICKS_WITHOUT_PROGRESS_BEFORE_CANCEL = 5;
    private PathfinderTaskHandler pathfinderTaskHandler;

    @Before
    public void setup() {
        this.pathfinderTaskHandler = new PathfinderTaskHandler(configProviderMock);

        when(configProviderMock.ticksWithoutProgressBeforeCancelingTask()).thenReturn(TICKS_WITHOUT_PROGRESS_BEFORE_CANCEL);
    }

    @Test
    public void testCancelTask_noProgress() {
        // When path calculation has no progress after threshold amount of ticks, cancel task
        final WorldPoint start = new WorldPoint(0, 0, 0);
        final WorldPoint target = new WorldPoint(10, 0, 0);
        when(pathfinderTaskMock.getTarget()).thenReturn(target);
        when(pathfinderTaskMock.isDone()).thenReturn(false);

        final Path noProgressPath = new Path(Arrays.asList(start, start));

        // Add task to list of tasks to manage
        pathfinderTaskHandler.add(pathfinderTaskMock);
        Assert.assertEquals(1, pathfinderTaskHandler.numberOfTasks());

        // No progress on the path, but threshold not reached yet, shouldn't cancel
        when(pathfinderTaskMock.getPath()).thenReturn(noProgressPath);
        for (int i = 0; i < TICKS_WITHOUT_PROGRESS_BEFORE_CANCEL - 1; ++i) {
            pathfinderTaskHandler.evaluateTasks();
        }
        verify(pathfinderTaskMock, never()).abortTask();

        // Set-up for next step
        reset(pathfinderTaskMock);
        when(pathfinderTaskMock.getPath()).thenReturn(noProgressPath);
        when(pathfinderTaskMock.getTarget()).thenReturn(target);

        // Threshold reached, this time cancel task
        pathfinderTaskHandler.evaluateTasks();
        verify(pathfinderTaskMock).abortTask();
        Assert.assertEquals(0, pathfinderTaskHandler.numberOfTasks());
    }

    @Test
    public void testCancelTask_progress() {
        // Do not cancel task if it has progress
        final WorldPoint start = new WorldPoint(0, 0, 0);
        final WorldPoint someProgressPoint = new WorldPoint(5, 0, 0);
        final WorldPoint target = new WorldPoint(10, 0, 0);
        when(pathfinderTaskMock.getTarget()).thenReturn(target);

        final Path noProgressPath = new Path(Arrays.asList(start, start));
        final Path someProgressPath = new Path(Arrays.asList(start, someProgressPoint));

        // Add task to list of tasks to manage
        pathfinderTaskHandler.add(pathfinderTaskMock);
        Assert.assertEquals(1, pathfinderTaskHandler.numberOfTasks());

        // No progress on the path, but threshold not reached yet, shouldn't cancel
        when(pathfinderTaskMock.getPath()).thenReturn(noProgressPath);
        for (int i = 0; i < TICKS_WITHOUT_PROGRESS_BEFORE_CANCEL - 1; ++i) {
            pathfinderTaskHandler.evaluateTasks();
        }

        // Some progress on the path, counter should be reset, do not abort task
        when(pathfinderTaskMock.getPath()).thenReturn(someProgressPath);
        pathfinderTaskHandler.evaluateTasks();

        verify(pathfinderTaskMock, never()).abortTask();
        Assert.assertEquals(1, pathfinderTaskHandler.numberOfTasks());
    }
}
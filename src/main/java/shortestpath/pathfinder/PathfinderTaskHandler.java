package shortestpath.pathfinder;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;
import shortestpath.ConfigProvider;

import java.util.ArrayList;
import java.util.List;

public class PathfinderTaskHandler {
    private static class PathfinderTaskInfo {
        @Getter
        private final PathfinderTask task;
        @Getter
        @Setter
        private int bestDistance;
        @Getter
        @Setter
        private int ticksSinceBetterPath;

        PathfinderTaskInfo(final PathfinderTask task,
                           final int bestDistance,
                           final int ticksSinceBetterPath) {
            this.task = task;
            this.bestDistance = bestDistance;
            this.ticksSinceBetterPath = ticksSinceBetterPath;
        }
    }

    final ConfigProvider configProvider;
    private final List<PathfinderTaskInfo> pathfinderTasks;

    public PathfinderTaskHandler(final ConfigProvider configProvider) {
        this.configProvider = configProvider;
        this.pathfinderTasks = new ArrayList<>();
    }

    public void evaluateTasks() {
        final List<PathfinderTaskInfo> tasksToRemove = new ArrayList<>();
        for (final PathfinderTaskInfo task : pathfinderTasks) {
            evaluateTaskHasBetterPath(task);
            if (task.getTicksSinceBetterPath() >= configProvider.ticksWithoutProgressBeforeCancelingTask()) {
                task.getTask().abortTask();
                tasksToRemove.add(task);
            }
        }

        // Cannot remove entries while iterating through the list, so collect paths to remove
        // them here instead.
        for (final PathfinderTaskInfo task : tasksToRemove) {
            pathfinderTasks.remove(task);
        }
    }

    public void add(final PathfinderTask task) {
        final PathfinderTaskInfo taskInfo = new PathfinderTaskInfo(task, Integer.MAX_VALUE, 1);
        pathfinderTasks.add(taskInfo);
    }

    public int numberOfTasks() {
        return pathfinderTasks.size();
    }

    private static void evaluateTaskHasBetterPath(final PathfinderTaskInfo task) {
        // Check if last point in Path is closer to target than last time (tick).
        // If not then increment counter.
        final List<WorldPoint> taskPathPoints = task.getTask().getPath().getPoints();
        final WorldPoint bestPathEnd = taskPathPoints.get(taskPathPoints.size() - 1);
        final int distanceFromTarget = task.getTask().getTarget().distanceTo(bestPathEnd);

        if (distanceFromTarget < task.getBestDistance()) {
            task.setBestDistance(distanceFromTarget);
            task.setTicksSinceBetterPath(1);
        }
        else {
            ++task.ticksSinceBetterPath;
        }
    }
}

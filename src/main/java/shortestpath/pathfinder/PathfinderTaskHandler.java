package shortestpath.pathfinder;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;
import shortestpath.ConfigProvider;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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

        PathfinderTaskInfo(final PathfinderTask task) {
            this.task = task;
            this.bestDistance = Integer.MAX_VALUE;
            this.ticksSinceBetterPath = 1;
        }
    }

    final ConfigProvider configProvider;
    final WorldMapProvider worldMapProvider;
    final SectionMapper sectionMapper;
    private final List<PathfinderTaskInfo> pathfinderTasks;

    public PathfinderTaskHandler(final ConfigProvider configProvider, final WorldMapProvider worldMapProvider, final SectionMapper sectionMapper) {
        this.configProvider = configProvider;
        this.worldMapProvider = worldMapProvider;
        this.sectionMapper = sectionMapper;
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

    public PathfinderTask newTask(final WorldPoint start, final WorldPoint target) {
        final PathfinderTask newTask = new PathfinderTask(worldMapProvider.getWorldMap(), configProvider.getPathFinderConfig(), start, target);
        final PathfinderTaskInfo taskInfo = new PathfinderTaskInfo(newTask);
        pathfinderTasks.add(taskInfo);

        final Integer startId = this.sectionMapper.getSectionId(start);
        final Integer targetId = this.sectionMapper.getSectionId(target);
        log.debug("New task. Start section: " + startId + ", target: " + targetId);

        return newTask;
    }

    public void add(final PathfinderTask task) {
        final PathfinderTaskInfo taskInfo = new PathfinderTaskInfo(task);
        pathfinderTasks.add(taskInfo);
    }

    public int numberOfTasks() {
        return pathfinderTasks.size();
    }

    private static void evaluateTaskHasBetterPath(final PathfinderTaskInfo task) {
        // Check if last point in Path is closer to target than last time (tick).
        // If not then increment counter.
        final WorldPoint bestPathDestination = task.getTask().getPath().getDestination();
        final int distanceFromTarget = task.getTask().getTarget().distanceTo(bestPathDestination);

        if (distanceFromTarget < task.getBestDistance()) {
            task.setBestDistance(distanceFromTarget);
            task.setTicksSinceBetterPath(1);
        }
        else {
            ++task.ticksSinceBetterPath;
        }
    }
}

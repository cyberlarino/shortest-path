package shortestpath.pathfinder;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.pathfindertask.ComplexPathfinderTask;
import shortestpath.pathfinder.pathfindertask.PathfinderTask;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.pathfinder.pathfindertask.SimplePathfinderTask;
import shortestpath.utils.Util;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PathfinderTaskHandler {
    private static class PathfinderTaskInfo {
        @Getter
        private final PathfinderTask task;
        @Getter
        @Setter
        private Path lastPath;
        @Getter
        @Setter
        private int ticksSinceBetterPath;

        PathfinderTaskInfo(final PathfinderTask task) {
            this.task = task;
            this.lastPath = task.getPath();
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
        for (final PathfinderTaskInfo taskInfo : pathfinderTasks) {
            final PathfinderTask task = taskInfo.getTask();
            if (task != null && (task.getStatus() == PathfinderTaskStatus.DONE || task.getStatus() == PathfinderTaskStatus.CANCELLED)) {
                tasksToRemove.add(taskInfo);
                continue;
            }

            if (configProvider.ticksWithoutProgressBeforeCancelingTask() < 0) {
                continue;
            }
            evaluateTaskHasBetterPath(taskInfo);
            if (taskInfo.getTicksSinceBetterPath() >= configProvider.ticksWithoutProgressBeforeCancelingTask()) {
                taskInfo.getTask().cancelTask();
                tasksToRemove.add(taskInfo);

                final WorldPoint taskStart = taskInfo.getTask().getStart();
                final WorldPoint taskTarget = taskInfo.getTask().getTarget();
                log.debug("Task from " + Util.worldPointToString(taskStart) + " to " + Util.worldPointToString(taskTarget)
                        + " has made no progress in " + taskInfo.getTicksSinceBetterPath() + " ticks. Cancelling task.");
            }
        }

        // Cannot remove entries while iterating through the list, so collect paths to remove
        // them here instead.
        for (final PathfinderTaskInfo task : tasksToRemove) {
            pathfinderTasks.remove(task);
        }
    }

    @Nullable
    public PathfinderTask newTask(final WorldPoint start, final WorldPoint target) {
        PathfinderTask task;

        final Integer startSection = sectionMapper.getSection(start);
        final Integer targetSection = sectionMapper.getSection(target);
        if (startSection != null && targetSection != null && !startSection.equals(targetSection)) {
            task = new ComplexPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, configProvider.getPathFinderConfig(), start, target);
        }
        else {
            task = new SimplePathfinderTask(worldMapProvider.getWorldMap(), start, target, configProvider.getPathFinderConfig());
        }

        final PathfinderTaskInfo taskInfo = new PathfinderTaskInfo(task);
        pathfinderTasks.add(taskInfo);

        log.debug(String.format("New PathfinderTask started: %s (section %s) to %s (section %s)",
                Util.worldPointToString(start), startSection, Util.worldPointToString(target), targetSection));
        return task;
    }

    public void add(final SimplePathfinderTask task) {
        final PathfinderTaskInfo taskInfo = new PathfinderTaskInfo(task);
        pathfinderTasks.add(taskInfo);
    }

    public int numberOfTasks() {
        return pathfinderTasks.size();
    }

    private static void evaluateTaskHasBetterPath(final PathfinderTaskInfo task) {
        // Check if last point in Path is closer to target than last time (tick).
        // If not then increment counter.
        final Path path = task.getTask().getPath();
        if (path == null) {
            ++task.ticksSinceBetterPath;
            return;
        }

        if (task.getLastPath() == null || !path.equals(task.getLastPath())) {
            task.setTicksSinceBetterPath(1);
            task.setLastPath(path);
        }
        else {
            ++task.ticksSinceBetterPath;
        }
    }
}

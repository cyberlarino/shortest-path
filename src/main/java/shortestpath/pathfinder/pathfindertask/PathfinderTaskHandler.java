package shortestpath.pathfinder.pathfindertask;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.path.Path;
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
        for (final PathfinderTaskInfo task : pathfinderTasks) {
            evaluateTaskHasBetterPath(task);
            if (task.getTicksSinceBetterPath() >= configProvider.ticksWithoutProgressBeforeCancelingTask()) {
                task.getTask().cancelTask();
                tasksToRemove.add(task);
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

        final Integer startSection = sectionMapper.getSectionId(start);
        final Integer targetSection = sectionMapper.getSectionId(target);
        if (startSection != null && targetSection != null && !startSection.equals(targetSection)) {
            task = new ComplexPathfinderTask(worldMapProvider.getWorldMap(), sectionMapper, configProvider.getPathFinderConfig(), start, target);
        }
        else {
            task = new SimplePathfinderTask(worldMapProvider.getWorldMap(), configProvider.getPathFinderConfig(), start, target);
        }

        final PathfinderTaskInfo taskInfo = new PathfinderTaskInfo(task);
        pathfinderTasks.add(taskInfo);

        final Integer startId = this.sectionMapper.getSectionId(start);
        final Integer targetId = this.sectionMapper.getSectionId(target);
        log.debug("New task. Start section: " + startId + ", target: " + targetId);

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

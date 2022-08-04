package shortestpath.pathfinder.pathfindertask;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Path;
import shortestpath.worldmap.WorldMap;
import shortestpath.worldmap.sections.SectionMapper;
import shortestpath.worldmap.sections.SectionPathfinderTask;
import shortestpath.worldmap.sections.SectionRoute;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

@Slf4j
public class ComplexPathfinderTask implements PathfinderTask {
    @Getter
    private final WorldPoint start;
    @Getter
    private final WorldPoint target;
    @Getter
    private PathfinderTaskStatus status = PathfinderTaskStatus.CALCULATING;

    private final SectionPathfinderTask sectionPathfinderTask;
    private final WorldMap worldMap;
    private final PathfinderConfig pathfinderConfig;

    private int bestRouteLength = Integer.MAX_VALUE;
    private boolean updatingTasks = false;
    private int sectionRouteEvaluatedIndex = 0;
    private boolean shouldCancelTask = false;
    private PathfinderRouteTask activeTask = null;

    public ComplexPathfinderTask(final WorldMap worldMap,
                                 final SectionMapper sectionMapper,
                                 final PathfinderConfig pathfinderConfig,
                                 final WorldPoint start,
                                 final WorldPoint target) {
        this.worldMap = worldMap;
        this.pathfinderConfig = pathfinderConfig;
        this.start = start;
        this.target = target;

        this.sectionPathfinderTask = new SectionPathfinderTask(worldMap, sectionMapper, start, target);
        // Start complex task off with a simple task, which should be replaced by better path if one found
//        this.pathTask = new SimplePathfinderTaskCollection(new SectionRoute(start, target, new ArrayList<>()), worldMap, pathfinderConfig);
        new Thread(this).start();
    }
    @Nullable
    public Path getPath() {
        if (activeTask == null) {
            return null;
        }
        return activeTask.getPath();
    }

    public void cancelTask() {
        sectionPathfinderTask.cancelTask();
        shouldCancelTask = true;
    }

    public void run() {
        waitForSectionPathfinderTaskCompletion();

        List<SectionRoute> routes = sectionPathfinderTask.getRoutes();
        final int totalRoutes = routes.size();
        routes.sort(Comparator.comparingInt(SectionRoute::length));
        List<PathfinderRouteTask> tasks = new ArrayList<>();

        final BooleanSupplier isStillCalculating = () -> {
            if (!routes.isEmpty()) {
                return true;
            }
            if (activeTask != null && activeTask.getStatus() == PathfinderTaskStatus.CALCULATING) {
                return true;
            }
            return false;
        };

        int routesExplored = 0;
        final int MAX_CONCURRENT_TASKS = 5;
        do {
            final List<SectionRoute> routesToRemove = new LinkedList<>();
            final List<PathfinderRouteTask> tasksToRemove = new LinkedList<>();

            // Make new tasks if room
            for (final SectionRoute route : routes) {
                if (tasks.size() >= MAX_CONCURRENT_TASKS) {
                    break;
                }
                final PathfinderRouteTask routeTask = new PathfinderRouteTask(route, worldMap, pathfinderConfig);
                routesToRemove.add(route);
                tasks.add(routeTask);
                ++routesExplored;
            }

            // Evaluate tasks
            for (final PathfinderRouteTask routeTask : tasks) {
                final PathfinderTaskStatus taskStatus = routeTask.getStatus();
                switch (taskStatus) {
                    case DONE:
                    case CANCELLED:
                        tasksToRemove.add(routeTask);
                    case CALCULATING:
                        shouldTaskReplaceActiveTask(routeTask);
                        break;
                    default:
                        throw new RuntimeException("Unexpected task status gotten: " + taskStatus);
                }
            }

            // Since elements cannot be removed from list while iterating said list, instead collect each element to
            // remove and remove them here.
            for (final SectionRoute route : routesToRemove) {
                routes.remove(route);
            }
            for (final PathfinderRouteTask task : tasksToRemove) {
                tasks.remove(task);
            }

            if (status == PathfinderTaskStatus.CALCULATING && activeTask != null
                    && activeTask.getStatus() == PathfinderTaskStatus.DONE && !routes.isEmpty()) {
                status = PathfinderTaskStatus.LOOKING_FOR_BETTER_PATH;
            }

            if (shouldCancelTask) {
                tasks.forEach(PathfinderRouteTask::cancelTask);
            }
        } while (isStillCalculating.getAsBoolean() && !shouldCancelTask);

        if (shouldCancelTask) {
            status = PathfinderTaskStatus.CANCELLED;
        }
        else {
            status = PathfinderTaskStatus.DONE;
        }

        final String taskStatusInfo = (shouldCancelTask ? "cancelled" : "finished calculating");
        log.debug("PathfinderTask done (" + taskStatusInfo + "). Routes total: " + totalRoutes + ", routes explored: " + routesExplored);
    }

    private void waitForSectionPathfinderTaskCompletion() {
        do {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (Exception ignore) {
            }
        } while (sectionPathfinderTask.getStatus() == PathfinderTaskStatus.CALCULATING);
    }

    private void shouldTaskReplaceActiveTask(final PathfinderRouteTask task) {
        boolean shouldTaskReplaceActiveTask = false;
        if (activeTask == null) {
            shouldTaskReplaceActiveTask = true;
        }
        else {
            final PathfinderTaskStatus activeTaskStatus = activeTask.getStatus();
            final PathfinderTaskStatus taskStatus = task.getStatus();

            if (taskStatus == PathfinderTaskStatus.CANCELLED) {
            }
            else if (activeTaskStatus == PathfinderTaskStatus.CANCELLED) {
                shouldTaskReplaceActiveTask = true;
            }
            else if (activeTaskStatus != PathfinderTaskStatus.DONE && taskStatus == PathfinderTaskStatus.DONE) {
                shouldTaskReplaceActiveTask = true;
            }
            else if (activeTaskStatus == PathfinderTaskStatus.DONE && task.getStatus() == PathfinderTaskStatus.DONE
                    && Objects.requireNonNull(task.getPath()).getMovements().size() < task.getPath().getMovements().size()) {
                shouldTaskReplaceActiveTask = true;
            }

        }

        if (shouldTaskReplaceActiveTask) {
            activeTask = task;
        }
    }
}

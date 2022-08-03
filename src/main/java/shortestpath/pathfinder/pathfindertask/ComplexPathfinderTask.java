package shortestpath.pathfinder.pathfindertask;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMap;
import shortestpath.worldmap.sections.SectionMapper;
import shortestpath.worldmap.sections.SectionPathfinderTask;
import shortestpath.worldmap.sections.SectionRoute;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ComplexPathfinderTask implements PathfinderTask {
    @Getter
    private final WorldPoint start;
    @Getter
    private final WorldPoint target;
    private PathfinderTaskStatus status = PathfinderTaskStatus.CALCULATING;

    private final SectionPathfinderTask sectionPathfinderTask;
    private final WorldMap worldMap;
    private final PathfinderConfig pathfinderConfig;

    private int bestRouteLength = Integer.MAX_VALUE;
    private boolean updatingTasks = false;
    private int sectionRouteEvaluatedIndex = 0;
    private boolean shouldCancelTask = false;
    private SimplePathfinderTaskCollection simplePathfinderTaskCollection;

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
        new Thread(this).start();
    }

    public PathfinderTaskStatus getStatus() {
        if (status == PathfinderTaskStatus.CANCELLED) {
            return PathfinderTaskStatus.CANCELLED;
        }

        if (simplePathfinderTaskCollection == null || updatingTasks) {
            return PathfinderTaskStatus.CALCULATING;
        }

        if (simplePathfinderTaskCollection.getSectionTasks().stream().anyMatch(task -> task.getStatus() == PathfinderTaskStatus.CANCELLED)
                || sectionPathfinderTask.getStatus() == PathfinderTaskStatus.CANCELLED) {
            cancelTask();
            return PathfinderTaskStatus.CANCELLED;
        }

        if (simplePathfinderTaskCollection.allTasksDone()) {
            if (sectionPathfinderTask.getStatus() == PathfinderTaskStatus.CALCULATING) {
                return PathfinderTaskStatus.LOOKING_FOR_BETTER_PATH;
            }
            return PathfinderTaskStatus.DONE;
        }

        return PathfinderTaskStatus.CALCULATING;
    }

    @Nullable
    public Path getPath() {
        if (simplePathfinderTaskCollection == null || simplePathfinderTaskCollection.getSectionTasks().isEmpty()) {
            return null;
        }
        else if (simplePathfinderTaskCollection.getSectionTasks().size() == 1) {
            return simplePathfinderTaskCollection.getSectionTasks().get(0).getPath();
        }
        else {
            final List<Movement> movements = new LinkedList<>();
            List<SimplePathfinderTask> routeTasks = simplePathfinderTaskCollection.getSectionTasks();
            List<Transport> routeTransports = simplePathfinderTaskCollection.getRoute().getTransports();
            for (int i = 0; i < routeTasks.size(); ++i) {
                final Path routeTaskPath = routeTasks.get(i).getPath();
                if (routeTaskPath == null) {
                    return null;
                }
                movements.addAll(routeTaskPath.getMovements());
                if (i != routeTasks.size() - 1) {
                    movements.add(routeTransports.get(i));
                }
            }
            return new Path(movements);
        }
    }

    public void cancelTask() {
        if (simplePathfinderTaskCollection != null) {
            simplePathfinderTaskCollection.cancelTasks();
        }
        sectionPathfinderTask.cancelTask();
        shouldCancelTask = true;
    }

    public void run() {
        final BooleanSupplier isStillCalculating = () -> {
            status = getStatus();
            if (shouldCancelTask) {
                return false;
            }
            return status == PathfinderTaskStatus.CALCULATING || status == PathfinderTaskStatus.LOOKING_FOR_BETTER_PATH;
        };

        while (isStillCalculating.getAsBoolean()) {
            SectionRoute newRoute = null;
            final int indexToEvaluateTo = sectionPathfinderTask.getRoutes().size();
            for (int i = sectionRouteEvaluatedIndex; i < sectionPathfinderTask.getRoutes().size(); ++i) {
                final SectionRoute route = sectionPathfinderTask.getRoutes().get(i);
                if (route.length() < bestRouteLength) {
                    newRoute = route;
                    bestRouteLength = route.length();
                }
            }
            sectionRouteEvaluatedIndex = indexToEvaluateTo;

            if (newRoute != null) {
                updatingTasks = true;
                if (simplePathfinderTaskCollection != null) {
                    simplePathfinderTaskCollection.cancelTasks();
                }
                simplePathfinderTaskCollection = new SimplePathfinderTaskCollection(newRoute, worldMap, pathfinderConfig);
                updatingTasks = false;
            }
        }
    }
}

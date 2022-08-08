package shortestpath.pathfinder.pathfindertask;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMap;
import shortestpath.worldmap.sections.MovementSections;
import shortestpath.worldmap.sections.SectionMapper;
import shortestpath.worldmap.sections.SectionRoute;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class PathfinderRouteTask implements PathfinderTask {
    @Getter
    private final SectionRoute route;
    private final WorldMap worldMap;
    private final SectionMapper sectionMapper;

    private final List<SimplePathfinderTask> sectionTasks;
    private PathfinderTaskStatus status = PathfinderTaskStatus.CALCULATING;
    private Path finalPath = null;

    public PathfinderRouteTask(final SectionRoute route, final WorldMap worldMap, final SectionMapper sectionMapper, final PathfinderConfig pathfinderConfig) {
        this.sectionTasks = new ArrayList<>();
        this.route = route;
        this.worldMap = worldMap;
        this.sectionMapper = sectionMapper;

        final BiFunction<WorldPoint, WorldPoint, SimplePathfinderTask> createTask = (start, target) -> {
            return new SimplePathfinderTask(worldMap, start, target, pathfinderConfig, getTransportPredicate());
        };

        if (route.getTransports().isEmpty()) {
            sectionTasks.add(createTask.apply(route.getOrigin(), route.getDestination()));
        } else {
            final Transport firstTransport = route.getTransports().get(0);
            final Transport lastTransport = route.getTransports().get(route.getTransports().size() - 1);
            sectionTasks.add(createTask.apply(route.getOrigin(), firstTransport.getOrigin()));

            for (int i = 0; i < route.getTransports().size() - 1; ++i) {
                final Transport currentTransport = route.getTransports().get(i);
                final Transport nextTransport = route.getTransports().get(i + 1);
                sectionTasks.add(createTask.apply(currentTransport.getDestination(), nextTransport.getOrigin()));
            }

            sectionTasks.add(createTask.apply(lastTransport.getDestination(), route.getDestination()));
        }
    }

    public WorldPoint getStart() {
        return route.getOrigin();
    }

    public WorldPoint getTarget() {
        return route.getDestination();
    }

    public PathfinderTaskStatus getStatus() {
        if (status == PathfinderTaskStatus.DONE || status == PathfinderTaskStatus.CANCELLED) {
            return status;
        }

        if (sectionTasks.stream().anyMatch(task -> task.getStatus() == PathfinderTaskStatus.CANCELLED)) {
            cancelTask();
            return PathfinderTaskStatus.CANCELLED;
        }

        final boolean allTasksDone = sectionTasks.stream().allMatch(task -> task.getStatus() == PathfinderTaskStatus.DONE);
        return (status = (allTasksDone ? PathfinderTaskStatus.DONE : PathfinderTaskStatus.CALCULATING));
    }

    @Nullable
    public Path getPath() {
        if (finalPath != null) {
            return finalPath;
        }

        if (sectionTasks.isEmpty()) {
            return null;
        } else if (sectionTasks.size() == 1) {
            return sectionTasks.get(0).getPath();
        } else {
            final List<Movement> movements = new LinkedList<>();
            final List<SimplePathfinderTask> routeTasks = sectionTasks;
            final List<Transport> routeTransports = route.getTransports();
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

            final Path path = new Path(movements);
            if (status == PathfinderTaskStatus.DONE || status == PathfinderTaskStatus.CANCELLED) {
                finalPath = path;
            }
            return path;
        }
    }

    public void cancelTask() {
        sectionTasks.forEach(SimplePathfinderTask::cancelTask);
        status = PathfinderTaskStatus.CANCELLED;
    }

    public void run() {
    }

    private Predicate<Transport> getTransportPredicate() {
        return (transport) -> {
            final MovementSections transportSections = sectionMapper.getSection(transport);
            if (Objects.equals(transportSections.getOriginSection(), transportSections.getDestinationSection())) {
               return true;
            }
            if (transport.getAgilityLevelRequired() != 0) {
                return true;
            }
            return false;
        };
    }
}

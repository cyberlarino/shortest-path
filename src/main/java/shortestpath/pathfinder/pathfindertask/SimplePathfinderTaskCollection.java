package shortestpath.pathfinder.pathfindertask;

import lombok.Getter;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMap;
import shortestpath.worldmap.sections.SectionRoute;

import java.util.ArrayList;
import java.util.List;

public class SimplePathfinderTaskCollection {
    @Getter
    private final List<SimplePathfinderTask> sectionTasks;
    @Getter
    private final SectionRoute route;

    public SimplePathfinderTaskCollection(final SectionRoute route, final WorldMap worldMap, final PathfinderConfig pathfinderConfig) {
        this.sectionTasks = new ArrayList<>();
        this.route = route;

        if (route.getTransports().isEmpty()) {
        }
        else if (route.getTransports().size() == 1) {
            sectionTasks.add(new SimplePathfinderTask(worldMap, pathfinderConfig, route.getOrigin(), route.getDestination()));
        }
        else {
            final Transport firstTransport = route.getTransports().get(0);
            final Transport lastTransport = route.getTransports().get(route.getTransports().size() - 1);
            sectionTasks.add(new SimplePathfinderTask(worldMap, pathfinderConfig, route.getOrigin(), firstTransport.getOrigin()));

            for (int i = 0; i < route.getTransports().size() - 1; ++i) {
                final Transport currentTransport = route.getTransports().get(i);
                final Transport nextTransport = route.getTransports().get(i + 1);
                sectionTasks.add(new SimplePathfinderTask(worldMap, pathfinderConfig, currentTransport.getDestination(), nextTransport.getOrigin()));
            }

            sectionTasks.add(new SimplePathfinderTask(worldMap, pathfinderConfig, lastTransport.getDestination(), route.getDestination()));
        }
    }

    public boolean allTasksDone() {
        return sectionTasks.stream().allMatch(task -> task.getStatus() == PathfinderTaskStatus.DONE);
    }

    public void cancelTasks() {
        sectionTasks.forEach(SimplePathfinderTask::cancelTask);
    }
}

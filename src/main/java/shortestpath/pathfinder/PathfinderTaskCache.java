package shortestpath.pathfinder;

import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.pathfindertask.PathfinderTask;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PathfinderTaskCache {
    final Set<PathfinderTask> tasks;

    public PathfinderTaskCache() {
        this.tasks = new HashSet<>();
    }

    public synchronized void addTask(final PathfinderTask task) {
        final WorldPoint start = task.getStart();
        final WorldPoint target = task.getTarget();
        final boolean isTaskAlreadyInCache = tasks.stream().anyMatch((cachedTask) -> start.equals(task.getStart()) && target.equals(task.getTarget()));
        if (!isTaskAlreadyInCache) {
            tasks.add(task);
        }
    }

    @Nullable
    public PathfinderTask getCachedTask(final WorldPoint start, final WorldPoint target) {
        final Optional<PathfinderTask> result = tasks.stream()
                .filter((task) -> start.equals(task.getStart()) && target.equals(task.getTarget()))
                .findAny();
        return result.orElse(null);
    }
}

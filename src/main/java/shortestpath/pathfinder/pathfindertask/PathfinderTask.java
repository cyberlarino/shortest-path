package shortestpath.pathfinder.pathfindertask;

import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.path.Path;

public interface PathfinderTask extends Runnable {
    public WorldPoint getStart();
    public WorldPoint getTarget();
    public Path getPath();
    public PathfinderTaskStatus getStatus();
    public void cancelTask();
}

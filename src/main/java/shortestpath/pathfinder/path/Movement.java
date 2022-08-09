package shortestpath.pathfinder.path;

import net.runelite.api.coords.WorldPoint;

import javax.annotation.Nullable;

public interface Movement {
    public MovementType getType();
    public WorldPoint getOrigin();
    public WorldPoint getDestination();
}

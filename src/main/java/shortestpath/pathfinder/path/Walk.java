package shortestpath.pathfinder.path;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

public class Walk implements Movement {
    @Getter
    private final WorldPoint origin;
    @Getter
    private final WorldPoint destination;

    public Walk(final WorldPoint origin, final WorldPoint destination) {
        this.origin = origin;
        this.destination = destination;
    }

    @Override
    public MovementType getType() {
        return MovementType.WALK;
    }
}

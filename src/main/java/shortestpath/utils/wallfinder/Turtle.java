package shortestpath.utils.wallfinder;

import lombok.Getter;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import shortestpath.utils.OrdinalDirection;
import shortestpath.worldmap.WorldMap;

public class Turtle {
    private final WorldMap worldMap;
    @Getter
    private WorldPoint position;
    @Getter
    private OrdinalDirection facingDirection;

    Turtle(final WorldMap worldMap, final WorldPoint position) {
        this(worldMap, position, OrdinalDirection.NORTH);
    }

    Turtle(final WorldMap worldMap, final WorldPoint position, final OrdinalDirection facingDirection) {
        this.worldMap = worldMap;
        this.position = position;
        this.facingDirection = facingDirection;

        if (worldMap.isBlocked(position)) {
            throw new RuntimeException("Turtle position is blocked.");
        }
    }

    public boolean check(final Direction direction) {
        final OrdinalDirection ordinalDirection = OrdinalDirection.applyDirection(facingDirection, direction);
        return worldMap.checkDirection(position, ordinalDirection);
    }

    public boolean move(final Direction direction) {
        if (!check(direction)) {
            return false;
        }
        final OrdinalDirection ordinalDirection = OrdinalDirection.applyDirection(facingDirection, direction);
        final Point directionPoint = ordinalDirection.toPoint();
        position = position.dx(directionPoint.getX()).dy(directionPoint.getY());
        return true;
    }

    public OrdinalDirection turn(final Direction direction) {
        final OrdinalDirection ordinalDirection = OrdinalDirection.applyDirection(facingDirection, direction);
        facingDirection = ordinalDirection;
        return ordinalDirection;
    }
}

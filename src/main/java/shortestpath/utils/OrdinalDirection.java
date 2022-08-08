package shortestpath.utils;

import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import shortestpath.utils.wallfinder.Direction;

import java.util.Arrays;
import java.util.List;

public enum OrdinalDirection {
    NORTH {
        public Point toPoint() {
            return new Point(0, 1);
        }
    },
    NORTH_EAST {
        public Point toPoint() {
            return new Point(1, 1);
        }
    },
    EAST {
        public Point toPoint() {
            return new Point(1, 0);
        }
    },
    SOUTH_EAST {
        public Point toPoint() {
            return new Point(1, -1);
        }
    },
    SOUTH {
        public Point toPoint() {
            return new Point(0, -1);
        }
    },
    SOUTH_WEST {
        public Point toPoint() {
            return new Point(-1, -1);
        }
    },
    WEST {
        public Point toPoint() {
            return new Point(-1, 0);
        }
    },
    NORTH_WEST {
        public Point toPoint() {
            return new Point(-1, 1);
        }
    };

    public static final List<OrdinalDirection> CARDINAL_DIRECTIONS = Arrays.asList(OrdinalDirection.NORTH,
            OrdinalDirection.EAST,
            OrdinalDirection.SOUTH,
            OrdinalDirection.WEST);

    public abstract Point toPoint();

    public static OrdinalDirection fromPoint(final Point point) {
        return Arrays.stream(OrdinalDirection.values())
                        .filter(direction -> direction.toPoint().equals(point))
                        .findAny()
                        .orElseThrow(() -> new RuntimeException("Couldn't convert Point to Direction - " + point));
    }

    public static OrdinalDirection applyDirection(final OrdinalDirection ordinalDirection, final Direction direction) {
        return OrdinalDirection.values()[(ordinalDirection.ordinal() + direction.ordinal() * 2) % OrdinalDirection.values().length];
    }

    public static OrdinalDirection getDirection(final WorldPoint origin, final WorldPoint destination) {
        final int x = Math.min(Math.max(destination.getX() - origin.getX(), -1), 1);
        final int y = Math.min(Math.max(destination.getY() - origin.getY(), -1), 1);
        return fromPoint(new Point(x, y));
    }
}

package shortestpath.pathfinder;

import java.awt.Point;
import java.util.Arrays;
import java.util.List;

public enum OrdinalDirection {
    NORTH {
        public Point toPoint() {
            return new Point(0, 1);
        }
    },
    EAST {
        public Point toPoint() {
            return new Point(1, 0);
        }
    },
    SOUTH {
        public Point toPoint() {
            return new Point(0, -1);
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
    },
    NORTH_EAST {
        public Point toPoint() {
            return new Point(1, 1);
        }
    },
    SOUTH_EAST {
        public Point toPoint() {
            return new Point(1, -1);
        }
    },
    SOUTH_WEST {
        public Point toPoint() {
            return new Point(-1, -1);
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
}

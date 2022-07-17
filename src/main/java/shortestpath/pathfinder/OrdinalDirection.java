package shortestpath.pathfinder;

import java.awt.Point;

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

    public abstract Point toPoint();

    public static OrdinalDirection fromPoint(final Point point) {
        if (point.equals(new Point(0, 1))) {
            return OrdinalDirection.NORTH;
        } else if (point.equals(new Point(1, 0))) {
            return OrdinalDirection.EAST;
        } else if (point.equals(new Point(0, -1))) {
            return OrdinalDirection.SOUTH;
        } else if (point.equals(new Point(-1, 0))) {
            return OrdinalDirection.WEST;
        } else if (point.equals(new Point(-1, 1))) {
            return OrdinalDirection.NORTH_WEST;
        } else if (point.equals(new Point(1, 1))) {
            return OrdinalDirection.NORTH_EAST;
        } else if (point.equals(new Point(1, -1))) {
            return OrdinalDirection.SOUTH_EAST;
        } else if (point.equals(new Point(-1, -1))) {
            return OrdinalDirection.SOUTH_WEST;
        }
        return null;
    }
}

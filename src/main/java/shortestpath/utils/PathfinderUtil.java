package shortestpath.utils;

import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.MovementType;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.path.Walk;
import shortestpath.pathfinder.pathfindertask.PathfinderTask;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.worldmap.WorldMap;
import shortestpath.worldmap.sections.SectionPathfinderTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PathfinderUtil {
    private static final long DEFAULT_TIMEOUT_SECONDS = 5;

    public static boolean waitForTaskCompletion(final PathfinderTask task) {
        return waitForTaskCompletion(task, DEFAULT_TIMEOUT_SECONDS);
    }

    public static boolean waitForTaskCompletion(final PathfinderTask task, final long timeout) {
        long startTime = System.nanoTime();
        while (task.getStatus() != PathfinderTaskStatus.DONE) {
            if (timeout > 0 && (System.nanoTime() - startTime) >= TimeUnit.SECONDS.toNanos(timeout)) {
                return false;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    public static boolean waitForTaskCompletion(final SectionPathfinderTask task) {
        return waitForTaskCompletion(task, DEFAULT_TIMEOUT_SECONDS);
    }

    public static boolean waitForTaskCompletion(final SectionPathfinderTask task, final long timeout) {
        long startTime = System.nanoTime();
        while (task.getStatus() != PathfinderTaskStatus.DONE) {
            if ((System.nanoTime() - startTime) >= TimeUnit.SECONDS.toNanos(timeout)) {
                return false;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (Exception ignore) {
            }
        }
        return true;
    }

    public static boolean isPathValid(final WorldMap worldMap, final Path path) {
        final Predicate<Movement> movementTransportOrNotBlocked = movement -> {
            final WorldPoint movementOrigin = movement.getOrigin();
            final WorldPoint movementDestination = movement.getDestination();
            if (!worldMap.isBlocked(movementDestination)) {
                return true;
            }

            return worldMap.getTransports(movementOrigin).size() != 0;
        };
        if (!path.getMovements().stream().allMatch(movementTransportOrNotBlocked)) {
            return false;
        }

        // Check if all walks possible
        final Predicate<Walk> walkPossible = (walk) -> {
            if (walk.getOrigin().distanceTo(walk.getDestination()) > 2) {
                return false;
            }
            if (walk.getOrigin().equals(walk.getDestination())) {
                return true;
            }
            final OrdinalDirection direction = OrdinalDirection.getDirection(walk.getOrigin(), walk.getDestination());
            return worldMap.checkDirection(walk.getOrigin(), direction);
        };
        final List<Walk> walks = path.getMovements().stream()
                .filter(movement -> movement.getType().equals(MovementType.WALK))
                .map(movement -> (Walk) movement)
                .collect(Collectors.toList());
        final boolean areAllWalksPossible = walks.stream().allMatch(walkPossible);
        if (!areAllWalksPossible) {
            return false;
        }

        //  Check if all Movements next to each other are possible
        for (int i = 0; i < path.getMovements().size() - 1; ++i) {
            if (!path.getMovements().get(i).getDestination().equals(path.getMovements().get(i + 1).getOrigin())) {
                return false;
            }

            final WorldPoint point = path.getMovements().get(i).getDestination();
            final WorldPoint nextPoint = path.getMovements().get(i + 1).getDestination();

            if (point.distanceTo(nextPoint) > 1) {
                // A 'jump' in the path, either transport was used, or path isn't connected properly
                boolean pathTransportUsed = false;
                for (Transport transport : worldMap.getTransports(point)) {
                    if (transport.getDestination().equals(nextPoint)) {
                        pathTransportUsed = true;
                        break;
                    }
                }
                if (!pathTransportUsed) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isPointInsideRectangle(final WorldPoint rectangleCorner,
                                                 final WorldPoint rectangleOppositeCorner,
                                                 final WorldPoint point) {
        if (rectangleCorner.getPlane() != rectangleOppositeCorner.getPlane()) {
            throw new RuntimeException("Rectangle corners not in same plane - " + rectangleCorner + ", " + rectangleOppositeCorner);
        }

        final WorldPoint upperLeftRectangleCorner = new WorldPoint(Math.min(rectangleCorner.getX(), rectangleOppositeCorner.getX()),
                Math.max(rectangleCorner.getY(), rectangleOppositeCorner.getY()),
                rectangleCorner.getPlane());

        final WorldPoint lowerRightRectangleCorner = new WorldPoint(Math.max(rectangleCorner.getX(), rectangleOppositeCorner.getX()),
                Math.min(rectangleCorner.getY(), rectangleOppositeCorner.getY()),
                rectangleCorner.getPlane());

        return (point.getX() >= upperLeftRectangleCorner.getX() && point.getX() <= lowerRightRectangleCorner.getX()) &&
                (point.getY() >= lowerRightRectangleCorner.getY() && point.getY() <= upperLeftRectangleCorner.getY()) &&
                point.getPlane() == upperLeftRectangleCorner.getPlane();
    }

    public static Predicate<WorldPoint> isPointInsideRectanglePredicate(final WorldPoint rectangleCorner,
                                                                        final WorldPoint rectangleOppositeCorner) {
        return (point) -> isPointInsideRectangle(rectangleCorner, rectangleOppositeCorner, point);
    }

    public static Set<WorldPoint> getPointsInsideRectangle(final WorldPoint rectangleCorner,
                                                           final WorldPoint rectangleOppositeCorner) {
        if (rectangleCorner.getPlane() != rectangleOppositeCorner.getPlane()) {
            throw new RuntimeException("Rectangle corners not in same plane - " + rectangleCorner + ", " + rectangleOppositeCorner);
        }

        final int lowerX = Math.min(rectangleCorner.getX(), rectangleOppositeCorner.getX());
        final int lowerY = Math.min(rectangleCorner.getY(), rectangleOppositeCorner.getY());
        final int upperX = Math.max(rectangleCorner.getX(), rectangleOppositeCorner.getX()) + 1;
        final int upperY = Math.max(rectangleCorner.getY(), rectangleOppositeCorner.getY()) + 1;
        final int plane = rectangleCorner.getPlane();

        final Set<WorldPoint> rectangle = new HashSet<>();
        for (int x = lowerX; x < upperX; ++x) {
            for (int y = lowerY; y < upperY; ++y) {
                rectangle.add(new WorldPoint(x, y, plane));
            }
        }
        return rectangle;
    }

    public static Set<Transport> getTransportsInsideRectangle(final WorldPoint rectangleCorner,
                                                              final WorldPoint rectangleOppositeCorner,
                                                              final WorldMap worldMap) {
        final Predicate<WorldPoint> pointInsideRectangle = isPointInsideRectanglePredicate(rectangleCorner, rectangleOppositeCorner);
        final Predicate<Transport> isTransportInsideRectangle =
                (transport -> pointInsideRectangle.test(transport.getOrigin()) || pointInsideRectangle.test(transport.getDestination()));
        return worldMap.getTransports().stream().filter(isTransportInsideRectangle).collect(Collectors.toSet());
    }
}

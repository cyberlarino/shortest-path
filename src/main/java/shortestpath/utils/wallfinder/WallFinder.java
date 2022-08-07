package shortestpath.utils.wallfinder;

import net.runelite.api.coords.WorldPoint;
import org.graalvm.compiler.core.phases.EconomyLowTier;
import shortestpath.pathfinder.OrdinalDirection;
import shortestpath.worldmap.WorldMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class WallFinder {
    final WorldMap worldMap;

    public WallFinder(final WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    @Nullable
    public Wall findFirstWall(final WorldPoint point, final OrdinalDirection direction) {
        if (worldMap.isBlocked(point)) {
            System.out.println("Point blocked: " + point);
            return null;
        }

        final Turtle turtle = new Turtle(worldMap, point, direction);
        // Move until wall has been found
        while (turtle.move(Direction.FORWARD)) {
        }
        while (!turtle.check(Direction.FORWARD)) {
            turtle.turn(Direction.LEFT);
        }


        final WorldPoint wallStart = turtle.getPosition();
        int clockwiseTurns = 0;
        final BiPredicate<Turtle, Integer> isWallStartReached = (turtle1, turns) -> {
            return turtle1.getPosition().equals(wallStart) && Math.abs(turns) == 4;
        };

        final List<WorldPoint> worldPointList = new ArrayList<>();
        worldPointList.add(turtle.getPosition());
        while (true) {
            if (turtle.check(Direction.RIGHT)) {
                turtle.turn(Direction.RIGHT);
                ++clockwiseTurns;
            } else if (!turtle.check(Direction.FORWARD)) {
                while (!turtle.check(Direction.FORWARD)) {
                    turtle.turn(Direction.LEFT);
                    --clockwiseTurns;
                }
            }

            if (isWallStartReached.test(turtle, clockwiseTurns)) {
                break;
            }

            final boolean moveSuccessful = turtle.move(Direction.FORWARD);
            if (moveSuccessful) {
                worldPointList.add(turtle.getPosition());
            }
        }

        if (worldPointList.size() > 1 && wallStart.equals(worldPointList.get(worldPointList.size() - 1))) {
            worldPointList.remove(worldPointList.size() - 1);
        }

        final boolean isIsland = clockwiseTurns > 0;
        return new Wall(worldPointList, isIsland);
    }

    @Nullable
    public Wall findSectionWall(WorldPoint point) {
        if (worldMap.isBlocked(point)) {
            return null;
        }

        Wall wall = null;
        do {
            wall = findFirstWall(point, OrdinalDirection.NORTH);
            assert wall != null;

            WorldPoint uppermostPoint = point;
            for (final WorldPoint pointIterator : wall) {
                if (pointIterator.getY() > uppermostPoint.getY()) {
                    uppermostPoint = pointIterator;
                }
            }
            point = uppermostPoint;
        } while (wall.isIsland());
        return wall;
    }
}

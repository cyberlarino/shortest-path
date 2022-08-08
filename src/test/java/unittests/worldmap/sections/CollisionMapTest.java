package unittests.worldmap.sections;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import shortestpath.utils.OrdinalDirection;
import shortestpath.worldmap.WorldMapProvider;

import java.util.Arrays;
import java.util.Collection;

public class CollisionMapTest {
    private static WorldMapProvider worldMapProvider = new WorldMapProvider();

    @RunWith(Parameterized.class)
    public static class CheckDirectionTest {
        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    // point, direction, expectedResult
                    {new WorldPoint(3159, 3500, 0), OrdinalDirection.NORTH, true},
                    {new WorldPoint(3161, 3498, 0), OrdinalDirection.NORTH, false},
                    {new WorldPoint(3159, 3501, 0), OrdinalDirection.EAST, true},
                    {new WorldPoint(3159, 3499, 0), OrdinalDirection.EAST, false},
                    {new WorldPoint(3159, 3500, 0), OrdinalDirection.SOUTH, true},
                    {new WorldPoint(3159, 3499, 0), OrdinalDirection.SOUTH, false},
                    {new WorldPoint(3159, 3500, 0), OrdinalDirection.WEST, true},
                    {new WorldPoint(3161, 3498, 0), OrdinalDirection.WEST, false},

                    {new WorldPoint(3159, 3500, 0), OrdinalDirection.NORTH_EAST, false},
                    {new WorldPoint(3161, 3501, 0), OrdinalDirection.SOUTH_EAST, false},
                    {new WorldPoint(3162, 3499, 0), OrdinalDirection.SOUTH_WEST, false},
                    {new WorldPoint(3162, 3500, 0), OrdinalDirection.NORTH_WEST, false},
            });
        }

        private final WorldPoint point;
        private final OrdinalDirection direction;
        private final boolean expectedResult;

        public CheckDirectionTest(final WorldPoint point,
                                  final OrdinalDirection direction,
                                  final boolean expectedResult) {
            this.point = point;
            this.direction = direction;
            this.expectedResult = expectedResult;
        }

        @Test
        public void testCheckDirection() {
            final boolean result = worldMapProvider.getWorldMap().checkDirection(point, direction);
            Assert.assertEquals(expectedResult, result);
        }
    }
}

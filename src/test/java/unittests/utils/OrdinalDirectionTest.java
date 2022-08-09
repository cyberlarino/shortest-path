package unittests.utils;

import static org.junit.Assert.assertEquals;

import net.runelite.api.Point;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import shortestpath.utils.OrdinalDirection;
import shortestpath.utils.wallfinder.Direction;

import java.util.Arrays;
import java.util.Collection;

public class OrdinalDirectionTest {
    @Test
    public void translateFromPointToOrdinalDirection() {
        final Point inputPoint = new Point(0, -1);

        final OrdinalDirection result = OrdinalDirection.fromPoint(inputPoint);
        assertEquals(OrdinalDirection.SOUTH, result);
    }

    @Test
    public void fromPoint_throwsOnInvalidArgument() {
        final Point inputPoint = new Point(10, -10);
        Assert.assertThrows(RuntimeException.class, () -> OrdinalDirection.fromPoint(inputPoint));
    }

    @Test
    public void fromPoint_throwsOnNullArgument() {
        Assert.assertThrows(RuntimeException.class, () -> OrdinalDirection.fromPoint(null));
    }

    @RunWith(Parameterized.class)
    public static class ApplyDirectionTest {
        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    // ordinalDirection, direction, expectedOrdinalDirection
                    {OrdinalDirection.NORTH, Direction.FORWARD, OrdinalDirection.NORTH},
                    {OrdinalDirection.EAST, Direction.RIGHT, OrdinalDirection.SOUTH},
                    {OrdinalDirection.WEST, Direction.LEFT, OrdinalDirection.SOUTH},
                    {OrdinalDirection.WEST, Direction.LEFT, OrdinalDirection.SOUTH},
                    {OrdinalDirection.SOUTH_EAST, Direction.LEFT, OrdinalDirection.NORTH_EAST},
                    {OrdinalDirection.SOUTH_WEST, Direction.BACK, OrdinalDirection.NORTH_EAST}
            });
        }

        private final OrdinalDirection ordinalDirection;
        private final Direction direction;
        private final OrdinalDirection expectedOrdinalDirection;

        public ApplyDirectionTest(final OrdinalDirection ordinalDirection,
                                  final Direction direction,
                                  final OrdinalDirection expectedOrdinalDirection) {
            this.ordinalDirection = ordinalDirection;
            this.direction = direction;
            this.expectedOrdinalDirection = expectedOrdinalDirection;
        }

        @Test
        public void testApplyDirection() {
            final OrdinalDirection result = OrdinalDirection.applyDirection(ordinalDirection, direction);
            Assert.assertEquals(expectedOrdinalDirection, result);
        }
    }

}
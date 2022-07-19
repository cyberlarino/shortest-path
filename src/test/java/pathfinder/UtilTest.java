package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import shortestpath.pathfinder.OrdinalDirection;
import shortestpath.utils.Util;

import java.util.Arrays;
import java.util.Collection;

public class UtilTest {
    @RunWith(Parameterized.class)
    public static class IsPointInsideRectangleParameterizedTest {
        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    // rectangleCorner, rectangleCornerOpposite, point, expectedResult

                    // 'Normal' rectangles: first point is upper left corner, second point is bottom right.
                    {new WorldPoint(3162, 3492, 0), new WorldPoint(3167, 3487, 0), new WorldPoint(3164, 3490, 0), true},
                    {new WorldPoint(3162, 3492, 0), new WorldPoint(3167, 3487, 0), new WorldPoint(3160, 3490, 0), false},

                    // Rectangles where first point is bottom left, second point is upper right.
                    {new WorldPoint(3162, 3487, 0), new WorldPoint(3167, 3492, 0), new WorldPoint(3164, 3490, 0), true},
                    {new WorldPoint(3162, 3487, 0), new WorldPoint(3167, 3492, 0), new WorldPoint(3160, 3490, 0), false},

                    // Flat horizontal rectangle
                    {new WorldPoint(3162, 3487, 0), new WorldPoint(3167, 3487, 0), new WorldPoint(3164, 3487, 0), true},
                    {new WorldPoint(3162, 3487, 0), new WorldPoint(3167, 3487, 0), new WorldPoint(3161, 3487, 0), false},
                    {new WorldPoint(3162, 3487, 0), new WorldPoint(3167, 3487, 0), new WorldPoint(3164, 3480, 0), false},

                    // Flat vertical rectangle
                    {new WorldPoint(3162, 3487, 0), new WorldPoint(3162, 3492, 0), new WorldPoint(3162, 3490, 0), true},
                    {new WorldPoint(3162, 3487, 0), new WorldPoint(3162, 3492, 0), new WorldPoint(3160, 3489, 0), false},

                    // Point is in different plane than rectangle
                    {new WorldPoint(3162, 3492, 0), new WorldPoint(3167, 3487, 0), new WorldPoint(3164, 3490, 1), false}
            });
        }

        private final WorldPoint rectangleCorner;
        private final WorldPoint rectangleOppositeCorner;
        private final WorldPoint point;
        private final boolean expectedResult;

        public IsPointInsideRectangleParameterizedTest(final WorldPoint rectangleCorner,
                                                       final WorldPoint rectangleOppositeCorner,
                                                       final WorldPoint point,
                                                       final boolean expectedResult) {
            this.rectangleCorner = rectangleCorner;
            this.rectangleOppositeCorner = rectangleOppositeCorner;
            this.point = point;
            this.expectedResult = expectedResult;
        }

        @Test
        public void testIsPointInsideRectangle() {
            final boolean result = Util.isPointInsideRectangle(rectangleCorner, rectangleOppositeCorner, point);
            Assert.assertEquals(expectedResult, result);
        }
    }

    @Test
    public void testThrowOnRectangleCornersInDifferentPlanes() {
        final WorldPoint rectangleCorner = new WorldPoint(3162, 3492, 0);
        final WorldPoint rectangleOppositeCorner = new WorldPoint(3167, 3487, 1);
        final WorldPoint point = new WorldPoint(3164, 3490, 0);
        Assert.assertThrows(RuntimeException.class, () ->
                Util.isPointInsideRectangle(rectangleCorner, rectangleOppositeCorner, point));
    }
}

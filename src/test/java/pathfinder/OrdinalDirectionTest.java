package pathfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;
import shortestpath.pathfinder.OrdinalDirection;

import java.awt.Point;

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
}
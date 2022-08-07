package unittests.utils;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import shortestpath.pathfinder.OrdinalDirection;
import shortestpath.utils.PathfinderUtil;
import shortestpath.utils.wallfinder.Wall;
import shortestpath.utils.wallfinder.WallFinder;
import shortestpath.worldmap.WorldMapProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WallFinderTest {
    static WorldMapProvider worldMapProvider = new WorldMapProvider();
    WallFinder wallFinder;

    @Before
    public void setup() {
        this.wallFinder = new WallFinder(worldMapProvider.getWorldMap());
    }

    @RunWith(Parameterized.class)
    public static class FindWallTest {
        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    // rectangleCorner, rectangleCornerOpposite, point, direction

                    // Grand Exchange center
                    {new WorldPoint(3167, 3487, 0), new WorldPoint(3162, 3492, 0), new WorldPoint(3165, 3477, 0), OrdinalDirection.NORTH},
                    // Grand Exchange bottom middle pillar
                    {new WorldPoint(3158, 3482, 0), new WorldPoint(3163, 3478, 0), new WorldPoint(3160, 3474, 0), OrdinalDirection.NORTH},
                    {new WorldPoint(3158, 3482, 0), new WorldPoint(3163, 3478, 0), new WorldPoint(3161, 3488, 0), OrdinalDirection.SOUTH}

            });
        }

        private final WorldPoint rectangleCorner;
        private final WorldPoint rectangleOppositeCorner;
        private final WorldPoint start;
        private final OrdinalDirection direction;

        public FindWallTest(final WorldPoint rectangleCorner,
                            final WorldPoint rectangleOppositeCorner,
                            final WorldPoint start,
                            final OrdinalDirection direction) {
            this.rectangleCorner = rectangleCorner;
            this.rectangleOppositeCorner = rectangleOppositeCorner;
            this.start = start;
            this.direction = direction;
        }

        @Test
        public void testAllWallPointsInResult() {
            Set<WorldPoint> expectedWall = PathfinderUtil.getPointsInsideRectangle(rectangleCorner, rectangleOppositeCorner);
            expectedWall = expectedWall.stream()
                    .filter(point -> !worldMapProvider.getWorldMap().isBlocked(point))
                    .filter(point -> {
                        for (final OrdinalDirection direction : OrdinalDirection.values()) {
                            if (!worldMapProvider.getWorldMap().checkDirection(point, direction)) {
                                return true;
                            }
                        }
                        return false;
                    }).collect(Collectors.toSet());

            final WallFinder wallFinder = new WallFinder(worldMapProvider.getWorldMap());
            final Wall result = wallFinder.findFirstWall(start, direction);

            Assert.assertNotNull(result);
            Assert.assertEquals(expectedWall.size(), result.size());
            final boolean areAllExpectedPointsInWall = expectedWall.stream().allMatch(result::contains);
            Assert.assertTrue(areAllExpectedPointsInWall);
        }
    }

    @Test
    public void testFindSectionWall() {
        // Set-up
        final Set<WorldPoint> pointsOnMiscellaniaBorderWall = new HashSet<>();
        pointsOnMiscellaniaBorderWall.add(new WorldPoint(2565, 3863, 0));
        pointsOnMiscellaniaBorderWall.add(new WorldPoint(2524, 3896, 0));
        pointsOnMiscellaniaBorderWall.add(new WorldPoint(2625, 3872, 0));
        pointsOnMiscellaniaBorderWall.add(new WorldPoint(2502, 3882, 0));
        pointsOnMiscellaniaBorderWall.add(new WorldPoint(2560, 3891, 0));
        pointsOnMiscellaniaBorderWall.add(new WorldPoint(2510, 3837, 0));

        final WorldPoint start = new WorldPoint(2555, 3871, 0);
        final Wall sectionWall = wallFinder.findSectionWall(start);
        Assert.assertNotNull(sectionWall);
        final boolean areAllPointsInWall = pointsOnMiscellaniaBorderWall.stream().allMatch(sectionWall::contains);
        Assert.assertTrue(areAllPointsInWall);
    }
}

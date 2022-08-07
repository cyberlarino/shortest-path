package unittests.worldmap.sections;

import net.runelite.api.coords.WorldPoint;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import shortestpath.pathfinder.path.Transport;
import shortestpath.utils.PathfinderUtil;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class SectionMapperTest {
    private WorldMapProvider worldMapProvider;
    private SectionMapper sectionMapper;

    @Before
    public void setup() {
        this.worldMapProvider = new WorldMapProvider();
        this.sectionMapper = SectionMapper.fromFile(worldMapProvider);
    }

//    @Test
//    public void testGetShortcuts_steppingStoneShortcut() {
//        // Set-up, find transports belonging to Stepping Stone shortcut
//        final WorldPoint steppingStoneUpperLeftBoundary = new WorldPoint(3156, 3361, 0);
//        final WorldPoint steppingStoneLowerRightBoundary = new WorldPoint(3148, 3364, 0);
//        final Predicate<WorldPoint> isPointInsideBoundary = PathfinderUtil.isPointInsideRectanglePredicate(steppingStoneUpperLeftBoundary, steppingStoneLowerRightBoundary);
//        final Set<Transport> steppingStoneTransports = new HashSet<>();
//        for (final Transport transport : worldMapProvider.getWorldMap().getTransports()) {
//            if (isPointInsideBoundary.test(transport.getOrigin()) || isPointInsideBoundary.test(transport.getDestination())) {
//                steppingStoneTransports.add(transport);
//            }
//        }
//
//        // Make sure all stepping stone shortcut transports are in set
//        final Integer section = sectionMapper.getSection(new WorldPoint(3161, 3364, 0));
//        Assert.assertNotNull(section);
//        final Set<Transport> shortcutTransports = sectionMapper.getSectionShortcuts(section);
//
//        final boolean isAllShortcutTransportsInOutput = shortcutTransports.containsAll(steppingStoneTransports);
//        Assert.assertTrue(isAllShortcutTransportsInOutput);
//    }
}

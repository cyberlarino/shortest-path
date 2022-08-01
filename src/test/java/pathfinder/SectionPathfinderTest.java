package pathfinder;

import net.runelite.api.coords.WorldPoint;
import org.junit.Before;
import org.junit.Test;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMapProvider;
import shortestpath.worldmap.sections.SectionMapper;
import shortestpath.worldmap.sections.SectionPathfinder;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class SectionPathfinderTest {
    private WorldMapProvider worldMapProvider;
    private SectionMapper sectionMapper;
    private SectionPathfinder sectionPathfinder;

    @Before
    public void setup() {
        this.worldMapProvider = new WorldMapProvider();
        this.sectionMapper = new SectionMapper(worldMapProvider);
        sectionMapper.findSections();
        this.sectionPathfinder = new SectionPathfinder(worldMapProvider, sectionMapper);
    }

    // Utility functions
    private boolean allRoutesValid(final List<List<Transport>> routes, final WorldPoint start, final WorldPoint target) {
        final Integer startSection = sectionMapper.getSectionId(start);
        final Integer targetSection = sectionMapper.getSectionId(target);

        final Predicate<List<Transport>> isRouteValid = route -> {
            if (route.isEmpty()) {
                return false;
            }

            final Predicate<Transport> isTransportValid = transport -> {
                return transport.getOrigin() != null && transport.getDestination() != null;
            };
            if (!route.stream().allMatch(isTransportValid)) {
                return false;
            }

            final Integer routeStartSection = sectionMapper.getSectionId(route.get(0).getOrigin());
            final Integer routeTargetSection = sectionMapper.getSectionId(route.get(route.size() - 1).getDestination());
            if (!Objects.equals(routeStartSection, startSection) || !Objects.equals(routeTargetSection, targetSection)) {
                return false;
            }

            for (int i = 0; i < route.size() - 1; ++i) {
                final Integer currentTransportDestinationSection = sectionMapper.getSectionId(route.get(i).getDestination());
                final Integer nextTransportOriginSection = sectionMapper.getSectionId(route.get(i + 1).getOrigin());
                if (!Objects.equals(currentTransportDestinationSection, nextTransportOriginSection)) {
                    return false;
                }
            }

            return true;
        };
        return routes.stream().allMatch(isRouteValid);
    }

    @Test
    public void simpleSectionTraverseTest() {
        final WorldPoint start = new WorldPoint(3232, 3401, 0);
        final WorldPoint target = new WorldPoint(3089, 3523, 0);

        final List<List<Transport>> routes = sectionPathfinder.getRoute(start, target);
        allRoutesValid(routes, start, target);
    }
}

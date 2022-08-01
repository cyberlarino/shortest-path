package shortestpath.worldmap.sections;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMapProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SectionPathfinder {
    private final WorldMapProvider worldMapProvider;
    private final SectionMapper sectionMapper;

    public SectionPathfinder(final WorldMapProvider worldMapProvider, final SectionMapper sectionMapper) {
        this.worldMapProvider = worldMapProvider;
        this.sectionMapper = sectionMapper;
    }

    private static class SectionNode {
        @Getter
        final int section;
        final SectionNode previous;
        final Transport transport;

        public SectionNode(final int section, final SectionNode previous, final Transport transport) {
            this.section = section;
            this.previous = previous;
            this.transport = transport;
        }

        public List<Transport> getTransportRoute() {
            final List<Transport> transports = new LinkedList<>();

            SectionNode sectionNodeIterator = this;
            while (sectionNodeIterator.previous != null) {
                transports.add(0, sectionNodeIterator.transport);
                sectionNodeIterator = sectionNodeIterator.previous;
            }

            return transports;
        }
    }

    public List<SectionRoute> getPossibleRoutes(final WorldPoint start, final WorldPoint target) {
        Integer startSectionId = sectionMapper.getSectionId(start);
        Integer targetSectionId = sectionMapper.getSectionId(target);
        if (startSectionId == null || targetSectionId == null) {
            return null;
        }

        List<SectionNode> boundary = new LinkedList<>();
        boundary.add(new SectionNode(startSectionId, null, null));

        Set<Integer> visited = new HashSet<>();
        visited.add(startSectionId);

        List<SectionRoute> routes = new ArrayList<>();
        while (!boundary.isEmpty()) {
            SectionNode node = boundary.remove(0);

            for (final Transport transport : worldMapProvider.getWorldMap().getTransports()) {
                final Integer transportOriginSectionId = sectionMapper.getSectionId(transport.getOrigin());
                final Integer transportDestinationSectionId = sectionMapper.getSectionId(transport.getDestination());
                assert transportOriginSectionId != null;
                assert transportDestinationSectionId != null;

                if (transportOriginSectionId.equals(transportDestinationSectionId)) {
                    continue;
                }

                if (transportOriginSectionId.equals(node.getSection())) {
                    if (transportDestinationSectionId.equals(targetSectionId)) {
                        final SectionNode reachedDestinationNode = new SectionNode(transportDestinationSectionId, node, transport);
                        final SectionRoute route = new SectionRoute(start, target, reachedDestinationNode.getTransportRoute());
                        routes.add(route);
                    }
                    else if (visited.add(transportDestinationSectionId)) {
                        boundary.add(new SectionNode(transportDestinationSectionId, node, transport));
                    }
                }
            }
        }

        return routes;
    }
}

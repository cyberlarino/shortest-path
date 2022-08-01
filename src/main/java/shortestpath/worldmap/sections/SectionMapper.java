package shortestpath.worldmap.sections;

import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.Node;
import shortestpath.pathfinder.NodeGraph;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMapProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SectionMapper {
    final WorldMapProvider worldMapProvider;

    private final List<Set<WorldPoint>> sections = new ArrayList<>();

    public SectionMapper(final WorldMapProvider worldMapProvider) {
        this.worldMapProvider = worldMapProvider;
    }

    public void findSections() {
        for (final Transport transport : worldMapProvider.getWorldMap().getTransports()) {
            floodFill(transport.getOrigin());
            floodFill(transport.getDestination());
        }
    }

    @Nullable
    public Integer getSectionId(final WorldPoint point) {
        for (int i = 0; i < sections.size(); ++i) {
            if (sections.get(i).contains(point)) {
                return i;
            }
        }
        return null;
    }

    private void floodFill(final WorldPoint point) {
        if (getSectionId(point) != null) {
            return;
        }

        final NodeGraph graph = new NodeGraph(worldMapProvider.getWorldMap());
        graph.addBoundaryNode(Node.createInitialNode(point));

        while (!graph.getBoundary().isEmpty()) {
            graph.evaluateBoundaryNode(0, (x) -> true, (x) -> false);
        }

        sections.add(graph.getVisited());
        if (graph.getVisited().isEmpty()) {
            Set<WorldPoint> temporarySet = new HashSet<>();
            temporarySet.add(point);
            sections.add(temporarySet);
        }
    }
}

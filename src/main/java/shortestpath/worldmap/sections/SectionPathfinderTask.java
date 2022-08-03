package shortestpath.worldmap.sections;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.worldmap.WorldMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SectionPathfinderTask implements Runnable {
    private final WorldMap worldMap;
    private final SectionMapper sectionMapper;

    @Getter
    private final WorldPoint start;
    @Getter
    private final WorldPoint target;
    @Getter
    private PathfinderTaskStatus status = PathfinderTaskStatus.CALCULATING;
    @Getter
    private final List<SectionRoute> routes = new ArrayList<>();

    private boolean shouldCancelTask = false;

    public SectionPathfinderTask(final WorldMap worldMap, final SectionMapper sectionMapper,
                                 final WorldPoint start, final WorldPoint target) {
        this.worldMap = worldMap;
        this.sectionMapper = sectionMapper;
        this.start = start;
        this.target = target;

        new Thread(this).start();
    }

    public void cancelTask() {
        shouldCancelTask = true;
    }

    @Override
    public void run() {
        Integer startSectionId = sectionMapper.getSectionId(start);
        Integer targetSectionId = sectionMapper.getSectionId(target);
        if (startSectionId == null || targetSectionId == null) {
            status = PathfinderTaskStatus.CANCELLED;
            return;
        }

        List<SectionNode> boundary = new LinkedList<>();
        boundary.add(new SectionNode(startSectionId, null, null));

        Set<Integer> visited = new HashSet<>();
        visited.add(startSectionId);

        while (!boundary.isEmpty() && !shouldCancelTask) {
            SectionNode node = boundary.remove(0);

            for (final Transport transport : worldMap.getTransports()) {
                final MovementSections movementSections = sectionMapper.getSectionId(transport);
                final Integer transportOriginSectionId = movementSections.getOriginSection();
                final Integer transportDestinationSectionId = movementSections.getDestinationSection();
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

        if (shouldCancelTask) {
            status = PathfinderTaskStatus.CANCELLED;
        }
        else {
            status = PathfinderTaskStatus.DONE;
        }
    }
}

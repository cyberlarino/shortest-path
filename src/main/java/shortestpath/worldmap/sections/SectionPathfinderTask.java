package shortestpath.worldmap.sections;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.PathfinderConfig;
import shortestpath.pathfinder.path.Transport;
import shortestpath.pathfinder.pathfindertask.PathfinderTaskStatus;
import shortestpath.worldmap.WorldMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

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

    final Predicate<Transport> transportPredicate;
    private boolean shouldCancelTask = false;

    public SectionPathfinderTask(final WorldMap worldMap, final SectionMapper sectionMapper,
                                 final WorldPoint start, final WorldPoint target) {
        this(worldMap, sectionMapper, start, target, (x) -> true);
    }

    public SectionPathfinderTask(final WorldMap worldMap, final SectionMapper sectionMapper,
                                 final WorldPoint start, final WorldPoint target, final Predicate<Transport> transportPredicate) {
        this.worldMap = worldMap;
        this.sectionMapper = sectionMapper;
        this.start = start;
        this.target = target;
        this.transportPredicate = transportPredicate;

        new Thread(this).start();
    }

    public void cancelTask() {
        shouldCancelTask = true;
    }

    @Override
    public void run() {
        Integer startSection = sectionMapper.getSectionId(start);
        Integer targetSection = sectionMapper.getSectionId(target);
        if (startSection == null || targetSection == null) {
            status = PathfinderTaskStatus.CANCELLED;
            return;
        }

        final BiPredicate<SectionNode, Integer> hasNodeBeenInSectionBefore = (node, section) -> {
            final List<Transport> route = node.getTransportRoute();
            final Set<Integer> visitedSections = new HashSet<>();

            if (route.isEmpty()) {
                return false;
            }

            final Transport firstTransport = route.get(0);
            final MovementSections firstTransportSections = sectionMapper.getSectionId(firstTransport);
            visitedSections.add(firstTransportSections.getOriginSection());

            for (final Transport transport : route) {
                final MovementSections transportSections = sectionMapper.getSectionId(transport);
                if (!visitedSections.add(transportSections.getDestinationSection())) {
                    return true;
                }
            }
            return false;
        };

        List<SectionNode> boundary = new LinkedList<>();
        boundary.add(new SectionNode(startSection, null, null));

        Set<Integer> visited = new HashSet<>();
        visited.add(startSection);

        while (!boundary.isEmpty() && !shouldCancelTask) {
            final SectionNode currentNode = boundary.remove(0);
            final List<SectionNode> neighbors = getNeighbors(currentNode);
            for (final SectionNode node : neighbors) {

                final MovementSections nodeSections = sectionMapper.getSectionId(node.transport);
                if (node.getSection() == targetSection) {
                    routes.add(new SectionRoute(start, target, node.getTransportRoute()));
                }
                else {
                    boundary.add(node);
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

    private List<SectionNode> getNeighbors(final SectionNode node) {
        final Integer nodeSection = node.getSection();
        final BiPredicate<SectionNode, Integer> hasNodeBeenInSectionBefore = (nodeToCheck, section) -> {
            final List<Transport> route = nodeToCheck.getTransportRoute();
            final Set<Integer> visitedSections = new HashSet<>();

            if (route.isEmpty()) {
                return false;
            }

            final Transport firstTransport = route.get(0);
            final MovementSections firstTransportSections = sectionMapper.getSectionId(firstTransport);
            visitedSections.add(firstTransportSections.getOriginSection());

            for (final Transport transport : route) {
                final MovementSections transportSections = sectionMapper.getSectionId(transport);
                if (!visitedSections.add(transportSections.getDestinationSection())) {
                    return true;
                }
            }
            return false;
        };

        final List<SectionNode> neighbors = new ArrayList<>();
        for (final Transport transport : worldMap.getTransports()) {
            final MovementSections transportSections = sectionMapper.getSectionId(transport);
            if (!transportSections.getOriginSection().equals(nodeSection)) {
                continue;
            }
            if (transportSections.getOriginSection().equals(transportSections.getDestinationSection())) {
                continue;
            }

            final boolean neighborToSectionAlreadyAdded = neighbors.stream().anyMatch((neighborNode) -> {
                return neighborNode.section == transportSections.getDestinationSection();
            });
            if (neighborToSectionAlreadyAdded) {
                continue;
            }

            if (!transportPredicate.test(transport)) {
                continue;
            }

            if (!hasNodeBeenInSectionBefore.test(node, transportSections.getDestinationSection())) {
                neighbors.add(new SectionNode(transportSections.getDestinationSection(), node, transport));
            }
        }
        return neighbors;
    }
}

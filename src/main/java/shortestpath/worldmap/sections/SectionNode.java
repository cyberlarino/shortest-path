package shortestpath.worldmap.sections;

import lombok.Getter;
import shortestpath.pathfinder.path.Transport;

import java.util.LinkedList;
import java.util.List;

class SectionNode {
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

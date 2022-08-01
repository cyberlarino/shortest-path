package shortestpath.worldmap.sections;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import shortestpath.pathfinder.path.Transport;

import java.util.List;

public class SectionRoute {
    @Getter
    private final WorldPoint origin;
    @Getter
    private final WorldPoint destination;
    @Getter
    private final List<Transport> transports;

    public SectionRoute(final WorldPoint origin, final WorldPoint destination, final List<Transport> transports) {
        this.origin = origin;
        this.destination = destination;
        this.transports = transports;
    }

    public int length() {
        int length = origin.distanceTo2D(transports.get(0).getOrigin());

        for (int i = 0; i < transports.size() - 1; ++i) {
            final Transport currentTransport = transports.get(i);
            final Transport nextTransport = transports.get(i + 1);
            final int distanceBetweenTransports = currentTransport.getDestination().distanceTo2D(nextTransport.getOrigin());
            length += distanceBetweenTransports;
        }

        length += origin.distanceTo2D(transports.get(transports.size() - 1).getDestination());
        return length;
    }
}

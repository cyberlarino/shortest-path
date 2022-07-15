package shortestpath.pathfinder;

import net.runelite.api.coords.WorldPoint;
import shortestpath.Transport;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class PathfinderConfig {
    public CollisionMap map;
    public Map<WorldPoint, List<Transport>> transports;
    public boolean avoidWilderness = true;
    public boolean useTransports = true;
    public boolean useAgilityShortcuts = false;
    public boolean useGrappleShortcuts = false;
    public int agilityLevel = 1;
    public int rangedLevel = 1;
    public int strengthLevel = 1;

    public PathfinderConfig(CollisionMap map) {
        this.map = map;
        this.transports = null;
    }

    public PathfinderConfig(CollisionMap map, Map<WorldPoint, List<Transport>> transports) {
        this.map = map;
        this.transports = transports;
    }

    public boolean canPlayerUseTransport(final Transport transport) {
        final int transportAgilityLevel = transport.getAgilityLevelRequired();
        final int transportRangedLevel = transport.getRangedLevelRequired();
        final int transportStrengthLevel = transport.getStrengthLevelRequired();

        final boolean isAgilityShortcut = transportAgilityLevel > 1;
        final boolean isGrappleShortcut = isAgilityShortcut && (transportRangedLevel > 1 || transportStrengthLevel > 1);

        if (!isAgilityShortcut) {
            return true;
        }

        if (!useAgilityShortcuts) {
            return false;
        }

        if (!useGrappleShortcuts && isGrappleShortcut) {
            return false;
        }

        if (useGrappleShortcuts && isGrappleShortcut && agilityLevel >= transportAgilityLevel &&
                rangedLevel >= transportRangedLevel && strengthLevel >= transportStrengthLevel) {
            return true;
        }

        return agilityLevel >= transportAgilityLevel;
    }

    public Predicate<Transport> getCanPlayerUseTransportPredicate() {
        return (transport) -> (canPlayerUseTransport(transport));
    }
}

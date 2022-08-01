package shortestpath.pathfinder;

import shortestpath.pathfinder.path.Transport;

import java.util.function.Predicate;

public class PathfinderConfig {
    public boolean avoidWilderness = true;
    public boolean useTransports = true;
    public boolean useAgilityShortcuts = true;
    public boolean useGrappleShortcuts = false;
    public int agilityLevel = 1;
    public int rangedLevel = 1;
    public int strengthLevel = 1;

    public boolean canPlayerUseTransport(final Transport transport) {
        final int transportAgilityLevel = transport.getAgilityLevelRequired();
        final int transportRangedLevel = transport.getRangedLevelRequired();
        final int transportStrengthLevel = transport.getStrengthLevelRequired();

        final boolean isAgilityShortcut = transportAgilityLevel > 1;
        final boolean isGrappleShortcut = isAgilityShortcut && (transportRangedLevel > 1 || transportStrengthLevel > 1);

        if (isAgilityShortcut && !useAgilityShortcuts) {
            return false;
        }

        if (isGrappleShortcut && !useGrappleShortcuts) {
            return false;
        }

        if (isGrappleShortcut && useGrappleShortcuts) {
            return agilityLevel >= transportAgilityLevel &&
                    rangedLevel >= transportRangedLevel && strengthLevel >= transportStrengthLevel;
        }

        return agilityLevel >= transportAgilityLevel;
    }

    public Predicate<Transport> getCanPlayerUseTransportPredicate() {
        return this::canPlayerUseTransport;
    }
}

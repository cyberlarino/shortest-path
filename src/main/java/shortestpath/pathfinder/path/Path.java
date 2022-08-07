package shortestpath.pathfinder.path;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

public class Path {
    @Getter
    private final List<Movement> movements;

    public Path(final List<Movement> movements) {
        this.movements = movements;
    }

    public WorldPoint getOrigin() {
        return movements.get(0).getOrigin();
    }

    public WorldPoint getDestination() {
        return movements.get(movements.size() - 1).getDestination();
    }

    public <T extends Movement> List<T> getMovementOfType(final Class<T> type) {
        return movements.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }
}

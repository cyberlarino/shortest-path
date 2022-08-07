package shortestpath.utils.wallfinder;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

import java.util.Iterator;
import java.util.List;

public class Wall implements Iterable<WorldPoint> {
    private final List<WorldPoint> points;
    @Getter
    private final boolean island;

    public Wall(final List<WorldPoint> points, final boolean island) {
        this.points = points;
        this.island = island;
    }

    public WorldPoint get(int index) {
        while (index < 0) {
            index += size();
        }
        index %= size();
        return points.get(index);
    }

    public int size() {
        return points.size();
    }

    public boolean contains(final WorldPoint point) {
        for (final WorldPoint pointIterator : points) {
            if (point.equals(pointIterator)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<WorldPoint> iterator() {
        return new Iterator<WorldPoint>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size();
            }

            @Override
            public WorldPoint next() {
                return get(currentIndex++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

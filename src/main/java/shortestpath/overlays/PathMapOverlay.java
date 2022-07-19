package shortestpath.overlays;

import com.google.inject.Inject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;
import shortestpath.ClientInfoProvider;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.Path;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.worldmap.Transport;
import shortestpath.worldmap.WorldMapProvider;

public class PathMapOverlay extends Overlay {
    private final Client client;
    private final WorldMapOverlay worldMapOverlay;
    private final ClientInfoProvider clientInfoProvider;
    private final PathfinderRequestHandler pathfinderRequestHandler;
    private final WorldMapProvider worldMapProvider;
    private final ConfigProvider configProvider;

    private Area mapClipArea;

    public PathMapOverlay(final Client client,
                   final WorldMapOverlay worldMapOverlay,
                   final ClientInfoProvider clientInfoProvider,
                   final PathfinderRequestHandler pathfinderRequestHandler,
                   final WorldMapProvider worldMapProvider,
                   final ConfigProvider configProvider) {
        this.client = client;
        this.worldMapOverlay = worldMapOverlay;
        this.clientInfoProvider = clientInfoProvider;
        this.pathfinderRequestHandler = pathfinderRequestHandler;
        this.worldMapProvider = worldMapProvider;
        this.configProvider = configProvider;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.MANUAL);
        drawAfterLayer(WidgetInfo.WORLD_MAP_VIEW);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!configProvider.drawPathOnWorldMap()) {
            return null;
        }

        if (client.getWidget(WidgetInfo.WORLD_MAP_VIEW) == null) {
            return null;
        }

        mapClipArea = getWorldMapClipArea(client.getWidget(WidgetInfo.WORLD_MAP_VIEW).getBounds());
        graphics.setClip(mapClipArea);

        if (configProvider.drawCollisionMap()) {
            graphics.setColor(configProvider.colorCollisionMap());
            Rectangle extent = getWorldMapExtent(client.getWidget(WidgetInfo.WORLD_MAP_VIEW).getBounds());
            final int z = client.getPlane();
            for (int x = extent.x; x < (extent.x + extent.width + 1); x++) {
                for (int y = extent.y - extent.height; y < (extent.y + 1); y++) {
                    WorldPoint point = new WorldPoint(x, y, z);
                    if (worldMapProvider.getCollisionMap().isBlocked(point)) {
                        drawOnMap(graphics, point);
                    }
                }
            }
        }

        if (configProvider.drawTransports()) {
            graphics.setColor(Color.WHITE);
            for (WorldPoint a : worldMapProvider.getTransports().keySet()) {
                Point mapA = worldMapOverlay.mapWorldPointToGraphicsPoint(a);
                if (mapA == null) {
                    continue;
                }

                for (Transport b : worldMapProvider.getTransports().get(a)) {
                    Point mapB = worldMapOverlay.mapWorldPointToGraphicsPoint(b.getOrigin());
                    if (mapB == null) {
                        continue;
                    }

                    graphics.drawLine(mapA.getX(), mapA.getY(), mapB.getX(), mapB.getY());
                }
            }
        }

        if (pathfinderRequestHandler.getActivePath() != null) {
            final boolean done = pathfinderRequestHandler.isActivePathDone();
            graphics.setColor(done ? configProvider.colorPath() : configProvider.colorPathCalculating());
            final Path path = pathfinderRequestHandler.getActivePath();
            if (path != null) {
                for (WorldPoint point : path.getPoints()) {
                    drawOnMap(graphics, point);
                }
            }
        }

        return null;
    }

    private void drawOnMap(final Graphics2D graphics, final WorldPoint point) {
        final Point start = clientInfoProvider.mapWorldPointToGraphicsPoint(point);
        final Point end = clientInfoProvider.mapWorldPointToGraphicsPoint(point.dx(1).dy(-1));

        if (start == null || end == null) {
            return;
        }

        int x = start.getX();
        int y = start.getY();
        final int width = end.getX() - x;
        final int height = end.getY() - y;
        x -= width / 2;
        y -= height / 2;

        graphics.fillRect(x, y, width, height);
    }

    private Area getWorldMapClipArea(Rectangle baseRectangle) {
        final Widget overview = client.getWidget(WidgetInfo.WORLD_MAP_OVERVIEW_MAP);
        final Widget surfaceSelector = client.getWidget(WidgetInfo.WORLD_MAP_SURFACE_SELECTOR);

        Area clipArea = new Area(baseRectangle);

        if (overview != null && !overview.isHidden()) {
            clipArea.subtract(new Area(overview.getBounds()));
        }

        if (surfaceSelector != null && !surfaceSelector.isHidden()) {
            clipArea.subtract(new Area(surfaceSelector.getBounds()));
        }

        return clipArea;
    }

    private Rectangle getWorldMapExtent(Rectangle baseRectangle) {
        WorldPoint topLeft = clientInfoProvider.calculateMapPoint(new Point(baseRectangle.x, baseRectangle.y));
        WorldPoint bottomRight = clientInfoProvider.calculateMapPoint(
                new Point(baseRectangle.x + baseRectangle.width, baseRectangle.y + baseRectangle.height));
        return new Rectangle(topLeft.getX(), topLeft.getY(), bottomRight.getX() - topLeft.getX(), topLeft.getY() - bottomRight.getY());
    }
}

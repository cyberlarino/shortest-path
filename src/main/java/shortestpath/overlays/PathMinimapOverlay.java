package shortestpath.overlays;

import com.google.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import shortestpath.ClientInfoProvider;
import shortestpath.ConfigProvider;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.pathfinder.path.Movement;
import shortestpath.worldmap.WorldMapProvider;

public class PathMinimapOverlay extends Overlay {
    private static final int TILE_WIDTH = 4;
    private static final int TILE_HEIGHT = 4;

    private final Client client;
    private ClientInfoProvider clientInfoProvider;
    private PathfinderRequestHandler pathfinderRequestHandler;
    private WorldMapProvider worldMapProvider;
    private ConfigProvider configProvider;

    public PathMinimapOverlay(final Client client,
                       final ClientInfoProvider clientInfoProvider,
                       final PathfinderRequestHandler pathfinderRequestHandler,
                       final WorldMapProvider worldMapProvider,
                       final ConfigProvider configProvider) {
        this.client = client;
        this.clientInfoProvider = clientInfoProvider;
        this.pathfinderRequestHandler = pathfinderRequestHandler;
        this.worldMapProvider = worldMapProvider;
        this.configProvider = configProvider;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!configProvider.drawPathOnMinimap() || pathfinderRequestHandler.getActivePath() == null) {
            return null;
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setClip(clientInfoProvider.getMinimapClipArea());

        if (pathfinderRequestHandler.isActivePathDone()) {
            List<Movement> pathMovements = pathfinderRequestHandler.getActivePath().getMovements();
            if (pathMovements != null) {
                for (Movement movement : pathMovements) {
                    if (movement.getDestination().getPlane() != client.getPlane()) {
                        continue;
                    }

                    final Color pathColor = (pathfinderRequestHandler.isActivePathDone() ? configProvider.colorPath() : configProvider.colorPathCalculating());
                    drawOnMinimap(graphics, movement.getDestination(), pathColor);
                }
            }
        }

        return null;
    }

    private void drawOnMinimap(Graphics2D graphics, WorldPoint point, Color color) {
        final LocalPoint lp = LocalPoint.fromWorld(client, point);

        if (lp == null) {
            return;
        }

        Point posOnMinimap = Perspective.localToMinimap(client, lp);

        if (posOnMinimap == null) {
            return;
        }

        renderMinimapRect(client, graphics, posOnMinimap, TILE_WIDTH, TILE_HEIGHT, color);
    }

    public static void renderMinimapRect(Client client, Graphics2D graphics, Point center, int width, int height, Color color) {
        double angle = client.getMapAngle() * Math.PI / 1024.0d;

        graphics.setColor(color);
        graphics.rotate(angle, center.getX(), center.getY());
        graphics.fillRect(center.getX() - width / 2, center.getY() - height / 2, width, height);
        graphics.rotate(-angle, center.getX(), center.getY());
    }
}

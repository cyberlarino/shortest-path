package shortestpath.overlays;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import shortestpath.ConfigProvider;
import shortestpath.utils.OrdinalDirection;
import shortestpath.pathfinder.path.Movement;
import shortestpath.pathfinder.path.Path;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.pathfinder.path.Transport;
import shortestpath.worldmap.WorldMap;
import shortestpath.worldmap.WorldMapProvider;

public class PathTileOverlay extends Overlay {
    private final Client client;
    private final PathfinderRequestHandler pathfinderRequestHandler;
    private final WorldMapProvider worldMapProvider;
    private final ConfigProvider configProvider;

    public PathTileOverlay(final Client client,
                           final PathfinderRequestHandler pathfinderRequestHandler,
                           final WorldMapProvider worldMapProvider,
                           final ConfigProvider configProvider) {
        this.client = client;
        this.pathfinderRequestHandler = pathfinderRequestHandler;
        this.worldMapProvider = worldMapProvider;
        this.configProvider = configProvider;

        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.LOW);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    private void renderTransports(Graphics2D graphics) {
        for (Transport transport : worldMapProvider.getWorldMap().getTransports()) {
            final WorldPoint transportDestination = transport.getDestination();
            final WorldPoint transportOrigin = transport.getOrigin();

            drawTile(graphics, transportDestination, configProvider.colorTransports(), -1);

            final Point centerDestination = tileCenter(transportDestination);
            final Point centerOrigin = tileCenter(transportOrigin);
            if (centerOrigin == null || centerDestination == null) {
                continue;
            }
            graphics.drawLine(centerOrigin.x, centerOrigin.y, centerDestination.x, centerDestination.y);

            final StringBuilder s = new StringBuilder();
            if (transportOrigin.getPlane() > transportDestination.getPlane()) {
                s.append("+");
            } else if (transportOrigin.getPlane() < transportDestination.getPlane()) {
                s.append("-");
            } else {
                s.append("=");
            }
            graphics.setColor(Color.WHITE);
            graphics.drawString(s.toString(), centerDestination.x, centerDestination.y);
        }
    }

    private void renderCollisionMap(Graphics2D graphics) {
        for (Tile[] row : client.getScene().getTiles()[client.getPlane()]) {
            for (Tile tile : row) {
                if (tile == null) {
                    continue;
                }

                final Polygon tilePolygon = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
                if (tilePolygon == null) {
                    continue;
                }

                final WorldPoint worldPoint = tile.getWorldLocation();
                final WorldMap map = worldMapProvider.getWorldMap();
                final String s = (!map.checkDirection(worldPoint, OrdinalDirection.NORTH) ? "n" : "") +
                        (!map.checkDirection(worldPoint, OrdinalDirection.SOUTH) ? "s" : "") +
                        (!map.checkDirection(worldPoint, OrdinalDirection.EAST) ? "e" : "") +
                        (!map.checkDirection(worldPoint, OrdinalDirection.WEST) ? "w" : "");

                if (!s.isEmpty() && !s.equals("nsew")) {
                    graphics.setColor(Color.WHITE);
                    int stringX = (int) (tilePolygon.getBounds().getCenterX() - graphics.getFontMetrics().getStringBounds(s, graphics).getWidth() / 2);
                    int stringY = (int) tilePolygon.getBounds().getCenterY();
                    graphics.drawString(s, stringX, stringY);
                } else if (!s.isEmpty()) {
                    graphics.setColor(configProvider.colorCollisionMap());
                    graphics.fill(tilePolygon);
                }
            }
        }
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (configProvider.drawTransports()) {
            this.renderTransports(graphics);
        }

        if (configProvider.drawCollisionMap()) {
            this.renderCollisionMap(graphics);
        }

        if (configProvider.drawPathOnTiles() && pathfinderRequestHandler.getActivePath() != null) {
            Color color;
            if (pathfinderRequestHandler.isActivePathDone()) {
                color = new Color(
                        configProvider.colorPath().getRed(),
                        configProvider.colorPath().getGreen(),
                        configProvider.colorPath().getBlue(),
                        configProvider.colorPath().getAlpha() / 2);
            } else {
                color = new Color(
                        configProvider.colorPathCalculating().getRed(),
                        configProvider.colorPathCalculating().getGreen(),
                        configProvider.colorPathCalculating().getBlue(),
                        configProvider.colorPathCalculating().getAlpha() / 2);
            }

            final Path path = pathfinderRequestHandler.getActivePath();
            int counter = 0;
            for (Movement movement : path.getMovements()) {
                drawTile(graphics, movement.getDestination(), color, counter++);
            }
        }

        return null;
    }

    private Point tileCenter(WorldPoint b) {
        if (b.getPlane() != client.getPlane()) {
            return null;
        }

        LocalPoint lp = LocalPoint.fromWorld(client, b);
        if (lp == null) {
            return null;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly == null) {
            return null;
        }

        int cx = poly.getBounds().x + poly.getBounds().width / 2;
        int cy = poly.getBounds().y + poly.getBounds().height / 2;
        return new Point(cx, cy);
    }

    private void drawTile(Graphics2D graphics, WorldPoint point, Color color, int counter) {
        if (point.getPlane() != client.getPlane()) {
            return;
        }

        LocalPoint lp = LocalPoint.fromWorld(client, point);
        if (lp == null) {
            return;
        }

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly == null) {
            return;
        }

        graphics.setColor(color);
        graphics.fill(poly);

        if (counter >= 0 && !TileCounter.DISABLED.equals(configProvider.showTileCounter())) {
            if (TileCounter.REMAINING.equals(configProvider.showTileCounter())) {
                counter = pathfinderRequestHandler.getActivePath().getMovements().size() - counter - 1;
            }
            String counterText = Integer.toString(counter);
            graphics.setColor(Color.WHITE);
            graphics.drawString(
                    counterText,
                    (int) (poly.getBounds().getCenterX() -
                            graphics.getFontMetrics().getStringBounds(counterText, graphics).getWidth() / 2),
                    (int) poly.getBounds().getCenterY());
        }
    }
}

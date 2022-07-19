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
import shortestpath.pathfinder.OrdinalDirection;
import shortestpath.pathfinder.Path;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.worldmap.CollisionMap;
import shortestpath.worldmap.Transport;
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
        for (WorldPoint a : worldMapProvider.getTransports().keySet()) {
            drawTile(graphics, a, configProvider.colorTransports(), -1);

            Point ca = tileCenter(a);

            if (ca == null) {
                continue;
            }

            for (Transport b : worldMapProvider.getTransports().get(a)) {
                Point cb = tileCenter(b.getOrigin());
                if (cb != null) {
                    graphics.drawLine(ca.x, ca.y, cb.x, cb.y);
                }
            }

            StringBuilder s = new StringBuilder();
            for (Transport b : worldMapProvider.getTransports().get(a)) {
                if (b.getOrigin().getPlane() > a.getPlane()) {
                    s.append("+");
                } else if (b.getOrigin().getPlane() < a.getPlane()) {
                    s.append("-");
                } else {
                    s.append("=");
                }
            }
            graphics.setColor(Color.WHITE);
            graphics.drawString(s.toString(), ca.x, ca.y);
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
                final CollisionMap map = worldMapProvider.getCollisionMap();
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
            for (WorldPoint point : path.getPoints()) {
                drawTile(graphics, point, color, counter++);
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
                counter = pathfinderRequestHandler.getActivePath().getPoints().size() - counter - 1;
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

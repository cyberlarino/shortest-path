package shortestpath;

import com.google.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapOverlay;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import shortestpath.pathfinder.PathfinderRequestHandler;
import shortestpath.pathfinder.PathfinderTask;
import shortestpath.overlays.PathMapOverlay;
import shortestpath.overlays.PathMinimapOverlay;
import shortestpath.overlays.PathTileOverlay;
import shortestpath.pathfinder.PathfinderTaskHandler;
import shortestpath.worldmap.Transport;
import shortestpath.worldmap.WorldMapProvider;

import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@PluginDescriptor(
        name = "Shortest Path",
        description = "Draws the shortest path to a chosen destination on the map (right click a spot on the world map to use)",
        tags = {"pathfinder", "map", "waypoint", "navigation"}
)
public class ShortestPathPlugin extends Plugin {
    private static final String ADD_START = "Add start";
    private static final String ADD_END = "Add end";
    private static final String CLEAR = "Clear";
    private static final String PATH = ColorUtil.wrapWithColorTag("Path", JagexColors.MENU_TARGET);
    private static final String SET = "Set";
    private static final String START = ColorUtil.wrapWithColorTag("Start", JagexColors.MENU_TARGET);
    private static final String TARGET = ColorUtil.wrapWithColorTag("Target", JagexColors.MENU_TARGET);
    private static final String TRANSPORT = ColorUtil.wrapWithColorTag("Transport", JagexColors.MENU_TARGET);
    private static final String WALK_HERE = "Walk here";
    private static final BufferedImage MARKER_IMAGE = ImageUtil.loadImageResource(ShortestPathPlugin.class, "/marker.png");

    @Inject
    public Client client;

    @Inject
    public ShortestPathConfig config;

    @Inject
    public OverlayManager overlayManager;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private WorldMapPointManager worldMapPointManager;

    @Inject
    private WorldMapOverlay worldMapOverlay;

    private Point lastMenuOpenedPoint;
    public WorldMapPoint marker;
    private WorldPoint transportStart;
    private MenuEntry lastClick;

    private PathTileOverlay pathOverlay;
    private PathMinimapOverlay pathMinimapOverlay;
    private PathMapOverlay pathMapOverlay;

    private ClientInfoProvider clientInfoProvider;
    private ConfigProvider configProvider;
    private WorldMapProvider worldMapProvider;
    private PathfinderTaskHandler pathfinderTaskHandler;
    private PathfinderRequestHandler pathfinderRequestHandler;

    @Override
    protected void startUp() {
        // Providers
        this.clientInfoProvider = new ClientInfoProvider(client, spriteManager);
        this.configProvider = new ConfigProvider(config, clientInfoProvider);
        this.worldMapProvider = new WorldMapProvider();

        // Pathfinder
        this.pathfinderTaskHandler = new PathfinderTaskHandler(configProvider, worldMapProvider);
        this.pathfinderRequestHandler = new PathfinderRequestHandler(clientInfoProvider, worldMapProvider, pathfinderTaskHandler);

        // Overlays
        this.pathOverlay = new PathTileOverlay(client, pathfinderRequestHandler, worldMapProvider, configProvider);
        this.pathMinimapOverlay = new PathMinimapOverlay(client, clientInfoProvider, pathfinderRequestHandler, worldMapProvider, configProvider);
        this.pathMapOverlay = new PathMapOverlay(client, worldMapOverlay, clientInfoProvider, pathfinderRequestHandler, worldMapProvider, configProvider);

        overlayManager.add(pathOverlay);
        overlayManager.add(pathMinimapOverlay);
        overlayManager.add(pathMapOverlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(pathOverlay);
        overlayManager.remove(pathMinimapOverlay);
        overlayManager.remove(pathMapOverlay);
    }

    public boolean isNearPath(WorldPoint location) {
        if (!pathfinderRequestHandler.hasActivePath() || pathfinderRequestHandler.getActivePath().getPoints().isEmpty()) {
            return true;
        }

        for (final WorldPoint point : pathfinderRequestHandler.getActivePath().getPoints()) {
            if (config.recalculateDistance() < 0 || location.distanceTo2D(point) < config.recalculateDistance()) {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onMenuOpened(MenuOpened event) {
        lastMenuOpenedPoint = client.getMouseCanvasPosition();
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        pathfinderTaskHandler.evaluateTasks();

        if (!pathfinderRequestHandler.hasActivePath()) {
            return;
        }

        final WorldPoint currentLocation = clientInfoProvider.getPlayerLocation();
        if (currentLocation.distanceTo(pathfinderRequestHandler.getTarget()) < config.reachedDistance()) {
            setTarget(null);
            return;
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (client.isKeyPressed(KeyCode.KC_SHIFT) && event.getOption().equals(WALK_HERE) && event.getTarget().isEmpty()) {
            if (config.drawTransports()) {
                addMenuEntry(event, ADD_START, TRANSPORT, 1);
                addMenuEntry(event, ADD_END, TRANSPORT, 1);
                // addMenuEntry(event, "Copy Position");
            }

            addMenuEntry(event, SET, TARGET, 1);
            if (pathfinderRequestHandler.hasActivePath()) {
                addMenuEntry(event, SET, START, 1);

                final WorldPoint selectedTile = getSelectedWorldPoint();
                for (final WorldPoint tile : pathfinderRequestHandler.getActivePath().getPoints()) {
                    if (tile.equals(selectedTile)) {
                        addMenuEntry(event, CLEAR, PATH, 1);
                        break;
                    }
                }
            }
        }

        final Widget map = client.getWidget(WidgetInfo.WORLD_MAP_VIEW);
        if (map != null && map.getBounds().contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY())) {
            addMenuEntry(event, SET, TARGET, 0);
            if (pathfinderRequestHandler.hasActivePath()) {
                addMenuEntry(event, SET, START, 0);
                addMenuEntry(event, CLEAR, PATH, 0);
            }
        }

        final Shape minimap = clientInfoProvider.getMinimapClipArea();
        if (minimap != null && pathfinderRequestHandler.hasActivePath() &&
                minimap.contains(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY())) {
            addMenuEntry(event, CLEAR, PATH, 0);
        }
    }

    private void onMenuOptionClicked(MenuEntry entry) {
        final WorldPoint currentLocation = clientInfoProvider.getPlayerLocation();

        if (entry.getOption().equals(ADD_START) && entry.getTarget().equals(TRANSPORT)) {
            transportStart = currentLocation;
        }

        if (entry.getOption().equals(ADD_END) && entry.getTarget().equals(TRANSPORT)) {
            WorldPoint transportEnd = client.getLocalPlayer().getWorldLocation();
            System.out.println(transportStart.getX() + " " + transportStart.getY() + " " + transportStart.getPlane() + " " +
                    currentLocation.getX() + " " + currentLocation.getY() + " " + currentLocation.getPlane() + " " +
                    lastClick.getOption() + " " + Text.removeTags(lastClick.getTarget()) + " " + lastClick.getIdentifier()
            );
            final Transport transport = new Transport(transportStart, transportEnd);
            worldMapProvider.getWorldMap().addTransport(transport);
        }

        if (entry.getOption().equals("Copy Position")) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection("(" + currentLocation.getX() + ", "
                            + currentLocation.getY() + ", "
                            + currentLocation.getPlane() + ")"), null);
        }

        if (entry.getOption().equals(SET) && entry.getTarget().equals(TARGET)) {
            setTarget(getSelectedWorldPoint());
        }

        if (entry.getOption().equals(SET) && entry.getTarget().equals(START)) {
            pathfinderRequestHandler.setStart(getSelectedWorldPoint());
        }

        if (entry.getOption().equals(CLEAR) && entry.getTarget().equals(PATH)) {
            setTarget(null);
        }

        if (entry.getType() != MenuAction.WALK) {
            lastClick = entry;
        }
    }

    private WorldPoint getSelectedWorldPoint() {
        if (client.getWidget(WidgetInfo.WORLD_MAP_VIEW) == null) {
            if (client.getSelectedSceneTile() != null) {
                return client.getSelectedSceneTile().getWorldLocation();
            }
        } else {
            return clientInfoProvider.calculateMapPoint(client.isMenuOpen() ? lastMenuOpenedPoint : client.getMouseCanvasPosition());
        }
        return null;
    }

    private void setTarget(final WorldPoint target) {
        if (target == null) {
            worldMapPointManager.remove(marker);
            marker = null;
            pathfinderRequestHandler.clearPath();
        } else {
            worldMapPointManager.removeIf(x -> x == marker);
            marker = new WorldMapPoint(target, MARKER_IMAGE);
            marker.setName("Target");
            marker.setTarget(marker.getWorldPoint());
            marker.setJumpOnClick(true);
            worldMapPointManager.add(marker);

            pathfinderRequestHandler.setTarget(target);
        }
    }

    @Provides
    public ShortestPathConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ShortestPathConfig.class);
    }

    private void addMenuEntry(MenuEntryAdded event, String option, String target, int position) {
        List<MenuEntry> entries = new LinkedList<>(Arrays.asList(client.getMenuEntries()));

        if (entries.stream().anyMatch(e -> e.getOption().equals(option) && e.getTarget().equals(target))) {
            return;
        }

        client.createMenuEntry(position)
                .setOption(option)
                .setTarget(target)
                .setParam0(event.getActionParam0())
                .setParam1(event.getActionParam1())
                .setIdentifier(event.getIdentifier())
                .setType(MenuAction.RUNELITE)
                .onClick(this::onMenuOptionClicked);
    }
}

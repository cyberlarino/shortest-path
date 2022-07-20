package shortestpath;

import net.runelite.api.Skill;
import shortestpath.overlays.TileCounter;
import shortestpath.pathfinder.PathfinderConfig;

import java.awt.Color;

public class ConfigProvider {
    private final ClientInfoProvider clientInfoProvider;
    private final ShortestPathConfig pluginConfig;

    public ConfigProvider(final ShortestPathConfig pluginConfig, final ClientInfoProvider clientInfoProvider) {
        this.pluginConfig = pluginConfig;
        this.clientInfoProvider = clientInfoProvider;
    }

    public PathfinderConfig getPathFinderConfig() {
        PathfinderConfig config = new PathfinderConfig();
        config.avoidWilderness = pluginConfig.avoidWilderness();
        config.useTransports = true;
        config.useAgilityShortcuts = pluginConfig.useAgilityShortcuts();
        config.useGrappleShortcuts = pluginConfig.useGrappleShortcuts();
        config.agilityLevel = clientInfoProvider.getBoostedSkillLevel(Skill.AGILITY);
        config.rangedLevel = clientInfoProvider.getBoostedSkillLevel(Skill.RANGED);
        config.strengthLevel = clientInfoProvider.getBoostedSkillLevel(Skill.STRENGTH);
        return config;
    }

    public boolean drawCollisionMap() {
        return pluginConfig.drawCollisionMap();
    }

    public boolean drawPathOnWorldMap() {
        return pluginConfig.drawMap();
    }

    public boolean drawPathOnMinimap() {
        return pluginConfig.drawMinimap();
    }

    public boolean drawPathOnTiles() {
        return pluginConfig.drawTiles();
    }

    public boolean drawTransports() {
        return pluginConfig.drawTransports();
    }

    public Color colorPath() {
        return pluginConfig.colourPath();
    }

    public Color colorPathCalculating() {
        return pluginConfig.colourPathCalculating();
    }

    public Color colorTransports() {
        return pluginConfig.colourTransports();
    }

    public Color colorCollisionMap() {
        return pluginConfig.colourCollisionMap();
    }

    public TileCounter showTileCounter() {
        return pluginConfig.showTileCounter();
    }

    public int ticksWithoutProgressBeforeCancelingTask() {
        return pluginConfig.abortTicks();
    }
}

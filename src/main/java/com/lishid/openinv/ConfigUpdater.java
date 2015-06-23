package com.lishid.openinv;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.lishid.openinv.utils.UUIDUtil;

public class ConfigUpdater {
    private final OpenInv plugin;

    private static final int LATEST_CONFIG_VERSION = 2;

    public ConfigUpdater(OpenInv plugin) {
        this.plugin = plugin;
    }

    private int getConfigVersion() {
        return plugin.getConfig().getInt("config-version", 1);
    }

    private boolean isConfigOutdated() {
        return getConfigVersion() < LATEST_CONFIG_VERSION;
    }

    public void checkForUpdates() {
        if (isConfigOutdated()) {
            plugin.getLogger().info("[Config] Update found! Performing update...");
            updateConfig();
        } else {
            plugin.getLogger().info("[Config] Update not found. Config is already up-to-date.");
        }
    }

    private void updateConfig() {
        // Get the old config settings
        int itemOpenInvItemId = plugin.getConfig().getInt("ItemOpenInvItemID", 280);
        boolean checkForUpdates = plugin.getConfig().getBoolean("CheckForUpdates", true);
        boolean notifySilentChest = plugin.getConfig().getBoolean("NotifySilentChest", true);
        boolean notifyAnyChest = plugin.getConfig().getBoolean("NotifyAnyChest", true);

        Map<UUID, Boolean> anyChestToggles = null;
        Map<UUID, Boolean> itemOpenInvToggles = null;
        Map<UUID, Boolean> silentChestToggles = null;

        if (plugin.getConfig().isSet("AnyChest")) {
            anyChestToggles = updateAnyChestToggles();
        }

        if (plugin.getConfig().isSet("ItemOpenInv")) {
            itemOpenInvToggles = updateItemOpenInvToggles();
        }

        if (plugin.getConfig().isSet("SilentChest")) {
            silentChestToggles = updateSilentChestToggles();
        }

        // Clear the old config
        for (String key : plugin.getConfig().getKeys(false)) {
            plugin.getConfig().set(key, null);
        }

        // Set the new config options
        plugin.getConfig().set("config-version", LATEST_CONFIG_VERSION);
        plugin.getConfig().set("check-for-updates", checkForUpdates);
        plugin.getConfig().set("items.open-inv", getMaterialById(itemOpenInvItemId).toString());
        plugin.getConfig().set("notify.any-chest", notifyAnyChest);
        plugin.getConfig().set("notify.silent-chest", notifySilentChest);

        if (anyChestToggles != null && !anyChestToggles.isEmpty()) {
            for (Map.Entry<UUID, Boolean> entry : anyChestToggles.entrySet()) {
                plugin.getConfig().set("toggles.any-chest." + entry.getKey(), entry.getValue());
            }
        }

        if (itemOpenInvToggles != null && !itemOpenInvToggles.isEmpty()) {
            for (Map.Entry<UUID, Boolean> entry : itemOpenInvToggles.entrySet()) {
                plugin.getConfig().set("toggles.items.open-inv." + entry.getKey(), entry.getValue());
            }
        }

        if (silentChestToggles != null && !silentChestToggles.isEmpty()) {
            for (Map.Entry<UUID, Boolean> entry : silentChestToggles.entrySet()) {
                plugin.getConfig().set("toggles.silent-chest." + entry.getKey(), entry.getValue());
            }
        }

        // Save the new config
        plugin.saveConfig();

        plugin.getLogger().info("[Config] Update complete.");

    }

    private Map<UUID, Boolean> updateAnyChestToggles() {
        Map<UUID, Boolean> toggles = new HashMap<UUID, Boolean>();

        ConfigurationSection anyChestSection = plugin.getConfig().getConfigurationSection("AnyChest");
        Set<String> keys = anyChestSection.getKeys(false);
        if (keys == null || keys.isEmpty()) return null;

        for (String playerName : keys) {
            UUID uuid = UUIDUtil.getUUIDOf(playerName);
            if (uuid != null) {
                boolean toggled = anyChestSection.getBoolean(playerName + ".toggle", false);
                toggles.put(uuid, toggled);
            }
        }

        return toggles;
    }

    private Map<UUID, Boolean> updateItemOpenInvToggles() {
        Map<UUID, Boolean> toggles = new HashMap<UUID, Boolean>();

        ConfigurationSection anyChestSection = plugin.getConfig().getConfigurationSection("ItemOpenInv");
        Set<String> keys = anyChestSection.getKeys(false);
        if (keys == null || keys.isEmpty()) return null;

        for (String playerName : keys) {
            UUID uuid = UUIDUtil.getUUIDOf(playerName);
            if (uuid != null) {
                boolean toggled = anyChestSection.getBoolean(playerName + ".toggle", false);
                toggles.put(uuid, toggled);
            }
        }

        return toggles;
    }

    private Map<UUID, Boolean> updateSilentChestToggles() {
        Map<UUID, Boolean> toggles = new HashMap<UUID, Boolean>();

        ConfigurationSection silentChestSection = plugin.getConfig().getConfigurationSection("SilentChest");
        Set<String> keys = silentChestSection.getKeys(false);
        if (keys == null || keys.isEmpty()) return null;

        for (String playerName : keys) {
            UUID uuid = UUIDUtil.getUUIDOf(playerName);
            if (uuid != null) {
                boolean toggled = silentChestSection.getBoolean(playerName + ".toggle", false);
                toggles.put(uuid, toggled);
            }
        }

        return toggles;
    }

    @SuppressWarnings("deprecation")
    private Material getMaterialById(int id) {
        Material material = Material.getMaterial(id);
        if (material == null) {
            material = Material.STICK;
        }
        return material;
    }
}

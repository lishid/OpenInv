package com.lishid.openinv;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.lishid.openinv.utils.UUIDUtil;

public class ConfigUpdater {
    private final OpenInv plugin;

    private static final int CONFIG_VERSION = 2;

    public ConfigUpdater(OpenInv plugin) {
        this.plugin = plugin;
    }

    private int getConfigVersion() {
        return plugin.getConfig().getInt("config-version", 1);
    }

    private boolean isConfigOutdated() {
        return getConfigVersion() < CONFIG_VERSION;
    }

    public void checkForUpdates() {
        if (isConfigOutdated()) {
            plugin.getLogger().info("[Config] Update found! Performing update...");
            performUpdate();
        } else {
            plugin.getLogger().info("[Config] Update not found. Config is already up-to-date.");
        }
    }

    private void performUpdate() {
        // Update according to the right version
        switch (getConfigVersion()) {
            case 1:
                updateConfig1To2();
                break;
        }
    }

    private void updateConfig1To2() {
        // Get the old config settings
        FileConfiguration config = plugin.getConfig();

        int itemOpenInvItemId = config.getInt("ItemOpenInvItemID", 280);
        boolean checkForUpdates = config.getBoolean("CheckForUpdates", true);
        boolean notifySilentChest = config.getBoolean("NotifySilentChest", true);
        boolean notifyAnyChest = config.getBoolean("NotifyAnyChest", true);

        Map<UUID, Boolean> anyChestToggles = null;
        Map<UUID, Boolean> itemOpenInvToggles = null;
        Map<UUID, Boolean> silentChestToggles = null;

        if (config.isSet("AnyChest")) {
            anyChestToggles = updateToggles("AnyChest");
        }

        if (config.isSet("ItemOpenInv")) {
            itemOpenInvToggles = updateToggles("ItemOpenInv");
        }

        if (config.isSet("SilentChest")) {
            silentChestToggles = updateToggles("SilentChest");
        }

        // Clear the old config
        for (String key : config.getKeys(false)) {
            plugin.getConfig().set(key, null);
        }

        // Set the new config options
        config.set("config-version", "2");
        config.set("check-for-updates", checkForUpdates);
        config.set("items.open-inv", getMaterialById(itemOpenInvItemId).toString());
        config.set("notify.any-chest", notifyAnyChest);
        config.set("notify.silent-chest", notifySilentChest);

        if (anyChestToggles != null && !anyChestToggles.isEmpty()) {
            for (Map.Entry<UUID, Boolean> entry : anyChestToggles.entrySet()) {
                config.set("toggles.any-chest." + entry.getKey(), entry.getValue());
            }
        }

        if (itemOpenInvToggles != null && !itemOpenInvToggles.isEmpty()) {
            for (Map.Entry<UUID, Boolean> entry : itemOpenInvToggles.entrySet()) {
                config.set("toggles.items.open-inv." + entry.getKey(), entry.getValue());
            }
        }

        if (silentChestToggles != null && !silentChestToggles.isEmpty()) {
            for (Map.Entry<UUID, Boolean> entry : silentChestToggles.entrySet()) {
                config.set("toggles.silent-chest." + entry.getKey(), entry.getValue());
            }
        }

        // Save the new config
        plugin.saveConfig();
        plugin.getLogger().info("[Config] Update complete.");
    }

    private Map<UUID, Boolean> updateToggles(String sectionName) {
        Map<UUID, Boolean> toggles = new HashMap<UUID, Boolean>();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection(sectionName);
        Set<String> keys = section.getKeys(false);
        if (keys == null || keys.isEmpty()) return null;

        for (String playerName : keys) {
            UUID uuid = UUIDUtil.getUUIDOf(playerName);
            if (uuid != null) {
                boolean toggled = section.getBoolean(playerName + ".toggle", false);
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

package com.lishid.openinv;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigUpdater {

    private static final int CONFIG_VERSION = 2;

    private final OpenInv plugin;

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
            plugin.getLogger().info("Configuration update found! Performing update...");
            performUpdate();
            plugin.getLogger().info("Configuration update complete!");
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
        // Backup the old config file
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config_old.yml"));
            plugin.getLogger().info("Backed up config.yml to config_old.yml before updating.");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not back up config.yml before updating!");
        }

        // Get the old config settings
        int itemOpenInvItemId = plugin.getConfig().getInt("ItemOpenInvItemID", 280);
        plugin.getConfig().set("ItemOpenInvItemID", null);
        boolean notifySilentChest = plugin.getConfig().getBoolean("NotifySilentChest", true);
        plugin.getConfig().set("NotifySilentChest", null);
        boolean notifyAnyChest = plugin.getConfig().getBoolean("NotifyAnyChest", true);
        plugin.getConfig().set("NotifyAnyChest", null);

        updateToggles("AnyChest", ".toggle", "toggles.any-chest");
        updateToggles("ItemOpenInv", ".toggle", "toggles.items.open-inv");
        updateToggles("SilentChest", ".toggle", "toggles.silent-chest");

        plugin.getConfig().set("config-version", 2);
        plugin.getConfig().set("items.open-inv", getMaterialById(itemOpenInvItemId).toString());
        plugin.getConfig().set("notify.any-chest", notifyAnyChest);
        plugin.getConfig().set("notify.silent-chest", notifySilentChest);

        // Save the new config
        plugin.saveConfig();
    }

    private void updateToggles(String sectionName, String suffix, String newSectionName) {
        // Ensure section exists
        if (!plugin.getConfig().isConfigurationSection(sectionName)) {
            return;
        }

        ConfigurationSection section = plugin.getConfig().getConfigurationSection(sectionName);
        Set<String> keys = section.getKeys(false);

        // Ensure section has content
        if (keys == null || keys.isEmpty()) {
            return;
        }

        Map<String, Boolean> toggles = new HashMap<String, Boolean>();

        for (String playerName : keys) {
            OfflinePlayer player = plugin.matchPlayer(playerName);
            String dataID = plugin.getPlayerID(player);
            toggles.put(dataID, section.getBoolean(playerName + suffix, false));
        }

        // Wipe old ConfigurationSection
        plugin.getConfig().set(sectionName, null);
        // Prepare new ConfigurationSection
        if (plugin.getConfig().isConfigurationSection(newSectionName)) {
            section = plugin.getConfig().getConfigurationSection(newSectionName);
        } else {
            section = plugin.getConfig().createSection(newSectionName);
        }

        // Set new values
        for (Map.Entry<String, Boolean> entry : toggles.entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }
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

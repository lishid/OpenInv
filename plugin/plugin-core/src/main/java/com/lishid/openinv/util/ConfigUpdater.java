package com.lishid.openinv.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lishid.openinv.OpenInv;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfigUpdater {

    private static final int CONFIG_VERSION = 3;

    private final OpenInv plugin;

    public ConfigUpdater(OpenInv plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        final int version = plugin.getConfig().getInt("config-version", 1);
        if (version >= CONFIG_VERSION) {
            return;
        }

        plugin.getLogger().info("Configuration update found! Performing update...");

        // Backup the old config file
        try {
            plugin.getConfig().save(new File(plugin.getDataFolder(), "config_old.yml"));
            plugin.getLogger().info("Backed up config.yml to config_old.yml before updating.");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not back up config.yml before updating!");
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                switch (version) {
                case 1:
                    updateConfig1To2();
                case 2:
                    updateConfig2To3();
                    break;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.saveConfig();
                        plugin.getLogger().info("Configuration update complete!");
                    }
                }.runTaskLater(plugin, 1L); // Run on 1 tick delay; on older versions Bukkit's scheduler is not guaranteed FIFO
            }
        }.runTaskAsynchronously(plugin);
    }

    private void updateConfig2To3() {
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getConfig().set("config-version", 3);
                plugin.getConfig().set("items.open-inv", null);
                plugin.getConfig().set("toggles.items.open-inv", null);
                plugin.getConfig().set("settings.disable-saving",
                        plugin.getConfig().getBoolean("DisableSaving", false));
                plugin.getConfig().set("DisableSaving", null);
            }
        }.runTask(plugin);
    }

    private void updateConfig1To2() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Get the old config settings
                int itemOpenInvItemId = plugin.getConfig().getInt("ItemOpenInvItemID", 280);
                boolean notifySilentChest = plugin.getConfig().getBoolean("NotifySilentChest", true);
                boolean notifyAnyChest = plugin.getConfig().getBoolean("NotifyAnyChest", true);
                plugin.getConfig().set("ItemOpenInvItemID", null);
                plugin.getConfig().set("NotifySilentChest", null);
                plugin.getConfig().set("NotifyAnyChest", null);
                plugin.getConfig().set("config-version", 2);
                plugin.getConfig().set("items.open-inv",
                        getMaterialById(itemOpenInvItemId).toString());
                plugin.getConfig().set("notify.any-chest", notifyAnyChest);
                plugin.getConfig().set("notify.silent-chest", notifySilentChest);
            }
        }.runTask(plugin);

        updateToggles("AnyChest", ".toggle", "toggles.any-chest");
        updateToggles("ItemOpenInv", ".toggle", "toggles.items.open-inv");
        updateToggles("SilentChest", ".toggle", "toggles.silent-chest");
    }

    private void updateToggles(final String sectionName, String suffix, final String newSectionName) {
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

        final Map<String, Boolean> toggles = new HashMap<String, Boolean>();

        for (String playerName : keys) {
            OfflinePlayer player = plugin.matchPlayer(playerName);
            String dataID = plugin.getPlayerID(player);
            toggles.put(dataID, section.getBoolean(playerName + suffix, false));
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                // Wipe old ConfigurationSection
                plugin.getConfig().set(sectionName, null);

                // Prepare new ConfigurationSection
                ConfigurationSection newSection;
                if (plugin.getConfig().isConfigurationSection(newSectionName)) {
                    newSection = plugin.getConfig().getConfigurationSection(newSectionName);
                } else {
                    newSection = plugin.getConfig().createSection(newSectionName);
                }
                // Set new values
                for (Map.Entry<String, Boolean> entry : toggles.entrySet()) {
                    newSection.set(entry.getKey(), entry.getValue());
                }
            }
        }.runTask(plugin);
    }

    private Material getMaterialById(int id) {
        Material material = Material.getMaterial(id);

        if (material == null) {
            material = Material.STICK;
        }

        return material;
    }
}

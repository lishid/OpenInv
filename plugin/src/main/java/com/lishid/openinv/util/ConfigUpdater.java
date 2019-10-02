/*
 * Copyright (C) 2011-2019 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.util;

import com.lishid.openinv.OpenInv;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfigUpdater {

    private final OpenInv plugin;

    public ConfigUpdater(OpenInv plugin) {
        this.plugin = plugin;
    }

    public void checkForUpdates() {
        final int version = plugin.getConfig().getInt("config-version", 1);
        if (version >= plugin.getConfig().getDefaults().getInt("config-version")) {
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
                if (version < 2) {
                    updateConfig1To2();
                }
                if (version < 3) {
                    updateConfig2To3();
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
                plugin.getConfig().set("ItemOpenInv", null);
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
                boolean notifySilentChest = plugin.getConfig().getBoolean("NotifySilentChest", true);
                boolean notifyAnyChest = plugin.getConfig().getBoolean("NotifyAnyChest", true);
                plugin.getConfig().set("ItemOpenInvItemID", null);
                plugin.getConfig().set("NotifySilentChest", null);
                plugin.getConfig().set("NotifyAnyChest", null);
                plugin.getConfig().set("config-version", 2);
                plugin.getConfig().set("notify.any-chest", notifyAnyChest);
                plugin.getConfig().set("notify.silent-chest", notifySilentChest);
            }
        }.runTask(plugin);

        updateToggles("AnyChest", "toggles.any-chest");
        updateToggles("SilentChest", "toggles.silent-chest");
    }

    private void updateToggles(final String sectionName, final String newSectionName) {
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

        final Map<String, Boolean> toggles = new HashMap<>();

        for (String playerName : keys) {
            OfflinePlayer player = plugin.matchPlayer(playerName);
            if (player != null) {
                toggles.put(plugin.getPlayerID(player), section.getBoolean(playerName + ".toggle", false));
            }
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

}

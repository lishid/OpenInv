/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.openinv.commands.*;
import com.lishid.openinv.internal.AnySilentChest;
import com.lishid.openinv.internal.InventoryAccess;
import com.lishid.openinv.internal.PlayerDataManager;
import com.lishid.openinv.internal.SpecialEnderChest;
import com.lishid.openinv.internal.SpecialPlayerInventory;
import com.lishid.openinv.listeners.OpenInvEntityListener;
import com.lishid.openinv.listeners.OpenInvInventoryListener;
import com.lishid.openinv.listeners.OpenInvPlayerListener;

/**
 * Open other player's inventory
 *
 * @author lishid
 */
public class OpenInv extends JavaPlugin {
    public static final Logger logger = Logger.getLogger("Minecraft.OpenInv");

    public static final Map<UUID, SpecialPlayerInventory> inventories = new HashMap<UUID, SpecialPlayerInventory>();
    public static final Map<UUID, SpecialEnderChest> enderChests = new HashMap<UUID, SpecialEnderChest>();

    public static OpenInv mainPlugin;

    public static PlayerDataManager playerLoader;
    public static InventoryAccess inventoryAccess;
    public static AnySilentChest anySilentChest;

    @Override
    public void onEnable() {
        // Get plugin manager
        PluginManager pm = getServer().getPluginManager();

        playerLoader = new PlayerDataManager();
        inventoryAccess = new InventoryAccess();
        anySilentChest = new AnySilentChest();

        mainPlugin = this;
        FileConfiguration config = getConfig();
        config.set("CheckForUpdates", config.getBoolean("CheckForUpdates", true));
        config.set("NotifySilentChest", config.getBoolean("NotifySilentChest", true));
        config.set("NotifyAnyChest", config.getBoolean("NotifyAnyChest", true));
        config.set("ItemOpenInvItemID", config.getInt("ItemOpenInvItemID", 280));
        config.addDefault("ItemOpenInvItemID", 280);
        config.addDefault("CheckForUpdates", true);
        config.addDefault("NotifySilentChest", true);
        config.addDefault("NotifyAnyChest", true);
        config.options().copyDefaults(true);
        saveConfig();

        pm.registerEvents(new OpenInvPlayerListener(), this);
        pm.registerEvents(new OpenInvEntityListener(), this);
        pm.registerEvents(new OpenInvInventoryListener(), this);

        getCommand("openinv").setExecutor(new OpenInvPluginCommand(this));
        getCommand("searchinv").setExecutor(new SearchInvPluginCommand());
        getCommand("toggleopeninv").setExecutor(new ToggleOpenInvPluginCommand());
        getCommand("silentchest").setExecutor(new SilentChestPluginCommand());
        getCommand("anychest").setExecutor(new AnyChestPluginCommand());
        getCommand("openender").setExecutor(new OpenEnderPluginCommand(this));
    }

    public static boolean notifySilentChest() {
        return mainPlugin.getConfig().getBoolean("NotifySilentChest", true);
    }

    public static boolean notifyAnyChest() {
        return mainPlugin.getConfig().getBoolean("NotifyAnyChest", true);
    }

    public static boolean getPlayerItemOpenInvStatus(String name) {
        return mainPlugin.getConfig().getBoolean("ItemOpenInv." + name.toLowerCase() + ".toggle", false);
    }

    public static void setPlayerItemOpenInvStatus(String name, boolean status) {
        mainPlugin.getConfig().set("ItemOpenInv." + name.toLowerCase() + ".toggle", status);
        mainPlugin.saveConfig();
    }

    public static boolean getPlayerSilentChestStatus(String name) {
        return mainPlugin.getConfig().getBoolean("SilentChest." + name.toLowerCase() + ".toggle", false);
    }

    public static void setPlayerSilentChestStatus(String name, boolean status) {
        mainPlugin.getConfig().set("SilentChest." + name.toLowerCase() + ".toggle", status);
        mainPlugin.saveConfig();
    }

    public static boolean getPlayerAnyChestStatus(String name) {
        return mainPlugin.getConfig().getBoolean("AnyChest." + name.toLowerCase() + ".toggle", true);
    }

    public static void setPlayerAnyChestStatus(String name, boolean status) {
        mainPlugin.getConfig().set("AnyChest." + name.toLowerCase() + ".toggle", status);
        mainPlugin.saveConfig();
    }

    public static int getItemOpenInvItem() {
        if (mainPlugin.getConfig().get("ItemOpenInvItemID") == null) {
            saveToConfig("ItemOpenInvItemID", 280);
        }
        return mainPlugin.getConfig().getInt("ItemOpenInvItemID", 280);
    }

    public static Object getFromConfig(String data, Object defaultValue) {
        Object val = mainPlugin.getConfig().get(data);
        if (val == null) {
            mainPlugin.getConfig().set(data, defaultValue);
            return defaultValue;
        }
        else {
            return val;
        }
    }

    public static void saveToConfig(String data, Object value) {
        mainPlugin.getConfig().set(data, value);
        mainPlugin.saveConfig();
    }

    /**
     * Log an information
     */
    public static void log(String text) {
        logger.info("[OpenInv] " + text);
    }

    /**
     * Log an error
     */
    public static void log(Throwable e) {
        logger.severe("[OpenInv] " + e.toString());
        e.printStackTrace();
    }

    public static void showHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "/openinv <Player> - Open a player's inventory");
        player.sendMessage(ChatColor.GREEN + "   (aliases: oi, inv, open)");
        player.sendMessage(ChatColor.GREEN + "/openender <Player> - Open a player's enderchest");
        player.sendMessage(ChatColor.GREEN + "   (aliases: oe, enderchest)");
        player.sendMessage(ChatColor.GREEN + "/toggleopeninv - Toggle item openinv function");
        player.sendMessage(ChatColor.GREEN + "   (aliases: toi, toggleoi, toggleinv)");
        player.sendMessage(ChatColor.GREEN + "/searchinv <Item> [MinAmount] - ");
        player.sendMessage(ChatColor.GREEN + "   Search and list players having a specific item.");
        player.sendMessage(ChatColor.GREEN + "   (aliases: si, search)");
        player.sendMessage(ChatColor.GREEN + "/anychest - Toggle anychest function");
        player.sendMessage(ChatColor.GREEN + "   (aliases: ac)");
        player.sendMessage(ChatColor.GREEN + "/silentchest - Toggle silent chest function");
        player.sendMessage(ChatColor.GREEN + "   (aliases: sc, silent)");
    }

    public static boolean hasPermission(Permissible player, String permission) {
        String[] parts = permission.split("\\.");
        String perm = "";
        for (int i = 0; i < parts.length; i++) {
            if (player.hasPermission(perm + "*")) {
                return true;
            }
            perm += parts[i] + ".";
        }
        return player.hasPermission(permission);
    }
}

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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.openinv.commands.AnyChestCommand;
import com.lishid.openinv.commands.OpenEnderCommand;
import com.lishid.openinv.commands.OpenInvCommand;
import com.lishid.openinv.commands.SearchInvCommand;
import com.lishid.openinv.commands.SilentChestCommand;
import com.lishid.openinv.commands.ToggleOpenInvCommand;
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
    public static final Map<UUID, SpecialPlayerInventory> inventories = new HashMap<UUID, SpecialPlayerInventory>();
    public static final Map<UUID, SpecialEnderChest> enderChests = new HashMap<UUID, SpecialEnderChest>();

    public static OpenInv mainPlugin;

    private static PlayerDataManager playerLoader;
    private static InventoryAccess inventoryAccess;
    private static AnySilentChest anySilentChest;

    @Override
    public void onEnable() {
        // Plugin
        mainPlugin = this;

        // Config
        ConfigUpdater configUpdater = new ConfigUpdater(this);
        configUpdater.checkForUpdates();

        // Initialize
        playerLoader = new PlayerDataManager();
        inventoryAccess = new InventoryAccess();
        anySilentChest = new AnySilentChest();

        // Save the default config.yml if it doesn't already exist
        saveDefaultConfig();

        // Register the plugin's events & commands
        registerEvents();
        registerCommands();
    }

    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new OpenInvPlayerListener(), this);
        pm.registerEvents(new OpenInvEntityListener(), this);
        pm.registerEvents(new OpenInvInventoryListener(), this);
    }

    private void registerCommands() {
        getCommand("openinv").setExecutor(new OpenInvCommand(this));
        getCommand("searchinv").setExecutor(new SearchInvCommand());
        getCommand("toggleopeninv").setExecutor(new ToggleOpenInvCommand());
        getCommand("silentchest").setExecutor(new SilentChestCommand());
        getCommand("anychest").setExecutor(new AnyChestCommand());
        getCommand("openender").setExecutor(new OpenEnderCommand(this));
    }

    public static PlayerDataManager getPlayerLoader() {
        return playerLoader;
    }

    public static InventoryAccess getInventoryAccess() {
        return inventoryAccess;
    }

    public static AnySilentChest getAnySilentChest() {
        return anySilentChest;
    }

    /*
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
    */

    public static void saveToConfig(String data, Object value) {
        mainPlugin.getConfig().set(data, value);
        mainPlugin.saveConfig();
    }

    public static Material getOpenInvItem() {
        if (!mainPlugin.getConfig().isSet("items.open-inv")) {
            saveToConfig("items.open-inv", "STICK");
        }

        String itemName = mainPlugin.getConfig().getString("items.open-inv", "STICK");
        Material material = Material.getMaterial(itemName);
        if (material == null) {
            mainPlugin.getLogger().warning("OpenInv item '" + itemName + "' does not match to a valid item. Defaulting to stick.");
            material = Material.STICK;
        }

        return material;
    }

    public static boolean notifySilentChest() {
        return mainPlugin.getConfig().getBoolean("notify.silent-chest", true);
    }

    public static boolean notifyAnyChest() {
        return mainPlugin.getConfig().getBoolean("notify.any-chest", true);
    }

    public static boolean getPlayerAnyChestStatus(Player player) {
        return mainPlugin.getConfig().getBoolean("toggles.any-chest." + player.getUniqueId(), false);
    }

    public static void setPlayerAnyChestStatus(Player player, boolean status) {
        saveToConfig("toggles.any-chest." + player.getUniqueId(), status);
    }

    public static boolean getPlayerItemOpenInvStatus(Player player) {
        return mainPlugin.getConfig().getBoolean("toggles.items.open-inv" + player.getUniqueId(), false);
    }

    public static void setPlayerItemOpenInvStatus(Player player, boolean status) {
        saveToConfig("toggles.items.open-inv." + player.getUniqueId(), status);
    }

    public static boolean getPlayerSilentChestStatus(Player player) {
        return mainPlugin.getConfig().getBoolean("toggles.silent-chest." + player.getUniqueId(), false);
    }

    public static void setPlayerSilentChestStatus(Player player, boolean status) {
        saveToConfig("toggles.silent-chest." + player.getUniqueId(), status);
    }

    /**
     * Logs a given message to console.
     *
     * @param text the text to log
     */
    public static void log(String text) {
        mainPlugin.getLogger().info("[OpenInv] " + text);
    }

    /**
     * Logs an error to console.
     *
     * @param e the throwable error to log
     */
    public static void log(Throwable e) {
        mainPlugin.getLogger().severe("[OpenInv] " + e.toString());
        e.printStackTrace();
    }

    /**
     * Sends a specified message to a given CommandSender with the OpenInv prefix.
     *
     * @param sender the CommandSender to message
     * @param message the message to send to the player
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.AQUA + "[OpenInv] " + ChatColor.WHITE + message);
    }

    public static void showHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "/openinv <player> - Open a player's inventory");
        player.sendMessage(ChatColor.GREEN + "   (aliases: oi, inv, open)");
        player.sendMessage(ChatColor.GREEN + "/openender <player> - Open a player's ender chest");
        player.sendMessage(ChatColor.GREEN + "   (aliases: oe, enderchest)");
        player.sendMessage(ChatColor.GREEN + "/toggleopeninv - Toggle item openinv function");
        player.sendMessage(ChatColor.GREEN + "   (aliases: toi, toggleoi, toggleinv)");
        player.sendMessage(ChatColor.GREEN + "/searchinv <item> [minAmount] - ");
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

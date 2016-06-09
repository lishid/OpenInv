/*
 * Copyright (C) 2011-2016 lishid.  All rights reserved.
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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.openinv.commands.AnyChestCommand;
import com.lishid.openinv.commands.OpenEnderCommand;
import com.lishid.openinv.commands.OpenInvCommand;
import com.lishid.openinv.commands.SearchEnderCommand;
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

public class OpenInv extends JavaPlugin {

    private final Map<UUID, SpecialPlayerInventory> inventories = new HashMap<UUID, SpecialPlayerInventory>();
    private final Map<UUID, SpecialEnderChest> enderChests = new HashMap<UUID, SpecialEnderChest>();

    private Configuration configuration;

    private PlayerDataManager playerLoader;
    private InventoryAccess inventoryAccess;
    private AnySilentChest anySilentChest;

    @Override
    public void onEnable() {
        // Save the default config.yml if it doesn't already exist
        saveDefaultConfig();

        // Config
        configuration = new Configuration(this);

        // Initialize
        playerLoader = new PlayerDataManager(this);
        inventoryAccess = new InventoryAccess(this);
        anySilentChest = new AnySilentChest(this);

        // Register the plugin's events
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new OpenInvPlayerListener(this), this);
        pm.registerEvents(new OpenInvEntityListener(this), this);
        pm.registerEvents(new OpenInvInventoryListener(this), this);

        // Register the plugin's commands
        getCommand("openinv").setExecutor(new OpenInvCommand(this));
        getCommand("openender").setExecutor(new OpenEnderCommand(this));
        getCommand("searchinv").setExecutor(new SearchInvCommand(this));
        getCommand("searchender").setExecutor(new SearchEnderCommand());
        getCommand("toggleopeninv").setExecutor(new ToggleOpenInvCommand(this));
        getCommand("anychest").setExecutor(new AnyChestCommand(this));
        getCommand("silentchest").setExecutor(new SilentChestCommand(this));
    }

    /**
     * Returns the plugin Configuration.
     *
     * @return the plugin Configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns an instance of PlayerDataManager.
     *
     * @return an instance of PlayerDataManager
     */
    public PlayerDataManager getPlayerLoader() {
        return playerLoader;
    }

    /**
     * Returns an instance of InventoryAccess.
     *
     * @return an instance of InventoryAccess
     */
    public InventoryAccess getInventoryAccess() {
        return inventoryAccess;
    }

    /**
     * Returns an instance of AnySilentChest.
     *
     * @return an instance of AnySilentChest
     */
    public AnySilentChest getAnySilentChest() {
        return anySilentChest;
    }

    /**
     * Returns a player's SpecialPlayerInventory.
     *
     * @param player the player to get the SpecialPlayerInventory of
     * @param createIfNull whether or not to create it if it doesn't exist
     * @return the player's SpecialPlayerInventory or null
     */
    public SpecialPlayerInventory getPlayerInventory(Player player, boolean createIfNull) {
        SpecialPlayerInventory inventory = inventories.get(player.getUniqueId());

        if (inventory == null && createIfNull) {
            inventory = new SpecialPlayerInventory(player, player.isOnline());
            inventories.put(player.getUniqueId(), inventory);
        }

        return inventory;
    }

    /**
     * Returns a player's SpecialEnderChest.
     *
     * @param player the player to get the SpecialEnderChest of
     * @param createIfNull whether or not to create it if it doesn't exist
     * @return the player's SpecialEnderChest or null
     */
    public SpecialEnderChest getPlayerEnderChest(Player player, boolean createIfNull) {
        SpecialEnderChest enderChest = enderChests.get(player.getUniqueId());

        if (enderChest == null && createIfNull) {
            enderChest = new SpecialEnderChest(player, player.isOnline());
            enderChests.put(player.getUniqueId(), enderChest);
        }

        return enderChest;
    }

    /**
     * Removes a player's loaded inventory if it exists.
     *
     * @param player the player to remove the loaded inventory of
     */
    public void removeLoadedInventory(Player player) {
        if (inventories.containsKey(player.getUniqueId())) {
            inventories.remove(player.getUniqueId());
        }
    }

    /**
     * Removes a player's loaded ender chest if it exists.
     *
     * @param player the player to remove the loaded ender chest of
     */
    public void removeLoadedEnderChest(Player player) {
        if (enderChests.containsKey(player.getUniqueId())) {
            enderChests.remove(player.getUniqueId());
        }
    }

    /**
     * Logs a message to console.
     *
     * @param text the message to log
     */
    public void log(String text) {
        getLogger().info(text);
    }

    /**
     * Logs a Throwable to console.
     *
     * @param e the Throwable to log
     */
    public void log(Throwable e) {
        getLogger().severe(e.toString());
        e.printStackTrace();
    }

    /**
     * Sends an OpenInv message to a player.
     *
     * @param sender the CommandSender to message
     * @param message the message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.AQUA + "[OpenInv] " + ChatColor.WHITE + message);
    }

    /**
     * Outputs OpenInv help information to a CommandSender.
     *
     * @param sender the CommandSender to show help to
     */
    public static void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "/openinv <player> - Opens a player's inventory.");
        sender.sendMessage(ChatColor.GREEN + "   (aliases: oi, inv, open)");

        sender.sendMessage(ChatColor.GREEN + "/openender <player> - Opens a player's ender chest.");
        sender.sendMessage(ChatColor.GREEN + "   (aliases: oe)");

        sender.sendMessage(ChatColor.GREEN + "/searchinv <item> [minAmount] -");
        sender.sendMessage(ChatColor.GREEN + "   Searches and lists players that have a specific item in their inventory.");
        sender.sendMessage(ChatColor.GREEN + "   (aliases: si)");

        sender.sendMessage(ChatColor.GREEN + "/searchender <item> [minAmount] -");
        sender.sendMessage(ChatColor.GREEN + "   Searches and lists players that have a specific item in their ender chest.");
        sender.sendMessage(ChatColor.GREEN + "   (aliases: se)");

        sender.sendMessage(ChatColor.GREEN + "/toggleopeninv - Toggles the item openinv function.");
        sender.sendMessage(ChatColor.GREEN + "   (aliases: toi, toggleoi, toggleinv)");

        sender.sendMessage(ChatColor.GREEN + "/anychest - Toggles the any chest function.");
        sender.sendMessage(ChatColor.GREEN + "   (aliases: ac)");

        sender.sendMessage(ChatColor.GREEN + "/silentchest - Toggles the silent chest function.");
        sender.sendMessage(ChatColor.GREEN + "   (aliases: sc, silent)");
    }

    /**
     * Returns whether or not a player has a permission.
     *
     * @param player the player to check
     * @param permission the permission node to check for
     * @return true if the player has the permission; false otherwise
     */
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

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

    public static final Map<UUID, SpecialPlayerInventory> inventories = new HashMap<UUID, SpecialPlayerInventory>();
    public static final Map<UUID, SpecialEnderChest> enderChests = new HashMap<UUID, SpecialEnderChest>();

    private PlayerDataManager playerLoader;
    private InventoryAccess inventoryAccess;
    private AnySilentChest anySilentChest;

    private Configuration configuration;

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
     * Outputs OpenInv help information to a player.
     *
     * @param player the player to show help to
     */
    public static void showHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "/openinv <player> - Opens a player's inventory.");
        player.sendMessage(ChatColor.GREEN + "   (aliases: oi, inv, open)");

        player.sendMessage(ChatColor.GREEN + "/openender <player> - Opens a player's ender chest.");
        player.sendMessage(ChatColor.GREEN + "   (aliases: oe)");

        player.sendMessage(ChatColor.GREEN + "/searchinv <item> [minAmount] -");
        player.sendMessage(ChatColor.GREEN + "   Searches and lists players that have a specific item in their inventory.");
        player.sendMessage(ChatColor.GREEN + "   (aliases: si)");

        player.sendMessage(ChatColor.GREEN + "/searchender <item> [minAmount] -");
        player.sendMessage(ChatColor.GREEN + "   Searches and lists players that have a specific item in their ender chest.");
        player.sendMessage(ChatColor.GREEN + "   (aliases: se)");

        player.sendMessage(ChatColor.GREEN + "/toggleopeninv - Toggles the item openinv function.");
        player.sendMessage(ChatColor.GREEN + "   (aliases: toi, toggleoi, toggleinv)");

        player.sendMessage(ChatColor.GREEN + "/anychest - Toggles the any chest function.");
        player.sendMessage(ChatColor.GREEN + "   (aliases: ac)");

        player.sendMessage(ChatColor.GREEN + "/silentchest - Toggles the silent chest function.");
        player.sendMessage(ChatColor.GREEN + "   (aliases: sc, silent)");
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

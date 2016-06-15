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

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.lishid.openinv.commands.AnyChestPluginCommand;
import com.lishid.openinv.commands.OpenEnderPluginCommand;
import com.lishid.openinv.commands.OpenInvPluginCommand;
import com.lishid.openinv.commands.SearchInvPluginCommand;
import com.lishid.openinv.commands.SilentChestPluginCommand;
import com.lishid.openinv.internal.IAnySilentChest;
import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.InternalAccessor;
import com.lishid.openinv.internal.PlayerDataManager;

/**
 * Open other player's inventory
 * 
 * @author lishid
 */
public class OpenInv extends JavaPlugin {

    private final Map<String, ISpecialPlayerInventory> inventories = new HashMap<String, ISpecialPlayerInventory>();
    private final Map<String, ISpecialEnderChest> enderChests = new HashMap<String, ISpecialEnderChest>();

    private InternalAccessor accessor;
    private PlayerDataManager playerLoader;
    private IInventoryAccess inventoryAccess;
    private IAnySilentChest anySilentChest;

    @Override
    public void onEnable() {
        // Get plugin manager
        PluginManager pm = getServer().getPluginManager();

        accessor = new InternalAccessor(this);
        // Version check
        if (!accessor.initialize(getServer())) {
            getLogger().info("Your version of CraftBukkit is not supported.");
            getLogger().info("Please look for an updated version of OpenInv.");
            pm.disablePlugin(this);
            return;
        }

        playerLoader = accessor.newPlayerDataManager();
        inventoryAccess = accessor.newInventoryAccess();
        anySilentChest = accessor.newAnySilentChest();

        FileConfiguration config = getConfig();
        boolean dirtyConfig = false;
        if (!config.isBoolean("NotifySilentChest")) {
            config.set("NotifySilentChest", true);
            dirtyConfig = true;
        }
        if (!config.isBoolean("NotifyAnyChest")) {
            config.set("NotifyAnyChest", true);
            dirtyConfig = true;
        }
        if (!config.isBoolean("DisableSaving")) {
            config.set("DisableSaving", false);
            dirtyConfig = true;
        }
        config.addDefault("NotifySilentChest", true);
        config.addDefault("NotifyAnyChest", true);
        config.addDefault("DisableSaving", false);
        config.options().copyDefaults(true);
        if (dirtyConfig) {
            saveConfig();
        }

        pm.registerEvents(new OpenInvPlayerListener(this), this);
        pm.registerEvents(new OpenInvInventoryListener(this), this);

        getCommand("openinv").setExecutor(new OpenInvPluginCommand(this));
        getCommand("searchinv").setExecutor(new SearchInvPluginCommand());
        getCommand("silentchest").setExecutor(new SilentChestPluginCommand(this));
        getCommand("anychest").setExecutor(new AnyChestPluginCommand(this));
        getCommand("openender").setExecutor(new OpenEnderPluginCommand(this));

    }

    public InternalAccessor getInternalAccessor() {
        return this.accessor;
    }

    public PlayerDataManager getPlayerLoader() {
        return this.playerLoader;
    }

    public IInventoryAccess getInventoryAccess() {
        return this.inventoryAccess;
    }

    public IAnySilentChest getAnySilentChest() {
        return this.anySilentChest;
    }

    public ISpecialPlayerInventory getInventoryFor(Player player) {
        String id = getPlayerLoader().getPlayerDataID(player);
        if (inventories.containsKey(id)) {
            return inventories.get(id);
        }
        return null;
    }

    public ISpecialPlayerInventory getOrCreateInventoryFor(Player player, boolean online) {
        String id = getPlayerLoader().getPlayerDataID(player);
        if (inventories.containsKey(id)) {
            return inventories.get(id);
        }
        ISpecialPlayerInventory inv = getInternalAccessor().newSpecialPlayerInventory(player, online);
        inventories.put(id, inv);
        return inv;
    }

    public void removeLoadedInventory(Player player) {
        String id = getPlayerLoader().getPlayerDataID(player);
        if (inventories.containsKey(id)) {
            inventories.remove(id);
        }
    }

    public ISpecialEnderChest getEnderChestFor(Player player) {
        String id = getPlayerLoader().getPlayerDataID(player);
        if (enderChests.containsKey(id)) {
            return enderChests.get(id);
        }
        return null;
    }

    public ISpecialEnderChest getOrCreateEnderChestFor(Player player, boolean online) {
        String id = getPlayerLoader().getPlayerDataID(player);
        if (enderChests.containsKey(id)) {
            return enderChests.get(id);
        }
        ISpecialEnderChest inv = getInternalAccessor().newSpecialEnderChest(player, online);
        enderChests.put(id, inv);
        return inv;
    }

    public void removeLoadedEnderChest(Player player) {
        String id = getPlayerLoader().getPlayerDataID(player);
        if (enderChests.containsKey(id)) {
            enderChests.remove(id);
        }
    }

    public boolean disableSaving() {
        return getConfig().getBoolean("DisableSaving", false);
    }

    public boolean notifySilentChest() {
        return getConfig().getBoolean("NotifySilentChest", true);
    }

    public boolean notifyAnyChest() {
        return getConfig().getBoolean("NotifyAnyChest", true);
    }

    public boolean getPlayerSilentChestStatus(OfflinePlayer player) {
        return getConfig().getBoolean("SilentChest." + getPlayerLoader().getPlayerDataID(player) + ".toggle", false);
    }

    public void setPlayerSilentChestStatus(OfflinePlayer player, boolean status) {
        getConfig().set("SilentChest." + getPlayerLoader().getPlayerDataID(player) + ".toggle", status);
        saveConfig();
    }

    public boolean getPlayerAnyChestStatus(OfflinePlayer player) {
        return getConfig().getBoolean("AnyChest." + getPlayerLoader().getPlayerDataID(player) + ".toggle", true);
    }

    public void setPlayerAnyChestStatus(OfflinePlayer player, boolean status) {
        getConfig().set("AnyChest." + getPlayerLoader().getPlayerDataID(player) + ".toggle", status);
        saveConfig();
    }

    /**
     * Get an OfflinePlayer by name.
     * 
     * @param name the name of the Player
     * @return the OfflinePlayer, or null if no players have ever logged in
     */
    public OfflinePlayer matchPlayer(String name) {

        // Warn if called on the main thread - if we resort to searching offline players, this may take several seconds.
        if (getServer().isPrimaryThread()) {
            getLogger().warning("Call to OpenInv#matchPlayer made on the main thread!");
            getLogger().warning("This can cause the server to hang, potentially severely.");
            getLogger().warning("Trace:");
            for (StackTraceElement element : new Throwable().fillInStackTrace().getStackTrace()) {
                getLogger().warning(element.toString());
            }
        }

        OfflinePlayer player = getServer().getPlayerExact(name);

        if (player != null) {
            return player;
        }

        player = getServer().getOfflinePlayer(name);

        /*
         * Compatibility: Pre-UUID, getOfflinePlayer always returns an OfflinePlayer. Post-UUID,
         * getOfflinePlayer will return null if no matching player is found. To preserve
         * compatibility, only return the player if they have played before. Ignoring current online
         * status is fine, they'd have been found by getPlayerExact otherwise.
         */
        if (player != null && player.hasPlayedBefore()) {
            return player;
        }

        player = getServer().getPlayer(name);

        if (player != null) {
            return player;
        }

        int bestMatch = Integer.MAX_VALUE;
        for (OfflinePlayer offline : getServer().getOfflinePlayers()) {
            if (offline.getName() == null) {
                // Loaded by UUID only, name has never been looked up.
                continue;
            }

            // Compatibility: Lang3 is only bundled with 1.8+
            int currentMatch = org.apache.commons.lang.StringUtils.getLevenshteinDistance(name, offline.getName());

            if (currentMatch == 0) {
                return offline;
            }

            if (currentMatch < bestMatch) {
                bestMatch = currentMatch;
                player = offline;
            }
        }

        // Only null if no players have played ever, otherwise even the worst match will do.
        return player;
    }

    public static void ShowHelp(Player player) {
        player.sendMessage(ChatColor.GREEN + "/openinv <Player> - Open a player's inventory");
        player.sendMessage(ChatColor.GREEN + "   (aliases: oi, inv, open)");
        player.sendMessage(ChatColor.GREEN + "/openender <Player> - Open a player's enderchest");
        player.sendMessage(ChatColor.GREEN + "   (aliases: oe, enderchest)");
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

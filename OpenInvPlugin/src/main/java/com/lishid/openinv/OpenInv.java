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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.lishid.openinv.commands.AnyChestPluginCommand;
import com.lishid.openinv.commands.OpenEnderPluginCommand;
import com.lishid.openinv.commands.OpenInvPluginCommand;
import com.lishid.openinv.commands.SearchInvPluginCommand;
import com.lishid.openinv.commands.SilentChestPluginCommand;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.InternalAccessor;
import com.lishid.openinv.util.Cache;
import com.lishid.openinv.util.Function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Open other player's inventory
 * 
 * @author lishid
 */
public class OpenInv extends JavaPlugin {

    private final Map<String, ISpecialPlayerInventory> inventories = new HashMap<String, ISpecialPlayerInventory>();
    private final Map<String, ISpecialEnderChest> enderChests = new HashMap<String, ISpecialEnderChest>();
    private final Cache<String, Player> playerCache = new Cache<String, Player>(300000L,
            new Function<Player>() {
                @Override
                public boolean run(Player value) {
                    String key = playerLoader.getPlayerDataID(value);
                    return inventories.containsKey(key) && inventories.get(key).isInUse()
                            || enderChests.containsKey(key) && enderChests.get(key).isInUse();
                }
            },
            new Function<Player>() {
                @Override
                public boolean run(Player value) {
                    String key = playerLoader.getPlayerDataID(value);

                    // Check if inventory is stored, and if it is, remove it and eject all viewers
                    if (inventories.containsKey(key)) {
                        Inventory inv = inventories.remove(key).getBukkitInventory();
                        for (HumanEntity entity : inv.getViewers()) {
                            entity.closeInventory();
                        }
                    }

                    // Check if ender chest is stored, and if it is, remove it and eject all viewers
                    if (enderChests.containsKey(key)) {
                        Inventory inv = enderChests.remove(key).getBukkitInventory();
                        for (HumanEntity entity : inv.getViewers()) {
                            entity.closeInventory();
                        }
                    }

                    if (!OpenInv.this.disableSaving() && !value.isOnline()) {
                        value.saveData();
                    }
                    return true;
                }
            });

    private InternalAccessor accessor;
    private IPlayerDataManager playerLoader;
    private IInventoryAccess inventoryAccess;
    private IAnySilentContainer anySilentContainer;

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
        anySilentContainer = accessor.newAnySilentContainer();

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

    @Override
    public void onDisable() {

        if (this.disableSaving()) {
            return;
        }

        this.playerCache.invalidateAll();
    }

    public IInventoryAccess getInventoryAccess() {
        return this.inventoryAccess;
    }

    public IAnySilentContainer getAnySilentContainer() {
        return this.anySilentContainer;
    }

    /**
     * @deprecated Use {@link #getAnySilentContainer()}
     */
    @Deprecated
    public com.lishid.openinv.internal.IAnySilentChest getAnySilentChest() {
        return this.getAnySilentContainer();
    }

    public ISpecialPlayerInventory getInventoryFor(Player player, boolean online) {
        String id = playerLoader.getPlayerDataID(player);
        if (inventories.containsKey(id)) {
            return inventories.get(id);
        }
        ISpecialPlayerInventory inv = accessor.newSpecialPlayerInventory(player, online);
        inventories.put(id, inv);
        playerCache.put(id, player);
        return inv;
    }

    public ISpecialEnderChest getEnderChestFor(Player player, boolean online) {
        String id = playerLoader.getPlayerDataID(player);
        if (enderChests.containsKey(id)) {
            return enderChests.get(id);
        }
        ISpecialEnderChest inv = accessor.newSpecialEnderChest(player, online);
        enderChests.put(id, inv);
        playerCache.put(id, player);
        return inv;
    }

    /**
     * Unload a cached Player's data.
     * 
     * @param player the OfflinePlayer to unload
     */
    public void unload(OfflinePlayer player) {
        this.playerCache.invalidate(this.playerLoader.getPlayerDataID(player));
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
        return getConfig().getBoolean("SilentChest." + playerLoader.getPlayerDataID(player) + ".toggle", false);
    }

    public void setPlayerSilentChestStatus(OfflinePlayer player, boolean status) {
        getConfig().set("SilentChest." + playerLoader.getPlayerDataID(player) + ".toggle", status);
        saveConfig();
    }

    public boolean getPlayerAnyChestStatus(OfflinePlayer player) {
        return getConfig().getBoolean("AnyChest." + playerLoader.getPlayerDataID(player) + ".toggle", true);
    }

    public void setPlayerAnyChestStatus(OfflinePlayer player, boolean status) {
        getConfig().set("AnyChest." + playerLoader.getPlayerDataID(player) + ".toggle", status);
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

        // Ensure name is valid if server is in online mode to avoid unnecessary searching
        if (getServer().getOnlineMode() && !name.matches("[a-zA-Z0-9_]{3,16}")) {
            return null;
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

    /**
     * Load a Player from an OfflinePlayer.
     * 
     * @param offline the OfflinePlayer to load a Player for
     * @return the Player
     */
    public Player loadPlayer(final OfflinePlayer offline) {

        if (offline == null) {
            return null;
        }

        String key = this.playerLoader.getPlayerDataID(offline);
        if (this.playerCache.containsKey(key)) {
            return this.playerCache.get(key);
        }

        Player loaded;

        if (offline.isOnline()) {
            loaded = offline.getPlayer();
            this.playerCache.put(key, loaded);
            return loaded;
        }

        if (Bukkit.isPrimaryThread()) {
            return this.playerLoader.loadPlayer(offline);
        }

        Future<Player> future = Bukkit.getScheduler().callSyncMethod(this,
                new Callable<Player>() {
                    @Override
                    public Player call() throws Exception {
                        return playerLoader.loadPlayer(offline);
                    }
                });

        int ticks = 0;
        while (!future.isDone() && !future.isCancelled() && ticks < 10) {
            ++ticks;
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }

        if (!future.isDone() || future.isCancelled()) {
            return null;
        }

        try {
            loaded = future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        if (loaded != null) {
            this.playerCache.put(key, loaded);
        }

        return loaded;
    }

    /**
     * Method for handling a Player coming online.
     * 
     * @param player
     */
    public void setPlayerOnline(final Player player) {

        String key = this.playerLoader.getPlayerDataID(player);

        // Check if the player is cached. If not, neither of their inventories is open.
        if (!this.playerCache.containsKey(key)) {
            return;
        }

        this.playerCache.put(key, player);

        if (this.inventories.containsKey(key)) {
            this.inventories.get(key).setPlayerOnline(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.updateInventory();
                    }
                }
            }.runTask(this);
        }

        if (this.enderChests.containsKey(key)) {
            this.enderChests.get(key).setPlayerOnline(player);
        }
    }

    /**
     * Method for handling a Player going offline.
     * 
     * @param player
     */
    public void setPlayerOffline(final Player player) {

        String key = this.playerLoader.getPlayerDataID(player);

        // Check if the player is cached. If not, neither of their inventories is open.
        if (!this.playerCache.containsKey(key)) {
            return;
        }

        if (this.inventories.containsKey(key)) {
            this.inventories.get(key).setPlayerOffline();
        }

        if (this.enderChests.containsKey(key)) {
            this.enderChests.get(key).setPlayerOffline();
        }
    }

    public static void ShowHelp(Player player) {
        // TODO: Do not show commands players lack permissions for
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

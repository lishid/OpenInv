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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.lishid.openinv.commands.AnyChestPluginCommand;
import com.lishid.openinv.commands.OpenEnderPluginCommand;
import com.lishid.openinv.commands.OpenInvPluginCommand;
import com.lishid.openinv.commands.SearchEnchantPluginCommand;
import com.lishid.openinv.commands.SearchInvPluginCommand;
import com.lishid.openinv.commands.SilentChestPluginCommand;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.listeners.InventoryClickListener;
import com.lishid.openinv.listeners.InventoryDragListener;
import com.lishid.openinv.listeners.PlayerListener;
import com.lishid.openinv.listeners.PluginListener;
import com.lishid.openinv.util.Cache;
import com.lishid.openinv.util.ConfigUpdater;
import com.lishid.openinv.util.Function;
import com.lishid.openinv.util.InternalAccessor;
import com.lishid.openinv.util.Permissions;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Open other player's inventory
 * 
 * @author lishid
 */
public class OpenInv extends JavaPlugin implements IOpenInv {

    private final Map<String, ISpecialPlayerInventory> inventories = new HashMap<String, ISpecialPlayerInventory>();
    private final Map<String, ISpecialEnderChest> enderChests = new HashMap<String, ISpecialEnderChest>();
    private final Multimap<String, Class<? extends Plugin>> pluginUsage = HashMultimap.create();

    private final Cache<String, Player> playerCache = new Cache<String, Player>(300000L,
            new Function<Player>() {
                @Override
                public boolean run(Player value) {
                    String key = playerLoader.getPlayerDataID(value);
                    return inventories.containsKey(key) && inventories.get(key).isInUse()
                            || enderChests.containsKey(key) && enderChests.get(key).isInUse()
                            || pluginUsage.containsKey(key);
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
        if (!accessor.isSupported()) {
            getLogger().info("Your version of CraftBukkit (" + accessor.getVersion() + ") is not supported.");
            getLogger().info("Please look for an updated version of OpenInv.");
            pm.disablePlugin(this);
            return;
        }

        playerLoader = accessor.newPlayerDataManager();
        inventoryAccess = accessor.newInventoryAccess();
        anySilentContainer = accessor.newAnySilentContainer();

        new ConfigUpdater(this).checkForUpdates();

        // Register listeners
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new PluginListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        // Bukkit will handle missing events for us, attempt to register InventoryDragEvent without a version check
        pm.registerEvents(new InventoryDragListener(this), this);

        // Register commands to their executors
        getCommand("openinv").setExecutor(new OpenInvPluginCommand(this));
        getCommand("openender").setExecutor(new OpenEnderPluginCommand(this));
        SearchInvPluginCommand searchInv = new SearchInvPluginCommand(this);
        getCommand("searchinv").setExecutor(searchInv);
        getCommand("searchender").setExecutor(searchInv);
        getCommand("searchenchant").setExecutor(new SearchEnchantPluginCommand(this));
        getCommand("silentchest").setExecutor(new SilentChestPluginCommand(this));
        getCommand("anychest").setExecutor(new AnyChestPluginCommand(this));

    }

    @Override
    public void onDisable() {

        if (this.disableSaving()) {
            return;
        }

        this.playerCache.invalidateAll();
    }

    /**
     * Checks if the server version is supported by OpenInv.
     * 
     * @return true if the server version is supported
     */
    @Override
    public boolean isSupportedVersion() {
        return this.accessor != null && this.accessor.isSupported();
    }

    /**
     * Gets the active IInventoryAccess implementation. May return null if the server version is
     * unsupported.
     * 
     * @return the IInventoryAccess
     */
    @Override
    public IInventoryAccess getInventoryAccess() {
        return this.inventoryAccess;
    }

    /**
     * Gets the active ISilentContainer implementation. May return null if the server version is
     * unsupported.
     * 
     * @return the ISilentContainer
     */
    @Override
    public IAnySilentContainer getAnySilentContainer() {
        return this.anySilentContainer;
    }

    /**
     * Gets an ISpecialPlayerInventory for the given Player.
     * 
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialPlayerInventory
     */
    @Override
    public ISpecialPlayerInventory getInventory(Player player, boolean online) {
        String id = playerLoader.getPlayerDataID(player);
        if (inventories.containsKey(id)) {
            return inventories.get(id);
        }
        ISpecialPlayerInventory inv = accessor.newSpecialPlayerInventory(player, online);
        inventories.put(id, inv);
        playerCache.put(id, player);
        return inv;
    }

    /**
     * Gets an ISpecialEnderChest for the given Player.
     * 
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialEnderChest
     */
    @Override
    public ISpecialEnderChest getEnderChest(Player player, boolean online) {
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
     * Forcibly unload a cached Player's data.
     * 
     * @param player the OfflinePlayer to unload
     */
    @Override
    public void unload(OfflinePlayer player) {
        this.playerCache.invalidate(this.playerLoader.getPlayerDataID(player));
    }

    /**
     * Check the configuration value for whether or not OpenInv saves player data when unloading
     * players. This is exclusively for users who do not allow editing of inventories, only viewing,
     * and wish to prevent any possibility of bugs such as lishid#40. If true, OpenInv will not ever
     * save any edits made to players.
     * 
     * @return false unless configured otherwise
     */
    @Override
    public boolean disableSaving() {
        return getConfig().getBoolean("settings.disable-saving", false);
    }

    /**
     * Check the configuration value for whether or not OpenInv displays a notification to the user
     * when a container is activated with SilentChest.
     * 
     * @return true unless configured otherwise
     */
    @Override
    public boolean notifySilentChest() {
        return getConfig().getBoolean("notify.silent-chest", true);
    }

    /**
     * Check the configuration value for whether or not OpenInv displays a notification to the user
     * when a container is activated with AnyChest.
     * 
     * @return true unless configured otherwise
     */
    @Override
    public boolean notifyAnyChest() {
        return getConfig().getBoolean("notify.any-chest", true);
    }

    /**
     * Gets a player's SilentChest setting.
     * 
     * @param player the OfflinePlayer
     * @return true if SilentChest is enabled
     */
    @Override
    public boolean getPlayerSilentChestStatus(OfflinePlayer player) {
        return getConfig().getBoolean("toggles.silent-chest." + playerLoader.getPlayerDataID(player), false);
    }

    /**
     * Sets a player's SilentChest setting.
     * 
     * @param player the OfflinePlayer
     * @param status the status
     */
    @Override
    public void setPlayerSilentChestStatus(OfflinePlayer player, boolean status) {
        getConfig().set("toggles.silent-chest." + playerLoader.getPlayerDataID(player), status);
        saveConfig();
    }

    /**
     * Gets the provided player's AnyChest setting.
     * 
     * @param player the OfflinePlayer
     * @return true if AnyChest is enabled
     */
    @Override
    public boolean getPlayerAnyChestStatus(OfflinePlayer player) {
        return getConfig().getBoolean("toggles.any-chest." + playerLoader.getPlayerDataID(player), false);
    }

    /**
     * Sets a player's AnyChest setting.
     * 
     * @param player the OfflinePlayer
     * @param status the status
     */
    @Override
    public void setPlayerAnyChestStatus(OfflinePlayer player, boolean status) {
        getConfig().set("toggles.any-chest." + playerLoader.getPlayerDataID(player), status);
        saveConfig();
    }

    /**
     * Gets a unique identifier by which the OfflinePlayer can be referenced. Using the value
     * returned to look up a Player will generally be much faster for later implementations.
     * 
     * @param offline the OfflinePlayer
     * @return the identifier
     */
    @Override
    public String getPlayerID(OfflinePlayer offline) {
        return this.playerLoader.getPlayerDataID(offline);
    }

    /**
     * Get an OfflinePlayer by name.
     * <p>
     * Note: This method is potentially very heavily blocking. It should not ever be called on the
     * main thread, and if it is, a stack trace will be displayed alerting server owners to the
     * call.
     * 
     * @param name the name of the Player
     * @return the OfflinePlayer with the closest matching name or null if no players have ever logged in
     */
    @Override
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

        // Attempt exact offline match first - adds UUID support for later versions
        OfflinePlayer player = this.playerLoader.getPlayerByID(name);

        if (player != null) {
            return player;
        }

        // Ensure name is valid if server is in online mode to avoid unnecessary searching
        if (getServer().getOnlineMode() && !name.matches("[a-zA-Z0-9_]{3,16}")) {
            return null;
        }

        player = getServer().getPlayerExact(name);

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
     * Load a Player from an OfflinePlayer. May return null under some circumstances.
     * 
     * @param offline the OfflinePlayer to load a Player for
     * @return the Player
     */
    @Override
    public Player loadPlayer(final OfflinePlayer offline) {

        if (offline == null) {
            return null;
        }

        String key = this.playerLoader.getPlayerDataID(offline);
        if (this.playerCache.containsKey(key)) {
            return this.playerCache.get(key);
        }

        // TODO: wrap Player to ensure all methods can safely be called offline
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
     * @see com.lishid.openinv.IOpenInv#retainPlayer(org.bukkit.entity.Player, org.bukkit.plugin.Plugin)
     */
    @Override
    public void retainPlayer(Player player, Plugin plugin) {
        String key = this.playerLoader.getPlayerDataID(player);

        if (this.pluginUsage.containsEntry(key, plugin.getClass())) {
            return;
        }

        this.pluginUsage.put(key, plugin.getClass());
    }

    /**
     * @see com.lishid.openinv.IOpenInv#releasePlayer(org.bukkit.entity.Player, org.bukkit.plugin.Plugin)
     */
    @Override
    public void releasePlayer(Player player, Plugin plugin) {
        String key = this.playerLoader.getPlayerDataID(player);

        if (!this.pluginUsage.containsEntry(key, plugin.getClass())) {
            return;
        }

        this.pluginUsage.remove(key, plugin.getClass());
    }

    /**
     * Unmark any Players in use by the specified Plugin.
     * 
     * @param plugin
     */
    public void releaseAllPlayers(Plugin plugin) {
        this.pluginUsage.removeAll(plugin.getClass());
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends Player> getOnlinePlayers() {

        if (this.playerLoader != null) {
            return this.playerLoader.getOnlinePlayers();
        }

        Method getOnlinePlayers;
        try {
            getOnlinePlayers = Bukkit.class.getDeclaredMethod("getOnlinePlayers");
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        Object onlinePlayers;
        try {
            onlinePlayers = getOnlinePlayers.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        if (onlinePlayers instanceof List) {
            return (Collection<Player>) onlinePlayers;
        }

        return Arrays.asList((Player[]) onlinePlayers);
    }

    /**
     * Method for handling a Player coming online.
     * 
     * @param player the Player
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
                @SuppressWarnings("deprecation") // Unlikely to ever be a viable alternative, Spigot un-deprecated.
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
     * @param player the Player
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

    /**
     * Evicts all viewers lacking cross-world permissions from a Player's inventory.
     * 
     * @param player the Player
     */
    public void changeWorld(final Player player) {

        String key = this.playerLoader.getPlayerDataID(player);

        // Check if the player is cached. If not, neither of their inventories is open.
        if (!this.playerCache.containsKey(key)) {
            return;
        }

        if (this.inventories.containsKey(key)) {
            Iterator<HumanEntity> iterator = this.inventories.get(key).getBukkitInventory().getViewers().iterator();
            while (iterator.hasNext()) {
                HumanEntity human = iterator.next();
                // If player has permission or is in the same world, allow continued access
                // Just in case, also allow null worlds.
                if (Permissions.CROSSWORLD.hasPermission(human) || human.getWorld() == null
                        || human.getWorld().equals(player.getWorld())) {
                    continue;
                }
                human.closeInventory();
            }
        }

        if (this.enderChests.containsKey(key)) {
            Iterator<HumanEntity> iterator = this.enderChests.get(key).getBukkitInventory().getViewers().iterator();
            while (iterator.hasNext()) {
                HumanEntity human = iterator.next();
                if (Permissions.CROSSWORLD.hasPermission(human) || human.getWorld() == null
                        || human.getWorld().equals(player.getWorld())) {
                    continue;
                }
                human.closeInventory();
            }
        }
    }

    /**
     * Displays all applicable help for OpenInv commands.
     * 
     * @param player the Player to help
     */
    public void showHelp(Player player) {
        // Get registered commands
        for (String commandName : this.getDescription().getCommands().keySet()) {
            PluginCommand command = this.getCommand(commandName);

            // Ensure command is successfully registered and player can use it
            if (command == null  || !command.testPermissionSilent(player)) {
                continue;
            }

            // Send usage
            player.sendMessage(command.getUsage().replace("<command>", commandName));

            List<String> aliases = command.getAliases();
            if (aliases.isEmpty()) {
                continue;
            }

            // Assemble alias list
            StringBuilder aliasBuilder = new StringBuilder("   (aliases: ");
            for (String alias : aliases) {
                aliasBuilder.append(alias).append(", ");
            }
            aliasBuilder.delete(aliasBuilder.length() - 2, aliasBuilder.length()).append(')');

            // Send all aliases
            player.sendMessage(aliasBuilder.toString());
        }
    }

}

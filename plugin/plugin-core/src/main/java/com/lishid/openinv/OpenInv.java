/*
 * Copyright (C) 2011-2018 lishid. All rights reserved.
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

package com.lishid.openinv;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lishid.openinv.commands.AnyChestPluginCommand;
import com.lishid.openinv.commands.OpenEnderPluginCommand;
import com.lishid.openinv.commands.OpenInvPluginCommand;
import com.lishid.openinv.commands.SearchEnchantPluginCommand;
import com.lishid.openinv.commands.SearchInvPluginCommand;
import com.lishid.openinv.commands.SilentChestPluginCommand;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.listeners.InventoryClickListener;
import com.lishid.openinv.listeners.InventoryCloseListener;
import com.lishid.openinv.listeners.InventoryDragListener;
import com.lishid.openinv.listeners.PlayerListener;
import com.lishid.openinv.listeners.PluginListener;
import com.lishid.openinv.util.Cache;
import com.lishid.openinv.util.ConfigUpdater;
import com.lishid.openinv.util.Function;
import com.lishid.openinv.util.InternalAccessor;
import com.lishid.openinv.util.Permissions;
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
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

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
                public boolean run(final Player value) {
                    String key = OpenInv.this.accessor.getPlayerDataManager().getPlayerDataID(value);
                    return OpenInv.this.inventories.containsKey(key)
                            && OpenInv.this.inventories.get(key).isInUse()
                            || OpenInv.this.enderChests.containsKey(key)
                                    && OpenInv.this.enderChests.get(key).isInUse()
                            || OpenInv.this.pluginUsage.containsKey(key);
                }
            }, new Function<Player>() {
                @Override
                public boolean run(final Player value) {
                    String key = OpenInv.this.accessor.getPlayerDataManager().getPlayerDataID(value);

                    // Check if inventory is stored, and if it is, remove it and eject all viewers
                    if (OpenInv.this.inventories.containsKey(key)) {
                        Inventory inv = OpenInv.this.inventories.remove(key).getBukkitInventory();
                        List<HumanEntity> viewers = inv.getViewers();
                        for (HumanEntity entity : viewers.toArray(new HumanEntity[0])) {
                            entity.closeInventory();
                        }
                    }

                    // Check if ender chest is stored, and if it is, remove it and eject all viewers
                    if (OpenInv.this.enderChests.containsKey(key)) {
                        Inventory inv = OpenInv.this.enderChests.remove(key).getBukkitInventory();
                        List<HumanEntity> viewers = inv.getViewers();
                        for (HumanEntity entity : viewers.toArray(new HumanEntity[0])) {
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

    /**
     * Evicts all viewers lacking cross-world permissions from a Player's inventory.
     *
     * @param player the Player
     */
    public void changeWorld(final Player player) {

        String key = this.accessor.getPlayerDataManager().getPlayerDataID(player);

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

    @Override
    public boolean disableSaving() {
        return this.getConfig().getBoolean("settings.disable-saving", false);
    }

    @NotNull
    @Override
    public IAnySilentContainer getAnySilentContainer() {
        return this.accessor.getAnySilentContainer();
    }

    @NotNull
    @Override
    public IInventoryAccess getInventoryAccess() {
        return this.accessor.getInventoryAccess();
    }

    private int getLevenshteinDistance(final String string1, final String string2) {
        if (string1 == null || string2 == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        if (string1.isEmpty()) {
            return string2.length();
        }
        if (string2.isEmpty()) {
            return string2.length();
        }
        if (string1.equals(string2)) {
            return 0;
        }

        int len1 = string1.length();
        int len2 = string2.length();

        int[] prevDistances = new int[len1 + 1];
        int[] distances = new int[len1 + 1];

        for (int i = 0; i <= len1; ++i) {
            prevDistances[i] = i;
        }

        for (int i = 1; i <= len2; ++i) {
            // TODO: include tweaks available in Simmetrics?
            char string2char = string2.charAt(i - 1);
            distances[0] = i;

            for (int j = 1; j <= len1; ++j) {
                int cost = string1.charAt(j - 1) == string2char ? 0 : 1;

                distances[j] = Math.min(Math.min(distances[j - 1] + 1, prevDistances[j] + 1), prevDistances[j - 1] + cost);
            }

            int[] swap = prevDistances;
            prevDistances = distances;
            distances = swap;
        }

        return prevDistances[len1];
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends Player> getOnlinePlayers() {

        if (this.accessor.isSupported()) {
            return this.accessor.getPlayerDataManager().getOnlinePlayers();
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

    @Override
    public boolean getPlayerAnyChestStatus(@NotNull final OfflinePlayer player) {
        boolean defaultState = false;

        if (player.isOnline()) {
            Player onlinePlayer = player.getPlayer();
            if (onlinePlayer != null) {
                defaultState = Permissions.ANY_DEFAULT.hasPermission(onlinePlayer);
            }
        }

        return this.getConfig().getBoolean("toggles.any-chest." + this.accessor.getPlayerDataManager().getPlayerDataID(player), defaultState);
    }

    @NotNull
    @Override
    public String getPlayerID(@NotNull final OfflinePlayer offline) {
        return this.accessor.getPlayerDataManager().getPlayerDataID(offline);
    }

    @Override
    public boolean getPlayerSilentChestStatus(@NotNull final OfflinePlayer offline) {
        boolean defaultState = false;

        if (offline.isOnline()) {
            Player onlinePlayer = offline.getPlayer();
            if (onlinePlayer != null) {
                defaultState = Permissions.SILENT_DEFAULT.hasPermission(onlinePlayer);
            }
        }

        return this.getConfig().getBoolean("toggles.silent-chest." + this.accessor.getPlayerDataManager().getPlayerDataID(offline), defaultState);
    }

    @NotNull
    @Override
    public ISpecialEnderChest getSpecialEnderChest(@NotNull final Player player, final boolean online)
            throws InstantiationException {
        String id = this.accessor.getPlayerDataManager().getPlayerDataID(player);
        if (this.enderChests.containsKey(id)) {
            return this.enderChests.get(id);
        }
        ISpecialEnderChest inv = this.accessor.newSpecialEnderChest(player, online);
        this.enderChests.put(id, inv);
        this.playerCache.put(id, player);
        return inv;
    }

    @NotNull
    @Override
    public ISpecialPlayerInventory getSpecialInventory(@NotNull final Player player, final boolean online)
            throws InstantiationException {
        String id = this.accessor.getPlayerDataManager().getPlayerDataID(player);
        if (this.inventories.containsKey(id)) {
            return this.inventories.get(id);
        }
        ISpecialPlayerInventory inv = this.accessor.newSpecialPlayerInventory(player, online);
        this.inventories.put(id, inv);
        this.playerCache.put(id, player);
        return inv;
    }

    @Override
    public boolean isSupportedVersion() {
        return this.accessor != null && this.accessor.isSupported();
    }

    @Nullable
    @Override
    public Player loadPlayer(@NotNull final OfflinePlayer offline) {

        String key = this.accessor.getPlayerDataManager().getPlayerDataID(offline);
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

        if (!this.isSupportedVersion()) {
            return null;
        }

        if (Bukkit.isPrimaryThread()) {
            return this.accessor.getPlayerDataManager().loadPlayer(offline);
        }

        Future<Player> future = Bukkit.getScheduler().callSyncMethod(this,
                new Callable<Player>() {
            @Override
            public Player call() {
                return OpenInv.this.accessor.getPlayerDataManager().loadPlayer(offline);
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

    @Nullable
    @Override
    public OfflinePlayer matchPlayer(@NotNull final String name) {

        // Warn if called on the main thread - if we resort to searching offline players, this may take several seconds.
        if (this.getServer().isPrimaryThread()) {
            this.getLogger().warning("Call to OpenInv#matchPlayer made on the main thread!");
            this.getLogger().warning("This can cause the server to hang, potentially severely.");
            this.getLogger().warning("Trace:");
            for (StackTraceElement element : new Throwable().fillInStackTrace().getStackTrace()) {
                this.getLogger().warning(element.toString());
            }
        }

        OfflinePlayer player;

        if (this.isSupportedVersion()) {
            // Attempt exact offline match first - adds UUID support for later versions
            player = this.accessor.getPlayerDataManager().getPlayerByID(name);

            if (player != null) {
                return player;
            }
        }

        // Ensure name is valid if server is in online mode to avoid unnecessary searching
        if (this.getServer().getOnlineMode() && !name.matches("[a-zA-Z0-9_]{3,16}")) {
            return null;
        }

        player = this.getServer().getPlayerExact(name);

        if (player != null) {
            return player;
        }

        player = this.getServer().getOfflinePlayer(name);

        /*
         * Compatibility: Pre-UUID, getOfflinePlayer always returns an OfflinePlayer. Post-UUID,
         * getOfflinePlayer will return null if no matching player is found. To preserve
         * compatibility, only return the player if they have played before. Ignoring current online
         * status is fine, they'd have been found by getPlayerExact otherwise.
         */
        if (player != null && player.hasPlayedBefore()) {
            return player;
        }

        player = this.getServer().getPlayer(name);

        if (player != null) {
            return player;
        }

        int bestMatch = Integer.MAX_VALUE;
        for (OfflinePlayer offline : this.getServer().getOfflinePlayers()) {
            if (offline.getName() == null) {
                // Loaded by UUID only, name has never been looked up.
                continue;
            }

            int currentMatch = this.getLevenshteinDistance(name, offline.getName());

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

    @Override
    public @Nullable InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory) {
        return this.accessor.getPlayerDataManager().openInventory(player, inventory);
    }

    @Override
    public boolean notifyAnyChest() {
        return this.getConfig().getBoolean("notify.any-chest", true);
    }

    @Override
    public boolean notifySilentChest() {
        return this.getConfig().getBoolean("notify.silent-chest", true);
    }

    @Override
    public void onDisable() {

        if (this.disableSaving()) {
            return;
        }

        if (this.isSupportedVersion()) {
            this.playerCache.invalidateAll();
        }
    }

    @Override
    public void onEnable() {
        // Get plugin manager
        PluginManager pm = this.getServer().getPluginManager();

        this.accessor = new InternalAccessor(this);
        // Version check
        if (!this.accessor.isSupported()) {
            this.getLogger().info("Your version of CraftBukkit (" + this.accessor.getVersion() + ") is not supported.");
            this.getLogger().info("If this version is a recent release, check for an update.");
            this.getLogger().info("If this is an older version, ensure that you've downloaded the legacy support version.");
            pm.disablePlugin(this);
            return;
        }

        this.saveDefaultConfig();
        new ConfigUpdater(this).checkForUpdates();

        // Register listeners
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new PluginListener(this), this);
        pm.registerEvents(new InventoryClickListener(this), this);
        pm.registerEvents(new InventoryCloseListener(this), this);
        // Bukkit will handle missing events for us, attempt to register InventoryDragEvent without a version check
        pm.registerEvents(new InventoryDragListener(this), this);

        // Register commands to their executors
        this.getCommand("openinv").setExecutor(new OpenInvPluginCommand(this));
        this.getCommand("openender").setExecutor(new OpenEnderPluginCommand(this));
        SearchInvPluginCommand searchInv = new SearchInvPluginCommand(this);
        this.getCommand("searchinv").setExecutor(searchInv);
        this.getCommand("searchender").setExecutor(searchInv);
        this.getCommand("searchenchant").setExecutor(new SearchEnchantPluginCommand(this));
        this.getCommand("silentchest").setExecutor(new SilentChestPluginCommand(this));
        this.getCommand("anychest").setExecutor(new AnyChestPluginCommand(this));

    }

    public void releaseAllPlayers(final Plugin plugin) {
        Iterator<Map.Entry<String, Class<? extends Plugin>>> iterator = this.pluginUsage.entries().iterator();

        if (!iterator.hasNext()) {
            return;
        }

        for (Map.Entry<String, Class<? extends Plugin>> entry = iterator.next(); iterator.hasNext(); entry = iterator.next()) {
            if (entry.getValue().equals(plugin.getClass())) {
                iterator.remove();
            }
        }
    }

    @Override
    public void releasePlayer(@NotNull final Player player, @NotNull final Plugin plugin) {
        String key = this.accessor.getPlayerDataManager().getPlayerDataID(player);

        if (!this.pluginUsage.containsEntry(key, plugin.getClass())) {
            return;
        }

        this.pluginUsage.remove(key, plugin.getClass());
    }

    @Override
    public void retainPlayer(@NotNull final Player player, @NotNull final Plugin plugin) {
        String key = this.accessor.getPlayerDataManager().getPlayerDataID(player);

        if (this.pluginUsage.containsEntry(key, plugin.getClass())) {
            return;
        }

        this.pluginUsage.put(key, plugin.getClass());
    }

    @Override
    public void setPlayerAnyChestStatus(@NotNull final OfflinePlayer offline, final boolean status) {
        this.getConfig().set("toggles.any-chest." + this.accessor.getPlayerDataManager().getPlayerDataID(offline), status);
        this.saveConfig();
    }

    /**
     * Method for handling a Player going offline.
     *
     * @param player the Player
     * @throws IllegalStateException if the server version is unsupported
     */
    public void setPlayerOffline(final Player player) {

        String key = this.accessor.getPlayerDataManager().getPlayerDataID(player);

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
     * Method for handling a Player coming online.
     *
     * @param player the Player
     * @throws IllegalStateException if the server version is unsupported
     */
    public void setPlayerOnline(final Player player) {

        String key = this.accessor.getPlayerDataManager().getPlayerDataID(player);

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

    @Override
    public void setPlayerSilentChestStatus(@NotNull final OfflinePlayer offline, final boolean status) {
        this.getConfig().set("toggles.silent-chest." + this.accessor.getPlayerDataManager().getPlayerDataID(offline), status);
        this.saveConfig();
    }

    /**
     * Displays all applicable help for OpenInv commands.
     *
     * @param player the Player to help
     */
    public void showHelp(final Player player) {
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

    @Override
    public void unload(@NotNull final OfflinePlayer offline) {
        this.playerCache.invalidate(this.accessor.getPlayerDataManager().getPlayerDataID(offline));
    }

}

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

package com.lishid.openinv;

import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.InventoryAccess;
import com.lishid.openinv.util.StringMetric;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface defining behavior for the OpenInv plugin.
 *
 * @author Jikoo
 */
public interface IOpenInv {

    /**
     * Check the configuration value for whether or not OpenInv saves player data when unloading
     * players. This is exclusively for users who do not allow editing of inventories, only viewing,
     * and wish to prevent any possibility of bugs such as lishid#40. If true, OpenInv will not ever
     * save any edits made to players.
     *
     * @return false unless configured otherwise
     */
    boolean disableSaving();

    /**
     * Gets the active ISilentContainer implementation.
     *
     * @return the ISilentContainer
     * @throws IllegalStateException if the server version is unsupported
     */
    @NotNull IAnySilentContainer getAnySilentContainer();

    /**
     * Gets the active IInventoryAccess implementation.
     *
     * @return the IInventoryAccess
     * @throws IllegalStateException if the server version is unsupported
     */
    @Deprecated
    default @NotNull IInventoryAccess getInventoryAccess() {
        return new InventoryAccess();
    }

    /**
     * Gets the provided player's AnyChest setting.
     *
     * @param player the OfflinePlayer
     * @return true if AnyChest is enabled
     * @throws IllegalStateException if the server version is unsupported
     */
    boolean getPlayerAnyChestStatus(@NotNull OfflinePlayer player);

    /**
     * Gets a unique identifier by which the OfflinePlayer can be referenced. Using the value
     * returned to look up a Player will generally be much faster for later implementations.
     *
     * @param offline the OfflinePlayer
     * @return the identifier
     * @throws IllegalStateException if the server version is unsupported
     */
    default @NotNull String getPlayerID(@NotNull OfflinePlayer offline) {
        return offline.getUniqueId().toString();
    }

    /**
     * Gets a player's SilentChest setting.
     *
     * @param offline the OfflinePlayer
     * @return true if SilentChest is enabled
     * @throws IllegalStateException if the server version is unsupported
     */
    boolean getPlayerSilentChestStatus(@NotNull OfflinePlayer offline);

    /**
     * Gets an ISpecialEnderChest for the given Player.
     *
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialEnderChest
     * @throws IllegalStateException  if the server version is unsupported
     * @throws InstantiationException if the ISpecialEnderChest could not be instantiated
     */
    @NotNull ISpecialEnderChest getSpecialEnderChest(@NotNull Player player, boolean online) throws InstantiationException;

    /**
     * Gets an ISpecialPlayerInventory for the given Player.
     *
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialPlayerInventory
     * @throws IllegalStateException  if the server version is unsupported
     * @throws InstantiationException if the ISpecialPlayerInventory could not be instantiated
     */
    @NotNull ISpecialPlayerInventory getSpecialInventory(@NotNull Player player, boolean online) throws InstantiationException;

    /**
     * Checks if the server version is supported by OpenInv.
     *
     * @return true if the server version is supported
     */
    boolean isSupportedVersion();

    /**
     * Load a Player from an OfflinePlayer. May return null under some circumstances.
     *
     * @param offline the OfflinePlayer to load a Player for
     * @return the Player, or null
     * @throws IllegalStateException if the server version is unsupported
     */
    @Nullable Player loadPlayer(@NotNull final OfflinePlayer offline);

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
    default @Nullable OfflinePlayer matchPlayer(@NotNull String name) {

        // Warn if called on the main thread - if we resort to searching offline players, this may take several seconds.
        if (Bukkit.getServer().isPrimaryThread()) {
            this.getLogger().warning("Call to OpenInv#matchPlayer made on the main thread!");
            this.getLogger().warning("This can cause the server to hang, potentially severely.");
            this.getLogger().warning("Trace:");
            for (StackTraceElement element : new Throwable().fillInStackTrace().getStackTrace()) {
                this.getLogger().warning(element.toString());
            }
        }

        OfflinePlayer player;

        try {
            UUID uuid = UUID.fromString(name);
            player = Bukkit.getOfflinePlayer(uuid);
            // Ensure player is a real player, otherwise return null
            if (player.hasPlayedBefore() || player.isOnline()) {
                return player;
            }
        } catch (IllegalArgumentException ignored) {
            // Not a UUID
        }

        // Ensure name is valid if server is in online mode to avoid unnecessary searching
        if (Bukkit.getServer().getOnlineMode() && !name.matches("[a-zA-Z0-9_]{3,16}")) {
            return null;
        }

        player = Bukkit.getServer().getPlayerExact(name);

        if (player != null) {
            return player;
        }

        player = Bukkit.getServer().getOfflinePlayer(name);

        if (player.hasPlayedBefore()) {
            return player;
        }

        player = Bukkit.getServer().getPlayer(name);

        if (player != null) {
            return player;
        }

        float bestMatch = 0;
        for (OfflinePlayer offline : Bukkit.getServer().getOfflinePlayers()) {
            if (offline.getName() == null) {
                // Loaded by UUID only, name has never been looked up.
                continue;
            }

            float currentMatch = StringMetric.compareJaroWinkler(name, offline.getName());

            if (currentMatch == 1.0F) {
                return offline;
            }

            if (currentMatch > bestMatch) {
                bestMatch = currentMatch;
                player = offline;
            }
        }

        // Only null if no players have played ever, otherwise even the worst match will do.
        return player;
    }

    /**
     * Open an ISpecialInventory for a Player.
     *
     * @param player the Player
     * @param inventory the ISpecialInventory
     * @return the InventoryView for the opened ISpecialInventory
     */
    @Nullable InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory);

    /**
     * Check the configuration value for whether or not OpenInv displays a notification to the user
     * when a container is activated with AnyChest.
     *
     * @return true unless configured otherwise
     */
    boolean notifyAnyChest();

    /**
     * Check the configuration value for whether or not OpenInv displays a notification to the user
     * when a container is activated with SilentChest.
     *
     * @return true unless configured otherwise
     */
    boolean notifySilentChest();

    /**
     * Mark a Player as no longer in use by a Plugin to allow OpenInv to remove it from the cache
     * when eligible.
     *
     * @param player the Player
     * @param plugin the Plugin no longer holding a reference to the Player
     * @throws IllegalStateException if the server version is unsupported
     */
    void releasePlayer(@NotNull Player player, @NotNull Plugin plugin);

    /**
     * Mark a Player as in use by a Plugin to prevent it from being removed from the cache. Used to
     * prevent issues with multiple copies of the same Player being loaded such as lishid#49.
     * Changes made to loaded copies overwrite changes to the others when saved, leading to
     * duplication bugs and more.
     * <p>
     * When finished with the Player object, be sure to call {@link #releasePlayer(Player, Plugin)}
     * to prevent the cache from keeping it stored until the plugin is disabled.
     * <p>
     * When using a Player object from OpenInv, you must handle the Player coming online, replacing
     * your Player reference with the Player from the PlayerJoinEvent. In addition, you must change
     * any values in the Player to reflect any unsaved alterations to the existing Player which do
     * not affect the inventory or ender chest contents.
     * <p>
     * OpenInv only saves player data when unloading a Player from the cache, and then only if
     * {@link #disableSaving()} returns false. If you are making changes that OpenInv does not cause
     * to persist when a Player logs in as noted above, it is suggested that you manually call
     * {@link Player#saveData()} when releasing your reference to ensure your changes persist.
     *
     * @param player the Player
     * @param plugin the Plugin holding the reference to the Player
     * @throws IllegalStateException if the server version is unsupported
     */
    void retainPlayer(@NotNull Player player, @NotNull Plugin plugin);

    /**
     * Sets a player's AnyChest setting.
     *
     * @param offline the OfflinePlayer
     * @param status the status
     * @throws IllegalStateException if the server version is unsupported
     */
    void setPlayerAnyChestStatus(@NotNull OfflinePlayer offline, boolean status);

    /**
     * Sets a player's SilentChest setting.
     *
     * @param offline the OfflinePlayer
     * @param status the status
     * @throws IllegalStateException if the server version is unsupported
     */
    void setPlayerSilentChestStatus(@NotNull OfflinePlayer offline, boolean status);

    /**
     * Forcibly unload a cached Player's data.
     *
     * @param offline the OfflinePlayer to unload
     * @throws IllegalStateException if the server version is unsupported
     */
    void unload(@NotNull OfflinePlayer offline);

    Logger getLogger();

}

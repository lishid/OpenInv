package com.lishid.openinv;

import javax.annotation.Nullable;

import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
    public boolean disableSaving();

    /**
     * Gets the active ISilentContainer implementation. May return null if the server version is
     * unsupported.
     *
     * @return the ISilentContainer
     * @throws IllegalStateException if the server version is unsupported
     */
    public IAnySilentContainer getAnySilentContainer();

    /**
     * Gets an ISpecialEnderChest for the given Player. Returns null if the ISpecialEnderChest could
     * not be instantiated.
     *
     * @deprecated Use {@link IOpenInv#getSpecialEnderChest(Player, boolean)}
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialEnderChest
     * @throws IllegalStateException if the server version is unsupported
     */
    @Deprecated
    @Nullable
    public ISpecialEnderChest getEnderChest(Player player, boolean online);

    /**
     * Gets an ISpecialPlayerInventory for the given Player. Returns null if the
     * ISpecialPlayerInventory could not be instantiated.
     *
     * @deprecated Use {@link IOpenInv#getSpecialInventory(Player, boolean)}
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialPlayerInventory
     * @throws IllegalStateException if the server version is unsupported
     */
    @Deprecated
    @Nullable
    public ISpecialPlayerInventory getInventory(Player player, boolean online);

    /**
     * Gets the active IInventoryAccess implementation. May return null if the server version is
     * unsupported.
     *
     * @return the IInventoryAccess
     * @throws IllegalStateException if the server version is unsupported
     */
    public IInventoryAccess getInventoryAccess();

    /**
     * Gets the provided player's AnyChest setting.
     *
     * @param player the OfflinePlayer
     * @return true if AnyChest is enabled
     * @throws IllegalStateException if the server version is unsupported
     */
    public boolean getPlayerAnyChestStatus(OfflinePlayer player);

    /**
     * Gets a unique identifier by which the OfflinePlayer can be referenced. Using the value
     * returned to look up a Player will generally be much faster for later implementations.
     *
     * @param offline the OfflinePlayer
     * @return the identifier
     * @throws IllegalStateException if the server version is unsupported
     */
    public String getPlayerID(OfflinePlayer offline);

    /**
     * Gets a player's SilentChest setting.
     *
     * @param player the OfflinePlayer
     * @return true if SilentChest is enabled
     * @throws IllegalStateException if the server version is unsupported
     */
    public boolean getPlayerSilentChestStatus(OfflinePlayer player);

    /**
     * Gets an ISpecialEnderChest for the given Player.
     *
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialEnderChest
     * @throws IllegalStateException if the server version is unsupported
     * @throws InstantiationException if the ISpecialEnderChest could not be instantiated
     */
    public ISpecialEnderChest getSpecialEnderChest(Player player, boolean online) throws InstantiationException;

    /**
     * Gets an ISpecialPlayerInventory for the given Player.
     *
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialPlayerInventory
     * @throws IllegalStateException if the server version is unsupported
     * @throws InstantiationException if the ISpecialPlayerInventory could not be instantiated
     */
    public ISpecialPlayerInventory getSpecialInventory(Player player, boolean online) throws InstantiationException;

    /**
     * Checks if the server version is supported by OpenInv.
     *
     * @return true if the server version is supported
     */
    public boolean isSupportedVersion();

    /**
     * Load a Player from an OfflinePlayer. May return null under some circumstances.
     *
     * @param offline the OfflinePlayer to load a Player for
     * @return the Player, or null
     * @throws IllegalStateException if the server version is unsupported
     */
    @Nullable
    public Player loadPlayer(final OfflinePlayer offline);

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
    @Nullable
    public OfflinePlayer matchPlayer(String name);

    /**
     * Check the configuration value for whether or not OpenInv displays a notification to the user
     * when a container is activated with AnyChest.
     *
     * @return true unless configured otherwise
     */
    public boolean notifyAnyChest();

    /**
     * Check the configuration value for whether or not OpenInv displays a notification to the user
     * when a container is activated with SilentChest.
     *
     * @return true unless configured otherwise
     */
    public boolean notifySilentChest();

    /**
     * Mark a Player as no longer in use by a Plugin to allow OpenInv to remove it from the cache
     * when eligible.
     *
     * @param player the Player
     * @param plugin the Plugin no longer holding a reference to the Player
     * @throws IllegalStateException if the server version is unsupported
     */
    public void releasePlayer(Player player, Plugin plugin);

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
    public void retainPlayer(Player player, Plugin plugin);

    /**
     * Sets a player's AnyChest setting.
     *
     * @param player the OfflinePlayer
     * @param status the status
     * @throws IllegalStateException if the server version is unsupported
     */
    public void setPlayerAnyChestStatus(OfflinePlayer player, boolean status);

    /**
     * Sets a player's SilentChest setting.
     *
     * @param player the OfflinePlayer
     * @param status the status
     * @throws IllegalStateException if the server version is unsupported
     */
    public void setPlayerSilentChestStatus(OfflinePlayer player, boolean status);

    /**
     * Forcibly unload a cached Player's data.
     *
     * @param player the OfflinePlayer to unload
     * @throws IllegalStateException if the server version is unsupported
     */
    public void unload(OfflinePlayer player);

}

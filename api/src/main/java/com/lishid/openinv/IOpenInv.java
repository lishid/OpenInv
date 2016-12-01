package com.lishid.openinv;

import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Interface defining behavior for the OpenInv plugin.
 * 
 * @author Jikoo
 */
public interface IOpenInv {



    /**
     * Checks if the server version is supported by OpenInv.
     * 
     * @return true if the server version is supported
     */
    public boolean isSupportedVersion();

    /**
     * Gets the active IInventoryAccess implementation. May return null if the server version is
     * unsupported.
     * 
     * @return the IInventoryAccess
     */
    public IInventoryAccess getInventoryAccess();

    /**
     * Gets the active ISilentContainer implementation. May return null if the server version is
     * unsupported.
     * 
     * @return the ISilentContainer
     */
    public IAnySilentContainer getAnySilentContainer();

    /**
     * Gets an ISpecialPlayerInventory for the given Player.
     * 
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialPlayerInventory
     */
    public ISpecialPlayerInventory getInventory(Player player, boolean online);

    /**
     * Gets an ISpecialEnderChest for the given Player.
     * 
     * @param player the Player
     * @param online true if the Player is currently online
     * @return the ISpecialEnderChest
     */
    public ISpecialEnderChest getEnderChest(Player player, boolean online);

    /**
     * Forcibly unload a cached Player's data.
     * 
     * @param player the OfflinePlayer to unload
     */
    public void unload(OfflinePlayer player);

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
     * Check the configuration value for whether or not OpenInv displays a notification to the user
     * when a container is activated with SilentChest.
     * 
     * @return true unless configured otherwise
     */
    public boolean notifySilentChest();

    /**
     * Check the configuration value for whether or not OpenInv displays a notification to the user
     * when a container is activated with AnyChest.
     * 
     * @return true unless configured otherwise
     */
    public boolean notifyAnyChest();

    /**
     * Gets a player's SilentChest setting.
     * 
     * @param player the OfflinePlayer
     * @return true if SilentChest is enabled
     */
    public boolean getPlayerSilentChestStatus(OfflinePlayer player);

    /**
     * Sets a player's SilentChest setting.
     * 
     * @param player the OfflinePlayer
     * @param status the status
     */
    public void setPlayerSilentChestStatus(OfflinePlayer player, boolean status);

    /**
     * Gets the provided player's AnyChest setting.
     * 
     * @param player the OfflinePlayer
     * @return true if AnyChest is enabled
     */
    public boolean getPlayerAnyChestStatus(OfflinePlayer player);

    /**
     * Sets a player's AnyChest setting.
     * 
     * @param player the OfflinePlayer
     * @param status the status
     */
    public void setPlayerAnyChestStatus(OfflinePlayer player, boolean status);

    /**
     * Gets a unique identifier by which the OfflinePlayer can be referenced. Using the value
     * returned to look up a Player will generally be much faster for later implementations.
     * 
     * @param offline the OfflinePlayer
     * @return the identifier
     */
    public String getPlayerID(OfflinePlayer offline);

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
    public OfflinePlayer matchPlayer(String name);

    /**
     * Load a Player from an OfflinePlayer. May return null under some circumstances.
     * 
     * @param offline the OfflinePlayer to load a Player for
     * @return the Player
     */
    public Player loadPlayer(final OfflinePlayer offline);

}

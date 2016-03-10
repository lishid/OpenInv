package com.lishid.openinv;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Configuration {

    private final OpenInv plugin;

    private Material openInvItem;
    private boolean notifySilentChest;
    private boolean notifyAnyChest;

    public Configuration(OpenInv plugin) {
        this.plugin = plugin;

        // Check for config updates
        ConfigUpdater configUpdater = new ConfigUpdater(plugin);
        configUpdater.checkForUpdates();

        // Load the config settings
        load();
    }

    /**
     * Loads OpenInv's config settings.
     */
    public void load() {
        // OpenInv Item
        if (!plugin.getConfig().isSet("items.open-inv")) {
            saveToConfig("items.open-inv", "STICK");
        }

        String itemName = plugin.getConfig().getString("items.open-inv", "STICK");
        Material material = Material.getMaterial(itemName);

        if (material == null) {
            plugin.getLogger().warning("OpenInv item '" + itemName + "' does not match to a valid item. Defaulting to stick.");
            material = Material.STICK;
        }

        openInvItem = material;

        // Other Values
        notifySilentChest = plugin.getConfig().getBoolean("notify.silent-chest", true);
        notifyAnyChest = plugin.getConfig().getBoolean("notify.any-chest", true);
    }

    /**
     * Saves a value to the plugin config at the specified path.
     *
     * @param path the path to set the value to
     * @param value the value to set to the path
     */
    public void saveToConfig(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
    }

    /**
     * Returns the OpenInv item Material.
     *
     * @return the OpenInv item Material
     */
    public Material getOpenInvItem() {
        return openInvItem;
    }

    /**
     * Returns whether or not notify silent chest is enabled.
     *
     * @return true if notify silent chest is enabled; false otherwise
     */
    public boolean notifySilentChest() {
        return notifySilentChest;
    }

    /**
     * Returns whether or not notify any chest is enabled.
     *
     * @return true if notify any chest is enabled; false otherwise
     */
    public boolean notifyAnyChest() {
        return notifyAnyChest;
    }

    /**
     * Returns a player's item OpenInv status.
     *
     * @param player the player to get the item OpenInv status of
     * @return the player's item OpenInv status
     */
    public boolean getPlayerItemOpenInvStatus(Player player) {
        return plugin.getConfig().getBoolean("toggles.items.open-inv." + player.getUniqueId(), false);
    }

    /**
     * Returns a player's any chest status.
     *
     * @param player the player to get the any chest status of
     * @return the player's any chest status
     */
    public boolean getPlayerAnyChestStatus(Player player) {
        return plugin.getConfig().getBoolean("toggles.any-chest." + player.getUniqueId(), true);
    }

    /**
     * Sets a player's any chest status.
     *
     * @param player the player to set the any chest status of
     * @param status the status to set with
     */
    public void setPlayerAnyChestStatus(Player player, boolean status) {
        saveToConfig("toggles.any-chest." + player.getUniqueId(), status);
    }

    /**
     * Sets a player's item OpenInv status.
     *
     * @param player the player to set the item OpenInv status of
     * @param status the status to set with
     */
    public void setPlayerItemOpenInvStatus(Player player, boolean status) {
        saveToConfig("toggles.items.open-inv." + player.getUniqueId(), status);
    }

    /**
     * Returns a player's silent chest status.
     *
     * @param player the player to get the silent chest status of
     * @return the player's silent chest status
     */
    public boolean getPlayerSilentChestStatus(Player player) {
        return plugin.getConfig().getBoolean("toggles.silent-chest." + player.getUniqueId(), false);
    }

    /**
     * Sets a player's silent chest status.
     *
     * @param player the player to set the silent chest status of
     * @param status the status to set with
     */
    public void setPlayerSilentChestStatus(Player player, boolean status) {
        saveToConfig("toggles.silent-chest." + player.getUniqueId(), status);
    }
}

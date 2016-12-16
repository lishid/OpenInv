package com.lishid.openinv.listeners;

import com.lishid.openinv.OpenInv;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

/**
 * Listener for plugin-related events.
 * 
 * @author Jikoo
 */
public class PluginListener implements Listener {

    private final OpenInv plugin;

    public PluginListener(OpenInv plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        plugin.releaseAllPlayers(event.getPlugin());
    }

}

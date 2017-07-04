package com.lishid.openinv.listeners;

import com.lishid.openinv.IOpenInv;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 *
 *
 * @author Jikoo
 */
public class InventoryCloseListener implements Listener {

    private final IOpenInv plugin;

    public InventoryCloseListener(final IOpenInv plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (this.plugin.getPlayerSilentChestStatus(player)) {
            this.plugin.getAnySilentContainer().deactivateContainer(player);
        }
    }

}

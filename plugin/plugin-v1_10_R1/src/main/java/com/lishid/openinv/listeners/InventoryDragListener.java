package com.lishid.openinv.listeners;

import com.lishid.openinv.IOpenInv;
import com.lishid.openinv.util.Permissions;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * Listener for InventoryDragEvents to prevent unpermitted modification of special inventories.
 * 
 * @author Jikoo
 */
public class InventoryDragListener implements Listener {

    private final IOpenInv plugin;

    public InventoryDragListener(IOpenInv plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        HumanEntity entity = event.getWhoClicked();
        Inventory inventory = event.getInventory();
        if (plugin.getInventoryAccess().isSpecialPlayerInventory(inventory)
                && !Permissions.EDITINV.hasPermission(entity)
                || plugin.getInventoryAccess().isSpecialEnderChest(inventory)
                        && !Permissions.EDITENDER.hasPermission(entity)) {
            event.setCancelled(true);
        }
    }

}

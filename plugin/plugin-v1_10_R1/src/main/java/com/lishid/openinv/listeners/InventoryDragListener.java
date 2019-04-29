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

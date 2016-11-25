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

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.Inventory;

public class OpenInvInventoryListener implements Listener {

    private final OpenInv plugin;

    public OpenInvInventoryListener(OpenInv plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (cancelInteract(event.getWhoClicked(), event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (cancelInteract(event.getWhoClicked(), event.getInventory())) {
            event.setCancelled(true);
        }
    }

    private boolean cancelInteract(HumanEntity entity, Inventory inventory) {
        return plugin.getInventoryAccess().isSpecialPlayerInventory(inventory)
                && !Permissions.EDITINV.hasPermission(entity)
                || plugin.getInventoryAccess().isSpecialEnderChest(inventory)
                        && !Permissions.EDITENDER.hasPermission(entity);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.changeWorld(event.getPlayer());
    }

}

/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

package lishid.openinv;

import lishid.openinv.utils.OpenInvEnderChest;
import lishid.openinv.utils.OpenInvPlayerInventory;
import net.minecraft.server.IInventory;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OpenInvInventoryListener implements Listener
{
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event)
    {
        IInventory inv = ((CraftInventory) event.getInventory()).getInventory();
        HumanEntity player = event.getWhoClicked();
        if (inv instanceof OpenInvPlayerInventory && event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot())
        {
            if (!player.hasPermission(Permissions.PERM_EDITINV))
            {
                event.setCancelled(true);
            }
        }
        else if (inv instanceof OpenInvEnderChest && event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot())
        {
            if (!player.hasPermission(Permissions.PERM_EDITENDER))
            {
                event.setCancelled(true);
            }
        }
    }
}
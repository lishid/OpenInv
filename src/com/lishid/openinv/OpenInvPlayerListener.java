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

package com.lishid.openinv;

import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;

public class OpenInvPlayerListener implements Listener
{
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        ISpecialPlayerInventory inventory = OpenInv.inventories.get(event.getPlayer().getName().toLowerCase());
        
        if (inventory != null)
        {
            inventory.PlayerGoOnline(event.getPlayer());
        }
        
        ISpecialEnderChest chest = OpenInv.enderChests.get(event.getPlayer().getName().toLowerCase());
        
        if (chest != null)
        {
            chest.PlayerGoOnline(event.getPlayer());
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        ISpecialPlayerInventory inventory = OpenInv.inventories.get(event.getPlayer().getName().toLowerCase());
        if (inventory != null)
        {
            inventory.PlayerGoOffline();
            inventory.InventoryRemovalCheck();
        }
        ISpecialEnderChest chest = OpenInv.enderChests.get(event.getPlayer().getName().toLowerCase());
        if (chest != null)
        {
            chest.PlayerGoOffline();
            chest.InventoryRemovalCheck();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.useInteractedBlock() == Result.DENY)
            return;
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == org.bukkit.Material.ENDER_CHEST)
        {
            if (event.getPlayer().hasPermission(Permissions.PERM_SILENT) && OpenInv.GetPlayerSilentChestStatus(event.getPlayer().getName()))
            {
                event.setCancelled(true);
                event.getPlayer().openInventory(event.getPlayer().getEnderChest());
            }
        }
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Chest)
        {
            boolean silentchest = false;
            boolean anychest = false;
            int x = event.getClickedBlock().getX();
            int y = event.getClickedBlock().getY();
            int z = event.getClickedBlock().getZ();
            
            if (event.getPlayer().hasPermission(Permissions.PERM_SILENT) && OpenInv.GetPlayerSilentChestStatus(event.getPlayer().getName()))
            {
                silentchest = true;
            }
            
            if (event.getPlayer().hasPermission(Permissions.PERM_ANYCHEST) && OpenInv.GetPlayerAnyChestStatus(event.getPlayer().getName()))
            {
                try
                {
                    anychest = OpenInv.anySilentChest.IsAnyChestNeeded(event.getPlayer(), x, y, z);
                }
                catch (Exception e)
                {
                    event.getPlayer().sendMessage(ChatColor.RED + "Error while executing openinv. Unsupported CraftBukkit.");
                    e.printStackTrace();
                }
            }
            
            // If the anychest or silentchest is active
            if (anychest || silentchest)
            {
                if (!OpenInv.anySilentChest.ActivateChest(event.getPlayer(), anychest, silentchest, x, y, z))
                {
                    event.setCancelled(true);
                }
            }
        }
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign)
        {
            Player player = event.getPlayer();
            try
            {
                Sign sign = ((Sign) event.getClickedBlock().getState());
                if (player.hasPermission(Permissions.PERM_OPENINV) && sign.getLine(0).equalsIgnoreCase("[openinv]"))
                {
                    String text = sign.getLine(1).trim() + sign.getLine(2).trim() + sign.getLine(3).trim();
                    player.performCommand("openinv " + text);
                }
            }
            catch (Exception ex)
            {
                player.sendMessage("Internal Error.");
                ex.printStackTrace();
            }
        }
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            Player player = event.getPlayer();
            
            if (!(player.getItemInHand().getType().getId() == OpenInv.GetItemOpenInvItem()) || (!OpenInv.GetPlayerItemOpenInvStatus(player.getName()))
                    || !player.hasPermission(Permissions.PERM_OPENINV))
            {
                return;
            }
            
            player.performCommand("openinv");
        }
    }
}
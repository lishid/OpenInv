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

import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;

public class OpenInvPlayerListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        ISpecialPlayerInventory inventory = OpenInv.inventories.get(event.getPlayer().getName().toLowerCase());

        if (inventory != null) {
            inventory.playerOnline(event.getPlayer());
        }

        ISpecialEnderChest chest = OpenInv.enderChests.get(event.getPlayer().getName().toLowerCase());

        if (chest != null) {
            chest.playerOnline(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        ISpecialPlayerInventory inventory = OpenInv.inventories.get(event.getPlayer().getName().toLowerCase());
        if (inventory != null) {
            inventory.playerOffline();
        }
        ISpecialEnderChest chest = OpenInv.enderChests.get(event.getPlayer().getName().toLowerCase());
        if (chest != null) {
            chest.playerOffline();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getPlayer().isSneaking()) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.useInteractedBlock() == Result.DENY) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == org.bukkit.Material.ENDER_CHEST) {
            if (OpenInv.hasPermission(player, Permissions.PERM_SILENT) && OpenInv.GetPlayerSilentChestStatus(player.getName())) {
                event.setCancelled(true);
                player.openInventory(player.getEnderChest());
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Chest) {
            boolean silentchest = false;
            boolean anychest = false;
            int x = event.getClickedBlock().getX();
            int y = event.getClickedBlock().getY();
            int z = event.getClickedBlock().getZ();

            if (OpenInv.hasPermission(player, Permissions.PERM_SILENT) && OpenInv.GetPlayerSilentChestStatus(player.getName())) {
                silentchest = true;
            }

            if (OpenInv.hasPermission(player, Permissions.PERM_ANYCHEST) && OpenInv.GetPlayerAnyChestStatus(player.getName())) {
                try {
                    anychest = OpenInv.anySilentChest.IsAnyChestNeeded(player, x, y, z);
                }
                catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Error while executing openinv. Unsupported CraftBukkit.");
                    e.printStackTrace();
                }
            }

            // If the anychest or silentchest is active
            if (anychest || silentchest) {
                if (!OpenInv.anySilentChest.ActivateChest(player, anychest, silentchest, x, y, z)) {
                    event.setCancelled(true);
                }
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getState() instanceof Sign) {
            try {
                Sign sign = ((Sign) event.getClickedBlock().getState());
                if (OpenInv.hasPermission(player, Permissions.PERM_OPENINV) && sign.getLine(0).equalsIgnoreCase("[openinv]")) {
                    String text = sign.getLine(1).trim() + sign.getLine(2).trim() + sign.getLine(3).trim();
                    player.performCommand("openinv " + text);
                }
            }
            catch (Exception ex) {
                player.sendMessage("Internal Error.");
                ex.printStackTrace();
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!(player.getItemInHand().getType().getId() == OpenInv.GetItemOpenInvItem()) || (!OpenInv.GetPlayerItemOpenInvStatus(player.getName())) || !OpenInv.hasPermission(player, Permissions.PERM_OPENINV)) {
                return;
            }

            player.performCommand("openinv");
        }
    }
}
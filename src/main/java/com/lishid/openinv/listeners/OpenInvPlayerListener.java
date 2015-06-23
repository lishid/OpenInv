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

package com.lishid.openinv.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;
import com.lishid.openinv.internal.SpecialEnderChest;
import com.lishid.openinv.internal.SpecialPlayerInventory;

public class OpenInvPlayerListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        SpecialPlayerInventory inventory = OpenInv.inventories.get(player.getUniqueId());
        if (inventory != null) {
            inventory.playerOnline(event.getPlayer());
        }

        SpecialEnderChest chest = OpenInv.enderChests.get(player.getUniqueId());
        if (chest != null) {
            chest.playerOnline(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        SpecialPlayerInventory inventory = OpenInv.inventories.get(player.getUniqueId());
        if (inventory != null) {
            inventory.playerOffline();
        }

        SpecialEnderChest chest = OpenInv.enderChests.get(player.getUniqueId());
        if (chest != null) {
            chest.playerOffline();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            return;
        }

        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_BLOCK && event.useInteractedBlock() == Result.DENY) {
            return;
        }

        Block block = event.getClickedBlock();

        if (action == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.ENDER_CHEST) {
            if (OpenInv.hasPermission(player, Permissions.PERM_SILENT) && OpenInv.getPlayerSilentChestStatus(player)) {
                event.setCancelled(true);
                player.openInventory(player.getEnderChest());
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK && block.getState() instanceof Chest) {
            boolean silentChest = false;
            boolean anyChest = false;

            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            if (OpenInv.hasPermission(player, Permissions.PERM_SILENT) && OpenInv.getPlayerSilentChestStatus(player)) {
                silentChest = true;
            }

            if (OpenInv.hasPermission(player, Permissions.PERM_ANYCHEST) && OpenInv.getPlayerAnyChestStatus(player)) {
                try {
                    anyChest = OpenInv.getAnySilentChest().isAnyChestNeeded(player, x, y, z);
                }
                catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Error while executing openinv. Unsupported CraftBukkit.");
                    e.printStackTrace();
                }
            }

            // If the anychest or silentchest is active
            if (anyChest || silentChest) {
                if (!OpenInv.getAnySilentChest().activateChest(player, anyChest, silentChest, x, y, z)) {
                    event.setCancelled(true);
                }
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK && block.getState() instanceof Sign) {
            try {
                Sign sign = ((Sign) block.getState());
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

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (!(player.getItemInHand().getType() == OpenInv.getOpenInvItem()) || (!OpenInv.getPlayerItemOpenInvStatus(player)) || !OpenInv.hasPermission(player, Permissions.PERM_OPENINV)) {
                return;
            }

            player.performCommand("openinv");
        }
    }
}

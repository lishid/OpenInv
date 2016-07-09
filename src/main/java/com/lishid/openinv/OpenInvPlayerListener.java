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
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class OpenInvPlayerListener implements Listener {

    private final OpenInv plugin;

    public OpenInvPlayerListener(OpenInv plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        plugin.setPlayerOnline(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.setPlayerOffline(event.getPlayer());
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
            if (OpenInv.hasPermission(player, Permissions.PERM_SILENT) && plugin.getPlayerSilentChestStatus(player)) {
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

            if (OpenInv.hasPermission(player, Permissions.PERM_SILENT) && plugin.getPlayerSilentChestStatus(player)) {
                silentchest = true;
            }

            if (OpenInv.hasPermission(player, Permissions.PERM_ANYCHEST) && plugin.getPlayerAnyChestStatus(player)) {
                try {
                    anychest = plugin.getAnySilentChest().isAnyChestNeeded(player, x, y, z);
                }
                catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Error while executing openinv. Unsupported CraftBukkit.");
                    e.printStackTrace();
                }
            }

            // If the anychest or silentchest is active
            if (anychest || silentchest) {
                if (!plugin.getAnySilentChest().activateChest(player, anychest, silentchest, x, y, z)) {
                    if (silentchest && plugin.notifySilentChest()) {
                        player.sendMessage("You are opening a chest silently.");
                    }
                    if (anychest && plugin.notifyAnyChest()) {
                        player.sendMessage("You are opening a blocked chest.");
                    }
                    event.setCancelled(true);
                }
            }
        }
    }

}

/*
 * Copyright (C) 2011-2016 lishid.  All rights reserved.
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
import org.bukkit.scheduler.BukkitRunnable;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;
import com.lishid.openinv.Configuration;
import com.lishid.openinv.internal.SpecialEnderChest;
import com.lishid.openinv.internal.SpecialPlayerInventory;

public class OpenInvPlayerListener implements Listener {

    private final OpenInv plugin;
    private final Configuration configuration;

    public OpenInvPlayerListener(OpenInv plugin) {
        this.plugin = plugin;
        configuration = plugin.getConfiguration();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        SpecialPlayerInventory inventory = OpenInv.inventories.get(player.getUniqueId());
        if (inventory != null) {
            inventory.playerOnline(event.getPlayer());
        }

        SpecialEnderChest enderChest = OpenInv.enderChests.get(player.getUniqueId());
        if (enderChest != null) {
            enderChest.playerOnline(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        final SpecialPlayerInventory inventory = OpenInv.inventories.get(player.getUniqueId());
        if (inventory != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    inventory.playerOffline();
                }
            }.runTaskLater(plugin, 1);
        }

        final SpecialEnderChest enderChest = OpenInv.enderChests.get(player.getUniqueId());
        if (enderChest != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    enderChest.playerOffline();
                }
            }.runTaskLater(plugin, 1);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            return;
        }

        Action action = event.getAction();
        Block block = event.getClickedBlock();

        switch (action) {
            case RIGHT_CLICK_BLOCK:
                if (event.useInteractedBlock() == Result.DENY) {
                    return;
                }

                // Ender Chests
                if (block.getType() == Material.ENDER_CHEST) {
                    if (OpenInv.hasPermission(player, Permissions.PERM_SILENT) && configuration.getPlayerSilentChestStatus(player)) {
                        event.setCancelled(true);
                        player.openInventory(player.getEnderChest());
                        return;
                    }
                }

                // Chests
                if (block.getState() instanceof Chest) {
                    boolean silentChest = false;
                    boolean anyChest = false;
                    int x = block.getX();
                    int y = block.getY();
                    int z = block.getZ();

                    if (OpenInv.hasPermission(player, Permissions.PERM_SILENT) && configuration.getPlayerSilentChestStatus(player)) {
                        silentChest = true;
                    }

                    if (OpenInv.hasPermission(player, Permissions.PERM_ANYCHEST) && configuration.getPlayerAnyChestStatus(player)) {
                        try {
                            anyChest = plugin.getAnySilentChest().isAnyChestNeeded(player, x, y, z);
                        } catch (Exception e) {
                            player.sendMessage(ChatColor.RED + "Error while executing openinv. Unsupported CraftBukkit.");
                            e.printStackTrace();
                        }
                    }

                    // If the anyChest or silentChest is active
                    if (anyChest || silentChest) {
                        if (!plugin.getAnySilentChest().activateChest(player, anyChest, silentChest, x, y, z)) {
                            event.setCancelled(true);
                        }
                    }

                    return;
                }

                // Signs
                if (block.getState() instanceof Sign) {
                    try {
                        Sign sign = (Sign) block.getState();

                        if (OpenInv.hasPermission(player, Permissions.PERM_OPENINV) && sign.getLine(0).equalsIgnoreCase("[openinv]")) {
                            String text = sign.getLine(1).trim() + sign.getLine(2).trim() + sign.getLine(3).trim();
                            player.performCommand("openinv " + text);
                        }
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + "An internal error occured.");
                        e.printStackTrace();
                    }

                    return;
                }
            case RIGHT_CLICK_AIR:
                // OpenInv item
                if (player.getInventory().getItemInMainHand().getType() == configuration.getOpenInvItem() && configuration.getPlayerItemOpenInvStatus(player) && OpenInv.hasPermission(player, Permissions.PERM_OPENINV)) {
                    player.performCommand("openinv");
                }
        }
    }
}

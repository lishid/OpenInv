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

package com.lishid.openinv.commands;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;
import com.lishid.openinv.internal.SpecialEnderChest;
import com.lishid.openinv.utils.UUIDUtil;

public class OpenEnderCommand implements CommandExecutor {
    private final OpenInv plugin;
    private final Map<UUID, UUID> openEnderHistory = new ConcurrentHashMap<UUID, UUID>();

    public OpenEnderCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("openender")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can't use this command from the console.");
                return true;
            }

            if (!OpenInv.hasPermission(sender, Permissions.PERM_ENDERCHEST)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to access player ender chests.");
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("?")) {
                OpenInv.showHelp((Player) sender);
                return true;
            }

            final Player player = (Player) sender;

            // History management
            UUID history = openEnderHistory.get(player.getUniqueId());
            if (history == null) {
                history = player.getUniqueId();
                openEnderHistory.put(player.getUniqueId(), history);
            }

            final UUID uuid;

            // Read from history if target is not named
            if (args.length < 1) {
                if (history != null) {
                    uuid = history;
                }
                else {
                    sender.sendMessage(ChatColor.RED + "OpenEnder history is empty!");
                    return true;
                }
            }
            else {
                uuid = UUIDUtil.getUUIDOf(args[0]);
                if (uuid == null) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
            }

            final UUID playerUUID = player.getUniqueId();

            Player target = Bukkit.getPlayer(uuid);
            if (target == null) {
                // Targeted player was not found online, start asynchronous lookup in files
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        // Try loading the player's data asynchronously
                        final Player target = OpenInv.getPlayerLoader().loadPlayer(uuid);
                        if (target == null) {
                            player.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }

                        // Open target's inventory synchronously
                        Bukkit.getScheduler().runTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                Player player = Bukkit.getPlayer(playerUUID);
                                // If sender is no longer online after loading the target, abort!
                                if (player == null) {
                                    return;
                                }
                                openInventory(player, target);
                            }
                        });
                    }
                });
            } else {
                openInventory(player, target);
            }

            return true;
        }

        return false;
    }

    private void openInventory(Player player, Player target) {
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (target != player && !OpenInv.hasPermission(player, Permissions.PERM_ENDERCHEST_ALL)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to access other player's ender chests.");
            return;
        }

        // Permissions checks
        if (!OpenInv.hasPermission(player, Permissions.PERM_OVERRIDE) && OpenInv.hasPermission(target, Permissions.PERM_EXEMPT)) {
            player.sendMessage(ChatColor.RED + target.getDisplayName() + "'s ender chest is protected!");
            return;
        }

        // Crossworld check
        if ((!OpenInv.hasPermission(player, Permissions.PERM_CROSSWORLD) && !OpenInv.hasPermission(player, Permissions.PERM_OVERRIDE)) && target.getWorld() != player.getWorld()) {
            player.sendMessage(ChatColor.RED + target.getDisplayName() + " is not in your world!");
            return;
        }

        // Record the target
        openEnderHistory.put(player.getUniqueId(), target.getUniqueId());

        // Create the inventory
        SpecialEnderChest chest = OpenInv.enderChests.get(target.getUniqueId());
        if (chest == null) {
            chest = new SpecialEnderChest(target, target.isOnline());
        }

        // Open the inventory
        player.openInventory(chest.getBukkitInventory());
    }
}

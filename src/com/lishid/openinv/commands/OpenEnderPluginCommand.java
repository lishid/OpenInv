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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.InternalAccessor;

public class OpenEnderPluginCommand implements CommandExecutor {
    private final OpenInv plugin;
    public static HashMap<Player, String> openEnderHistory = new HashMap<Player, String>();

    public OpenEnderPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }

        if (!OpenInv.hasPermission(sender, Permissions.PERM_ENDERCHEST)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player enderchest");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("?")) {
            OpenInv.ShowHelp((Player) sender);
            return true;
        }

        final Player player = (Player) sender;

        // History management
        String history = openEnderHistory.get(player);

        if (history == null || history == "") {
            history = player.getName();
            openEnderHistory.put(player, history);
        }

        final String name;

        // Read from history if target is not named
        if (args.length < 1) {
            name = history;
        }
        else {
            name = args[0];
        }

        final UUID senderID = player.getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> matches = Bukkit.matchPlayer(name);
                if (!matches.isEmpty()) {
                    openInventory(player, matches.get(0).getUniqueId());
                    return;
                }
                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                if (Bukkit.getPlayer(senderID) == null) {
                    return;
                }
                if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Bukkit.getPlayer(senderID) == null) {
                            return;
                        }
                        openInventory(player, offlinePlayer.getUniqueId());
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }

    private void openInventory(Player player, UUID uuid) {

        Player target = this.plugin.getServer().getPlayer(uuid);

        if (target == null) {
            // Try loading the player's data
            target = OpenInv.playerLoader.loadPlayer(uuid);

            if (target == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }
        }


        if (target != player && !OpenInv.hasPermission(player, Permissions.PERM_ENDERCHEST_ALL)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to access other player's enderchest");
            return;
        }

        // Record the target
        openEnderHistory.put(player, target.getName());

        // Create the inventory
        ISpecialEnderChest chest = OpenInv.enderChests.get(target.getName().toLowerCase());
        if (chest == null) {
            chest = InternalAccessor.Instance.newSpecialEnderChest(target, !target.isOnline());
        }

        // Open the inventory
        player.openInventory(chest.getBukkitInventory());
    }
}

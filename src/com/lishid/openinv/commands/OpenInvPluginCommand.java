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
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.InternalAccessor;

public class OpenInvPluginCommand implements CommandExecutor {
    private final OpenInv plugin;
    public static HashMap<Player, String> openInvHistory = new HashMap<Player, String>();

    public OpenInvPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("?")) {
            OpenInv.ShowHelp((Player) sender);
            return true;
        }

        final Player player = (Player) sender;

        // History management
        String history = openInvHistory.get(player);

        if (history == null || history == "") {
            history = player.getName();
            openInvHistory.put(player, history);
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

        // Permissions checks
        if (!OpenInv.hasPermission(player, Permissions.PERM_OVERRIDE) && OpenInv.hasPermission(target, Permissions.PERM_EXEMPT)) {
            player.sendMessage(ChatColor.RED + target.getDisplayName() + "'s inventory is protected!");
            return;
        }

        // Crosswork check
        if ((!OpenInv.hasPermission(player, Permissions.PERM_CROSSWORLD) && !OpenInv.hasPermission(player, Permissions.PERM_OVERRIDE)) && target.getWorld() != player.getWorld()) {
            player.sendMessage(ChatColor.RED + target.getDisplayName() + " is not in your world!");
            return;
        }

        // Self-open check
        if (!OpenInv.hasPermission(player, Permissions.PERM_OPENSELF) && target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You're not allowed to openinv yourself.");
            return;
        }

        // Record the target
        openInvHistory.put(player, target.getName());

        // Create the inventory
        ISpecialPlayerInventory inv = OpenInv.inventories.get(target.getName().toLowerCase());
        if (inv == null) {
            inv = InternalAccessor.Instance.newSpecialPlayerInventory(target, !target.isOnline());
        }

        // Open the inventory
        player.openInventory(inv.getBukkitInventory());
    }
}

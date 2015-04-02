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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        Player player = (Player) sender;

        // History management
        String history = openEnderHistory.get(player);

        if (history == null || history == "") {
            history = player.getName();
            openEnderHistory.put(player, history);
        }

        final String name;

        // Read from history if target is not named
        if (args.length < 1) {
            if (history != null && history != "") {
                name = history;
            }
            else {
                sender.sendMessage(ChatColor.RED + "OpenEnder history is empty!");
                return true;
            }
        }
        else {
            name = args[0];
        }

        final String playername = player.getName();
        Player target = plugin.getServer().getPlayer(name);
        // Targeted player was not found online, start asynchron lookup in files
        if (target == null) {
            sender.sendMessage(ChatColor.GREEN + "Starting inventory lookup.");
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    // Try loading the player's data asynchronly
                    final Player target = OpenInv.playerLoader.loadPlayer(name);
                    // Back to synchron to send messages and display inventory
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            Player player = Bukkit.getPlayer(playername);
                            // If sender is no longer online after loading the target. Abort!
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

    private void openInventory(Player player, Player target) {
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        if (target != player && !OpenInv.hasPermission(player, Permissions.PERM_ENDERCHEST_ALL)) {
            player.sendMessage(ChatColor.RED + "You do not have permission to access other player's enderchest");
            return;
        }

        // Crossworld check
        if ((!OpenInv.hasPermission(player, Permissions.PERM_CROSSWORLD) && !OpenInv.hasPermission(player, Permissions.PERM_OVERRIDE)) && target.getWorld() != player.getWorld()) {
            player.sendMessage(ChatColor.RED + target.getDisplayName() + " is not in your world!");
            return;
        }

        // Record the target
        openEnderHistory.put(player, target.getName());

        // Create the inventory
        ISpecialEnderChest chest = OpenInv.enderChests.get(target.getName().toLowerCase());
        if (chest == null) {
            chest = InternalAccessor.Instance.newSpecialEnderChest(target, target.isOnline());
        }

        // Open the inventory
        player.openInventory(chest.getBukkitInventory());

        return;
    }
}

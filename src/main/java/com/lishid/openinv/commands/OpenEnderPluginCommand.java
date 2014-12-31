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
        boolean offline = false;

        // History management
        String history = openEnderHistory.get(player);

        if (history == null || history == "") {
            history = player.getName();
            openEnderHistory.put(player, history);
        }

        // Target selecting
        Player target;

        String name = "";

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

        target = this.plugin.getServer().getPlayer(name);

        if (target == null) {
            // Try loading the player's data
            target = OpenInv.playerLoader.loadPlayer(name);

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player " + name + " not found!");
                return true;
            }
        }

        if (target != sender && !OpenInv.hasPermission(sender, Permissions.PERM_ENDERCHEST_ALL)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access other player's enderchest");
            return true;
        }

        // Record the target
        history = target.getName();
        openEnderHistory.put(player, history);

        // Create the inventory
        ISpecialEnderChest chest = OpenInv.enderChests.get(target.getName().toLowerCase());
        if (chest == null) {
            chest = InternalAccessor.Instance.newSpecialEnderChest(target, !offline);
        }

        // Open the inventory
        player.openInventory(chest.getBukkitInventory());

        return true;
    }
}

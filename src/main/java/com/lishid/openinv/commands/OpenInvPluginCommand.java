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
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.InternalAccessor;

public class OpenInvPluginCommand implements CommandExecutor {
    private final OpenInv plugin;
    public static HashMap<Player, String> openInvHistory = new HashMap<Player, String>();

    public OpenInvPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }
        if (!OpenInv.hasPermission(sender, Permissions.PERM_OPENINV)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("?")) {
            OpenInv.ShowHelp((Player) sender);
            return true;
        }

        Player player = (Player) sender;
        boolean offline = false;

        // History management
        String history = openInvHistory.get(player);

        if (history == null || history == "") {
            history = player.getName();
            openInvHistory.put(player, history);
        }

        // Target selecting
        Player target;

        String name = "";

        // Read from history if target is not named
        if (args.length < 1) {
            name = history;
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

        // Permissions checks
        if (!OpenInv.hasPermission(player, Permissions.PERM_OVERRIDE) && OpenInv.hasPermission(target, Permissions.PERM_EXEMPT)) {
            sender.sendMessage(ChatColor.RED + target.getDisplayName() + "'s inventory is protected!");
            return true;
        }

        // Crosswork check
        if ((!OpenInv.hasPermission(player, Permissions.PERM_CROSSWORLD) && !OpenInv.hasPermission(player, Permissions.PERM_OVERRIDE)) && target.getWorld() != player.getWorld()) {
            sender.sendMessage(ChatColor.RED + target.getDisplayName() + " is not in your world!");
            return true;
        }

        // Self-open check
        if (!OpenInv.hasPermission(player, Permissions.PERM_OPENSELF) && target.equals(player)) {
            sender.sendMessage(ChatColor.RED + "You're not allowed to openinv yourself.");
            return true;
        }

        // Record the target
        history = target.getName();
        openInvHistory.put(player, history);

        // Create the inventory
        ISpecialPlayerInventory inv = OpenInv.inventories.get(target.getName().toLowerCase());
        if (inv == null) {
            inv = InternalAccessor.Instance.newSpecialPlayerInventory(target, !offline);
        }

        // Open the inventory
        player.openInventory(inv.getBukkitInventory());

        return true;
    }
}

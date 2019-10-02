/*
 * Copyright (C) 2011-2019 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.commands;

import com.lishid.openinv.OpenInv;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnyChestPluginCommand implements CommandExecutor {

    private final OpenInv plugin;

    public AnyChestPluginCommand(final OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("check")) {
            sender.sendMessage("AnyChest is " + (this.plugin.getPlayerAnyChestStatus(player) ? "ON" : "OFF") + ".");
            return true;
        }

        this.plugin.setPlayerAnyChestStatus(player, !this.plugin.getPlayerAnyChestStatus(player));
        sender.sendMessage("AnyChest is now " + (this.plugin.getPlayerAnyChestStatus(player) ? "ON" : "OFF") + ".");

        return true;
    }

}

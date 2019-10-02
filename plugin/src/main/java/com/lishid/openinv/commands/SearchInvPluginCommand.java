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
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SearchInvPluginCommand implements CommandExecutor {

    private final OpenInv plugin;

    public SearchInvPluginCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Material material = null;
        int count = 1;

        if (args.length >= 1) {
            String[] gData = args[0].split(":");
            material = Material.matchMaterial(gData[0]);
        }

        if (args.length >= 2) {
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not a number!");
                return false;
            }
        }

        if (material == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item");
            return false;
        }

        StringBuilder players = new StringBuilder();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Inventory inventory = command.getName().equals("searchinv") ? player.getInventory() : player.getEnderChest();
            if (inventory.contains(material, count)) {
                players.append(player.getName()).append(", ");
            }
        }

        // Matches found, delete trailing comma and space
        if (players.length() > 0) {
            players.delete(players.length() - 2, players.length());
        } else {
            sender.sendMessage("No players found with " + material.toString());
        }

        sender.sendMessage("Players with the item " + material.toString() + ": " + players.toString());
        return true;
    }

}

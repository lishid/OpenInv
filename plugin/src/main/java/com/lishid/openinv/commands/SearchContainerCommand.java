/*
 * Copyright (C) 2011-2020 lishid. All rights reserved.
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

import com.lishid.openinv.util.TabCompleter;
import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

/**
 * Command for searching containers in a radius of chunks.
 */
public class SearchContainerCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }

        if (args.length < 1) {
            // Must supply material
            return false;
        }

        Material material = Material.getMaterial(args[0]);

        if (material == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item: \"" + args[0] + "\"");
            return false;
        }

        int radius = 5;

        if (args.length > 1) {
            try {
                radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                // Invalid radius supplied
                return false;
            }
        }

        Player senderPlayer = (Player) sender;
        World world = senderPlayer.getWorld();
        Chunk centerChunk = senderPlayer.getLocation().getChunk();
        StringBuilder locations = new StringBuilder();

        for (int dX = -radius; dX <= radius; ++dX) {
            for (int dZ = -radius; dZ <= radius; ++dZ) {
                if (!world.loadChunk(centerChunk.getX() + dX, centerChunk.getZ() + dZ, false)) {
                    continue;
                }
                Chunk chunk = world.getChunkAt(centerChunk.getX() + dX, centerChunk.getZ() + dZ);
                for (BlockState tileEntity : chunk.getTileEntities()) {
                    if (!(tileEntity instanceof InventoryHolder)) {
                        continue;
                    }
                    InventoryHolder holder = (InventoryHolder) tileEntity;
                    if (!holder.getInventory().contains(material)) {
                        continue;
                    }
                    locations.append(holder.getInventory().getType().name().toLowerCase()).append(" (")
                            .append(tileEntity.getX()).append(',').append(tileEntity.getY()).append(',')
                            .append(tileEntity.getZ()).append("), ");
                }
            }
        }

        // Matches found, delete trailing comma and space
        if (locations.length() > 0) {
            locations.delete(locations.length() - 2, locations.length());
        } else {
            sender.sendMessage("No containers found with " + material.toString());
        }

        sender.sendMessage("Containers holding item " + material.toString() + ": " + locations.toString());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args.length > 2 || !command.testPermissionSilent(sender)) {
            return Collections.emptyList();
        }

        String argument = args[args.length - 1];
        if (args.length == 1) {
            return TabCompleter.completeEnum(argument, Material.class);
        } else {
            return TabCompleter.completeInteger(argument);
        }
    }

}

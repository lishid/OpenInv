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

package com.lishid.openinv.commands;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;
import com.lishid.openinv.internal.SpecialPlayerInventory;
import com.lishid.openinv.utils.UUIDUtils;

public class OpenInvCommand implements CommandExecutor {

    private final OpenInv plugin;
    private final Map<UUID, UUID> openInvHistory = new ConcurrentHashMap<UUID, UUID>();

    public OpenInvCommand(OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("openinv")) {
            final boolean isConsole = sender instanceof ConsoleCommandSender;

            if (!OpenInv.hasPermission(sender, Permissions.PERM_OPENINV)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to access player inventories.");
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("?")) {
                OpenInv.showHelp(sender);
                return true;
            }

            Player player = isConsole ? null : (Player) sender;
            final UUID uuid;

            // Read from history if target is not named
            if (args.length < 1) {
                if (isConsole) {
                    // TODO: Should this output the command's usage instead?
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }

                // History management
                UUID history = openInvHistory.get(player.getUniqueId());
                if (history == null) {
                    history = player.getUniqueId();
                    openInvHistory.put(player.getUniqueId(), history);
                }

                uuid = history;
            } else {
                uuid = UUIDUtils.getPlayerUUID(args[0]);
                if (uuid == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found!");
                    return true;
                }
            }

            final UUID playerUUID = isConsole ? null : player.getUniqueId();

            Player target = Bukkit.getPlayer(uuid);
            if (target == null) {
                // Targeted player was not found online, start asynchronous lookup in files
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        // Try loading the player's data asynchronously
                        final Player target = plugin.getPlayerLoader().loadPlayer(uuid);
                        if (target == null) {
                            sender.sendMessage(ChatColor.RED + "Player not found!");
                            return;
                        }

                        // Open/output target's inventory synchronously
                        if (isConsole) {
                            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    outputInventory(sender, target);
                                }
                            });
                        } else {
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
                    }
                });
            } else {
                if (isConsole) {
                    outputInventory(sender, target);
                } else {
                    openInventory(player, target);
                }
            }

            return true;
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    private void outputInventory(CommandSender sender, Player target) {
        // Get the inventory and open it
        SpecialPlayerInventory specialInv = plugin.getPlayerInventory(target, true);
        Inventory inventory = specialInv.getBukkitInventory();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);

            if (itemStack != null) {
                String itemID = (itemStack.getDurability() != -1)
                        ? (itemStack.getTypeId() + ":" + itemStack.getDurability())
                        : String.valueOf(itemStack.getTypeId());

                sender.sendMessage(ChatColor.GREEN + "Slot " + slot + ": " + ChatColor.WHITE
                        + itemStack.getType().toString() + "(" + itemID + ") x" + itemStack.getAmount());
            } else {
                sender.sendMessage(ChatColor.GREEN + "Slot " + slot + ": " + ChatColor.WHITE + "Empty");
            }
        }
    }

    private void openInventory(Player player, Player target) {
        // Null target check
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            return;
        }

        // Permissions checks
        if (!OpenInv.hasPermission(player, Permissions.PERM_OVERRIDE) && OpenInv.hasPermission(target, Permissions.PERM_EXEMPT)) {
            player.sendMessage(ChatColor.RED + target.getDisplayName() + "'s inventory is protected!");
            return;
        }

        // Crossworld check
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
        openInvHistory.put(player.getUniqueId(), target.getUniqueId());

        // Get the inventory and open it
        SpecialPlayerInventory inventory = plugin.getPlayerInventory(target, true);
        player.openInventory(inventory.getBukkitInventory());
    }
}

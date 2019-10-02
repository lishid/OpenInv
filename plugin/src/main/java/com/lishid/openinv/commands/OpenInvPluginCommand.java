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
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.util.Permissions;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class OpenInvPluginCommand implements CommandExecutor {

    private final OpenInv plugin;
    private final HashMap<Player, String> openInvHistory = new HashMap<Player, String>();
    private final HashMap<Player, String> openEnderHistory = new HashMap<Player, String>();

    public OpenInvPluginCommand(final OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("?")) {
            this.plugin.showHelp((Player) sender);
            return true;
        }

        final Player player = (Player) sender;
        final boolean openinv = command.getName().equals("openinv");

        // History management
        String history = (openinv ? this.openInvHistory : this.openEnderHistory).get(player);

        if (history == null || history.isEmpty()) {
            history = player.getName();
            (openinv ? this.openInvHistory : this.openEnderHistory).put(player, history);
        }

        final String name;

        // Read from history if target is not named
        if (args.length < 1) {
            name = history;
        } else {
            name = args[0];
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                final OfflinePlayer offlinePlayer = OpenInvPluginCommand.this.plugin.matchPlayer(name);

                if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Player not found!");
                    return;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            return;
                        }
                        OpenInvPluginCommand.this.openInventory(player, offlinePlayer, openinv);
                    }
                }.runTask(OpenInvPluginCommand.this.plugin);

            }
        }.runTaskAsynchronously(this.plugin);

        return true;
    }

    private void openInventory(final Player player, final OfflinePlayer target, boolean openinv) {


        Player onlineTarget;
        boolean online = target.isOnline();

        if (!online) {
            // Try loading the player's data
            onlineTarget = this.plugin.loadPlayer(target);

            if (onlineTarget == null) {
                player.sendMessage(ChatColor.RED + "Player not found!");
                return;
            }
        } else {
            onlineTarget = target.getPlayer();
        }

        // Permissions checks
        if (onlineTarget.equals(player)) {
            // Inventory: Additional permission required to open own inventory
            if (openinv && !Permissions.OPENSELF.hasPermission(player)) {
                player.sendMessage(ChatColor.RED + "You're not allowed to open your own inventory!");
                return;
            }
        } else {
            // Enderchest: Additional permission required to open others' ender chests
            if (!openinv && !Permissions.ENDERCHEST_ALL.hasPermission(player)) {
                player.sendMessage(ChatColor.RED + "You do not have permission to access other players' ender chests.");
                return;
            }

            // Protected check
            if (!Permissions.OVERRIDE.hasPermission(player)
                    && Permissions.EXEMPT.hasPermission(onlineTarget)) {
                player.sendMessage(ChatColor.RED + onlineTarget.getDisplayName() + "'s inventory is protected!");
                return;
            }

            // Crossworld check
            if (!Permissions.CROSSWORLD.hasPermission(player)
                    && !onlineTarget.getWorld().equals(player.getWorld())) {
                player.sendMessage(
                        ChatColor.RED + onlineTarget.getDisplayName() + " is not in your world!");
                return;
            }
        }

        // Record the target
        (openinv ? this.openInvHistory : this.openEnderHistory).put(player, this.plugin.getPlayerID(target));

        // Create the inventory
        final ISpecialInventory inv;
        try {
            inv = openinv ? this.plugin.getSpecialInventory(onlineTarget, online) : this.plugin.getSpecialEnderChest(onlineTarget, online);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred creating " + onlineTarget.getDisplayName() + "'s inventory!");
            e.printStackTrace();
            return;
        }

        // Open the inventory
        plugin.openInventory(player, inv);
    }

}

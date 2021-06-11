/*
 * Copyright (C) 2011-2021 lishid. All rights reserved.
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
import com.lishid.openinv.util.TabCompleter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class OpenInvCommand implements TabExecutor {

    private final OpenInv plugin;
    private final HashMap<Player, String> openInvHistory = new HashMap<>();
    private final HashMap<Player, String> openEnderHistory = new HashMap<>();

    public OpenInvCommand(final OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "messages.error.consoleUnsupported");
            return true;
        }

        if (args.length > 0 && (args[0].equalsIgnoreCase("help") || args[0].equals("?"))) {
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
                final OfflinePlayer offlinePlayer = OpenInvCommand.this.plugin.matchPlayer(name);

                if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                    plugin.sendMessage(player, "messages.error.invalidPlayer");
                    return;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            return;
                        }
                        OpenInvCommand.this.openInventory(player, offlinePlayer, openinv);
                    }
                }.runTask(OpenInvCommand.this.plugin);

            }
        }.runTaskAsynchronously(this.plugin);

        return true;
    }

    private void openInventory(final Player player, final OfflinePlayer target, boolean openinv) {
        Player onlineTarget;
        boolean online = target.isOnline();

        if (!online) {
            if (Permissions.OPENOFFLINE.hasPermission(player)) {
                // Try loading the player's data
                onlineTarget = this.plugin.loadPlayer(target);
            } else {
                plugin.sendMessage(player, "messages.error.permissionPlayerOffline");
                return;
            }
        } else {
            if (Permissions.OPENONLINE.hasPermission(player)) {
                onlineTarget = target.getPlayer();
            } else {
                plugin.sendMessage(player, "messages.error.permissionPlayerOnline");
                return;
            }
        }

        if (onlineTarget == null) {
            plugin.sendMessage(player, "messages.error.invalidPlayer");
            return;
        }

        // Permissions checks
        if (onlineTarget.equals(player)) {
            // Inventory: Additional permission required to open own inventory
            if (openinv && !Permissions.OPENSELF.hasPermission(player)) {
                plugin.sendMessage(player, "messages.error.permissionOpenSelf");
                return;
            }
        } else {
            // Enderchest: Additional permission required to open others' ender chests
            if (!openinv && !Permissions.ENDERCHEST_ALL.hasPermission(player)) {
                plugin.sendMessage(player, "messages.error.permissionEnderAll");
                return;
            }

            // Protected check
            if (!Permissions.OVERRIDE.hasPermission(player)
                    && Permissions.EXEMPT.hasPermission(onlineTarget)) {
                plugin.sendMessage(player, "messages.error.permissionExempt",
                        "%target%", onlineTarget.getDisplayName());
                return;
            }

            // Crossworld check
            if (!Permissions.CROSSWORLD.hasPermission(player)
                    && !onlineTarget.getWorld().equals(player.getWorld())) {
                plugin.sendMessage(player, "messages.error.permissionCrossWorld",
                        "%target%", onlineTarget.getDisplayName());
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
            plugin.sendMessage(player, "messages.error.commandException");
            e.printStackTrace();
            return;
        }

        // Open the inventory
        plugin.openInventory(player, inv);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.testPermissionSilent(sender) || args.length != 1) {
            return Collections.emptyList();
        }

        return TabCompleter.completeOnlinePlayer(sender, args[0]);
    }

}

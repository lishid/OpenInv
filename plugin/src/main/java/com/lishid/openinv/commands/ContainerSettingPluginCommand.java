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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ContainerSettingPluginCommand implements CommandExecutor, TabCompleter {

    private final OpenInv plugin;

    public ContainerSettingPluginCommand(final OpenInv plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You can't use this from the console.");
            return true;
        }

        Player player = (Player) sender;
        boolean any = command.getName().startsWith("any");
        String commandName = any ? "AnyContainer" : "SilentContainer";
        Function<Player, Boolean> getSetting = any ? plugin::getPlayerAnyChestStatus : plugin::getPlayerSilentChestStatus;
        BiConsumer<OfflinePlayer, Boolean> setSetting = any ? plugin::setPlayerAnyChestStatus : plugin::setPlayerSilentChestStatus;

        if (args.length > 0) {
            args[0] = args[0].toLowerCase();

            if (args[0].equals("on")) {
                setSetting.accept(player, true);
            } else if (args[0].equals("off")) {
                setSetting.accept(player, false);
            } else if (!args[0].equals("check")) {
                // Invalid argument, show usage.
                return false;
            }

        } else {
            setSetting.accept(player, !getSetting.apply(player));
        }

        sender.sendMessage(commandName + " is now " + (getSetting.apply(player) ? "ON" : "OFF") + ".");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!command.testPermissionSilent(sender) || args.length != 1) {
            return Collections.emptyList();
        }

        String argument = args[0].toLowerCase();
        List<String> completions = new ArrayList<>();

        for (String subcommand : new String[] {"check", "on", "off"}) {
            if (subcommand.startsWith(argument)) {
                completions.add(subcommand);
            }
        }

        return completions;
    }

}

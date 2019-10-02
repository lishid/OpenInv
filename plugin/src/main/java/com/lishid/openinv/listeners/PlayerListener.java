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

package com.lishid.openinv.listeners;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.util.Permissions;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final OpenInv plugin;

    public PlayerListener(OpenInv plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        plugin.setPlayerOnline(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.setPlayerOffline(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.changeWorld(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getPlayer().isSneaking()
                || event.useInteractedBlock() == Result.DENY
                || !plugin.getAnySilentContainer().isAnySilentContainer(event.getClickedBlock())) {
            return;
        }

        Player player = event.getPlayer();
        boolean any = Permissions.ANYCHEST.hasPermission(player) && plugin.getPlayerAnyChestStatus(player);
        boolean needsAny = plugin.getAnySilentContainer().isAnyContainerNeeded(player, event.getClickedBlock());

        if (!any && needsAny) {
            return;
        }

        boolean silent = Permissions.SILENT.hasPermission(player) && plugin.getPlayerSilentChestStatus(player);

        // If anycontainer or silentcontainer is active
        if ((any || silent) && plugin.getAnySilentContainer().activateContainer(player, silent, event.getClickedBlock())) {
            if (silent && plugin.notifySilentChest() && needsAny && plugin.notifyAnyChest()) {
                player.sendMessage("You are opening a blocked container silently.");
            } else if (silent && plugin.notifySilentChest()) {
                player.sendMessage("You are opening a container silently.");
            } else if (needsAny && plugin.notifyAnyChest()) {
                player.sendMessage("You are opening a blocked container.");
            }
            event.setCancelled(true);
        }
    }

}

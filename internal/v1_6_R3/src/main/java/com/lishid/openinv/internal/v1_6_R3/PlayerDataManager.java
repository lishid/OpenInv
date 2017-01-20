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

package com.lishid.openinv.internal.v1_6_R3;

import java.util.Arrays;
import java.util.Collection;

import com.lishid.openinv.internal.IPlayerDataManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.MinecraftServer;
import net.minecraft.server.v1_6_R3.PlayerInteractManager;

import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;

public class PlayerDataManager implements IPlayerDataManager {

    @Override
    public Player loadPlayer(OfflinePlayer offline) {
        // Ensure the player has data
        if (offline == null || !offline.hasPlayedBefore()) {
            return null;
        }

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

        // Create an entity to load the player data
        EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), offline.getName(),
                new PlayerInteractManager(server.getWorldServer(0)));

        // Get the bukkit entity
        Player target = (entity == null) ? null : entity.getBukkitEntity();
        if (target != null) {
            // Load data
            target.loadData();
        }
        // Return the entity
        return target;
    }

    @Override
    public String getPlayerDataID(OfflinePlayer player) {
        return player.getName();
    }

    @Override
    public OfflinePlayer getPlayerByID(String identifier) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(identifier);
        // Ensure player is a real player, otherwise return null
        if (player == null || !player.hasPlayedBefore() && !player.isOnline()) {
            return null;
        }
        return player;
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return Arrays.asList(Bukkit.getOnlinePlayers());
    }

    public static EntityPlayer getHandle(Player player) {
        if (player instanceof CraftPlayer) {
            return ((CraftPlayer) player).getHandle();
        }

        Server server = player.getServer();
        EntityPlayer nmsPlayer = null;

        if (server instanceof CraftServer) {
            nmsPlayer = ((CraftServer) server).getHandle().getPlayer(player.getName());
        }

        if (nmsPlayer == null) {
            // Could use reflection to examine fields, but it's honestly not worth the bother.
            throw new RuntimeException("Unable to fetch EntityPlayer from provided Player implementation");
        }

        return nmsPlayer;
    }

}

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

package com.lishid.openinv.internal.v1_9_R1;

import java.util.Collection;
import java.util.UUID;

import com.lishid.openinv.internal.IPlayerDataManager;

import com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

// Volatile
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.MinecraftServer;
import net.minecraft.server.v1_9_R1.PlayerInteractManager;

import org.bukkit.craftbukkit.v1_9_R1.CraftServer;

public class PlayerDataManager implements IPlayerDataManager {

    @Override
    public Player loadPlayer(OfflinePlayer offline) {
        // Ensure the player has data
        if (offline == null || !offline.hasPlayedBefore()) {
            return null;
        }

        // Create a profile and entity to load the player data
        GameProfile profile = new GameProfile(offline.getUniqueId(), offline.getName());
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile,
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
        return player.getUniqueId().toString();
    }

    @Override
    public OfflinePlayer getPlayerByID(String identifier) {
        try {
            UUID uuid = UUID.fromString(identifier);
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            // Ensure player is a real player, otherwise return null
            if (player == null || !player.hasPlayedBefore() && !player.isOnline()) {
                return null;
            }
            return player;
        } catch (IllegalArgumentException e) {
            // Not a UUID
            return null;
        }
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

}

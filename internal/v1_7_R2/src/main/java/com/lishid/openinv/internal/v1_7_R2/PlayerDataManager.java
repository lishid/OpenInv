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

package com.lishid.openinv.internal.v1_7_R2;

import com.lishid.openinv.internal.IPlayerDataManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

//Volatile
import net.minecraft.server.v1_7_R2.EntityPlayer;
import net.minecraft.server.v1_7_R2.MinecraftServer;
import net.minecraft.server.v1_7_R2.PlayerInteractManager;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.craftbukkit.v1_7_R2.CraftServer;

@SuppressWarnings("deprecation") // Deprecated methods are used properly and will not change.
public class PlayerDataManager implements IPlayerDataManager {

    @Override
    public Player loadPlayer(OfflinePlayer offline) {
        // Ensure the player has data
        if (offline == null || !offline.hasPlayedBefore()) {
            return null;
        }

        // Create a profile and entity to load the player data
        GameProfile profile = new GameProfile(null, offline.getName());
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

}

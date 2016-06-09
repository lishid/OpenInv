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

package com.lishid.openinv.internal;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.MinecraftServer;
import net.minecraft.server.v1_10_R1.PlayerInteractManager;

public class PlayerDataManager {

    private final OpenInv plugin;

    public PlayerDataManager(OpenInv plugin) {
        this.plugin = plugin;
    }

    public Player loadPlayer(UUID uuid) {
        try {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player == null || !player.hasPlayedBefore()) {
                return null;
            }

            GameProfile profile = new GameProfile(uuid, player.getName());
            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
            // Create an entity to load the player data
            EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));

            // Get the bukkit entity
            Player target = entity.getBukkitEntity();

            if (target != null) {
                // Load data
                target.loadData();

                // Return the entity
                return target;
            }
        } catch (Exception e) {
            plugin.log(e);
        }

        return null;
    }
}

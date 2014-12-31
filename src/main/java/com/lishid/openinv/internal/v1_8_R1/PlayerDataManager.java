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

package com.lishid.openinv.internal.v1_8_R1;

import java.io.File;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.mojang.authlib.GameProfile;

//Volatile
import net.minecraft.server.v1_8_R1.*;

import org.bukkit.craftbukkit.v1_8_R1.*;

public class PlayerDataManager implements IPlayerDataManager {
    public Player loadPlayer(String name) {
        try {
            UUID uuid = matchUser(name);
            if (uuid == null) {
                return null;
            }

            // Default player folder
            File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata");
            if (!playerfolder.exists()) {
                return null;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player == null) {
                return null;
            }
            GameProfile profile = new GameProfile(uuid, player.getName());
            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
            // Create an entity to load the player data
            EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));

            // Get the bukkit entity
            Player target = (entity == null) ? null : entity.getBukkitEntity();
            if (target != null) {
                // Load data
                target.loadData();
                // Return the entity
                return target;
            }
        }
        catch (Exception e) {
            OpenInv.log(e);
        }

        return null;
    }

    private static UUID matchUser(String search) {
        UUID found = null;

        String lowerSearch = search.toLowerCase();
        int delta = 2147483647;

        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        for (OfflinePlayer player : offlinePlayers) {
            String name = player.getName();
           
            if (name == null){
                continue;
            }
            if (name.equalsIgnoreCase(search)){
                return player.getUniqueId();
            }
            if (name.toLowerCase().startsWith(lowerSearch)) {
                int curDelta = name.length() - lowerSearch.length();
                if (curDelta < delta) {
                    found = player.getUniqueId();
                    delta = curDelta;
                }
                if (curDelta == 0) {
                    break;
                }
            }
        }

        return found;
    }
}

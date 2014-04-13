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

package com.lishid.openinv.internal.v1_7_R3;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import net.minecraft.server.v1_7_R3.EntityPlayer;
//Volatile
import net.minecraft.server.v1_7_R3.MinecraftServer;
import net.minecraft.server.v1_7_R3.PlayerInteractManager;
import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_7_R3.CraftServer;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IPlayerDataManager;

public class PlayerDataManager implements IPlayerDataManager {
    @Override
	public Player loadPlayer(String name) {
        try {
            // Default player folder
            File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata");
            if (!playerfolder.exists()) {
                return null;
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(name);

            if (player == null || matchUser(Arrays.asList(playerfolder.listFiles()), player.getUniqueId().toString()) == null) {
            	return null;
            }

            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

            GameProfile profile = new GameProfile(player.getUniqueId(), player.getName());
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

    /**
     * @author Balor (aka Antoine Aflalo)
     */
    private static String matchUser(final Collection<File> container, final String search) {
        String found = null;
        if (search == null) {
            return found;
        }
        final String lowerSearch = search.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (final File file : container) {
            final String filename = file.getName();
            final String str = filename.substring(0, filename.length() - 4);
            if (!str.toLowerCase().startsWith(lowerSearch)) {
                continue;
            }
            final int curDelta = str.length() - lowerSearch.length();
            if (curDelta < delta) {
                found = str;
                delta = curDelta;
            }
            if (curDelta == 0) {
                break;
            }

        }
        return found;
    }
}

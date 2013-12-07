/*
 * Copyright (C) 2011-2012 lishid.  All rights reserved.
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

package com.lishid.openinv.internal.v1_4_6;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IPlayerDataManager;

//Volatile
import net.minecraft.server.v1_4_6.*;
import org.bukkit.craftbukkit.v1_4_6.*;

public class PlayerDataManager implements IPlayerDataManager {
    public Player loadPlayer(String name) {
        try {
            // Default player folder
            File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
            if (!playerfolder.exists()) {
                return null;
            }

            String playername = matchUser(Arrays.asList(playerfolder.listFiles()), name);

            if (playername == null) {
                return null;
            }

            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();

            // Create an entity to load the player data
            EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), playername, new PlayerInteractManager(server.getWorldServer(0)));

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

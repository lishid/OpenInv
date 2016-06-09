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

package com.lishid.openinv.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class PlayerDataManager {
    public final Player loadPlayer(final Plugin plugin, final OfflinePlayer offline) {
        if (offline.isOnline()) {
            return offline.getPlayer();
        }
        if (Bukkit.isPrimaryThread()) {
            return this.loadOfflinePlayer(offline);
        }


        Future<Player> future = Bukkit.getScheduler().callSyncMethod(plugin,
                new Callable<Player>() {
                    @Override
                    public Player call() throws Exception {
                        return loadOfflinePlayer(offline);
                    }
                });

        int ticks = 0;
        while (!future.isDone() && !future.isCancelled() && ticks < 10) {
            ++ticks;
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            }
        }
        if (!future.isDone() || future.isCancelled()) {
            return null;
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract Player loadOfflinePlayer(OfflinePlayer offline);

    public abstract String getPlayerDataID(OfflinePlayer player);
}

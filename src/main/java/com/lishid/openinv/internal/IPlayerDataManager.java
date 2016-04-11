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

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public abstract class IPlayerDataManager {
    public final Player loadPlayer(OfflinePlayer offline) {
        if (offline.isOnline()) {
            return offline.getPlayer();
        }
        return this.loadOfflinePlayer(offline);
    }

    protected abstract Player loadOfflinePlayer(OfflinePlayer offline);

    public abstract String getPlayerDataID(OfflinePlayer player);
}

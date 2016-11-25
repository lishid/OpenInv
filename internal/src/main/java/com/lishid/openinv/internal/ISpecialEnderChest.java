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

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public interface ISpecialEnderChest {

    /**
     * Gets the Inventory associated with this ISpecialEnderChest.
     * 
     * @return the Inventory
     */
    public Inventory getBukkitInventory();

    /**
     * Sets the Player associated with this ISpecialEnderChest online.
     * 
     * @param player the Player coming online
     */
    public void setPlayerOnline(Player player);

    /**
     * @deprecated use {@link #setPlayerOnline(Player)}
     */
    @Deprecated
    public void playerOnline(Player player);

    /**
     * Sets the Player associated with this ISpecialEnderChest offline.
     */
    public void setPlayerOffline();

    /**
     * @deprecated use {@link #setPlayerOffline()}
     */
    @Deprecated
    public void playerOffline();

    /**
     * Gets whether or not this ISpecialEnderChest is in use.
     * 
     * @return true if the ISpecialEnderChest is in use
     */
    public boolean isInUse();

}

/*
 * Copyright (C) 2011-2019 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public interface ISpecialInventory {

    /**
     * Gets the Inventory associated with this ISpecialInventory.
     *
     * @return the Inventory
     */
    @NotNull Inventory getBukkitInventory();

    /**
     * Sets the Player associated with this ISpecialInventory online.
     *
     * @param player the Player coming online
     */
    void setPlayerOnline(@NotNull Player player);

    /**
     * Sets the Player associated with this ISpecialInventory offline.
     */
    void setPlayerOffline();

    /**
     * Gets whether or not this ISpecialInventory is in use.
     *
     * @return true if the ISpecialInventory is in use
     */
    boolean isInUse();

}

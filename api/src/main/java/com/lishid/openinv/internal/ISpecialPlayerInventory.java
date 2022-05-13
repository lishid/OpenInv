/*
 * Copyright (C) 2011-2022 lishid. All rights reserved.
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

import java.util.List;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

/**
 * Interface defining behavior specific to a player inventory.
 */
public interface ISpecialPlayerInventory extends ISpecialInventory {

    /*
     * Player inventory usage varies from all other inventories - as the inventory is technically open at all times,
     * if the player is online or has been online while the inventory is open, they are in the viewer list.
     */
    @Override
    default boolean isInUse() {
        Inventory inventory = getBukkitInventory();
        List<HumanEntity> viewers = inventory.getViewers();

        if (viewers.size() != 1) {
            return !viewers.isEmpty();
        }

        HumanEntity viewer = viewers.get(0);

        if (!viewer.getUniqueId().equals(getPlayer().getUniqueId())) {
            return true;
        }

        return viewer.getOpenInventory().getTopInventory().equals(inventory);
    }

}

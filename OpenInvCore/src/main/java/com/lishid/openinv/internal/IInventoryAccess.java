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

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

public interface IInventoryAccess {

    /**
     * Check if an entity has permission to modify the contents of an inventory.
     * 
     * @param inventory the Inventory
     * @param player the HumanEntity
     * @return true if the HumanEntity can modify the Inventory
     */
    public boolean check(Inventory inventory, HumanEntity player);

}

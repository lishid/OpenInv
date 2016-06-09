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

package com.lishid.openinv.internal.v1_9_R2;

// Volatile
import net.minecraft.server.v1_9_R2.ContainerChest;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.IInventory;
import net.minecraft.server.v1_9_R2.PlayerInventory;

public class SilentContainerChest extends ContainerChest {
    public IInventory inv;

    public SilentContainerChest(IInventory i1, IInventory i2, EntityHuman e1) {
        super(i1, i2, e1);
        inv = i2;
        // close signal
        inv.closeContainer(e1);
    }

    @Override
    public void b(EntityHuman entityHuman) {
        // Don't send close signal twice, might screw up
        PlayerInventory playerinventory = entityHuman.inventory;

        if (playerinventory.getCarried() != null) {
            entityHuman.drop(playerinventory.getCarried(), false);
            playerinventory.setCarried(null);
        }
    }
}

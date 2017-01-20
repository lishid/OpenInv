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

package com.lishid.openinv.internal.v1_11_R1;

import net.minecraft.server.v1_11_R1.ContainerChest;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.IInventory;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.PlayerInventory;

public class SilentContainerChest extends ContainerChest {

    public SilentContainerChest(PlayerInventory playerInventory, IInventory iInventory,
            EntityHuman entityHuman) {
        super(playerInventory, iInventory, entityHuman);
        // Send close signal
        iInventory.closeContainer(entityHuman);
    }

    @Override
    public void b(EntityHuman entityHuman) {
        // Don't send close signal twice, might screw up
        PlayerInventory playerinventory = entityHuman.inventory;

        if (playerinventory.getCarried() != ItemStack.a) {
            ItemStack carried = playerinventory.getCarried();
            playerinventory.setCarried(ItemStack.a);
            entityHuman.drop(carried, false);
        }
    }

}

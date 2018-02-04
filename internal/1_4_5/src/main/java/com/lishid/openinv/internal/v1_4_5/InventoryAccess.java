/*
 * Copyright (C) 2011-2018 lishid. All rights reserved.
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

package com.lishid.openinv.internal.v1_4_5;

import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.InternalAccessor;

import org.bukkit.inventory.Inventory;

import net.minecraft.server.v1_4_5.IInventory;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftInventory;

public class InventoryAccess implements IInventoryAccess {

    @Override
    public boolean isSpecialPlayerInventory(Inventory inventory) {
        if (inventory instanceof CraftInventory) {
            return ((CraftInventory) inventory).getInventory() instanceof ISpecialPlayerInventory;
        }
        return InternalAccessor.grabFieldOfTypeFromObject(IInventory.class, inventory) instanceof ISpecialPlayerInventory;
    }

    @Override
    public ISpecialPlayerInventory getSpecialPlayerInventory(Inventory inventory) {
        IInventory inv;
        if (inventory instanceof CraftInventory) {
            inv = ((CraftInventory) inventory).getInventory();
        } else {
            inv = InternalAccessor.grabFieldOfTypeFromObject(IInventory.class, inventory);
        }

        if (inv instanceof SpecialPlayerInventory) {
            return (SpecialPlayerInventory) inv;
        }
        return null;
    }

    @Override
    public boolean isSpecialEnderChest(Inventory inventory) {
        if (inventory instanceof CraftInventory) {
            return ((CraftInventory) inventory).getInventory() instanceof ISpecialEnderChest;
        }
        return InternalAccessor.grabFieldOfTypeFromObject(IInventory.class, inventory) instanceof ISpecialEnderChest;
    }

    @Override
    public ISpecialEnderChest getSpecialEnderChest(Inventory inventory) {
        IInventory inv;
        if (inventory instanceof CraftInventory) {
            inv = ((CraftInventory) inventory).getInventory();
        } else {
            inv = InternalAccessor.grabFieldOfTypeFromObject(IInventory.class, inventory);
        }

        if (inv instanceof SpecialEnderChest) {
            return (SpecialEnderChest) inv;
        }
        return null;
    }

}

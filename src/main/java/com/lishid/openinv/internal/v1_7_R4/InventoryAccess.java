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

package com.lishid.openinv.internal.v1_7_R4;

import java.lang.reflect.Field;


import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.Permissions;
import com.lishid.openinv.internal.IInventoryAccess;

//Volatile
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.craftbukkit.v1_7_R4.inventory.*;

public class InventoryAccess implements IInventoryAccess {
    public boolean check(Inventory inventory, HumanEntity player) {
        IInventory inv = grabInventory(inventory);
        
        if (inv instanceof SpecialPlayerInventory) {
            if (!OpenInv.hasPermission(player, Permissions.PERM_EDITINV)) {
                return false;
            }
        }

        else if (inv instanceof SpecialEnderChest) {
            if (!OpenInv.hasPermission(player, Permissions.PERM_EDITENDER)) {
                return false;
            }
        }

        return true;
    }
    
    private IInventory grabInventory(Inventory inventory) {
        if(inventory instanceof CraftInventory) {
            return ((CraftInventory) inventory).getInventory();
        }
        
        //Use reflection to find the iiventory
        Class<? extends Inventory> clazz = inventory.getClass();
        IInventory result = null;
        for(Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            if(IInventory.class.isAssignableFrom(f.getDeclaringClass())) {
                try {
                    result = (IInventory) f.get(inventory);
                }
                catch (Exception e) {
                    OpenInv.log(e);
                }
            }
        }
        return result;
    }
}

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

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.lishid.openinv.OpenInv;

public class InternalAccessor {
    public static InternalAccessor Instance;
    private String version;

    /*
     * Returns false if version not supported
     */
    public static boolean Initialize(Server server) {
        Instance = new InternalAccessor();
        String packageName = server.getClass().getPackage().getName();
        Instance.version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            Class.forName("com.lishid.openinv.internal." + Instance.version + ".AnySilentChest");
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void PrintError() {
        OpenInv.log("OpenInv encountered an error with the CraftBukkit version \"" + Instance.version + "\". Please look for an updated version of OpenInv.");
    }

    public IPlayerDataManager newPlayerDataManager() {
        return (IPlayerDataManager) createObject(IPlayerDataManager.class, "PlayerDataManager");
    }

    public IInventoryAccess newInventoryAccess() {
        return (IInventoryAccess) createObject(IInventoryAccess.class, "InventoryAccess");
    }

    public IAnySilentChest newAnySilentChest() {
        return (IAnySilentChest) createObject(IAnySilentChest.class, "AnySilentChest");
    }

    public ISpecialPlayerInventory newSpecialPlayerInventory(Player player, boolean offline) {
        try {
            Class<?> internalClass = Class.forName("com.lishid.openinv.internal." + version + ".SpecialPlayerInventory");
            if (ISpecialPlayerInventory.class.isAssignableFrom(internalClass)) {
                return (ISpecialPlayerInventory) internalClass.getConstructor(Player.class, Boolean.class).newInstance(player, offline);
            }
        }
        catch (Exception e) {
            PrintError();
            OpenInv.log(e);
        }

        return null;
    }

    public ISpecialEnderChest newSpecialEnderChest(Player player, boolean offline) {
        try {
            Class<?> internalClass = Class.forName("com.lishid.openinv.internal." + version + ".SpecialEnderChest");
            if (ISpecialEnderChest.class.isAssignableFrom(internalClass)) {
                return (ISpecialEnderChest) internalClass.getConstructor(Player.class, Boolean.class).newInstance(player, offline);
            }
        }
        catch (Exception e) {
            PrintError();
            OpenInv.log(e);
        }

        return null;
    }

    private Object createObject(Class<? extends Object> assignableClass, String className) {
        try {
            Class<?> internalClass = Class.forName("com.lishid.openinv.internal." + version + "." + className);
            if (assignableClass.isAssignableFrom(internalClass)) {
                return internalClass.getConstructor().newInstance();
            }
        }
        catch (Exception e) {
            PrintError();
            OpenInv.log(e);
        }

        return null;
    }
}

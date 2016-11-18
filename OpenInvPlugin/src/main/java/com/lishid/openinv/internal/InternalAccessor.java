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

import com.lishid.openinv.OpenInv;

import org.bukkit.Server;
import org.bukkit.entity.Player;

public class InternalAccessor {

    private final OpenInv plugin;

    private String version;

    public InternalAccessor(OpenInv plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if the current server version is supported, and, if it is, prepare to load version-specific code.
     * 
     * @param server the Server
     * 
     * @return true if supported
     */
    public boolean initialize(Server server) {
        String packageName = server.getClass().getPackage().getName();
        version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            Class.forName("com.lishid.openinv.internal." + version + ".AnySilentChest");
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private void printErrorMessage() {
        plugin.getLogger().warning("OpenInv encountered an error with the CraftBukkit version \"" + version + "\". Please look for an updated version of OpenInv.");
    }

    public IPlayerDataManager newPlayerDataManager() {
        return (IPlayerDataManager) createObject(IPlayerDataManager.class, "PlayerDataManager");
    }

    public IInventoryAccess newInventoryAccess() {
        return (IInventoryAccess) createObject(IInventoryAccess.class, "InventoryAccess");
    }

    public IAnySilentContainer newAnySilentContainer() {
        return (IAnySilentContainer) createObject(IAnySilentContainer.class, "AnySilentContainer");
    }

    /**
     * @deprecated Use {@link #newAnySilentContainer()}
     */
    @Deprecated
    public IAnySilentChest newAnySilentChest() {
        return newAnySilentContainer();
    }

    public ISpecialPlayerInventory newSpecialPlayerInventory(Player player, boolean offline) {
        try {
            Class<?> internalClass = Class.forName("com.lishid.openinv.internal." + version + ".SpecialPlayerInventory");
            if (ISpecialPlayerInventory.class.isAssignableFrom(internalClass)) {
                return (ISpecialPlayerInventory) internalClass
                        .getConstructor(Player.class, Boolean.class)
                        .newInstance(player, offline);
            }
        }
        catch (Exception e) {
            printErrorMessage();
            e.printStackTrace();
        }

        return null;
    }

    public ISpecialEnderChest newSpecialEnderChest(Player player, boolean offline) {
        try {
            Class<?> internalClass = Class.forName("com.lishid.openinv.internal." + version + ".SpecialEnderChest");
            if (ISpecialEnderChest.class.isAssignableFrom(internalClass)) {
                return (ISpecialEnderChest) internalClass
                        .getConstructor(Player.class, Boolean.class)
                        .newInstance(player, offline);
            }
        }
        catch (Exception e) {
            printErrorMessage();
            e.printStackTrace();
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
            printErrorMessage();
            e.printStackTrace();
        }

        return null;
    }
}

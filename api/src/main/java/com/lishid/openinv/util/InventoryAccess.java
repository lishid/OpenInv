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

package com.lishid.openinv.util;

import com.lishid.openinv.internal.IInventoryAccess;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryAccess implements IInventoryAccess {

    private static Class<?> craftInventory = null;
    private static Method getInventory = null;

    static {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        try {
            craftInventory = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftInventory");
        } catch (ClassNotFoundException ignored) {}
        try {
            getInventory = craftInventory.getDeclaredMethod("getInventory");
        } catch (NoSuchMethodException ignored) {}
    }

    public static boolean isUseable() {
        return craftInventory != null && getInventory != null;
    }

    public static boolean isPlayerInventory(@NotNull Inventory inventory) {
        if (craftInventory.isAssignableFrom(inventory.getClass())) {
            try {
                return getInventory.invoke(inventory) instanceof ISpecialPlayerInventory;
            } catch (ReflectiveOperationException ignored) {}
        }
        return grabFieldOfTypeFromObject(ISpecialPlayerInventory.class, inventory) != null;
    }

    public static ISpecialPlayerInventory getPlayerInventory(@NotNull Inventory inventory) {
        Object inv = null;
        if (craftInventory.isAssignableFrom(inventory.getClass())) {
            try {
                inv = getInventory.invoke(inventory);
            } catch (ReflectiveOperationException ignored) {}
        }

        if (inv == null) {
            inv = grabFieldOfTypeFromObject(ISpecialPlayerInventory.class, inventory);
        }

        if (inv instanceof ISpecialPlayerInventory) {
            return (ISpecialPlayerInventory) inv;
        }

        return null;
    }

    public static boolean isEnderChest(@NotNull Inventory inventory) {
        if (craftInventory.isAssignableFrom(inventory.getClass())) {
            try {
                return getInventory.invoke(inventory) instanceof ISpecialEnderChest;
            } catch (ReflectiveOperationException ignored) {}
        }
        return grabFieldOfTypeFromObject(ISpecialEnderChest.class, inventory) != null;
    }

    public static ISpecialEnderChest getEnderChest(@NotNull Inventory inventory) {
        Object inv = null;
        if (craftInventory.isAssignableFrom(inventory.getClass())) {
            try {
                inv = getInventory.invoke(inventory);
            } catch (ReflectiveOperationException ignored) {}
        }

        if (inv == null) {
            inv = grabFieldOfTypeFromObject(ISpecialEnderChest.class, inventory);
        }

        if (inv instanceof ISpecialEnderChest) {
            return (ISpecialEnderChest) inv;
        }

        return null;
    }

    private static <T> T grabFieldOfTypeFromObject(final Class<T> type, final Object object) {
        // Use reflection to find the IInventory
        Class<?> clazz = object.getClass();
        T result = null;
        for (Field f : clazz.getDeclaredFields()) {
            f.setAccessible(true);
            if (type.isAssignableFrom(f.getDeclaringClass())) {
                try {
                    result = type.cast(f.get(object));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Deprecated
    @Override
    public @Nullable ISpecialEnderChest getSpecialEnderChest(@NotNull Inventory inventory) {
        return getEnderChest(inventory);
    }

    @Deprecated
    @Override
    public @Nullable ISpecialPlayerInventory getSpecialPlayerInventory(@NotNull Inventory inventory) {
        return getPlayerInventory(inventory);
    }

    @Deprecated
    @Override
    public boolean isSpecialEnderChest(@NotNull Inventory inventory) {
        return isEnderChest(inventory);
    }

    @Deprecated
    @Override
    public boolean isSpecialPlayerInventory(@NotNull Inventory inventory) {
        return isPlayerInventory(inventory);
    }
}

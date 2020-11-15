/*
 * Copyright (C) 2011-2020 lishid. All rights reserved.
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
import com.lishid.openinv.internal.ISpecialInventory;
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
        try {
            craftInventory = Class.forName(packageName + ".inventory.CraftInventory");
        } catch (ClassNotFoundException ignored) {}
        try {
            getInventory = craftInventory.getDeclaredMethod("getInventory");
        } catch (NoSuchMethodException ignored) {}
    }

    public static boolean isUseable() {
        return craftInventory != null && getInventory != null;
    }

    public static boolean isPlayerInventory(@NotNull Inventory inventory) {
        return getPlayerInventory(inventory) != null;
    }

    public static @Nullable ISpecialPlayerInventory getPlayerInventory(@NotNull Inventory inventory) {
        return getSpecialInventory(ISpecialPlayerInventory.class, inventory);
    }

    public static boolean isEnderChest(@NotNull Inventory inventory) {
        return getEnderChest(inventory) != null;
    }

    public static @Nullable ISpecialEnderChest getEnderChest(@NotNull Inventory inventory) {
        return getSpecialInventory(ISpecialEnderChest.class, inventory);
    }

    private static <T extends ISpecialInventory> @Nullable T getSpecialInventory(@NotNull Class<T> expected, @NotNull Inventory inventory) {
        Object inv;
        if (craftInventory != null && getInventory != null && craftInventory.isAssignableFrom(inventory.getClass())) {
            try {
                inv = getInventory.invoke(inventory);
                if (expected.isInstance(inv)) {
                    return expected.cast(inv);
                }
            } catch (ReflectiveOperationException ignored) {}
        }

        inv = grabFieldOfTypeFromObject(expected, inventory);

        if (expected.isInstance(inv)) {
            return expected.cast(inv);
        }

        return null;
    }

    private static <T> @Nullable T grabFieldOfTypeFromObject(final Class<T> type, final Object object) {
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

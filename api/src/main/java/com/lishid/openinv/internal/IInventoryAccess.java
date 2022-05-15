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

import com.lishid.openinv.util.InventoryAccess;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use static {@link InventoryAccess} methods.
 */
@Deprecated(forRemoval = true)
public interface IInventoryAccess {

    /**
     * @deprecated Use static {@link InventoryAccess} methods.
     */
    @Deprecated(forRemoval = true)
    default @Nullable ISpecialEnderChest getSpecialEnderChest(@NotNull Inventory inventory) {
        return InventoryAccess.getEnderChest(inventory);
    }

    /**
     * @deprecated Use static {@link InventoryAccess} methods.
     */
    @Deprecated(forRemoval = true)
    default @Nullable ISpecialPlayerInventory getSpecialPlayerInventory(@NotNull Inventory inventory) {
        return InventoryAccess.getPlayerInventory(inventory);
    }

    /**
     * @deprecated Use static {@link InventoryAccess} methods.
     */
    @Deprecated(forRemoval = true)
    default boolean isSpecialEnderChest(@NotNull Inventory inventory) {
        return InventoryAccess.isEnderChest(inventory);
    }

    /**
     * @deprecated Use static {@link InventoryAccess} methods.
     */
    @Deprecated(forRemoval = true)
    default boolean isSpecialPlayerInventory(@NotNull Inventory inventory) {
        return InventoryAccess.isPlayerInventory(inventory);
    }

}

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

package com.lishid.openinv.internal;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
public interface IInventoryAccess {

    /**
     * Gets an ISpecialEnderChest from an Inventory or null if the Inventory is not backed by an
     * ISpecialEnderChest.
     *
     * @param inventory the Inventory
     * @return the ISpecialEnderChest or null
     */
    @Deprecated
    @Nullable ISpecialEnderChest getSpecialEnderChest(@NotNull Inventory inventory);

    /**
     * Gets an ISpecialPlayerInventory from an Inventory or null if the Inventory is not backed by
     * an ISpecialPlayerInventory.
     *
     * @param inventory the Inventory
     * @return the ISpecialPlayerInventory or null
     */
    @Deprecated
    @Nullable ISpecialPlayerInventory getSpecialPlayerInventory(@NotNull Inventory inventory);

    /**
     * Check if an Inventory is an ISpecialEnderChest implementation.
     *
     * @param inventory the Inventory
     * @return true if the Inventory is backed by an ISpecialEnderChest
     */
    @Deprecated
    boolean isSpecialEnderChest(@NotNull Inventory inventory);

    /**
     * Check if an Inventory is an ISpecialPlayerInventory implementation.
     *
     * @param inventory the Inventory
     * @return true if the Inventory is backed by an ISpecialPlayerInventory
     */
    @Deprecated
    boolean isSpecialPlayerInventory(@NotNull Inventory inventory);

}

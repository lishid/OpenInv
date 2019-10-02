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

package com.lishid.openinv.internal.v1_8_R3;

import com.lishid.openinv.internal.ISpecialPlayerInventory;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class SpecialPlayerInventory extends PlayerInventory implements ISpecialPlayerInventory {

    private final ItemStack[] extra = new ItemStack[5];
    private final CraftInventory inventory = new CraftInventory(this);
    private boolean playerOnline;

    public SpecialPlayerInventory(Player bukkitPlayer, Boolean online) {
        super(PlayerDataManager.getHandle(bukkitPlayer));
        this.playerOnline = online;
        this.items = player.inventory.items;
        this.armor = player.inventory.armor;
    }

    @Override
    public @NotNull Inventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public void setPlayerOnline(@NotNull Player player) {
        if (!playerOnline) {
            this.player = PlayerDataManager.getHandle(player);
            this.player.inventory.items = this.items;
            this.player.inventory.armor = this.armor;
            playerOnline = true;
        }
    }

    @Override
    public void setPlayerOffline() {
        playerOnline = false;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[getSize()];
        System.arraycopy(items, 0, contents, 0, items.length);
        System.arraycopy(armor, 0, contents, items.length, armor.length);
        return contents;
    }

    @Override
    public int getSize() {
        return super.getSize() + 5;
    }

    @Override
    public ItemStack getItem(int i) {
        ItemStack[] is = this.items;

        if (i >= is.length) {
            i -= is.length;
            is = this.armor;
        } else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extra;
        } else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        return is[i];
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        ItemStack[] is = this.items;

        if (i >= is.length) {
            i -= is.length;
            is = this.armor;
        } else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extra;
        } else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        if (is[i] != null) {
            ItemStack itemstack;

            if (is[i].count <= j) {
                itemstack = is[i];
                is[i] = null;
                return itemstack;
            } else {
                itemstack = is[i].cloneAndSubtract(j);
                if (is[i].count == 0) {
                    is[i] = null;
                }

                return itemstack;
            }
        }

        return null;
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        ItemStack[] is = this.items;

        if (i >= is.length) {
            i -= is.length;
            is = this.armor;
        } else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extra;
        } else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        if (is[i] != null) {
            ItemStack itemstack = is[i];

            is[i] = null;
            return itemstack;
        }

        return null;
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        ItemStack[] is = this.items;

        if (i >= is.length) {
            i -= is.length;
            is = this.armor;
        } else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extra;
        } else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        // Effects
        if (is == this.extra) {
            player.drop(itemstack, true);
            itemstack = null;
        }

        is[i] = itemstack;

        player.defaultContainer.b();
    }

    private int getReversedItemSlotNum(int i) {
        if (i >= 27) {
            return i - 27;
        }
        return i + 9;
    }

    private int getReversedArmorSlotNum(int i) {
        if (i == 0) {
            return 3;
        }
        if (i == 1) {
            return 2;
        }
        if (i == 2) {
            return 1;
        }
        if (i == 3) {
            return 0;
        }
        return i;
    }

    @Override
    public String getName() {
        if (player.getName().length() > 16) {
            return player.getName().substring(0, 16);
        }
        return player.getName();
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

}

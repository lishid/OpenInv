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

package com.lishid.openinv.internal.v1_13_R2;

import com.lishid.openinv.internal.ISpecialPlayerInventory;
import net.minecraft.server.v1_13_R2.ChatMessage;
import net.minecraft.server.v1_13_R2.ContainerUtil;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.NonNullList;
import net.minecraft.server.v1_13_R2.PlayerInventory;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class SpecialPlayerInventory extends PlayerInventory implements ISpecialPlayerInventory {

    private final CraftInventory inventory = new CraftInventory(this);
    private boolean playerOnline;

    public SpecialPlayerInventory(final Player bukkitPlayer, final Boolean online) {
        super(PlayerDataManager.getHandle(bukkitPlayer));
        this.playerOnline = online;
        this.setItemArrays(this, this.player.inventory.items, this.player.inventory.armor,
                this.player.inventory.extraSlots);
    }

    @Override
    public boolean a(final EntityHuman entityhuman) {
        return true;
    }

    @Override
    public Inventory getBukkitInventory() {
        return this.inventory;
    }

    @Override
    public ItemStack getItem(int i) {
        NonNullList<ItemStack> list = this.items;

        if (i >= list.size()) {
            i -= list.size();
            list = this.armor;
        } else {
            i = this.getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.extraSlots;
        } else if (list == this.armor) {
            i = this.getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            return ItemStack.a;
        }

        return list.get(i);
    }

    @Override
    public IChatBaseComponent getDisplayName() {
        return new ChatMessage(this.player.getName());
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    private int getReversedArmorSlotNum(final int i) {
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

    private int getReversedItemSlotNum(final int i) {
        if (i >= 27) {
            return i - 27;
        }
        return i + 9;
    }

    @Override
    public int getSize() {
        return super.getSize() + 4;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    @Override
    public void setItem(int i, final ItemStack itemstack) {
        NonNullList<ItemStack> list = this.items;

        if (i >= list.size()) {
            i -= list.size();
            list = this.armor;
        } else {
            i = this.getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.extraSlots;
        } else if (list == this.armor) {
            i = this.getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            this.player.drop(itemstack, true);
            return;
        }

        list.set(i, itemstack);
    }

    private void setItemArrays(final PlayerInventory inventory, final NonNullList<ItemStack> items,
            final NonNullList<ItemStack> armor, final NonNullList<ItemStack> extraSlots) {
        try {
            // Prepare to remove final modifier
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);

            // Access and replace main inventory list
            Field field = PlayerInventory.class.getField("items");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, items);

            // Access and replace armor inventory list
            field = PlayerInventory.class.getField("armor");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, armor);

            // Access and replace offhand inventory list
            field = PlayerInventory.class.getField("extraSlots");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, extraSlots);

            // Access and replace list containing all inventory lists
            field = PlayerInventory.class.getDeclaredField("f");
            field.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, Arrays.asList(items, armor, extraSlots));
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            // Unable to set final fields to item lists, we're screwed. Noisily fail.
            e.printStackTrace();
        }
    }

    @Override
    public void setPlayerOffline() {
        this.playerOnline = false;
    }

    @Override
    public void setPlayerOnline(final Player player) {
        if (!this.playerOnline) {
            this.player = PlayerDataManager.getHandle(player);
            this.setItemArrays(this.player.inventory, this.items, this.armor, this.extraSlots);
            this.playerOnline = true;
        }
    }

    @Override
    public ItemStack splitStack(int i, final int j) {
        NonNullList<ItemStack> list = this.items;

        if (i >= list.size()) {
            i -= list.size();
            list = this.armor;
        } else {
            i = this.getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.extraSlots;
        } else if (list == this.armor) {
            i = this.getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            return ItemStack.a;
        }

        return list.get(i).isEmpty() ? ItemStack.a : ContainerUtil.a(list, i, j);
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        NonNullList<ItemStack> list = this.items;

        if (i >= list.size()) {
            i -= list.size();
            list = this.armor;
        } else {
            i = this.getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.extraSlots;
        } else if (list == this.armor) {
            i = this.getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            return ItemStack.a;
        }

        if (!list.get(i).isEmpty()) {
            ItemStack itemstack = list.get(i);

            list.set(i, ItemStack.a);
            return itemstack;
        }

        return ItemStack.a;
    }

}

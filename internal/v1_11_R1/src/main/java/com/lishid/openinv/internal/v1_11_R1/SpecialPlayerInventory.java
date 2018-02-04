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

package com.lishid.openinv.internal.v1_11_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.lishid.openinv.internal.ISpecialPlayerInventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.minecraft.server.v1_11_R1.ContainerUtil;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.NonNullList;
import net.minecraft.server.v1_11_R1.PlayerInventory;

import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftInventory;

public class SpecialPlayerInventory extends PlayerInventory implements ISpecialPlayerInventory {

    private final CraftInventory inventory = new CraftInventory(this);
    private boolean playerOnline;

    public SpecialPlayerInventory(Player bukkitPlayer, Boolean online) {
        super(PlayerDataManager.getHandle(bukkitPlayer));
        this.playerOnline = online;
        setItemArrays(this, player.inventory.items, player.inventory.armor, player.inventory.extraSlots);
    }

    private void setItemArrays(PlayerInventory inventory, NonNullList<ItemStack> items,
            NonNullList<ItemStack> armor, NonNullList<ItemStack> extraSlots) {
        try {
            // Prepare to remove final modifier
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);

            // Access and replace main inventory array
            Field field = PlayerInventory.class.getField("items");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, items);

            // Access and replace armor inventory array
            field = PlayerInventory.class.getField("armor");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, armor);

            // Access and replace offhand inventory array
            field = PlayerInventory.class.getField("extraSlots");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, extraSlots);

            // Access and replace array containing all inventory arrays
            field = PlayerInventory.class.getDeclaredField("g");
            field.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, Arrays.asList(items, armor, extraSlots));
        } catch (NoSuchFieldException e) {
            // Unable to set final fields to item arrays, we're screwed. Noisily fail.
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Inventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public void setPlayerOnline(Player player) {
        if (!playerOnline) {
            this.player = PlayerDataManager.getHandle(player);
            setItemArrays(this.player.inventory, items, armor, extraSlots);
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
    public int getSize() {
        return super.getSize() + 4;
    }

    @Override
    public ItemStack getItem(int i) {
        NonNullList<ItemStack> list = this.items;

        if (i >= list.size()) {
            i -= list.size();
            list = this.armor;
        } else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.extraSlots;
        } else if (list == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            return ItemStack.a;
        }

        return list.get(i);
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        NonNullList<ItemStack> list = this.items;

        if (i >= list.size()) {
            i -= list.size();
            list = this.armor;
        } else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.extraSlots;
        } else if (list == this.armor) {
            i = getReversedArmorSlotNum(i);
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
            i = getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.extraSlots;
        } else if (list == this.armor) {
            i = getReversedArmorSlotNum(i);
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

    @Override
    public void setItem(int i, ItemStack itemstack) {
        NonNullList<ItemStack> list = this.items;

        if (i >= list.size()) {
            i -= list.size();
            list = this.armor;
        } else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.extraSlots;
        } else if (list == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            player.drop(itemstack, true);
            return;
        }

        list.set(i, itemstack);
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

    @Override
    public boolean a(EntityHuman entityhuman) {
        return true;
    }

}

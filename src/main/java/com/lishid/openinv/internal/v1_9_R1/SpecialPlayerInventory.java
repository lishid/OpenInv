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

package com.lishid.openinv.internal.v1_9_R1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.lishid.openinv.internal.ISpecialPlayerInventory;

// Volatile
import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.PlayerInventory;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftInventory;

public class SpecialPlayerInventory extends PlayerInventory implements ISpecialPlayerInventory {
    CraftPlayer owner;
    public boolean playerOnline = false;
    private final ItemStack[] extra = new ItemStack[4];
    private final CraftInventory inventory = new CraftInventory(this);

    public SpecialPlayerInventory(Player p, Boolean online) {
        super(((CraftPlayer) p).getHandle());
        this.owner = ((CraftPlayer) p);
        this.playerOnline = online;
        setItemArrays(this, player.inventory.items, player.inventory.armor, player.inventory.extraSlots);
    }

    private void setItemArrays(PlayerInventory inventory, ItemStack[] items, ItemStack[] armor,
            ItemStack[] extraSlots) {
        try {
            Field field = inventory.getClass().getField("items");
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, items);
            field = inventory.getClass().getField("armor");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, armor);
            field = inventory.getClass().getField("extraSlots");
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(inventory, extraSlots);
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
    public boolean inventoryRemovalCheck() {
        owner.saveData();
        return transaction.isEmpty() && !playerOnline;
    }

    @Override
    public void setPlayerOnline(Player player) {
        if (!playerOnline) {
            CraftPlayer p = (CraftPlayer) player;
            setItemArrays(p.getHandle().inventory, items, armor, extraSlots);
            p.saveData();
            playerOnline = true;
        }
    }

    @Override
    public boolean setPlayerOffline() {
        playerOnline = false;
        return this.inventoryRemovalCheck();
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
        this.inventoryRemovalCheck();
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] C = new ItemStack[getSize()];
        System.arraycopy(items, 0, C, 0, items.length);
        System.arraycopy(armor, 0, C, items.length, armor.length);
        System.arraycopy(extraSlots, 0, C, items.length + armor.length, extraSlots.length);
        return C;
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
        }
        else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extraSlots;
        }
        else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extra;
        }

        // extraSlots is, for now, just an array with length 1. No need for special handling.

        return is[i];
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        ItemStack[] is = this.items;

        if (i >= is.length) {
            i -= is.length;
            is = this.armor;
        }
        else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extraSlots;
        }
        else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extra;
        }

        if (is[i] != null) {
            ItemStack itemstack;

            if (is[i].count <= j) {
                itemstack = is[i];
                is[i] = null;
                return itemstack;
            }
            else {
                itemstack = is[i].cloneAndSubtract(j);
                if (is[i].count == 0) {
                    is[i] = null;
                }

                return itemstack;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        ItemStack[] is = this.items;

        if (i >= is.length) {
            i -= is.length;
            is = this.armor;
        }
        else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extraSlots;
        }
        else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extra;
        }

        if (is[i] != null) {
            ItemStack itemstack = is[i];

            is[i] = null;
            return itemstack;
        }
        else {
            return null;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        ItemStack[] is = this.items;

        if (i >= is.length) {
            i -= is.length;
            is = this.armor;
        }
        else {
            i = getReversedItemSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extraSlots;
        }
        else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        }

        if (i >= is.length) {
            i -= is.length;
            is = this.extra;
        }

        // Effects
        if (is == this.extra) {
            owner.getHandle().drop(itemstack, true);
            itemstack = null;
        }

        is[i] = itemstack;

        owner.getHandle().defaultContainer.b();
    }

    private int getReversedItemSlotNum(int i) {
        if (i >= 27)
            return i - 27;
        else
            return i + 9;
    }

    private int getReversedArmorSlotNum(int i) {
        if (i == 0)
            return 3;
        if (i == 1)
            return 2;
        if (i == 2)
            return 1;
        if (i == 3)
            return 0;
        else
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
    public boolean a(EntityHuman entityhuman) {
        return true;
    }
}

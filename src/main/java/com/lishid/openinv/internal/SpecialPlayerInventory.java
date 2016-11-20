/*
 * Copyright (C) 2011-2016 lishid.  All rights reserved.
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

import java.lang.reflect.Field;

import net.minecraft.server.v1_11_R1.*;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SpecialPlayerInventory extends PlayerInventory {

    private final CraftInventory inventory = new CraftInventory(this);
    private final NonNullList<ItemStack> extra = NonNullList.a();
    private CraftPlayer owner;
    private NonNullList<ItemStack>[] arrays;
    private boolean playerOnline;

    public SpecialPlayerInventory(Player p, boolean online) {
        super(((CraftPlayer) p).getHandle());
        this.owner = (CraftPlayer) p;
        this.playerOnline = online;
        reflectContents(getClass().getSuperclass(), player.inventory, this);
    }

    private void reflectContents(Class clazz, PlayerInventory src, PlayerInventory dest) {
        try {
            Field itemsField = clazz.getDeclaredField("items");
            itemsField.setAccessible(true);
            itemsField.set(dest, src.items);

            Field armorField = clazz.getDeclaredField("armor");
            armorField.setAccessible(true);
            armorField.set(dest, src.armor);

            Field extraSlotsField = clazz.getDeclaredField("extraSlots");
            extraSlotsField.setAccessible(true);
            extraSlotsField.set(dest, src.extraSlots);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //noinspection unchecked
        arrays = new NonNullList[] { this.items, this.armor, this.extraSlots, this.extra };
    }

    private void linkInventory(PlayerInventory inventory) {
        reflectContents(inventory.getClass(), inventory, this);
    }

    public Inventory getBukkitInventory() {
        return inventory;
    }

    public boolean inventoryRemovalCheck(boolean save) {
        boolean offline = transaction.isEmpty() && !playerOnline;

        if (offline && save) {
            owner.saveData();
        }

        return offline;
    }

    public void playerOnline(Player player) {
        if (!playerOnline) {
            owner = (CraftPlayer) player;
            this.player = owner.getHandle();
            linkInventory(owner.getHandle().inventory);
            playerOnline = true;
        }
    }

    public boolean playerOffline() {
        playerOnline = false;
        return inventoryRemovalCheck(false);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
        inventoryRemovalCheck(true);
    }

    @Override
    public NonNullList<ItemStack> getContents() {
        NonNullList<ItemStack> contents = NonNullList.a();
        contents.addAll(this.items);
        contents.addAll(this.armor);
        contents.addAll(this.extraSlots);
        return contents;
    }

    @Override
    public int getSize() {
        return super.getSize() + 4;
    }

    @Override
    public ItemStack getItem(int i) {
        NonNullList<ItemStack> is = null;
        NonNullList<ItemStack>[] contents = this.arrays;
        int j = contents.length;

        for (int k = 0; k < j; ++k) {
            NonNullList<ItemStack> is2 = contents[k];

            if (i < is2.size()) {
                is = is2;
                break;
            }

            i -= is2.size();
        }

        if (is == this.items) {
            i = getReversedItemSlotNum(i);
        } else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        } else if (is == this.extraSlots) {
            // Do nothing
        } else if (is == this.extra) {
            // Do nothing
        }

        return is == null ? ItemStack.a : is.get(i);
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        NonNullList<ItemStack> is = null;
        NonNullList<ItemStack>[] contents = this.arrays;
        int k = contents.length;

        for (int l = 0; l < k; ++l) {
            NonNullList<ItemStack> is2 = contents[l];

            if (i < is2.size()) {
                is = is2;
                break;
            }

            i -= is2.size();
        }

        if (is == this.items) {
            i = getReversedItemSlotNum(i);
        } else if (is == this.armor) {
            i = getReversedArmorSlotNum(i);
        } else if (is == this.extraSlots) {
            // Do nothing
        } else if (is == this.extra) {
            // Do nothing
        }

        return is != null && !is.get(i).isEmpty() ? ContainerUtil.a(is, i, j) : ItemStack.a;
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        NonNullList<ItemStack> is = null;
        NonNullList<ItemStack>[] contents = this.arrays;
        int j = contents.length;

        for (int object = 0; object < j; ++object) {
            NonNullList<ItemStack> is2 = contents[object];

            if (i < is2.size()) {
                is = is2;
                break;
            }

            i -= is2.size();
        }

        if (is != null && !is.get(i).isEmpty()) {
            if (is == this.items) {
                i = getReversedItemSlotNum(i);
            } else if (is == this.armor) {
                i = getReversedArmorSlotNum(i);
            } else if (is == this.extraSlots) {
                // Do nothing
            } else if (is == this.extra) {
                // Do nothing
            }

            Object object = is.get(i);
            is.set(i, ItemStack.a);
            return (ItemStack) object;
        } else {
            return ItemStack.a;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        NonNullList<ItemStack> is = null;
        NonNullList<ItemStack>[] contents = this.arrays;
        int j = contents.length;

        for (int k = 0; k < j; ++k) {
            NonNullList<ItemStack> is2 = contents[k];

            if (i < is2.size()) {
                is = is2;
                break;
            }

            i -= is2.size();
        }

        if (is != null) {
            if (is == this.items) {
                i = getReversedItemSlotNum(i);
            } else if (is == this.armor) {
                i = getReversedArmorSlotNum(i);
            } else if (is == this.extraSlots) {
                // Do nothing
            } else if (is == this.extra) {
                owner.getHandle().drop(itemStack, true);
                itemStack = ItemStack.a;
            }

            is.set(i, itemStack);

            owner.getHandle().defaultContainer.b();
        }
    }

    private int getReversedItemSlotNum(int i) {
        return (i >= 27) ? (i - 27) : (i + 9);
    }

    private int getReversedArmorSlotNum(int i) {
        if (i == 0) return 3;
        if (i == 1) return 2;
        if (i == 2) return 1;
        return (i == 3) ? 0 : i;
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return true;
    }

    @Override
    public void update() {
        super.update();
        player.inventory.update();
    }
}

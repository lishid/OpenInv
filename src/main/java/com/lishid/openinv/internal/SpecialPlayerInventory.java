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

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.lishid.openinv.OpenInv;

import net.minecraft.server.v1_9_R1.ContainerUtil;
import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.PlayerInventory;

public class SpecialPlayerInventory extends PlayerInventory {

    private final CraftInventory inventory = new CraftInventory(this);
    private final ItemStack[] extra = new ItemStack[4];
    private CraftPlayer owner;
    private ItemStack[][] arrays;
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

        arrays = new ItemStack[][] { this.items, this.armor, this.extraSlots, this.extra };
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
    public ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[getSize()];
        System.arraycopy(this.items, 0, contents, 0, this.items.length);
        System.arraycopy(this.armor, 0, contents, this.items.length, this.armor.length);
        System.arraycopy(this.extraSlots, 0, contents, this.items.length + this.armor.length, this.extraSlots.length);
        return contents;
    }

    @Override
    public int getSize() {
        return super.getSize() + 4;
    }

    @Override
    public ItemStack getItem(int i) {
        ItemStack[] is = null;
        ItemStack[][] contents = this.arrays;
        int j = contents.length;

        for (int k = 0; k < j; ++k) {
            ItemStack[] is2 = contents[k];

            if (i < is2.length) {
                is = is2;
                break;
            }

            i -= is2.length;
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

        return is == null ? null : is[i];
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        ItemStack[] is = null;
        ItemStack[][] contents = this.arrays;
        int k = contents.length;

        for (int l = 0; l < k; ++l) {
            ItemStack[] is2 = contents[l];

            if (i < is2.length) {
                is = is2;
                break;
            }

            i -= is2.length;
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

        return is != null && is[i] != null ? ContainerUtil.a(is, i, j) : null;
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        ItemStack[] is = null;
        ItemStack[][] contents = this.arrays;
        int j = contents.length;

        for (int object = 0; object < j; ++object) {
            ItemStack[] is2 = contents[object];

            if (i < is2.length) {
                is = is2;
                break;
            }

            i -= is2.length;
        }

        if (is != null && is[i] != null) {
            if (is == this.items) {
                i = getReversedItemSlotNum(i);
            } else if (is == this.armor) {
                i = getReversedArmorSlotNum(i);
            } else if (is == this.extraSlots) {
                // Do nothing
            } else if (is == this.extra) {
                // Do nothing
            }

            Object object = is[i];
            is[i] = null;
            return (ItemStack) object;
        } else {
            return null;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        ItemStack[] is = null;
        ItemStack[][] contents = this.arrays;
        int j = contents.length;

        for (int k = 0; k < j; ++k) {
            ItemStack[] is2 = contents[k];

            if (i < is2.length) {
                is = is2;
                break;
            }

            i -= is2.length;
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
                itemStack = null;
            }

            is[i] = itemStack;

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

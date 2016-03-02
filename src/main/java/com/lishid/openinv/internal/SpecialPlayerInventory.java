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

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.lishid.openinv.OpenInv;

// Volatile
import net.minecraft.server.v1_9_R1.*;

import org.bukkit.craftbukkit.v1_9_R1.entity.*;
import org.bukkit.craftbukkit.v1_9_R1.inventory.*;

/*
 * -----------------------------------------------
 * This class still needs to be updated for 1.9.
 *
 * It has been partially updated, but is very buggy
 * and does not work correctly.
 * -----------------------------------------------
 */
public class SpecialPlayerInventory extends PlayerInventory {
    private final CraftInventory inventory = new CraftInventory(this);
    private final ItemStack[] extra = new ItemStack[5];
    private final ItemStack[][] g;
    private final CraftPlayer owner;
    private boolean playerOnline;

    public SpecialPlayerInventory(Player p, boolean online) {
        super(((CraftPlayer) p).getHandle());
        this.owner = (CraftPlayer) p;
        System.arraycopy(player.inventory.items, 0, this.items, 0, this.items.length);
        System.arraycopy(player.inventory.armor, 0, this.armor, 0, this.armor.length);
        this.g = new ItemStack[][]{this.items, this.armor, this.extra};
        this.playerOnline = online;
        // OpenInv.inventories.put(owner.getUniqueId(), this);
    }

    public Inventory getBukkitInventory() {
        return inventory;
    }

    private void saveOnExit() {
        if (playerOnline) {
            linkInventory(player.inventory);
        }

        if (transaction.isEmpty() && !playerOnline) {
            owner.saveData();
            // OpenInv.inventories.remove(owner.getUniqueId());
        }
    }

    private void linkInventory(PlayerInventory inventory) {
        System.arraycopy(this.items, 0, inventory.items, 0, inventory.items.length);
        System.arraycopy(this.armor, 0, inventory.armor, 0, inventory.armor.length);
    }

    public void playerOnline(Player player) {
        if (!playerOnline) {
            CraftPlayer p = (CraftPlayer) player;
            linkInventory(p.getHandle().inventory);
            p.saveData();
            playerOnline = true;
        }
    }

    public void playerOffline() {
        playerOnline = false;
        owner.loadData();
        linkInventory(owner.getHandle().inventory);
        saveOnExit();
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
        this.saveOnExit();
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[getSize()];
        System.arraycopy(this.items, 0, contents, 0, this.items.length);
        System.arraycopy(this.armor, 0, contents, this.items.length, this.armor.length);
        return contents;
    }

    @Override
    public int getSize() {
        return super.getSize() + 5; // super.getSize() - this.extraSlots.length + 5;
    }

    @Override
    public ItemStack getItem(int i) {
        ItemStack[] is = null;
        ItemStack[][] contents = this.g;
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
        } else if (is == this.extra) {
            // Do nothing
        }

        return is == null ? null : is[i];
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        ItemStack[] is = null;
        ItemStack[][] contents = this.g;
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
        } else if (is == this.extra) {
            // Do nothing
        }

        return is != null && is[i] != null ? ContainerUtil.a(is, i, j) : null;
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        ItemStack[] is = null;
        ItemStack[][] contents = this.g;
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
        ItemStack[][] contents = this.g;
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
            } else if (is == this.extra) {
                owner.getHandle().drop(itemStack, true);
                itemStack = null;
            }

            is[i] = itemStack;

            // owner.getHandle().defaultContainer.b();
        }
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

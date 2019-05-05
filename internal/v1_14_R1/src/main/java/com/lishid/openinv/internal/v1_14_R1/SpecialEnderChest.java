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

package com.lishid.openinv.internal.v1_14_R1;

import com.lishid.openinv.internal.ISpecialEnderChest;
import java.util.List;
import net.minecraft.server.v1_14_R1.AutoRecipeStackManager;
import net.minecraft.server.v1_14_R1.ContainerUtil;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.IInventoryListener;
import net.minecraft.server.v1_14_R1.InventoryEnderChest;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.NonNullList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SpecialEnderChest extends InventoryEnderChest implements ISpecialEnderChest {

    private final CraftInventory inventory;
    private EntityPlayer owner;
    private NonNullList<ItemStack> items;
    private boolean playerOnline;

    public SpecialEnderChest(final Player player, final Boolean online) {
        super(PlayerDataManager.getHandle(player));
        this.inventory = new CraftInventory(this);
        this.owner = PlayerDataManager.getHandle(player);
        this.playerOnline = online;
        this.items = this.owner.getEnderChest().items;
    }

    @Override
    public @NotNull CraftInventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    @Override
    public void setPlayerOffline() {
        this.playerOnline = false;
    }

    @Override
    public void setPlayerOnline(@NotNull final Player player) {
        if (!this.playerOnline) {
            try {
                this.owner = PlayerDataManager.getHandle(player);
                InventoryEnderChest enderChest = owner.getEnderChest();
                for (int i = 0; i < enderChest.getSize(); ++i) {
                    enderChest.setItem(i, this.items.get(i));
                }
                this.items = enderChest.items;
            } catch (Exception ignored) {}
            this.playerOnline = true;
        }
    }

    @Override
    public void update() {
        this.owner.getEnderChest().update();
    }

    @Override
    public List<ItemStack> getContents() {
        return this.items;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        this.owner.getEnderChest().onOpen(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        this.owner.getEnderChest().onClose(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.owner.getEnderChest().getViewers();
    }

    @Override
    public void setMaxStackSize(int i) {
        this.owner.getEnderChest().setMaxStackSize(i);
    }

    @Override
    public InventoryHolder getOwner() {
        return this.owner.getEnderChest().getOwner();
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void a(IInventoryListener iinventorylistener) {
        this.owner.getEnderChest().a(iinventorylistener);
    }

    @Override
    public void b(IInventoryListener iinventorylistener) {
        this.owner.getEnderChest().b(iinventorylistener);
    }

    @Override
    public ItemStack getItem(int i) {
        return i >= 0 && i < this.items.size() ? this.items.get(i) : ItemStack.a;
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        ItemStack itemstack = ContainerUtil.a(this.items, i, j);
        if (!itemstack.isEmpty()) {
            this.update();
        }

        return itemstack;
    }

    @Override
    public ItemStack a(ItemStack itemstack) {
        ItemStack itemstack1 = itemstack.cloneItemStack();

        for (int i = 0; i < this.getSize(); ++i) {
            ItemStack itemstack2 = this.getItem(i);
            if (itemstack2.isEmpty()) {
                this.setItem(i, itemstack1);
                this.update();
                return ItemStack.a;
            }

            if (ItemStack.c(itemstack2, itemstack1)) {
                int j = Math.min(this.getMaxStackSize(), itemstack2.getMaxStackSize());
                int k = Math.min(itemstack1.getCount(), j - itemstack2.getCount());
                if (k > 0) {
                    itemstack2.add(k);
                    itemstack1.subtract(k);
                    if (itemstack1.isEmpty()) {
                        this.update();
                        return ItemStack.a;
                    }
                }
            }
        }

        if (itemstack1.getCount() != itemstack.getCount()) {
            this.update();
        }

        return itemstack1;
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        ItemStack itemstack = this.items.get(i);
        if (itemstack.isEmpty()) {
            return ItemStack.a;
        } else {
            this.items.set(i, ItemStack.a);
            return itemstack;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.items.set(i, itemstack);
        if (!itemstack.isEmpty() && itemstack.getCount() > this.getMaxStackSize()) {
            itemstack.setCount(this.getMaxStackSize());
        }

        this.update();
    }

    @Override
    public int getSize() {
        return this.owner.getEnderChest().getSize();
    }

    @Override
    public boolean isNotEmpty() {

        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return true;
    }

    @Override
    public void startOpen(EntityHuman entityhuman) {
    }

    @Override
    public void closeContainer(EntityHuman entityhuman) {
    }

    @Override
    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    @Override
    public void clear() {
        this.items.clear();
    }

    @Override
    public void a(AutoRecipeStackManager autorecipestackmanager) {

        for (ItemStack itemstack : this.items) {
            autorecipestackmanager.b(itemstack);
        }

    }

}

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

package com.lishid.openinv.internal.v1_13_R2;

import com.lishid.openinv.internal.ISpecialEnderChest;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.v1_13_R2.AutoRecipeOutput;
import net.minecraft.server.v1_13_R2.AutoRecipeStackManager;
import net.minecraft.server.v1_13_R2.ContainerUtil;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.IInventory;
import net.minecraft.server.v1_13_R2.IInventoryListener;
import net.minecraft.server.v1_13_R2.InventoryEnderChest;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.NonNullList;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SpecialEnderChest implements IInventory, ISpecialEnderChest, AutoRecipeOutput {

    private EntityPlayer owner;
    private final IChatBaseComponent displayName;
    private final CraftInventory inventory;
    private NonNullList<ItemStack> items;
    private boolean playerOnline;

    public SpecialEnderChest(final Player player, final Boolean online) {
        this.owner = PlayerDataManager.getHandle(player);
        this.displayName = this.owner.getEnderChest().getDisplayName();
        this.inventory = new CraftInventory(this);
        this.items = this.owner.getEnderChest().items;
        this.playerOnline = online;
    }

    @Override
    public @NotNull Inventory getBukkitInventory() {
        return this.inventory;
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

    public List<ItemStack> getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity who) {
        this.owner.getEnderChest().onOpen(who);
    }

    public void onClose(CraftHumanEntity who) {
        this.owner.getEnderChest().onClose(who);
    }

    public List<HumanEntity> getViewers() {
        return this.owner.getEnderChest().getViewers();
    }

    public void setMaxStackSize(int i) {
        this.owner.getEnderChest().setMaxStackSize(i);
    }

    public InventoryHolder getOwner() {
        return this.owner.getEnderChest().getOwner();
    }

    public Location getLocation() {
        return null;
    }

    public void a(IInventoryListener iinventorylistener) {
        this.owner.getEnderChest().a(iinventorylistener);
    }

    public void b(IInventoryListener iinventorylistener) {
        this.owner.getEnderChest().b(iinventorylistener);
    }

    public ItemStack getItem(int i) {
        return i >= 0 && i < this.items.size() ? this.items.get(i) : ItemStack.a;
    }

    public ItemStack splitStack(int i, int j) {
        ItemStack itemstack = ContainerUtil.a(this.items, i, j);
        if (!itemstack.isEmpty()) {
            this.update();
        }

        return itemstack;
    }

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

    public ItemStack splitWithoutUpdate(int i) {
        ItemStack itemstack = this.items.get(i);
        if (itemstack.isEmpty()) {
            return ItemStack.a;
        } else {
            this.items.set(i, ItemStack.a);
            return itemstack;
        }
    }

    public void setItem(int i, ItemStack itemstack) {
        this.items.set(i, itemstack);
        if (!itemstack.isEmpty() && itemstack.getCount() > this.getMaxStackSize()) {
            itemstack.setCount(this.getMaxStackSize());
        }

        this.update();
    }

    public int getSize() {
        return this.owner.getEnderChest().getSize();
    }

    public boolean P_() {

        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    @Nullable
    public IChatBaseComponent getCustomName() {
        return this.displayName;
    }

    public boolean hasCustomName() {
        return false;
    }

    public void a(@Nullable IChatBaseComponent ichatbasecomponent) {
        // Ignored - name is always player's name.
    }

    public int getMaxStackSize() {
        return 64;
    }

    public boolean a(EntityHuman entityhuman) {
        return true;
    }

    public void startOpen(EntityHuman entityhuman) {
    }

    public void closeContainer(EntityHuman entityhuman) {
    }

    public boolean b(int i, ItemStack itemstack) {
        return true;
    }

    public int getProperty(int i) {
        return 0;
    }

    public void setProperty(int i, int j) {
    }

    public int h() {
        return 0;
    }

    public void clear() {
        this.items.clear();
    }

    public void a(AutoRecipeStackManager autorecipestackmanager) {

        for (ItemStack itemstack : this.items) {
            autorecipestackmanager.b(itemstack);
        }

    }

}

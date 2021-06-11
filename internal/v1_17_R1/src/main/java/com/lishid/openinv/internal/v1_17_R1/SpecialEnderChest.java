/*
 * Copyright (C) 2011-2021 lishid. All rights reserved.
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

package com.lishid.openinv.internal.v1_17_R1;

import com.lishid.openinv.internal.ISpecialEnderChest;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventoryListener;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.InventoryEnderChest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntityEnderChest;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpecialEnderChest extends InventoryEnderChest implements ISpecialEnderChest {

    private final CraftInventory inventory;
    private EntityPlayer owner;
    private NonNullList<ItemStack> c;
    private boolean playerOnline;

    public SpecialEnderChest(final Player player, final Boolean online) {
        super(PlayerDataManager.getHandle(player));
        this.inventory = new CraftInventory(this);
        this.owner = PlayerDataManager.getHandle(player);
        this.playerOnline = online;
        this.c = this.owner.getEnderChest().c;
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
                    enderChest.setItem(i, this.c.get(i));
                }
                this.c = enderChest.c;
                enderChest.transaction.addAll(this.transaction);
            } catch (Exception ignored) {}
            this.playerOnline = true;
        }
    }

    @Override
    public @NotNull Player getPlayer() {
        return owner.getBukkitEntity();
    }

    @Override
    public void update() {
        this.owner.getEnderChest().update();
    }

    @Override
    public List<ItemStack> getContents() {
        return this.c;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        super.onOpen(who);
        this.owner.getEnderChest().onOpen(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        super.onClose(who);
        this.owner.getEnderChest().onClose(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.owner.getEnderChest().getViewers();
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return true;
    }

    @Override
    public void a(TileEntityEnderChest tileentityenderchest) {
        this.owner.getEnderChest().a(tileentityenderchest);
    }

    @Override
    public boolean b(TileEntityEnderChest tileentityenderchest) {
        return this.owner.getEnderChest().b(tileentityenderchest);
    }

    @Override
    public int getMaxStackSize() {
        return this.owner.getEnderChest().getMaxStackSize();
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
    public @Nullable Location getLocation() {
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
        return i >= 0 && i < this.c.size() ? this.c.get(i) : ItemStack.b;
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        ItemStack itemstack = ContainerUtil.a(this.c, i, j);
        if (!itemstack.isEmpty()) {
            this.update();
        }

        return itemstack;
    }

    @Override
    public ItemStack a(ItemStack itemstack) {
        ItemStack itemstack1 = itemstack.cloneItemStack();
        this.d(itemstack1);
        if (itemstack1.isEmpty()) {
            return ItemStack.b;
        } else {
            this.c(itemstack1);
            return itemstack1.isEmpty() ? ItemStack.b : itemstack1;
        }
    }

    private void c(ItemStack itemstack) {
        for(int i = 0; i < this.getSize(); ++i) {
            ItemStack itemstack1 = this.getItem(i);
            if (itemstack1.isEmpty()) {
                this.setItem(i, itemstack.cloneItemStack());
                itemstack.setCount(0);
                return;
            }
        }
    }

    private void d(ItemStack itemstack) {
        for(int i = 0; i < this.getSize(); ++i) {
            ItemStack itemstack1 = this.getItem(i);
            if (ItemStack.e(itemstack1, itemstack)) {
                this.a(itemstack, itemstack1);
                if (itemstack.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void a(ItemStack itemstack, ItemStack itemstack1) {
        int i = Math.min(this.getMaxStackSize(), itemstack1.getMaxStackSize());
        int j = Math.min(itemstack.getCount(), i - itemstack1.getCount());
        if (j > 0) {
            itemstack1.add(j);
            itemstack.subtract(j);
            this.update();
        }
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        ItemStack itemstack = this.c.get(i);
        if (itemstack.isEmpty()) {
            return ItemStack.b;
        } else {
            this.c.set(i, ItemStack.b);
            return itemstack;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        this.c.set(i, itemstack);
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
    public boolean isEmpty() {
        return this.c.stream().allMatch(ItemStack::isEmpty);
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
        this.c.clear();
        this.update();
    }

    @Override
    public void a(AutoRecipeStackManager autorecipestackmanager) {
        for (ItemStack itemstack : this.c) {
            autorecipestackmanager.b(itemstack);
        }

    }

    @Override
    public List<ItemStack> f() {
        List<ItemStack> list = this.c.stream().filter(Predicate.not(ItemStack::isEmpty)).collect(Collectors.toList());
        this.clear();
        return list;
    }

    @Override
    public ItemStack a(Item item, int i) {
        ItemStack itemstack = new ItemStack(item, 0);

        for(int j = this.getSize() - 1; j >= 0; --j) {
            ItemStack itemstack1 = this.getItem(j);
            if (itemstack1.getItem().equals(item)) {
                int k = i - itemstack.getCount();
                ItemStack itemstack2 = itemstack1.cloneAndSubtract(k);
                itemstack.add(itemstack2.getCount());
                if (itemstack.getCount() == i) {
                    break;
                }
            }
        }

        if (!itemstack.isEmpty()) {
            this.update();
        }

        return itemstack;
    }

    @Override
    public boolean b(ItemStack itemStack) {
        for (ItemStack itemStack1 : this.c) {
            if (itemStack1.isEmpty() || ItemStack.e(itemStack1, itemStack) && itemStack1.getCount() < itemStack1.getMaxStackSize()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return this.c.stream().filter((itemStack) -> !itemStack.isEmpty()).collect(Collectors.toList()).toString();
    }

    @Override
    public void a(NBTTagList nbttaglist) {
        for(int i = 0; i < nbttaglist.size(); ++i) {
            ItemStack itemstack = ItemStack.a(nbttaglist.getCompound(i));
            if (!itemstack.isEmpty()) {
                this.a(itemstack);
            }
        }

    }

}

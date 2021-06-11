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

import com.google.common.collect.ImmutableList;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.ChatMessage;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.tags.Tag;
import net.minecraft.world.ContainerUtil;
import net.minecraft.world.IInventory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.player.AutoRecipeStackManager;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemArmor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SpecialPlayerInventory extends PlayerInventory implements ISpecialPlayerInventory {

    private final CraftInventory inventory;
    private boolean playerOnline;
    private EntityHuman l;
    private NonNullList<ItemStack> h;
    private NonNullList<ItemStack> i;
    private NonNullList<ItemStack> j;
    private List<NonNullList<ItemStack>> n;

    public SpecialPlayerInventory(final Player bukkitPlayer, final Boolean online) {
        super(PlayerDataManager.getHandle(bukkitPlayer));
        this.inventory = new CraftInventory(this);
        this.playerOnline = online;
        this.l = super.l;
        this.h = this.l.getInventory().h;
        this.i = this.l.getInventory().i;
        this.j = this.l.getInventory().j;
        this.n = ImmutableList.of(this.h, this.i, this.j);
    }

    @Override
    public void setPlayerOnline(@NotNull final Player player) {
        if (!this.playerOnline) {
            EntityPlayer entityPlayer = PlayerDataManager.getHandle(player);
            entityPlayer.getInventory().transaction.addAll(this.transaction);
            this.l = entityPlayer;
            for (int i = 0; i < getSize(); ++i) {
                this.l.getInventory().setItem(i, getRawItem(i));
            }
            this.l.getInventory().k = this.k;
            this.h = this.l.getInventory().h;
            this.i = this.l.getInventory().i;
            this.j = this.l.getInventory().j;
            this.n = ImmutableList.of(this.h, this.i, this.j);
            this.playerOnline = true;
        }
    }

    @Override
    public boolean a(final EntityHuman entityhuman) {
        return true;
    }

    @Override
    public @NotNull CraftInventory getBukkitInventory() {
        return this.inventory;
    }

    @Override
    public ItemStack getItem(int i) {
        List<ItemStack> list = this.h;

        if (i >= list.size()) {
            i -= list.size();
            list = this.i;
        } else {
            i = this.getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.j;
        } else if (list == this.i) {
            i = this.getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            return ItemStack.b;
        }

        return list.get(i);
    }

    private ItemStack getRawItem(int i) {
        NonNullList<ItemStack> list = null;
        for (NonNullList<ItemStack> next : this.n) {
            if (i < next.size()) {
                list = next;
                break;
            }
            i -= next.size();
        }

        return list == null ? ItemStack.b : list.get(i);
    }

    @Override
    public IChatBaseComponent getDisplayName() {
        return new ChatMessage(this.l.getName());
    }

    @Override
    public boolean hasCustomName() {
        return false;
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
        return 45;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    @Override
    public void setItem(int i, final ItemStack itemstack) {
        List<ItemStack> list = this.h;

        if (i >= list.size()) {
            i -= list.size();
            list = this.i;
        } else {
            i = this.getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.j;
        } else if (list == this.i) {
            i = this.getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            this.l.drop(itemstack, true);
            return;
        }

        list.set(i, itemstack);
    }

    @Override
    public void setPlayerOffline() {
        this.playerOnline = false;
    }

    @Override
    public @NotNull HumanEntity getPlayer() {
        return this.l.getBukkitEntity();
    }

    @Override
    public ItemStack splitStack(int i, final int j) {
        List<ItemStack> list = this.h;

        if (i >= list.size()) {
            i -= list.size();
            list = this.i;
        } else {
            i = this.getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.j;
        } else if (list == this.i) {
            i = this.getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            return ItemStack.b;
        }

        return list.get(i).isEmpty() ? ItemStack.b : ContainerUtil.a(list, i, j);
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        List<ItemStack> list = this.h;

        if (i >= list.size()) {
            i -= list.size();
            list = this.i;
        } else {
            i = this.getReversedItemSlotNum(i);
        }

        if (i >= list.size()) {
            i -= list.size();
            list = this.j;
        } else if (list == this.i) {
            i = this.getReversedArmorSlotNum(i);
        }

        if (i >= list.size()) {
            return ItemStack.b;
        }

        if (!list.get(i).isEmpty()) {
            ItemStack itemstack = list.get(i);

            list.set(i, ItemStack.b);
            return itemstack;
        }

        return ItemStack.b;
    }

    @Override
    public List<ItemStack> getContents() {
        return this.n.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public boolean isEmpty() {
        return this.n.stream().flatMap(Collection::stream).allMatch(ItemStack::isEmpty);
    }

    @Override
    public List<ItemStack> getArmorContents() {
        return this.i;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        this.transaction.add(who);
        this.l.getInventory().transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        this.transaction.remove(who);
        this.l.getInventory().transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return this.l.getBukkitEntity();
    }

    public Location getLocation() {
        return this.l.getBukkitEntity().getLocation();
    }

    /* Below this point largely just copied out of NMS to redirect to our overridden variables. */

    @Override
    public ItemStack getItemInHand() {
        return d(this.k) ? this.h.get(this.k) : ItemStack.b;
    }

    private boolean isSimilarAndNotFull(ItemStack itemstack, ItemStack itemstack1) {
        return !itemstack.isEmpty() && ItemStack.e(itemstack, itemstack1) && itemstack.isStackable() && itemstack.getCount() < itemstack.getMaxStackSize() && itemstack.getCount() < this.getMaxStackSize();
    }

    @Override
    public int canHold(ItemStack itemstack) {
        int remains = itemstack.getCount();

        for(int i = 0; i < this.h.size(); ++i) {
            ItemStack itemstack1 = this.getItem(i);
            if (itemstack1.isEmpty()) {
                return itemstack.getCount();
            }

            if (this.isSimilarAndNotFull(itemstack1, itemstack)) {
                remains -= (Math.min(itemstack1.getMaxStackSize(), this.getMaxStackSize())) - itemstack1.getCount();
            }

            if (remains <= 0) {
                return itemstack.getCount();
            }
        }

        ItemStack offhandItemStack = this.getItem(this.h.size() + this.i.size());
        if (this.isSimilarAndNotFull(offhandItemStack, itemstack)) {
            remains -= (Math.min(offhandItemStack.getMaxStackSize(), this.getMaxStackSize())) - offhandItemStack.getCount();
        }

        return remains <= 0 ? itemstack.getCount() : itemstack.getCount() - remains;
    }

    @Override
    public int getFirstEmptySlotIndex() {
        for(int i = 0; i < this.h.size(); ++i) {
            if (this.h.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void a(ItemStack itemstack) {
        int i = this.b(itemstack);
        if (d(i)) {
            this.k = i;
        } else if (i == -1) {
            this.k = this.i();
            if (!this.h.get(this.k).isEmpty()) {
                int j = this.getFirstEmptySlotIndex();
                if (j != -1) {
                    this.h.set(j, this.h.get(this.k));
                }
            }

            this.h.set(this.k, itemstack);
        } else {
            this.c(i);
        }

    }

    @Override
    public void c(int i) {
        this.k = this.i();
        ItemStack itemstack = this.h.get(this.k);
        this.h.set(this.k, this.h.get(i));
        this.h.set(i, itemstack);
    }

    @Override
    public int b(ItemStack itemstack) {
        for (int i = 0; i < this.h.size(); ++i) {
            if (!this.h.get(i).isEmpty() && ItemStack.e(itemstack, this.h.get(i))) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int c(ItemStack itemstack) {
        for (int i = 0; i < this.h.size(); ++i) {
            ItemStack itemstack1 = this.h.get(i);
            if (!this.h.get(i).isEmpty() && ItemStack.e(itemstack, this.h.get(i)) && !this.h.get(i).g() && !itemstack1.hasEnchantments() && !itemstack1.hasName()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int i() {
        int i;
        int j;
        for (j = 0; j < 9; ++j) {
            i = (this.k + j) % 9;
            if (this.h.get(i).isEmpty()) {
                return i;
            }
        }

        for (j = 0; j < 9; ++j) {
            i = (this.k + j) % 9;
            if (!this.h.get(i).hasEnchantments()) {
                return i;
            }
        }

        return this.k;
    }

    @Override
    public void a(double d0) {
        if (d0 > 0.0D) {
            d0 = 1.0D;
        }

        if (d0 < 0.0D) {
            d0 = -1.0D;
        }

        this.k = (int) (this.k - d0);

        while (this.k < 0) {
            this.k += 9;
        }

        while (this.k >= 9) {
            this.k -= 9;
        }

    }

    @Override
    public int a(Predicate<ItemStack> predicate, int i, IInventory iinventory) {
        byte b0 = 0;
        boolean flag = i == 0;
        int j = b0 + ContainerUtil.a(this, predicate, i - b0, flag);
        j += ContainerUtil.a(iinventory, predicate, i - j, flag);
        ItemStack itemstack = this.l.bV.getCarried();
        j += ContainerUtil.a(itemstack, predicate, i - j, flag);
        if (itemstack.isEmpty()) {
            this.l.bV.setCarried(ItemStack.b);
        }

        return j;
    }

    private int i(ItemStack itemstack) {
        int i = this.firstPartial(itemstack);
        if (i == -1) {
            i = this.getFirstEmptySlotIndex();
        }

        return i == -1 ? itemstack.getCount() : this.d(i, itemstack);
    }

    private int d(int i, ItemStack itemstack) {
        Item item = itemstack.getItem();
        int j = itemstack.getCount();
        ItemStack itemstack1 = this.getItem(i);
        if (itemstack1.isEmpty()) {
            itemstack1 = new ItemStack(item, 0);
            if (itemstack.hasTag()) {
                itemstack1.setTag(Objects.requireNonNull(itemstack.getTag()).clone());
            }

            this.setItem(i, itemstack1);
        }

        k = Math.min(j, itemstack1.getMaxStackSize() - itemstack1.getCount());

        if (k > this.getMaxStackSize() - itemstack1.getCount()) {
            k = this.getMaxStackSize() - itemstack1.getCount();
        }

        if (k != 0) {
            j -= k;
            itemstack1.add(k);
            itemstack1.d(5);
        }

        return j;
    }

    @Override
    public int firstPartial(ItemStack itemstack) {
        if (this.isSimilarAndNotFull(this.getItem(this.k), itemstack)) {
            return this.k;
        } else if (this.isSimilarAndNotFull(this.getItem(40), itemstack)) {
            return 40;
        } else {
            for(int i = 0; i < this.h.size(); ++i) {
                if (this.isSimilarAndNotFull(this.h.get(i), itemstack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    @Override
    public void j() {
        for (NonNullList<ItemStack> nonNullList : this.n) {
            for (int i = 0; i < nonNullList.size(); ++i) {
                if (!nonNullList.get(i).isEmpty()) {
                    nonNullList.get(i).a(this.l.t, this.l, i, this.k == i);
                }
            }
        }

    }

    @Override
    public boolean pickup(ItemStack itemStack) {
        return this.c(-1, itemStack);
    }

    @Override
    public boolean c(int i, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else {
            try {
                if (itemStack.g()) {
                    if (i == -1) {
                        i = this.getFirstEmptySlotIndex();
                    }

                    if (i >= 0) {
                        this.h.set(i, itemStack.cloneItemStack());
                        this.h.get(i).d(5);
                        itemStack.setCount(0);
                        return true;
                    } else if (this.l.getAbilities().d) {
                        itemStack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int j;
                    do {
                        j = itemStack.getCount();
                        if (i == -1) {
                            itemStack.setCount(this.i(itemStack));
                        } else {
                            itemStack.setCount(this.d(i, itemStack));
                        }
                    } while(!itemStack.isEmpty() && itemStack.getCount() < j);

                    if (itemStack.getCount() == j && this.l.getAbilities().d) {
                        itemStack.setCount(0);
                        return true;
                    } else {
                        return itemStack.getCount() < j;
                    }
                }
            } catch (Throwable var6) {
                CrashReport crashreport = CrashReport.a(var6, "Adding item to inventory");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Item being added");
                crashreportsystemdetails.a("Item ID", Item.getId(itemStack.getItem()));
                crashreportsystemdetails.a("Item data", itemStack.getDamage());
                crashreportsystemdetails.a("Item name", () -> itemStack.getName().getString());
                throw new ReportedException(crashreport);
            }
        }
    }

    @Override
    public void f(ItemStack itemStack) {
        this.a(itemStack, true);
    }

    @Override
    public void a(ItemStack itemStack, boolean flag) {
        while(true) {
            if (!itemStack.isEmpty()) {
                int i = this.firstPartial(itemStack);
                if (i == -1) {
                    i = this.getFirstEmptySlotIndex();
                }

                if (i != -1) {
                    int j = itemStack.getMaxStackSize() - this.getItem(i).getCount();
                    if (this.c(i, itemStack.cloneAndSubtract(j)) && flag && this.l instanceof EntityPlayer) {
                        ((EntityPlayer)this.l).b.sendPacket(new PacketPlayOutSetSlot(-2, i, this.getItem(i)));
                    }
                    continue;
                }

                this.l.drop(itemStack, false);
            }

            return;
        }
    }

    @Override
    public void g(ItemStack itemStack) {
        for (NonNullList<ItemStack> nonNullList : this.n) {
            for (int i = 0; i < nonNullList.size(); ++i) {
                if (nonNullList.get(i) == itemStack) {
                    nonNullList.set(i, ItemStack.b);
                    break;
                }
            }
        }
    }

    @Override
    public float a(IBlockData iBlockData) {
        return this.h.get(this.k).a(iBlockData);
    }

    @Override
    public NBTTagList a(NBTTagList nbtTagList) {
        NBTTagCompound nbttagcompound;
        int i;
        for(i = 0; i < this.h.size(); ++i) {
            if (!this.h.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                this.h.get(i).save(nbttagcompound);
                nbtTagList.add(nbttagcompound);
            }
        }

        for(i = 0; i < this.i.size(); ++i) {
            if (!this.i.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)(i + 100));
                this.i.get(i).save(nbttagcompound);
                nbtTagList.add(nbttagcompound);
            }
        }

        for(i = 0; i < this.j.size(); ++i) {
            if (!this.j.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)(i + 150));
                this.j.get(i).save(nbttagcompound);
                nbtTagList.add(nbttagcompound);
            }
        }

        return nbtTagList;
    }

    @Override
    public void b(NBTTagList nbtTagList) {
        this.h.clear();
        this.i.clear();
        this.j.clear();

        for(int i = 0; i < nbtTagList.size(); ++i) {
            NBTTagCompound nbttagcompound = nbtTagList.getCompound(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.a(nbttagcompound);
            if (!itemstack.isEmpty()) {
                if (j < this.h.size()) {
                    this.h.set(j, itemstack);
                } else if (j >= 100 && j < this.i.size() + 100) {
                    this.i.set(j - 100, itemstack);
                } else if (j >= 150 && j < this.j.size() + 150) {
                    this.j.set(j - 150, itemstack);
                }
            }
        }

    }

    @Override
    public ItemStack e(int i) {
        return this.i.get(i);
    }

    @Override
    public void a(DamageSource damageSource, float f, int[] intArray) {
        if (f > 0.0F) {
            f /= 4.0F;
            if (f < 1.0F) {
                f = 1.0F;
            }

            for (int index : intArray) {
                ItemStack itemstack = this.i.get(index);
                if ((!damageSource.isFire() || !itemstack.getItem().w()) && itemstack.getItem() instanceof ItemArmor) {
                    itemstack.damage((int) f, this.l, (entityHuman) -> entityHuman.broadcastItemBreak(EnumItemSlot.a(EnumItemSlot.Function.b, index)));
                }
            }
        }

    }

    @Override
    public void dropContents() {
        for (List<ItemStack> list : this.n) {
            for (int i = 0; i < list.size(); ++i) {
                ItemStack itemstack = list.get(i);
                if (!itemstack.isEmpty()) {
                    list.set(i, ItemStack.b);
                    this.l.a(itemstack, true, false);
                }
            }
        }
    }

    @Override
    public boolean h(ItemStack itemStack) {
        return this.n.stream()
                .flatMap(Collection::stream)
                .anyMatch(itemStack1 -> !itemStack1.isEmpty() && itemStack1.doMaterialsMatch(itemStack));
    }

    @Override
    public boolean a(Tag<Item> tag) {
        return this.n.stream()
                .flatMap(Collection::stream)
                .anyMatch(itemStack -> !itemStack.isEmpty() && itemStack.a(tag));
    }

    @Override
    public void a(PlayerInventory playerInventory) {
        for(int i = 0; i < this.getSize(); ++i) {
            this.setItem(i, playerInventory.getItem(i));
        }

        this.k = playerInventory.k;
    }

    @Override
    public void clear() {
        for (List<ItemStack> list : this.n) {
            list.clear();
        }
    }

    @Override
    public void a(AutoRecipeStackManager autoRecipeStackManager) {
        for (ItemStack itemstack : this.h) {
            autoRecipeStackManager.a(itemstack);
        }
    }

}

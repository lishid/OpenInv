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

import com.google.common.collect.ImmutableList;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.v1_13_R2.AutoRecipeStackManager;
import net.minecraft.server.v1_13_R2.ChatMessage;
import net.minecraft.server.v1_13_R2.ContainerUtil;
import net.minecraft.server.v1_13_R2.CrashReport;
import net.minecraft.server.v1_13_R2.CrashReportSystemDetails;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.Item;
import net.minecraft.server.v1_13_R2.ItemArmor;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.NonNullList;
import net.minecraft.server.v1_13_R2.PacketPlayOutSetSlot;
import net.minecraft.server.v1_13_R2.PlayerInventory;
import net.minecraft.server.v1_13_R2.ReportedException;
import net.minecraft.server.v1_13_R2.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SpecialPlayerInventory extends PlayerInventory implements ISpecialPlayerInventory {

    private final CraftInventory inventory = new CraftInventory(this);
    private boolean playerOnline;
    private NonNullList<ItemStack> items, armor, extraSlots;
    private List<NonNullList<ItemStack>> f;

    public SpecialPlayerInventory(final Player bukkitPlayer, final Boolean online) {
        super(PlayerDataManager.getHandle(bukkitPlayer));
        this.playerOnline = online;
        this.items = this.player.inventory.items;
        this.armor = this.player.inventory.armor;
        this.extraSlots = this.player.inventory.extraSlots;
        this.f = ImmutableList.of(this.items, this.armor, this.extraSlots);
    }

    @Override
    public void setPlayerOnline(@NotNull final Player player) {
        if (!this.playerOnline) {
            EntityPlayer entityPlayer = PlayerDataManager.getHandle(player);
            entityPlayer.inventory.transaction.addAll(this.transaction);
            this.player = entityPlayer;
            for (int i = 0; i < getSize(); ++i) {
                this.player.inventory.setItem(i, getRawItem(i));
            }
            this.player.inventory.itemInHandIndex = this.itemInHandIndex;
            this.items = this.player.inventory.items;
            this.armor = this.player.inventory.armor;
            this.extraSlots = this.player.inventory.extraSlots;
            this.f = ImmutableList.of(this.items, this.armor, this.extraSlots);
            this.playerOnline = true;
        }
    }

    @Override
    public boolean a(final EntityHuman entityhuman) {
        return true;
    }

    @Override
    public @NotNull Inventory getBukkitInventory() {
        return inventory;
    }

    @Override
    public ItemStack getItem(int i) {
        List<ItemStack> list = this.items;

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

    private ItemStack getRawItem(int i) {
        NonNullList<ItemStack> list = null;
        for (NonNullList<ItemStack> next : this.f) {
            if (i < next.size()) {
                list = next;
                break;
            }
            i -= next.size();
        }

        return list == null ? ItemStack.a : list.get(i);
    }

    @Override
    public IChatBaseComponent getDisplayName() {
        return new ChatMessage(this.player.getName());
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
        return super.getSize() + 4;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    @Override
    public void setItem(int i, final ItemStack itemstack) {
        List<ItemStack> list = this.items;

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

    @Override
    public void setPlayerOffline() {
        this.playerOnline = false;
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

    public List<ItemStack> getContents() {
        List<ItemStack> combined = new ArrayList<>(this.items.size() + this.armor.size() + this.extraSlots.size());

        for (List<ItemStack> sub : this.f) {
            combined.addAll(sub);
        }

        return combined;
    }

    public List<ItemStack> getArmorContents() {
        return this.armor;
    }

    public void onOpen(CraftHumanEntity who) {
        this.transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        this.transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    public InventoryHolder getOwner() {
        return this.player.getBukkitEntity();
    }

    public Location getLocation() {
        return this.player.getBukkitEntity().getLocation();
    }

    public ItemStack getItemInHand() {
        return e(this.itemInHandIndex) ? this.items.get(this.itemInHandIndex) : ItemStack.a;
    }

    public static int getHotbarSize() {
        return 9;
    }

    private boolean a(ItemStack itemstack, ItemStack itemstack1) {
        return !itemstack.isEmpty() && this.b(itemstack, itemstack1) && itemstack.isStackable() && itemstack.getCount() < itemstack.getMaxStackSize() && itemstack.getCount() < this.getMaxStackSize();
    }

    private boolean b(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.getItem() == itemstack1.getItem() && ItemStack.equals(itemstack, itemstack1);
    }

    public int canHold(ItemStack itemstack) {
        int remains = itemstack.getCount();

        for(int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack1 = this.getItem(i);
            if (itemstack1.isEmpty()) {
                return itemstack.getCount();
            }

            if (!this.a(itemstack, itemstack1)) {
                remains -= (itemstack1.getMaxStackSize() < this.getMaxStackSize() ? itemstack1.getMaxStackSize() : this.getMaxStackSize()) - itemstack1.getCount();
            }

            if (remains <= 0) {
                return itemstack.getCount();
            }
        }

        return itemstack.getCount() - remains;
    }

    public int getFirstEmptySlotIndex() {
        for(int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    public void d(int i) {
        this.itemInHandIndex = this.l();
        ItemStack itemstack = this.items.get(this.itemInHandIndex);
        this.items.set(this.itemInHandIndex, this.items.get(i));
        this.items.set(i, itemstack);
    }

    public static boolean e(int i) {
        return i >= 0 && i < 9;
    }

    public int c(ItemStack itemstack) {
        for(int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack1 = this.items.get(i);
            if (!this.items.get(i).isEmpty() && this.b(itemstack, this.items.get(i)) && !this.items.get(i).f() && !itemstack1.hasEnchantments() && !itemstack1.hasName()) {
                return i;
            }
        }

        return -1;
    }

    public int l() {
        int i;
        int j;
        for(j = 0; j < 9; ++j) {
            i = (this.itemInHandIndex + j) % 9;
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }

        for(j = 0; j < 9; ++j) {
            i = (this.itemInHandIndex + j) % 9;
            if (!this.items.get(i).hasEnchantments()) {
                return i;
            }
        }

        return this.itemInHandIndex;
    }

    public int a(Predicate<ItemStack> predicate, int i) {
        int j = 0;

        int k;
        for(k = 0; k < this.getSize(); ++k) {
            ItemStack itemstack = this.getItem(k);
            if (!itemstack.isEmpty() && predicate.test(itemstack)) {
                int l = i <= 0 ? itemstack.getCount() : Math.min(i - j, itemstack.getCount());
                j += l;
                if (i != 0) {
                    itemstack.subtract(l);
                    if (itemstack.isEmpty()) {
                        this.setItem(k, ItemStack.a);
                    }

                    if (i > 0 && j >= i) {
                        return j;
                    }
                }
            }
        }

        if (!this.getCarried().isEmpty() && predicate.test(this.getCarried())) {
            k = i <= 0 ? this.getCarried().getCount() : Math.min(i - j, this.getCarried().getCount());
            j += k;
            if (i != 0) {
                this.getCarried().subtract(k);
                if (this.getCarried().isEmpty()) {
                    this.setCarried(ItemStack.a);
                }

                if (i > 0 && j >= i) {
                    return j;
                }
            }
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
                itemstack1.setTag(itemstack.getTag().clone());
            }

            this.setItem(i, itemstack1);
        }

        int k = j;
        if (j > itemstack1.getMaxStackSize() - itemstack1.getCount()) {
            k = itemstack1.getMaxStackSize() - itemstack1.getCount();
        }

        if (k > this.getMaxStackSize() - itemstack1.getCount()) {
            k = this.getMaxStackSize() - itemstack1.getCount();
        }

        if (k == 0) {
            return j;
        } else {
            j -= k;
            itemstack1.add(k);
            itemstack1.d(5);
            return j;
        }
    }

    public int firstPartial(ItemStack itemstack) {
        if (this.a(this.getItem(this.itemInHandIndex), itemstack)) {
            return this.itemInHandIndex;
        } else if (this.a(this.getItem(40), itemstack)) {
            return 40;
        } else {
            for(int i = 0; i < this.items.size(); ++i) {
                if (this.a(this.items.get(i), itemstack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    public void p() {

        for (NonNullList<ItemStack> itemStacks : this.f) {
            for (int i = 0; i < itemStacks.size(); ++i) {
                if (!itemStacks.get(i).isEmpty()) {
                    itemStacks.get(i).a(this.player.world, this.player, i, this.itemInHandIndex == i);
                }
            }
        }

    }

    public boolean pickup(ItemStack itemstack) {
        return this.c(-1, itemstack);
    }

    public boolean c(int i, ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            return false;
        } else {
            try {
                if (itemstack.f()) {
                    if (i == -1) {
                        i = this.getFirstEmptySlotIndex();
                    }

                    if (i >= 0) {
                        this.items.set(i, itemstack.cloneItemStack());
                        this.items.get(i).d(5);
                        itemstack.setCount(0);
                        return true;
                    } else if (this.player.abilities.canInstantlyBuild) {
                        itemstack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int j;
                    do {
                        j = itemstack.getCount();
                        if (i == -1) {
                            itemstack.setCount(this.i(itemstack));
                        } else {
                            itemstack.setCount(this.d(i, itemstack));
                        }
                    } while(!itemstack.isEmpty() && itemstack.getCount() < j);

                    if (itemstack.getCount() == j && this.player.abilities.canInstantlyBuild) {
                        itemstack.setCount(0);
                        return true;
                    } else {
                        return itemstack.getCount() < j;
                    }
                }
            } catch (Throwable var6) {
                CrashReport crashreport = CrashReport.a(var6, "Adding item to inventory");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Item being added");
                crashreportsystemdetails.a("Item ID", Item.getId(itemstack.getItem()));
                crashreportsystemdetails.a("Item data", itemstack.getDamage());
                crashreportsystemdetails.a("Item name", () -> itemstack.getName().getString());
                throw new ReportedException(crashreport);
            }
        }
    }

    public void a(World world, ItemStack itemstack) {
        if (!world.isClientSide) {
            while(!itemstack.isEmpty()) {
                int i = this.firstPartial(itemstack);
                if (i == -1) {
                    i = this.getFirstEmptySlotIndex();
                }

                if (i == -1) {
                    this.player.drop(itemstack, false);
                    break;
                }

                int j = itemstack.getMaxStackSize() - this.getItem(i).getCount();
                if (this.c(i, itemstack.cloneAndSubtract(j))) {
                    ((EntityPlayer)this.player).playerConnection.sendPacket(new PacketPlayOutSetSlot(-2, i, this.getItem(i)));
                }
            }
        }

    }

    public void f(ItemStack itemstack) {

        for (NonNullList<ItemStack> nonnulllist : this.f) {
            for (int i = 0; i < nonnulllist.size(); ++i) {
                if (nonnulllist.get(i) == itemstack) {
                    nonnulllist.set(i, ItemStack.a);
                    break;
                }
            }
        }
    }

    public float a(IBlockData iblockdata) {
        return this.items.get(this.itemInHandIndex).a(iblockdata);
    }

    public NBTTagList a(NBTTagList nbttaglist) {
        NBTTagCompound nbttagcompound;
        int i;
        for(i = 0; i < this.items.size(); ++i) {
            if (!this.items.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.items.get(i).save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        for(i = 0; i < this.armor.size(); ++i) {
            if (!this.armor.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) (i + 100));
                this.armor.get(i).save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        for(i = 0; i < this.extraSlots.size(); ++i) {
            if (!this.extraSlots.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) (i + 150));
                this.extraSlots.get(i).save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        return nbttaglist;
    }

    public void b(NBTTagList nbttaglist) {
        this.items.clear();
        this.armor.clear();
        this.extraSlots.clear();

        for(int i = 0; i < nbttaglist.size(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompound(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.a(nbttagcompound);
            if (!itemstack.isEmpty()) {
                if (j < this.items.size()) {
                    this.items.set(j, itemstack);
                } else if (j >= 100 && j < this.armor.size() + 100) {
                    this.armor.set(j - 100, itemstack);
                } else if (j >= 150 && j < this.extraSlots.size() + 150) {
                    this.extraSlots.set(j - 150, itemstack);
                }
            }
        }

    }

    public boolean P_() {
        Iterator iterator = this.items.iterator();

        ItemStack itemstack;
        while(iterator.hasNext()) {
            itemstack = (ItemStack)iterator.next();
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        iterator = this.armor.iterator();

        while(iterator.hasNext()) {
            itemstack = (ItemStack)iterator.next();
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        iterator = this.extraSlots.iterator();

        while(iterator.hasNext()) {
            itemstack = (ItemStack)iterator.next();
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    public IChatBaseComponent getCustomName() {
        return null;
    }

    public boolean b(IBlockData iblockdata) {
        return this.getItem(this.itemInHandIndex).b(iblockdata);
    }

    public void a(float f) {
        if (f > 0.0F) {
            f /= 4.0F;
            if (f < 1.0F) {
                f = 1.0F;
            }

            for (ItemStack itemstack : this.armor) {
                if (itemstack.getItem() instanceof ItemArmor) {
                    itemstack.damage((int) f, this.player);
                }
            }
        }
    }

    public void dropContents() {
        for (NonNullList<ItemStack> itemStacks : this.f) {
            for (int i = 0; i < itemStacks.size(); ++i) {
                ItemStack itemstack = itemStacks.get(i);
                if (!itemstack.isEmpty()) {
                    itemStacks.set(i, ItemStack.a);
                    this.player.a(itemstack, true, false);
                }
            }
        }
    }

    public boolean h(ItemStack itemstack) {
        return this.f.stream().flatMap(NonNullList::stream).anyMatch(itemStack1 -> !itemStack1.isEmpty() && itemStack1.doMaterialsMatch(itemstack));
    }

    public void a(PlayerInventory playerinventory) {
        for (int i = 0; i < playerinventory.getSize(); ++i) {
            this.setItem(i, playerinventory.getItem(i));
        }

        this.itemInHandIndex = playerinventory.itemInHandIndex;
    }

    public void clear() {
        this.f.forEach(NonNullList::clear);
    }

    public void a(AutoRecipeStackManager autorecipestackmanager) {
        for (ItemStack itemstack : this.items) {
            autorecipestackmanager.a(itemstack);
        }
    }

}

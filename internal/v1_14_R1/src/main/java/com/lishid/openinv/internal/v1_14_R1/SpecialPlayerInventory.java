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

import com.google.common.collect.ImmutableList;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.util.Pair;
import com.lishid.openinv.util.SingleFieldList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.server.v1_14_R1.AutoRecipeStackManager;
import net.minecraft.server.v1_14_R1.ChatMessage;
import net.minecraft.server.v1_14_R1.ContainerUtil;
import net.minecraft.server.v1_14_R1.CrashReport;
import net.minecraft.server.v1_14_R1.CrashReportSystemDetails;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EnumItemSlot;
import net.minecraft.server.v1_14_R1.IBlockData;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.IInventory;
import net.minecraft.server.v1_14_R1.Item;
import net.minecraft.server.v1_14_R1.ItemArmor;
import net.minecraft.server.v1_14_R1.ItemStack;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import net.minecraft.server.v1_14_R1.NBTTagList;
import net.minecraft.server.v1_14_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_14_R1.PlayerInventory;
import net.minecraft.server.v1_14_R1.ReportedException;
import net.minecraft.server.v1_14_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryCrafting;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpecialPlayerInventory extends PlayerInventory implements ISpecialPlayerInventory {

    private final CraftInventory inventory;
    private boolean playerOnline;
    private EntityHuman player;
    private List<ItemStack> items, armor, extraSlots, crafting;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // Backing field is mutable.
    // TODO: cursor requires an additional slot listener
    private final List<ItemStack> cursor = new SingleFieldList<>(this::getCarried, this::setCarried);
    private List<List<ItemStack>> f;

    public SpecialPlayerInventory(final Player bukkitPlayer, final Boolean online) {
        super(PlayerDataManager.getHandle(bukkitPlayer));
        this.inventory = new CraftInventory(this);
        this.playerOnline = online;
        this.player = super.player;
        this.items = this.player.inventory.items;
        this.armor = this.player.inventory.armor;
        this.extraSlots = this.player.inventory.extraSlots;
        this.crafting = this.getCrafting().getContents();
        this.f = ImmutableList.of(this.items, this.armor, this.extraSlots, this.crafting, this.cursor);
    }

    private IInventory getCrafting() {
        return ((CraftInventoryCrafting) this.player.defaultContainer.getBukkitView().getTopInventory()).getInventory();
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
            // Crafting/cursor are not insertable while player is offline and do not need special treatment.
            this.player.inventory.itemInHandIndex = this.itemInHandIndex;
            this.items = this.player.inventory.items;
            this.armor = this.player.inventory.armor;
            this.extraSlots = this.player.inventory.extraSlots;
            this.crafting = this.getCrafting().getContents();
            this.f = ImmutableList.of(this.items, this.armor, this.extraSlots, this.crafting, this.cursor);
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
        List<ItemStack> list = null;
        for (List<ItemStack> next : this.f) {
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
        return 54;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    private Pair<List<ItemStack>, Integer> getLocalizedIndex(int i) {
        List<ItemStack> localList = null;
        for (List<ItemStack> list : this.f) {
            if (i < list.size()) {
                localList = list;
                break;
            }
            i -= list.size();
        }

        if (localList == this.armor) {
            i = this.getReversedArmorSlotNum(i);
        } else if (localList == this.items) {
            i = this.getReversedItemSlotNum(i);
        }

        return new Pair<>(localList, i);
    }

    @Override
    public void setItem(int i, final ItemStack itemstack) {
        Pair<List<ItemStack>, Integer> localizedIndex = getLocalizedIndex(i);
        if (localizedIndex.getLeft() == null
                // TODO: should this be a constant instead of comparing to transient slot containers?
                || !playerOnline && (localizedIndex.getLeft() == crafting || localizedIndex.getLeft() == cursor)) {
            this.player.drop(itemstack, true);
        } else {
            localizedIndex.getLeft().set(localizedIndex.getRight(), itemstack);
        }
    }

    @Override
    public void setPlayerOffline() {
        this.playerOnline = false;
    }

    @Override
    public ItemStack splitStack(int i, final int j) {
        Pair<List<ItemStack>, Integer> localizedIndex = getLocalizedIndex(i);

        if (localizedIndex.getLeft() == null) {
            return ItemStack.a;
        }

        return localizedIndex.getLeft().get(i).isEmpty() ? ItemStack.a : ContainerUtil.a(localizedIndex.getLeft(), localizedIndex.getRight(), j);
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        Pair<List<ItemStack>, Integer> localizedIndex = getLocalizedIndex(i);

        if (localizedIndex.getLeft() == null) {
            return ItemStack.a;
        }

        List<ItemStack> list = localizedIndex.getLeft();
        i = localizedIndex.getRight();

        if (!list.get(i).isEmpty()) {
            ItemStack itemstack = list.get(i);

            list.set(i, ItemStack.a);
            return itemstack;
        }

        return ItemStack.a;
    }

    @Override
    public List<ItemStack> getContents() {
        return this.f.stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public List<ItemStack> getArmorContents() {
        return this.armor;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        this.transaction.add(who);
        this.getCrafting().getViewers().add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        this.transaction.remove(who);
        this.getCrafting().getViewers().remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return this.player.getBukkitEntity();
    }

    @Override
    public Location getLocation() {
        return this.player.getBukkitEntity().getLocation();
    }

    @Override
    public ItemStack getItemInHand() {
        return d(this.itemInHandIndex) ? this.items.get(this.itemInHandIndex) : ItemStack.a;
    }

    private boolean a(ItemStack itemstack, ItemStack itemstack1) {
        return !itemstack.isEmpty() && this.b(itemstack, itemstack1) && itemstack.isStackable() && itemstack.getCount() < itemstack.getMaxStackSize() && itemstack.getCount() < this.getMaxStackSize();
    }

    private boolean b(ItemStack itemstack, ItemStack itemstack1) {
        return itemstack.getItem() == itemstack1.getItem() && ItemStack.equals(itemstack, itemstack1);
    }

    @Override
    public int canHold(ItemStack itemstack) {
        int remains = itemstack.getCount();

        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack1 = this.getItem(i);
            if (itemstack1.isEmpty()) {
                return itemstack.getCount();
            }

            if (!this.a(itemstack, itemstack1)) {
                remains -= Math.min(itemstack1.getMaxStackSize(), this.getMaxStackSize()) - itemstack1.getCount();
            }

            if (remains <= 0) {
                return itemstack.getCount();
            }
        }

        return itemstack.getCount() - remains;
    }

    @Override
    public int getFirstEmptySlotIndex() {
        for (int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void c(int i) {
        this.itemInHandIndex = this.i();
        ItemStack itemstack = this.items.get(this.itemInHandIndex);
        this.items.set(this.itemInHandIndex, this.items.get(i));
        this.items.set(i, itemstack);
    }

    @Override
    public int c(ItemStack itemstack) {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack1 = this.items.get(i);
            if (!this.items.get(i).isEmpty() && this.b(itemstack, this.items.get(i)) && !this.items.get(i).f() && !itemstack1.hasEnchantments() && !itemstack1.hasName()) {
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
            i = (this.itemInHandIndex + j) % 9;
            if (this.items.get(i).isEmpty()) {
                return i;
            }
        }

        for (j = 0; j < 9; ++j) {
            i = (this.itemInHandIndex + j) % 9;
            if (!this.items.get(i).hasEnchantments()) {
                return i;
            }
        }

        return this.itemInHandIndex;
    }

    @Override
    public int a(Predicate<ItemStack> predicate, int i) {
        int j = 0;

        int k;
        for (k = 0; k < this.getSize(); ++k) {
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

    @Override
    public int firstPartial(ItemStack itemstack) {
        if (this.a(this.getItem(this.itemInHandIndex), itemstack)) {
            return this.itemInHandIndex;
        } else if (this.a(this.getItem(40), itemstack)) {
            return 40;
        } else {
            for (int i = 0; i < this.items.size(); ++i) {
                if (this.a(this.items.get(i), itemstack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    @Override
    public void j() {

        for (List<ItemStack> itemStacks : this.f) {
            for (int i = 0; i < itemStacks.size(); ++i) {
                if (!itemStacks.get(i).isEmpty()) {
                    itemStacks.get(i).a(this.player.world, this.player, i, this.itemInHandIndex == i);
                }
            }
        }

    }

    @Override
    public boolean pickup(ItemStack itemstack) {
        return this.c(-1, itemstack);
    }

    @Override
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

    @Override
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

    @Override
    public void f(ItemStack itemstack) {

        for (List<ItemStack> list : this.f) {
            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i) == itemstack) {
                    list.set(i, ItemStack.a);
                    break;
                }
            }
        }
    }

    @Override
    public float a(IBlockData iblockdata) {
        return this.items.get(this.itemInHandIndex).a(iblockdata);
    }

    @Override
    public NBTTagList a(NBTTagList nbttaglist) {
        NBTTagCompound nbttagcompound;
        int i;
        for (i = 0; i < this.items.size(); ++i) {
            if (!this.items.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) i);
                this.items.get(i).save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        for (i = 0; i < this.armor.size(); ++i) {
            if (!this.armor.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) (i + 100));
                this.armor.get(i).save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        for (i = 0; i < this.extraSlots.size(); ++i) {
            if (!this.extraSlots.get(i).isEmpty()) {
                nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte) (i + 150));
                this.extraSlots.get(i).save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        return nbttaglist;
    }

    @Override
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

    @Override
    public boolean isNotEmpty() {
        Iterator iterator = this.items.iterator();

        ItemStack itemstack;
        while (iterator.hasNext()) {
            itemstack = (ItemStack)iterator.next();
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        iterator = this.armor.iterator();

        while (iterator.hasNext()) {
            itemstack = (ItemStack)iterator.next();
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        iterator = this.extraSlots.iterator();

        while (iterator.hasNext()) {
            itemstack = (ItemStack)iterator.next();
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    @Override
    public IChatBaseComponent getCustomName() {
        return null;
    }

    @Override
    public boolean b(IBlockData iblockdata) {
        return this.getItem(this.itemInHandIndex).b(iblockdata);
    }

    @Override
    public void a(float f) {
        if (f > 0.0F) {
            f /= 4.0F;
            if (f < 1.0F) {
                f = 1.0F;
            }

            for (int i = 0; i < this.armor.size(); ++i) {
                ItemStack itemstack = this.armor.get(0);
                int index = i;
                if (itemstack.getItem() instanceof ItemArmor) {
                    itemstack.damage((int) f, this.player, (entityhuman) -> entityhuman.c(EnumItemSlot.a(EnumItemSlot.Function.ARMOR, index)));
                }
            }
        }
    }

    @Override
    public void dropContents() {
        for (List<ItemStack> itemStacks : this.f) {
            for (int i = 0; i < itemStacks.size(); ++i) {
                ItemStack itemstack = itemStacks.get(i);
                if (!itemstack.isEmpty()) {
                    itemStacks.set(i, ItemStack.a);
                    this.player.a(itemstack, true, false);
                }
            }
        }
    }

    @Override
    public boolean h(ItemStack itemstack) {
        return this.f.stream().flatMap(List::stream).anyMatch(itemStack1 -> !itemStack1.isEmpty() && itemStack1.doMaterialsMatch(itemstack));
    }

    @Override
    public void a(PlayerInventory playerinventory) {
        for (int i = 0; i < playerinventory.getSize(); ++i) {
            this.setItem(i, playerinventory.getItem(i));
        }

        this.itemInHandIndex = playerinventory.itemInHandIndex;
    }

    @Override
    public void clear() {
        this.f.forEach(List::clear);
    }

    @Override
    public void a(AutoRecipeStackManager autorecipestackmanager) {
        for (ItemStack itemstack : this.items) {
            autorecipestackmanager.a(itemstack);
        }
    }

}

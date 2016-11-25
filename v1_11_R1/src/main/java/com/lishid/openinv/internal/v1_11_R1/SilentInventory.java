package com.lishid.openinv.internal.v1_11_R1;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.server.v1_11_R1.ChestLock;
import net.minecraft.server.v1_11_R1.Container;
import net.minecraft.server.v1_11_R1.ContainerChest;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.ITileInventory;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.PlayerInventory;

public class SilentInventory implements ITileInventory {

    public ITileInventory inv;

    public SilentInventory(ITileInventory inv) {
        this.inv = inv;
    }

    @Override
    public boolean isLocked()
    {
        return inv.isLocked();
    }

    @Override
    public void a(ChestLock chestLock) {
        inv.a(chestLock);
    }

    @Override
    public ChestLock getLock() {
        return inv.getLock();
    }

    @Override
    public int getSize() {
        return inv.getSize();
    }

    @Override
    public boolean w_() {
        return inv.w_();
    }

    @Override
    public ItemStack getItem(int i) {
        return inv.getItem(i);
    }

    @Override
    public ItemStack splitStack(int i, int i1) {
        return inv.splitStack(i, i1);
    }

    @Override
    public ItemStack splitWithoutUpdate(int i) {
        return inv.splitWithoutUpdate(i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        inv.setItem(i, itemStack);
    }

    @Override
    public int getMaxStackSize() {
        return inv.getMaxStackSize();
    }

    @Override
    public void update() {
        inv.update();
    }

    @Override
    public boolean a(EntityHuman entityHuman) {
        return inv.a(entityHuman);
    }

    @Override
    public void startOpen(EntityHuman entityHuman) {
        // Don't do anything
    }

    @Override
    public void closeContainer(EntityHuman entityHuman) {
        // Don't do anything
    }

    @Override
    public boolean b(int i, ItemStack itemStack) {
        return inv.b(i, itemStack);
    }

    @Override
    public int getProperty(int i) {
        return inv.getProperty(i);
    }

    @Override
    public void setProperty(int i, int i1) {
        inv.setProperty(i, i1);
    }

    @Override
    public int h() {
        return inv.h();
    }

    @Override
    public void clear() {
        inv.clear();
    }

    @Override
    public List<ItemStack> getContents() {
        return inv.getContents();
    }

    @Override
    public void onOpen(CraftHumanEntity craftHumanEntity) {
        inv.onOpen(craftHumanEntity);
    }

    @Override
    public void onClose(CraftHumanEntity craftHumanEntity) {
        inv.onClose(craftHumanEntity);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return inv.getViewers();
    }

    @Override
    public InventoryHolder getOwner() {
        return inv.getOwner();
    }

    @Override
    public void setMaxStackSize(int i) {
        inv.setMaxStackSize(i);
    }

    @Override
    public Location getLocation() {
        return inv.getLocation();
    }

    @Override
    public String getName() {
        return inv.getName();
    }

    @Override
    public boolean hasCustomName() {
        return inv.hasCustomName();
    }

    @Override
    public IChatBaseComponent getScoreboardDisplayName() {
        return inv.getScoreboardDisplayName();
    }

    @Override
    public Container createContainer(PlayerInventory playerInventory, EntityHuman entityHuman) {
        // Don't let the chest itself create the container.
        return new ContainerChest(playerInventory, this, entityHuman);
    }

    @Override
    public String getContainerName() {
        return inv.getContainerName();
    }
}

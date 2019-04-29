/*
 * Copyright (C) 2011-2018 lishid. All rights reserved.
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

package com.lishid.openinv.internal.v1_9_R1;

import com.lishid.openinv.internal.ISpecialEnderChest;
import java.lang.reflect.Field;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.IInventory;
import net.minecraft.server.v1_9_R1.InventoryEnderChest;
import net.minecraft.server.v1_9_R1.InventorySubcontainer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class SpecialEnderChest extends InventorySubcontainer implements IInventory, ISpecialEnderChest {

    private final InventoryEnderChest enderChest;
    private final CraftInventory inventory = new CraftInventory(this);
    private boolean playerOnline;

    public SpecialEnderChest(Player player, Boolean online) {
        super(PlayerDataManager.getHandle(player).getEnderChest().getName(),
                PlayerDataManager.getHandle(player).getEnderChest().hasCustomName(),
                PlayerDataManager.getHandle(player).getEnderChest().getSize());
        this.playerOnline = online;
        EntityPlayer nmsPlayer = PlayerDataManager.getHandle(player);
        this.enderChest = nmsPlayer.getEnderChest();
        this.bukkitOwner = nmsPlayer.getBukkitEntity();
        this.items = enderChest.getContents();
    }

    @NotNull
    @Override
    public InventoryView getBukkitView(final Player viewer) {
        return new InventoryView() {
            @Override
            public Inventory getTopInventory() {
                return inventory;
            }
            @Override
            public Inventory getBottomInventory() {
                return viewer.getInventory();
            }
            @Override
            public HumanEntity getPlayer() {
                return viewer;
            }
            @Override
            public InventoryType getType() {
                return InventoryType.ENDER_CHEST;
            }
        };
    }

    @Override
    public void setPlayerOnline(@NotNull Player player) {
        if (!playerOnline) {
            try {
                EntityPlayer nmsPlayer = PlayerDataManager.getHandle(player);
                this.bukkitOwner = nmsPlayer.getBukkitEntity();
                InventoryEnderChest playerEnderChest = nmsPlayer.getEnderChest();
                Field field = playerEnderChest.getClass().getField("items");
                field.setAccessible(true);
                field.set(playerEnderChest, this.items);
            } catch (Exception ignored) {}
            playerOnline = true;
        }
    }

    @Override
    public void setPlayerOffline() {
        playerOnline = false;
    }

    @Override
    public boolean isInUse() {
        return !this.getViewers().isEmpty();
    }

    @Override
    public void update() {
        super.update();
        enderChest.update();
    }

}

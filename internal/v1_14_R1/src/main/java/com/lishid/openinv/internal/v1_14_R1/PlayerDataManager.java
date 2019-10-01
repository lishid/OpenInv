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

import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialInventory;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.Container;
import net.minecraft.server.v1_14_R1.Containers;
import net.minecraft.server.v1_14_R1.DimensionManager;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.MinecraftServer;
import net.minecraft.server.v1_14_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_14_R1.PlayerInteractManager;
import net.minecraft.server.v1_14_R1.PlayerInventory;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftContainer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class PlayerDataManager implements IPlayerDataManager {

    public static EntityPlayer getHandle(final Player player) {
        if (player instanceof CraftPlayer) {
            return ((CraftPlayer) player).getHandle();
        }

        Server server = player.getServer();
        EntityPlayer nmsPlayer = null;

        if (server instanceof CraftServer) {
            nmsPlayer = ((CraftServer) server).getHandle().getPlayer(player.getName());
        }

        if (nmsPlayer == null) {
            // Could use reflection to examine fields, but it's honestly not worth the bother.
            throw new RuntimeException("Unable to fetch EntityPlayer from provided Player implementation");
        }

        return nmsPlayer;
    }

    @Override
    public Player loadPlayer(@NotNull final OfflinePlayer offline) {
        // Ensure player has data
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        // Create a profile and entity to load the player data
        GameProfile profile = new GameProfile(offline.getUniqueId(), offline.getName());
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(DimensionManager.OVERWORLD), profile,
                new PlayerInteractManager(server.getWorldServer(DimensionManager.OVERWORLD)));

        // Get the bukkit entity
        Player target = entity.getBukkitEntity();
        if (target != null) {
            // Load data
            target.loadData();
        }
        // Return the entity
        return target;
    }

    @Override
    public InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory) {

        EntityPlayer nmsPlayer = getHandle(player);

        if (nmsPlayer == null || nmsPlayer.playerConnection == null) {
            return null;
        }

        String title;
        if (inventory instanceof SpecialEnderChest) {
            HumanEntity owner = (HumanEntity) ((SpecialEnderChest) inventory).getBukkitOwner();
            //noinspection ConstantConditions // Owner name can be null when loaded under certain conditions.
            title = (owner.getName() != null ? owner.getName() : owner.getUniqueId().toString()) + "'s Ender Chest";
        } else if (inventory instanceof SpecialPlayerInventory) {
            EntityHuman owner = ((PlayerInventory) inventory).player;
            title = (owner.getName() != null ? owner.getName() : owner.getUniqueID().toString()) + "'s Inventory";
        } else {
            return player.openInventory(inventory.getBukkitInventory());
        }

        Container container = new CraftContainer(new InventoryView() {
            @Override
            public @NotNull Inventory getTopInventory() {
                return inventory.getBukkitInventory();
            }
            @Override
            public @NotNull Inventory getBottomInventory() {
                return player.getInventory();
            }
            @Override
            public @NotNull HumanEntity getPlayer() {
                return player;
            }
            @Override
            public @NotNull InventoryType getType() {
                return inventory.getBukkitInventory().getType();
            }
            @Override
            public @NotNull String getTitle() {
                return title;
            }
        }, nmsPlayer, nmsPlayer.nextContainerCounter()) {
            @Override
            public Containers<?> getType() {
                switch (inventory.getBukkitInventory().getSize()) {
                    case 9:
                        return Containers.GENERIC_9X1;
                    case 18:
                        return Containers.GENERIC_9X2;
                    case 27:
                    default:
                        return Containers.GENERIC_9X3;
                    case 36:
                        return Containers.GENERIC_9X4;
                    case 45:
                        return Containers.GENERIC_9X5;
                    case 41: // PLAYER
                    case 54:
                        return Containers.GENERIC_9X6;
                }
            }
        };

        container.setTitle(new ChatComponentText(title));
        container = CraftEventFactory.callInventoryOpenEvent(nmsPlayer, container);

        if (container == null) {
            return null;
        }

        nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(container.windowId, container.getType(),
                new ChatComponentText(container.getBukkitView().getTitle())));
        nmsPlayer.activeContainer = container;
        container.addSlotListener(nmsPlayer);

        return container.getBukkitView();

    }

}

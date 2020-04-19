/*
 * Copyright (C) 2011-2020 lishid. All rights reserved.
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

package com.lishid.openinv.internal.v1_15_R1;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialInventory;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.ChatMessageType;
import net.minecraft.server.v1_15_R1.Container;
import net.minecraft.server.v1_15_R1.Containers;
import net.minecraft.server.v1_15_R1.DimensionManager;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_15_R1.PlayerInteractManager;
import net.minecraft.server.v1_15_R1.PlayerInventory;
import net.minecraft.server.v1_15_R1.WorldNBTStorage;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftContainer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerDataManager implements IPlayerDataManager {

    private Field bukkitEntity;

    public PlayerDataManager() {
        try {
            bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
        } catch (NoSuchFieldException e) {
            System.out.println("Unable to obtain field to inject custom save process - players' mounts may be deleted when loaded.");
            e.printStackTrace();
            bukkitEntity = null;
        }
    }

    @NotNull
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

    @Nullable
    @Override
    public Player loadPlayer(@NotNull final OfflinePlayer offline) {
        // Ensure player has data
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        // Create a profile and entity to load the player data
        // See net.minecraft.server.PlayerList#attemptLogin
        GameProfile profile = new GameProfile(offline.getUniqueId(),
                offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(DimensionManager.OVERWORLD), profile,
                new PlayerInteractManager(server.getWorldServer(DimensionManager.OVERWORLD)));

        try {
            injectPlayer(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // Get the bukkit entity
        Player target = entity.getBukkitEntity();
        if (target != null) {
            // Load data
            target.loadData();
        }
        // Return the entity
        return target;
    }

    void injectPlayer(EntityPlayer player) throws IllegalAccessException {
        if (bukkitEntity == null) {
            return;
        }

        bukkitEntity.setAccessible(true);

        bukkitEntity.set(player, new CraftPlayer(player.server.server, player) {
            @Override
            public void saveData() {
                super.saveData();
                // See net.minecraft.server.WorldNBTStorage#save(EntityPlayer)
                try {
                    WorldNBTStorage worldNBTStorage = (WorldNBTStorage) player.server.getPlayerList().playerFileData;

                    NBTTagCompound playerData = player.save(new NBTTagCompound());

                    if (!isOnline()) {
                        // Special case: save old vehicle data
                        NBTTagCompound oldData = worldNBTStorage.load(player);

                        if (oldData != null && oldData.hasKeyOfType("RootVehicle", 10)) {
                            // See net.minecraft.server.PlayerList#a(NetworkManager, EntityPlayer) and net.minecraft.server.EntityPlayer#b(NBTTagCompound)
                            playerData.set("RootVehicle", oldData.getCompound("RootVehicle"));
                        }
                    }

                    File file = new File(worldNBTStorage.getPlayerDir(), player.getUniqueIDString() + ".dat.tmp");
                    File file1 = new File(worldNBTStorage.getPlayerDir(), player.getUniqueIDString() + ".dat");

                    NBTCompressedStreamTools.a(playerData, new FileOutputStream(file));

                    if (file1.exists()) {
                        file1.delete();
                    }

                    file.renameTo(file1);
                } catch (Exception e) {
                    LogManager.getLogger().warn("Failed to save player data for {}", player.getDisplayName().getString());
                }
            }
        });
    }

    @NotNull
    @Override
    public Player inject(@NotNull Player player) {
        try {
            EntityPlayer nmsPlayer = getHandle(player);
            injectPlayer(nmsPlayer);
            return nmsPlayer.getBukkitEntity();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return player;
        }
    }

    @Nullable
	@Override
    public InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory) {

        EntityPlayer nmsPlayer = getHandle(player);

        if (nmsPlayer.playerConnection == null) {
            return null;
        }

        String title;
        if (inventory instanceof SpecialEnderChest) {
            HumanEntity owner = (HumanEntity) ((SpecialEnderChest) inventory).getBukkitOwner();
            title = OpenInv.getPlugin(OpenInv.class).getLocalizedMessage(player, "container.enderchest", "%player%", owner.getName());
            if (title == null) {
                title = owner.getName() + "'s Ender Chest";
            }
        } else if (inventory instanceof SpecialPlayerInventory) {
            EntityHuman owner = ((PlayerInventory) inventory).player;
            title = OpenInv.getPlugin(OpenInv.class).getLocalizedMessage(player, "container.player", "%player%", owner.getName());
            if (title == null) {
                title = owner.getName() + "'s Inventory";
            }
        } else {
            return player.openInventory(inventory.getBukkitInventory());
        }

        String finalTitle = title;
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
                return finalTitle;
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
                    case 41: // PLAYER
                    case 45:
                        return Containers.GENERIC_9X5;
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

    @Override
    public void sendSystemMessage(@NotNull Player player, @NotNull String message) {
        int newline = message.indexOf('\n');
        if (newline != -1) {
            // No newlines in action bar chat.
            message = message.substring(0, newline);
        }

        if (message.isEmpty()) {
            return;
        }

        EntityPlayer nmsPlayer = getHandle(player);

        // For action bar chat, color codes are still supported but JSON text color is not allowed. Do not convert text.
        if (nmsPlayer.playerConnection != null) {
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText(message), ChatMessageType.GAME_INFO));
        }
    }

}

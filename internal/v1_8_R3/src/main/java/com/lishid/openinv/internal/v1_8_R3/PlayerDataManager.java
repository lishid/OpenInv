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

package com.lishid.openinv.internal.v1_8_R3;

import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialInventory;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldNBTStorage;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
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
    public static EntityPlayer getHandle(Player player) {
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
    public Player loadPlayer(@NotNull OfflinePlayer offline) {
        // Ensure the player has data
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        // Create a profile and entity to load the player data
        GameProfile profile = new GameProfile(offline.getUniqueId(),
                offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile,
                new PlayerInteractManager(server.getWorldServer(0)));

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
                // See net.minecraft.server.WorldNBTStorage#save(EntityHuman)
                try {
                    WorldNBTStorage worldNBTStorage = (WorldNBTStorage) player.server.getPlayerList().playerFileData;

                    NBTTagCompound playerData = new NBTTagCompound();
                    player.e(playerData);

                    if (!isOnline()) {
                        // Special case: save old vehicle data
                        NBTTagCompound oldData = worldNBTStorage.load(player);

                        if (oldData != null && oldData.hasKeyOfType("Riding", 10)) {
                            // See net.minecraft.server.PlayerList#a(NetworkManager, EntityPlayer) and net.minecraft.server.EntityPlayer#b(NBTTagCompound)
                            playerData.set("Riding", oldData.getCompound("Riding"));
                        }
                    }

                    File file = new File(worldNBTStorage.getPlayerDir(), player.getUniqueID().toString() + ".dat.tmp");
                    File file1 = new File(worldNBTStorage.getPlayerDir(), player.getUniqueID().toString() + ".dat");

                    NBTCompressedStreamTools.a(playerData, new FileOutputStream(file));

                    if (file1.exists()) {
                        file1.delete();
                    }

                    file.renameTo(file1);
                } catch (Exception e) {
                    LogManager.getLogger().warn("Failed to save player data for {}", player.getName());
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
        return player.openInventory(inventory.getBukkitInventory());
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
            nmsPlayer.playerConnection.sendPacket(new PacketPlayOutChat(new ChatComponentText(message), (byte) 2));
        }
    }

    @NotNull
    @Override
    public String getLocale(Player player) {
        return getHandle(player).locale;
    }

}

/*
 * Copyright (C) 2011-2014 lishid.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.internal.v1_7_R4;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import net.minecraft.util.com.mojang.authlib.GameProfile;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.lishid.openinv.utils.ReflectionUtil;
import com.lishid.openinv.utils.ReflectionUtil.PackageType;
import com.lishid.openinv.utils.ReflectionUtil.SubPackageType;
 
/**
 * @author lishid, JuicyDev (reflection)
 */
public class PlayerDataManager {
 
    private Plugin plugin;
    private String versionString;
    private int version;
    private static String playerFolderName;
 
    public PlayerDataManager(Plugin plugin) {
        this.plugin = plugin;
 
        this.versionString = this.plugin
                .getServer()
                .getClass()
                .getPackage()
                .getName()
                .substring(
                        this.plugin.getServer().getClass().getPackage()
                                .getName().lastIndexOf('.') + 1);
        try {
            this.version = Integer.valueOf(versionString.replace("v", "")
                    .replace("_", "").replace("R", ""));
        } catch (NumberFormatException e) { // Fallback
            this.version = 173;
        }
        playerFolderName = version >= 173 ? "playerdata" : "players";
    }
 
    /**
     * @param uuid
     *            UUID of the player
     * @return Instance of the player (null if doesn't exist)
     */
    public Player loadPlayer(UUID uuid) {
        try {
            File playerFolder = new File(
                    ((World) Bukkit.getWorlds().get(0)).getWorldFolder(),
                    playerFolderName);
            if (!playerFolder.exists()) {
                return null;
            }
 
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player == null)
                return null;
 
            GameProfile profile = new GameProfile(player.getUniqueId(),
                    player.getName());
 
            Class<?> obc_CraftServer = ReflectionUtil.getClass("CraftServer",
                    PackageType.CRAFTBUKKIT);
            Object craftServer = obc_CraftServer.cast(Bukkit.getServer());
            Method m_getServer = ReflectionUtil.getMethod(obc_CraftServer,
                    "getServer");
            Class<?> nms_MinecraftServer = ReflectionUtil.getClass(
                    "MinecraftServer", PackageType.MINECRAFT_SERVER);
            Object minecraftServer = m_getServer.invoke(craftServer);
 
            Class<?> nms_EntityPlayer = ReflectionUtil.getClass("EntityPlayer",
                    PackageType.MINECRAFT_SERVER);
            Class<?> nms_WorldServer = ReflectionUtil.getClass("WorldServer",
                    PackageType.MINECRAFT_SERVER);
            Class<?> nms_PlayerInteractManager = ReflectionUtil.getClass(
                    "PlayerInteractManager", PackageType.MINECRAFT_SERVER);
            Object worldServer = ReflectionUtil.getMethod(nms_MinecraftServer,
                    "getWorldServer", Integer.class).invoke(minecraftServer, 0);
 
            Constructor<?> c_EntityPlayer = ReflectionUtil.getConstructor(
                    nms_EntityPlayer, nms_MinecraftServer, nms_WorldServer,
                    GameProfile.class, nms_PlayerInteractManager);
            Constructor<?> c_PlayerInteractManager = ReflectionUtil
                    .getConstructor(nms_PlayerInteractManager, nms_WorldServer);
            Object playerInteractManager = c_PlayerInteractManager
                    .newInstance(worldServer);
 
            Object entityPlayer = c_EntityPlayer.newInstance(minecraftServer,
                    worldServer, profile, playerInteractManager);
 
            Class<?> obc_CraftPlayer = ReflectionUtil.getClass("CraftPlayer",
                    SubPackageType.ENTITY);
            Method m_getBukkitEntity = ReflectionUtil.getMethod(
                    nms_EntityPlayer, "getBukkitEntity");
 
            Player target = entityPlayer == null ? null
                    : (Player) obc_CraftPlayer.cast(m_getBukkitEntity
                            .invoke(entityPlayer));
 
            if (target != null) {
                target.loadData();
                return target;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return null;
    }
 
    /**
     * @param name
     *            Name of the player
     * @return Instance of the player (null if doesn't exist)
     */
    public Player loadPlayer(String name) {
        try {
            UUID uuid = matchUser(name);
            if (uuid == null)
                return null;
 
            Player target = loadPlayer(uuid);
            if (target != null) {
                return target;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        return null;
    }
 
    private static UUID matchUser(String search) {
        File playerFolder = new File(
                ((World) Bukkit.getWorlds().get(0)).getWorldFolder(),
                playerFolderName);
        if (!playerFolder.exists() || !playerFolder.isDirectory())
            return null;
        if (search == null)
            return null;
 
        UUID found = null;
 
        String lowerSearch = search.toLowerCase();
        int delta = 2147483647;
 
        for (File f : playerFolder.listFiles()) {
            if (!f.getName().endsWith(".dat"))
                continue;
            String uuidString = f.getName().substring(0,
                    f.getName().length() - 4);
            UUID uuid = UUID.fromString(uuidString);
 
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String name = player.getName();
 
            if (name.equalsIgnoreCase(search))
                return uuid;
 
            if (name.toLowerCase().startsWith(lowerSearch)) {
                int curDelta = name.length() - lowerSearch.length();
                if (curDelta < delta) {
                    found = player.getUniqueId();
                    delta = curDelta;
                }
                if (curDelta == 0) {
                    break;
                }
            }
        }
 
        return found;
    }
}

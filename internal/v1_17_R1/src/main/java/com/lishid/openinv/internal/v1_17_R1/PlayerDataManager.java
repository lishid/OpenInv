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

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.OpenInventoryView;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftContainer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerDataManager implements IPlayerDataManager {

    private @Nullable Field bukkitEntity;

    public PlayerDataManager() {
        try {
            bukkitEntity = Entity.class.getDeclaredField("bukkitEntity");
        } catch (NoSuchFieldException e) {
            Logger logger = OpenInv.getPlugin(OpenInv.class).getLogger();
            logger.warning("Unable to obtain field to inject custom save process - players' mounts may be deleted when loaded.");
            logger.log(Level.WARNING, e.getMessage(), e);
            bukkitEntity = null;
        }
    }

    public static @NotNull EntityPlayer getHandle(final Player player) {
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
    public @Nullable Player loadPlayer(@NotNull final OfflinePlayer offline) {
        // Ensure player has data
        if (!offline.hasPlayedBefore()) {
            return null;
        }

        // Create a profile and entity to load the player data
        // See net.minecraft.server.PlayerList#attemptLogin
        GameProfile profile = new GameProfile(offline.getUniqueId(),
                offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(World.f);

        if (worldServer == null) {
            return null;
        }

        EntityPlayer entity = new EntityPlayer(server, worldServer, profile);

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

        bukkitEntity.set(player, new OpenPlayer(player.c.server, player));
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

        if (nmsPlayer.b == null) {
            return null;
        }

        InventoryView view = getView(player, inventory);

        if (view == null) {
            return player.openInventory(inventory.getBukkitInventory());
        }

        Container container = new CraftContainer(view, nmsPlayer, nmsPlayer.nextContainerCounter()) {
            @Override
            public Containers<?> getType() {
                return getContainers(inventory.getBukkitInventory().getSize());
            }
        };

        container.setTitle(new ChatComponentText(view.getTitle()));
        container = CraftEventFactory.callInventoryOpenEvent(nmsPlayer, container);

        if (container == null) {
            return null;
        }

        nmsPlayer.b.sendPacket(new PacketPlayOutOpenWindow(nmsPlayer.bV.j, container.getType(),
                new ChatComponentText(container.getBukkitView().getTitle())));
        nmsPlayer.bV = container;
        nmsPlayer.initMenu(container);

        return container.getBukkitView();

    }

    private @Nullable InventoryView getView(Player player, ISpecialInventory inventory) {
        if (inventory instanceof SpecialEnderChest) {
            return new OpenInventoryView(player, inventory, "container.enderchest", "'s Ender Chest");
        } else if (inventory instanceof SpecialPlayerInventory) {
            return new OpenInventoryView(player, inventory, "container.player", "'s Inventory");
        } else {
            return null;
        }
    }

    static @NotNull Containers<?> getContainers(int inventorySize) {

        return switch (inventorySize) {
            case 9 -> Containers.a;
            case 18 -> Containers.b;
            case 36 -> Containers.d; // PLAYER
            case 41, 45 -> Containers.e;
            case 54 -> Containers.f;
            default -> Containers.c; // 9x3
        };
    }

    @Override
    public int convertToPlayerSlot(InventoryView view, int rawSlot) {
        int topSize = view.getTopInventory().getSize();
        if (topSize <= rawSlot) {
            // Slot is not inside special inventory, use Bukkit logic.
            return view.convertSlot(rawSlot);
        }

        // Main inventory, slots 0-26 -> 9-35
        if (rawSlot < 27) {
            return rawSlot + 9;
        }
        // Hotbar, slots 27-35 -> 0-8
        if (rawSlot < 36) {
            return rawSlot - 27;
        }
        // Armor, slots 36-39 -> 39-36
        if (rawSlot < 40) {
            return 36 + (39 - rawSlot);
        }
        // Off hand
        if (rawSlot == 40) {
            return 40;
        }
        // Drop slots, "out of inventory"
        return -1;
    }

}

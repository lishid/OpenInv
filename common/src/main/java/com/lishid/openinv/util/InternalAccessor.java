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

package com.lishid.openinv.util;

import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.IPlayerDataManager;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class InternalAccessor {

    private final Plugin plugin;
    private final String version;
    private boolean supported = false;
    private IPlayerDataManager playerDataManager;
    private IAnySilentContainer anySilentContainer;

    public InternalAccessor(final Plugin plugin) {
        this.plugin = plugin;

        String packageName = plugin.getServer().getClass().getPackage().getName();
        this.version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            // TODO: implement support for CraftMagicNumbers#getMappingsVersion
            Class.forName("com.lishid.openinv.internal." + this.version + ".SpecialPlayerInventory");
            Class.forName("com.lishid.openinv.internal." + this.version + ".SpecialEnderChest");
            this.playerDataManager = this.createObject(IPlayerDataManager.class, "PlayerDataManager");
            this.anySilentContainer = this.createObject(IAnySilentContainer.class, "AnySilentContainer");
            this.supported = InventoryAccess.isUseable();
        } catch (Exception ignored) {}
    }

    private <T> T createObject(final Class<? extends T> assignableClass, final String className,
            final Object... params) throws ClassCastException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        // Fetch internal class if it exists.
        Class<?> internalClass = Class.forName("com.lishid.openinv.internal." + this.version + "." + className);
        if (!assignableClass.isAssignableFrom(internalClass)) {
            String message = String.format("Found class %s but cannot cast to %s!", internalClass.getName(), assignableClass.getName());
            this.plugin.getLogger().warning(message);
            throw new IllegalStateException(message);
        }

        // Quick return: no parameters, no need to fiddle about finding the correct constructor.
        if (params.length == 0) {
            return assignableClass.cast(internalClass.getConstructor().newInstance());
        }

        // Search constructors for one matching the given parameters
        nextConstructor: for (Constructor<?> constructor : internalClass.getConstructors()) {
            Class<?>[] requiredClasses = constructor.getParameterTypes();
            if (requiredClasses.length != params.length) {
                continue;
            }
            for (int i = 0; i < params.length; ++i) {
                if (!requiredClasses[i].isAssignableFrom(params[i].getClass())) {
                    continue nextConstructor;
                }
            }
            return assignableClass.cast(constructor.newInstance(params));
        }

        StringBuilder builder = new StringBuilder("Found class ").append(internalClass.getName())
                .append(" but cannot find any matching constructors for [");
        for (Object object : params) {
            builder.append(object.getClass().getName()).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());

        String message = builder.append(']').toString();
        this.plugin.getLogger().warning(message);

        throw new IllegalArgumentException(message);
    }

    /**
     * Creates an instance of the IAnySilentContainer implementation for the current server version.
     *
     * @return the IAnySilentContainer
     * @throws IllegalStateException if server version is unsupported
     */
    public IAnySilentContainer getAnySilentContainer() {
        if (!this.supported) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", this.version));
        }
        return this.anySilentContainer;
    }

    /**
     * Creates an instance of the IPlayerDataManager implementation for the current server version.
     *
     * @return the IPlayerDataManager
     * @throws IllegalStateException if server version is unsupported
     */
    public IPlayerDataManager getPlayerDataManager() {
        if (!this.supported) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", this.version));
        }
        return this.playerDataManager;
    }

    /**
     * Gets the server implementation version. If not initialized, returns the string "null"
     * instead.
     *
     * @return the version, or "null"
     */
    public String getVersion() {
        return this.version != null ? this.version : "null";
    }

    /**
     * Checks if the server implementation is supported.
     *
     * @return true if initialized for a supported server version
     */
    public boolean isSupported() {
        return this.supported;
    }

    /**
     * Creates an instance of the ISpecialEnderChest implementation for the given Player, or
     * null if the current version is unsupported.
     *
     * @param player the Player
     * @param online true if the Player is online
     * @return the ISpecialEnderChest created
     * @throws InstantiationException if the ISpecialEnderChest could not be instantiated
     */
    public ISpecialEnderChest newSpecialEnderChest(final Player player, final boolean online) throws InstantiationException {
        if (!this.supported) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", this.version));
        }
        try {
            return this.createObject(ISpecialEnderChest.class, "SpecialEnderChest", player, online);
        } catch (Exception e) {
            throw new InstantiationException(String.format("Unable to create a new ISpecialEnderChest: %s", e.getMessage()));
        }
    }

    /**
     * Creates an instance of the ISpecialPlayerInventory implementation for the given Player..
     *
     * @param player the Player
     * @param online true if the Player is online
     * @return the ISpecialPlayerInventory created
     * @throws InstantiationException if the ISpecialPlayerInventory could not be instantiated
     */
    public ISpecialPlayerInventory newSpecialPlayerInventory(final Player player, final boolean online) throws InstantiationException {
        if (!this.supported) {
            throw new IllegalStateException(String.format("Unsupported server version %s!", this.version));
        }
        try {
            return this.createObject(ISpecialPlayerInventory.class, "SpecialPlayerInventory", player, online);
        } catch (Exception e) {
            throw new InstantiationException(String.format("Unable to create a new ISpecialPlayerInventory: %s", e.getMessage()));
        }
    }

}

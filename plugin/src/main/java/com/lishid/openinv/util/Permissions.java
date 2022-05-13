/*
 * Copyright (C) 2011-2022 lishid. All rights reserved.
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

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

public enum Permissions {

    OPENINV("openinv"),
    OVERRIDE("override"),
    EXEMPT("exempt"),
    CROSSWORLD("crossworld"),
    SILENT("silent"),
    SILENT_DEFAULT("silent.default", true),
    ANYCHEST("anychest"),
    ANY_DEFAULT("any.default", true),
    ENDERCHEST("openender"),
    ENDERCHEST_ALL("openenderall"),
    SEARCH("search"),
    EDITINV("editinv"),
    EDITENDER("editender"),
    OPENSELF("openself"),
    OPENONLINE("openonline"),
    OPENOFFLINE("openoffline"),
    SPECTATE("spectate");

    private final String permission;
    private final boolean uninheritable;

    Permissions(String permission) {
        this(permission, false);
    }

    Permissions(String permission, boolean uninheritable) {
        this.permission = "OpenInv." + permission;
        this.uninheritable = uninheritable;
    }

    public boolean hasPermission(@NotNull Permissible permissible) {

        boolean hasPermission = permissible.hasPermission(permission);
        if (uninheritable || hasPermission || permissible.isPermissionSet(permission)) {
            return hasPermission;
        }

        StringBuilder permissionDestroyer = new StringBuilder(permission);
        for (int lastPeriod = permissionDestroyer.lastIndexOf("."); lastPeriod > 0;
                lastPeriod = permissionDestroyer.lastIndexOf(".")) {
            permissionDestroyer.delete(lastPeriod + 1, permissionDestroyer.length()).append('*');

            hasPermission = permissible.hasPermission(permissionDestroyer.toString());
            if (hasPermission || permissible.isPermissionSet(permissionDestroyer.toString())) {
                return hasPermission;
            }

            permissionDestroyer.delete(lastPeriod, permissionDestroyer.length());

        }

        return permissible.hasPermission("*");

    }

}

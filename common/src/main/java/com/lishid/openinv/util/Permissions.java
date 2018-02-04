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

package com.lishid.openinv.util;

import org.bukkit.permissions.Permissible;

public enum Permissions {

    OPENINV("openinv"),
    OVERRIDE("override"),
    EXEMPT("exempt"),
    CROSSWORLD("crossworld"),
    SILENT("silent"),
    SILENT_DEFAULT("silent", "default"),
    ANYCHEST("anychest"),
    ANY_DEFAULT("any", "default"),
    ENDERCHEST("openender"),
    ENDERCHEST_ALL("openenderall"),
    SEARCH("search"),
    EDITINV("editinv"),
    EDITENDER("editender"),
    OPENSELF("openself");

    private final String[] permission;

    Permissions(String... permissions) {
        this.permission = new String[permissions.length + 1];
        this.permission[0] = "OpenInv";
        System.arraycopy(permissions, 0, this.permission, 1, permissions.length);
    }

    public boolean hasPermission(Permissible permissible) {
        StringBuilder permissionBuilder = new StringBuilder();

        // Support wildcard nodes.
        for (int i = 0; i < permission.length; i++) {
            if (permissible.hasPermission(permissionBuilder.toString() + "*")) {
                return true;
            }
            permissionBuilder.append(permission[i]).append('.');
        }

        // Delete trailing period.
        if (permissionBuilder.length() > 0) {
            permissionBuilder.deleteCharAt(permissionBuilder.length() - 1);
        }

        return permissible.hasPermission(permissionBuilder.toString());
    }

}

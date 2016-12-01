package com.lishid.openinv.util;

import org.bukkit.permissions.Permissible;

public enum Permissions {

    OPENINV("OpenInv.openinv"),
    OVERRIDE("OpenInv.override"),
    EXEMPT("OpenInv.exempt"),
    CROSSWORLD("OpenInv.crossworld"),
    SILENT("OpenInv.silent"),
    ANYCHEST("OpenInv.anychest"),
    ENDERCHEST("OpenInv.openender"),
    ENDERCHEST_ALL("OpenInv.openenderall"),
    SEARCH("OpenInv.search"),
    EDITINV("OpenInv.editinv"),
    EDITENDER("OpenInv.editender"),
    OPENSELF("OpenInv.openself");

    private final String permission;

    private Permissions(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission(Permissible permissible) {
        String[] parts = permission.split("\\.");
        String perm = "";
        for (int i = 0; i < parts.length; i++) {
            if (permissible.hasPermission(perm + "*")) {
                return true;
            }
            perm += parts[i] + ".";
        }
        return permissible.hasPermission(permission);
    }

}

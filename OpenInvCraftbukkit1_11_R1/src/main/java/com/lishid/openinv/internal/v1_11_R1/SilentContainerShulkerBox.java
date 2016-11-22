package com.lishid.openinv.internal.v1_11_R1;

import java.lang.reflect.Field;

import net.minecraft.server.v1_11_R1.ContainerShulkerBox;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.IInventory;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.PlayerInventory;
import net.minecraft.server.v1_11_R1.TileEntityShulkerBox;

public class SilentContainerShulkerBox extends ContainerShulkerBox {

    private static Field h;

    private static Field exposeOpenStatus() throws NoSuchFieldException, SecurityException {
        if (h == null) {
            h = TileEntityShulkerBox.class.getDeclaredField("h");
            h.setAccessible(true);
        }
        return h;
    }

    public static void setOpenValue(TileEntityShulkerBox tileShulkerBox, Object value) {
        try {
            exposeOpenStatus().set(tileShulkerBox, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Integer getOpenValue(TileEntityShulkerBox tileShulkerBox) {
        try {
            return (Integer) exposeOpenStatus().get(tileShulkerBox);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private final TileEntityShulkerBox tile;

    public SilentContainerShulkerBox(PlayerInventory playerInventory, IInventory iInventory,
            EntityHuman entityHuman) {
        super(playerInventory, iInventory, entityHuman);
        if (iInventory instanceof TileEntityShulkerBox) {
            tile = (TileEntityShulkerBox) iInventory;
        } else {
            tile = null;
        }
    }

    @Override
    public void b(EntityHuman entityHuman) {
        if (tile != null) {
            setOpenValue(tile, tile.getViewers().size());
        }

        PlayerInventory playerinventory = entityHuman.inventory;

        if (!playerinventory.getCarried().isEmpty()) {
            entityHuman.drop(playerinventory.getCarried(), false);
            playerinventory.setCarried(ItemStack.a);
        }
    }

}

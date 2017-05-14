package com.lishid.openinv.internal.v1_12_R1;

import java.lang.reflect.Field;

import net.minecraft.server.v1_12_R1.ContainerShulkerBox;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.IInventory;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.PlayerInventory;
import net.minecraft.server.v1_12_R1.TileEntityShulkerBox;

public class SilentContainerShulkerBox extends ContainerShulkerBox {

    private static Field fieldShulkerActionData;

    private static Field exposeOpenStatus() throws NoSuchFieldException, SecurityException {
        if (SilentContainerShulkerBox.fieldShulkerActionData == null) {
            SilentContainerShulkerBox.fieldShulkerActionData = TileEntityShulkerBox.class
                    .getDeclaredField("h");
            SilentContainerShulkerBox.fieldShulkerActionData.setAccessible(true);
        }
        return SilentContainerShulkerBox.fieldShulkerActionData;
    }

    public static Integer getOpenValue(final TileEntityShulkerBox tileShulkerBox) {
        try {
            return (Integer) SilentContainerShulkerBox.exposeOpenStatus().get(tileShulkerBox);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void setOpenValue(final TileEntityShulkerBox tileShulkerBox, final Object value) {
        try {
            SilentContainerShulkerBox.exposeOpenStatus().set(tileShulkerBox, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SilentContainerShulkerBox(final PlayerInventory playerInventory,
            final IInventory iInventory, final EntityHuman entityHuman) {
        super(playerInventory, iInventory, entityHuman);
    }

    @Override
    public void b(final EntityHuman entityHuman) {
        PlayerInventory playerinventory = entityHuman.inventory;

        if (!playerinventory.getCarried().isEmpty()) {
            entityHuman.drop(playerinventory.getCarried(), false);
            playerinventory.setCarried(ItemStack.a);
        }
    }

}

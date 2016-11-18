package com.lishid.openinv.internal.v1_11_R1;

import net.minecraft.server.v1_11_R1.ContainerShulkerBox;
import net.minecraft.server.v1_11_R1.EntityHuman;
import net.minecraft.server.v1_11_R1.IInventory;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.PlayerInventory;

public class SilentContainerShulkerBox extends ContainerShulkerBox {

    public SilentContainerShulkerBox(PlayerInventory playerInventory, IInventory iInventory,
            EntityHuman entityHuman) {
        super(playerInventory, iInventory, entityHuman);
        iInventory.closeContainer(entityHuman);
    }

    @Override
    public void b(EntityHuman entityHuman) {
        // Don't send close signal twice, might screw up
        PlayerInventory playerinventory = entityHuman.inventory;

        if (!playerinventory.getCarried().isEmpty()) {
            entityHuman.drop(playerinventory.getCarried(), false);
            playerinventory.setCarried(ItemStack.a);
        }
    }

}

package lishid.openinv.utils;

import net.minecraft.server.* ;

public class SilentContainerChest extends ContainerChest {
	public IInventory inv;
    public SilentContainerChest(IInventory i1, IInventory i2) {
    	super(i1, i2);
    	inv = i2;
    	inv.g();//close signal
    }
    
    @Override
    public void a(EntityHuman paramEntityHuman) {
        super.a(paramEntityHuman);
        inv.f();//open signal
    }
}
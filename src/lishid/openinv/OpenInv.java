
package lishid.openinv;

import lishid.openinv.commands.*;
import lishid.openinv.utils.PlayerInventoryChest;

import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

/**
 * Open other player's inventory
 *
 * @author lishid
 */
public class OpenInv extends JavaPlugin {
	private final OpenInvPlayerListener playerListener = new OpenInvPlayerListener(this);
	private final OpenInvEntityListener entityListener = new OpenInvEntityListener(this);
	//private final OpenInvInventoryListener inventoryListener = new OpenInvInventoryListener(this);
    public static PermissionHandler permissionHandler;
	public static OpenInv mainPlugin;
    public void onDisable() {
    }
    
    private void setupPermissions() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

        if (permissionHandler == null) {
            if (permissionsPlugin != null) {
                permissionHandler = ((Permissions) permissionsPlugin).getHandler();
            } else {
                //log.info("Permission system not detected, defaulting to OP");
            }
        }
    }

    public void onEnable() {
    	mainPlugin = this;
    	mainPlugin.getConfig().addDefault("ItemOpenInvItemID", 280);
    	mainPlugin.getConfig().options().copyDefaults(true);
    	mainPlugin.saveConfig();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Lowest, this);
		//pm.registerEvent(Event.Type.CUSTOM_EVENT, inventoryListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
    	setupPermissions();

        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " enabled!" );

        getCommand("openinv").setExecutor(new OpenInvPluginCommand(this));
        getCommand("searchinv").setExecutor(new SearchInvPluginCommand(this));
        getCommand("toggleopeninv").setExecutor(new OpenInvPluginCommand(this));
    }
    
    public static void ReplaceInv(CraftPlayer player)
    {
    	try{
	    	EntityPlayer entityplayer = player.getHandle();
	    	entityplayer.inventory = new PlayerInventoryChest(entityplayer.inventory, entityplayer);
	    	entityplayer.defaultContainer = new ContainerPlayer(entityplayer.inventory, !entityplayer.world.isStatic);
	    	//sync
	    	try
	    	{
	    		entityplayer.syncInventory();
	    	}catch(Exception e){}
	    	entityplayer.a(entityplayer.activeContainer, entityplayer.activeContainer.b());
	    	entityplayer.activeContainer.a();
	    	entityplayer.defaultContainer.a();
	        
	    	player.setHandle(entityplayer);
    	}
    	catch(Exception e)
    	{
            System.out.println("[OpenInv] Error while trying to override player inventory, error: " + e.getMessage());
    	}
    }
    
    public static boolean GetPlayerItemOpenInvStatus(String name)
    {
    	return mainPlugin.getConfig().getBoolean("ItemOpenInv." + name.toLowerCase() + ".toggle", false);
    }
    
    public static void SetPlayerItemOpenInvStatus(String name, boolean status)
    {
    	mainPlugin.getConfig().set("ItemOpenInv." + name.toLowerCase() + ".toggle", status);
    	mainPlugin.saveConfig();
    }
    
    public static int GetItemOpenInvItem()
    {
    	return mainPlugin.getConfig().getInt("ItemOpenInvItemID", 280);
    }
    
    public static Object GetFromConfig(String data, Object defaultValue)
    {
    	Object val = mainPlugin.getConfig().get(data);
        if (val == null)
        {
        	mainPlugin.getConfig().set(data, defaultValue);
            return defaultValue;
        }
        else
        {
        	return val;
        }
    }
    
    public static void SaveToConfig(String data, Object value)
    {
    	mainPlugin.getConfig().set(data, value);
    	mainPlugin.saveConfig();
    }
}

package lishid.openinv;

import lishid.openinv.commands.*;
import lishid.openinv.utils.PlayerInventoryChest;

import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ICrafting;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

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
	public static Configuration config;
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
        config = this.getConfiguration();

		PluginManager pm = getServer().getPluginManager();
		//pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Highest, this);
		//pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Lowest, this);
		//pm.registerEvent(Event.Type.INVENTORY_CLOSE, inventoryListener, Event.Priority.Normal, this);
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
	    	entityplayer.inventory = new PlayerInventoryChest(entityplayer.inventory);
	    	entityplayer.defaultContainer = new ContainerPlayer(entityplayer.inventory, !entityplayer.world.isStatic);
	    	entityplayer.activeContainer = entityplayer.defaultContainer;
	    	//sync
	    	((ICrafting)entityplayer).a(entityplayer.activeContainer, entityplayer.activeContainer.b());
	    	entityplayer.activeContainer.a();
	        
	    	player.setHandle(entityplayer);
    	}
    	catch(Exception e)
    	{
            System.out.println("[OpenInv] Error while trying to override player inventory, error: " + e.getMessage());
    	}
    }
    
    public static boolean GetPlayerItemOpenInvStatus(String name)
    {
    	return config.getBoolean("ItemOpenInv." + name.toLowerCase() + ".toggle", false);
    }
    
    public static void SetPlayerItemOpenInvStatus(String name, boolean status)
    {
    	config.setProperty("ItemOpenInv." + name.toLowerCase() + ".toggle", status);
    	config.save();
    }
    
    public static int GetItemOpenInvItem()
    {
    	return config.getInt("ItemOpenInvItemID", 280);
    }
    
    public static Object GetFromConfig(String data, Object defaultValue)
    {
    	Object val = config.getProperty(data);
        if (val == null)
        {
            config.setProperty(data, defaultValue);
            return defaultValue;
        }
        else
        {
        	return val;
        }
    }
    
    public static void SaveToConfig(String data, Object value)
    {
    	config.setProperty(data, value);
    	config.save();
    }
}
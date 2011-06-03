package lishid.openinv.utils;

import org.bukkit.entity.Player;

public class OpenInvHistory {

	public Player player = null;
	public String lastPlayer = "";
    
	public OpenInvHistory(Player player)
	{
		this.player = player;
	}
}

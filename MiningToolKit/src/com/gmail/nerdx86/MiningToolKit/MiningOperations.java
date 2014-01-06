package com.gmail.nerdx86.MiningToolKit;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;


public class MiningOperations extends ToolBaseObject{

	public MiningOperations(MiningToolKit aPlugin) {
		super(aPlugin);
		// TODO Auto-generated constructor stub
	}

	
	public boolean processCommandToThere(Player aPlayer, String[] args){
		Location start = aPlayer.getEyeLocation();
		Vector direction = start.getDirection();
		int stepsLeft=1000;
		//plugin.getLogger().info("start "+start);

		direction.normalize();
		Location currentLocation = start.clone();
		currentLocation.setPitch(0);
		currentLocation.setYaw(0);

		Location nextLocation = currentLocation.clone().add(direction);
		Location nextLocationHead = nextLocation.clone().add(0,1,0);
		while ((stepsLeft>0) && nextLocation.getBlock().isEmpty() && nextLocationHead.getBlock().isEmpty()) {
			//plugin.getLogger().info("next "+nextLocation);
			stepsLeft--;
			currentLocation=nextLocation.clone();
			nextLocation =nextLocation.add(direction);
			nextLocationHead = nextLocation.clone().add(0,1,0);
		}
		/*if (!nextLocation.getBlock().isEmpty())
			getLogger().info("feet hit "+nextLocation.getBlock().getType()+" at "+nextLocation);
		if (!nextLocationHead.getBlock().isEmpty())
			getLogger().info("head hit "+nextLocationHead.getBlock().getType()+" at "+nextLocationHead);*/

		//currentLocation.add(direction.multiply(-0.5));
		currentLocation.setDirection(direction);
		MiningUtils.centerLocation(currentLocation);
		//getLogger().info("current "+currentLocation);
		aPlayer.teleport(currentLocation, TeleportCause.PLUGIN);
		return true;
	}


}

package com.gmail.nerdx86.MiningToolKit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;


public class AutoMiner extends ToolBaseObject{
	public Map<String, AutoMinerConfiguration> autoMinerConfigurationMap = new HashMap<String, AutoMinerConfiguration>();

	public AutoMiner(MiningToolKit aPlugin) {
		super(aPlugin);
		// TODO Auto-generated constructor stub
	}
	public boolean processCommandAutoMiner(Player aPlayer, String[] args){
		AutoMinerConfiguration autoMinerConfiguration=getAutoMinerConfiguration(aPlayer);
		if (args.length > 0){
			autoMinerConfiguration.autoMinerRadius=Math.min(Integer.parseInt(args[0]),8);
		}
		else{
			if (autoMinerConfiguration.autoMinerRadius==0){
				autoMinerConfiguration.autoMinerRadius=2;
			}
			else{
				autoMinerConfiguration.autoMinerRadius=0;
			}
		}
		autoMinerConfigurationMap.put(aPlayer.getName(),autoMinerConfiguration);
		aPlayer.sendMessage("autominer distance set to "+autoMinerConfiguration.autoMinerRadius);
		return true;
	}

	public boolean processCommandMiningLaser(Player aPlayer, String[] args){
		AutoMinerConfiguration autoMinerConfiguration=getAutoMinerConfiguration(aPlayer);
		if (args.length > 0){
			autoMinerConfiguration.miningLaserDistance=Math.min(Integer.parseInt(args[0]),65);
		}
		else{
			if (autoMinerConfiguration.miningLaserDistance==0){
				autoMinerConfiguration.miningLaserDistance=8;
			}
			else{
				autoMinerConfiguration.miningLaserDistance=0;
			}
		}
		autoMinerConfigurationMap.put(aPlayer.getName(),autoMinerConfiguration);
		aPlayer.sendMessage("mining laser distance set to "+autoMinerConfiguration.miningLaserDistance);
		return true;
	}

	public boolean processCommandToTheSky(Player aPlayer, String[] args){
		AutoMinerConfiguration autoMinerConfiguration=getAutoMinerConfiguration(aPlayer);
		if (args.length > 0){
			autoMinerConfiguration.enableToTheSky=Boolean.parseBoolean(args[0]);
		}
		else{
			autoMinerConfiguration.enableToTheSky=!autoMinerConfiguration.enableToTheSky;
		}
		autoMinerConfigurationMap.put(aPlayer.getName(),autoMinerConfiguration);
		aPlayer.sendMessage("tothesky set to "+autoMinerConfiguration.enableToTheSky);
		return true;
	}

	public void doMiningLaser(PlayerMoveEvent event, Player aPlayer, AutoMinerConfiguration anAutoMinerConfiguration) {
		int distance=anAutoMinerConfiguration.miningLaserDistance;
		if (distance>0){
			//aPlayer.sendMessage("mining laser firing ");
			BlockIterator blockIterator=new BlockIterator(aPlayer, distance);
			
			while (blockIterator.hasNext()) {
				//plugin.getLogger().info("next "+nextLocation);
				Block currentBlock = blockIterator.next();
				if (!currentBlock.isEmpty()) {
					plugin.blockDissolver.addBlock(aPlayer, currentBlock, distance);
				}
			}
		}
	}

	public void doAutoMiner(PlayerMoveEvent event, Player aPlayer, AutoMinerConfiguration anAutoMinerConfiguration) {
		int radius=anAutoMinerConfiguration.autoMinerRadius;
		if (radius>0){
			double distance=0;
			double compareRadius=radius+0.5;
			//Get the player's location.
			Location loc = aPlayer.getLocation();
			Location locHead = loc.clone().add(0, 1, 0);
			int xp = loc.getBlockX(); 
			int yp = loc.getBlockY();
			int zp = loc.getBlockZ();

			int x1 = xp - radius; 
			int y1 = yp;
			int z1 = zp - radius;

			// Figure out the opposite corner of the cube by taking the corner and adding length to all coordinates.
			int x2 = xp + radius;
			int y2 = yp + radius+1;
			int z2 = zp + radius;

			World world = loc.getWorld();

			// Loop over the cube in the y dimension.
			for (int yPoint = y1; yPoint <= y2; yPoint++) {
				// Loop over the cube in the x dimension.
				for (int xPoint = x1; xPoint <= x2; xPoint++) { 
					// Loop over the cube in the z dimension.
					for (int zPoint = z1; zPoint <= z2; zPoint++) {
						// Get the block that we are currently looping over.
						Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
						if (yPoint==y1){
							distance=loc.distance(currentBlock.getLocation());
						}
						else {
							distance=locHead.distance(currentBlock.getLocation());
						}
						if ((!currentBlock.isEmpty()) && (distance<=compareRadius)) {
							plugin.blockDissolver.addBlock(aPlayer, currentBlock, distance);
						}
					}
				}
			}
		}
	}

	public void doToTheSky(PlayerMoveEvent aEvent, Player aPlayer, AutoMinerConfiguration anAutoMinerConfiguration) {
		if (anAutoMinerConfiguration.enableToTheSky){
			Location loc = aPlayer.getLocation();
			World world = loc.getWorld();
			int highestY = world.getHighestBlockYAt(loc);
			int xp = loc.getBlockX(); 
			int yp = loc.getBlockY() + 2;
			int zp = loc.getBlockZ();
			for (int yPoint = yp; yPoint <= highestY; yPoint++) {
				Block currentBlock = world.getBlockAt(xp, yPoint, zp);
				plugin.blockDissolver.addBlock(aPlayer, currentBlock, 1+yPoint-yp);
			}
		}
	}

	public void doPlayerMoveEvent(PlayerMoveEvent event){
		Player player = event.getPlayer();
		AutoMinerConfiguration autoMinerConfiguration=getAutoMinerConfiguration(player);
		doAutoMiner(event, player, autoMinerConfiguration);
		doMiningLaser(event, player, autoMinerConfiguration);
		doToTheSky(event, player, autoMinerConfiguration);
	}

	public class AutoMinerConfiguration {
		int autoMinerRadius=0;
		int miningLaserDistance=0;
		boolean enableToTheSky=false;
	}

	public AutoMinerConfiguration getAutoMinerConfiguration(Player aPlayer){
		AutoMinerConfiguration autoMinerConfiguration=autoMinerConfigurationMap.get(aPlayer.getName());
		if (autoMinerConfiguration==null){
			autoMinerConfiguration=new AutoMinerConfiguration();
			autoMinerConfigurationMap.put(aPlayer.getName(), autoMinerConfiguration);
		}
		return autoMinerConfiguration;
	}
}

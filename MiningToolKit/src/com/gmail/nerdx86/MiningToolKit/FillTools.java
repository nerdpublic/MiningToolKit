package com.gmail.nerdx86.MiningToolKit;

import java.awt.Container;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.gmail.nerdx86.MiningToolKit.NanoDissolver.NanoDissolveOperation;

public class FillTools extends ToolBaseObject{

	public FillTools(MiningToolKit aPlugin) {
		super(aPlugin);
		// TODO Auto-generated constructor stub
	}
	
	public boolean processCommandMakeALake(Player aPlayer, String[] args)
	{
		HashSet<Block> blocksFound=new HashSet<Block>();
		List<Location> locationsToProcess = new LinkedList<Location>();
		plugin.getLogger().info("processCommandMakeALake has been invoked!");
		Location playerFeet=aPlayer.getLocation();
		playerFeet.setY(playerFeet.getY() - 1);
		//plugin.getLogger().info("playerFeet:"+playerFeet);

		locationsToProcess.add(playerFeet);
		while (!locationsToProcess.isEmpty()){
			Location currentLocation=locationsToProcess.remove(0);
			BlockIterator blockIterator=new BlockIterator(currentLocation, 0, 140);
			boolean foundSide=false;
			while (blockIterator.hasNext()){
				Block currentBlock=blockIterator.next();
				if (currentBlock.isEmpty()){
					if (!blocksFound.contains(currentBlock)){
						Block eastBlock=currentBlock.getRelative(BlockFace.EAST);
						Block westBlock=currentBlock.getRelative(BlockFace.WEST);
						Block northBlock=currentBlock.getRelative(BlockFace.NORTH);
						Block southBlock=currentBlock.getRelative(BlockFace.SOUTH);
						
						locationsToProcess.add(currentBlock.getLocation().setDirection(eastBlock.getLocation().toVector().subtract(currentBlock.getLocation().toVector())));
						locationsToProcess.add(currentBlock.getLocation().setDirection(westBlock.getLocation().toVector().subtract(currentBlock.getLocation().toVector())));
						locationsToProcess.add(currentBlock.getLocation().setDirection(northBlock.getLocation().toVector().subtract(currentBlock.getLocation().toVector())));
						locationsToProcess.add(currentBlock.getLocation().setDirection(southBlock.getLocation().toVector().subtract(currentBlock.getLocation().toVector())));

						blocksFound.add(currentBlock);
					}
				}
				else{
					foundSide=true;
					break;
				}
			}
			if (!foundSide)
			{
				return false;
				//Bad JUJU
			}
 		}
		
		Iterator<Block> iterator = blocksFound.iterator();
		while (iterator.hasNext()) {
			Block block=iterator.next();
			block.setType(Material.WATER);
		}
		plugin.getLogger().info("processCommandMakeALake is Complete!");
		return true;
	}
	/*TNTPrimed tnt = player.getEyeLocation().getWorld().spawn(
    player.getEyeLocation(), TNTPrimed.class);
Vector direction = player.getEyeLocation().getDirection().multiply(2.5);
tnt.setVelocity(direction);
player.*/
/*boolean currentState=false; 
if (enableToTheSky.containsKey(player)){
currentState=enableToTheSky.get(player);
}
if (args.length > 0){
currentState=Boolean.parseBoolean(args[0]);
}
else{
currentState=!currentState;
}
enableToTheSky.put(player, currentState);
return true;*/

}

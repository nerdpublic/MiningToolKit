package com.gmail.nerdx86.MiningToolKit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.gmail.nerdx86.MiningToolKit.AutoMiner.AutoMinerConfiguration;


public class NanoDissolver extends ToolBaseObject{
	public List<NanoDissolveOperation> dissolveOperations = new LinkedList<NanoDissolveOperation>();
	public NanoDissolver(MiningToolKit aPlugin) {
		super(aPlugin);
		// TODO Auto-generated constructor stub
	}

	public boolean processCommandNanoDissolve(Player aPlayer, String[] args){
		Integer radius=10; 
		if (args.length > 0){
			radius=Math.min(Integer.parseInt(args[0]),1000);
		}
		dissolveOperations.add(new NanoDissolveOperation(aPlayer, aPlayer.getLocation(),radius));
		return true;
	}


	public class NanoDissolveOperation {
		Player player;
		Location center, head;
		double compareRadius;
		int  radius, xPoint, yPoint, zPoint, xStart, yStart, zStart, xStop, yStop, zStop;
		World world;
		public NanoDissolveOperation(Player thePlayer, Location theCenter, int theRadius){
			player=thePlayer;
			center=theCenter;
			radius=theRadius;
			compareRadius=radius+0.5;
			if (radius>0){
				//Get the player's location.
				head = center.clone().add(0, 1, 0);
				int xp = center.getBlockX(); 
				int yp = center.getBlockY();
				int zp = center.getBlockZ();

				xStart = xp - radius; 
				yStart = yp;
				zStart = zp - radius;

				// Figure out the opposite corner of the cube by taking the corner and adding length to all coordinates.
				xStop = xp + radius;
				yStop = yp + radius+1;
				zStop = zp + radius;

				xPoint=xStart;
				yPoint=yStart;
				zPoint=zStart;
				world = center.getWorld();
				//logToTextFile("x:,"+xStart+","+xStop+",y:,"+yStart+","+yStop+",z:,"+zStart+","+zStop+","+compareRadius);
			}
		}
		public boolean doNanoDissolve(Player aPlayer) {
			double distance=0;
			int blocksToDissolve=5000;
			if (radius>0){
				while ((yPoint<=yStop)){
					while ((xPoint<=xStop)){
						while ((zPoint<=zStop)){
							Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
							if (yPoint==yStart){
								distance=center.distance(currentBlock.getLocation());
							}
							else {
								distance=head.distance(currentBlock.getLocation());
							}
							if ((!currentBlock.isEmpty()) && (distance<=compareRadius)) {
								if (plugin.blockDissolver.addBlock(aPlayer, currentBlock, distance)){
									if (--blocksToDissolve<=0) 
									{
										zPoint++; //don't dissolve this point again
										return false;
									}
								}else
									return false;
							}
							zPoint++;
						}
						zPoint=zStart;
						xPoint++;
					}
					xPoint=xStart;
					yPoint++;
				}
			}
			//			logToTextFile("x:,,"+xPoint+",,y:,,"+yPoint+",,z:,,"+zPoint+",,");
			return (yPoint>yStop);
		}
	}


	public void doPlayerMoveEvent(PlayerMoveEvent event){
		Player player = event.getPlayer();
		if (!dissolveOperations.isEmpty()){
			Iterator<NanoDissolveOperation> it = dissolveOperations.iterator();
			while (it.hasNext()) {
				if (it.next().doNanoDissolve(player)) {
					it.remove();
				}
			}
		}
	}
}
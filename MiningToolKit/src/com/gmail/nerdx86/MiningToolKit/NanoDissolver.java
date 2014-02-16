package com.gmail.nerdx86.MiningToolKit;

import java.util.Date;
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
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.nerdx86.MiningToolKit.AutoMiner.AutoMinerConfiguration;


public class NanoDissolver extends BukkitRunnable{
	public MiningToolKit plugin;
	public List<NanoDissolveOperation> dissolveOperations = new LinkedList<NanoDissolveOperation>();
	public NanoDissolver(MiningToolKit aPlugin) {
		plugin=aPlugin;
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
		public boolean doNanoDissolve() {
			double distance=0;
			long startMS=(new Date().getTime());
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
								if (plugin.blockDissolver.addBlock(player, currentBlock, distance)){
									long nowMS=new Date().getTime();
									if ((nowMS<startMS) || (nowMS>startMS+1))
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


	@Override
    public void run() {
		if (!dissolveOperations.isEmpty()){
			long startMS=(new Date().getTime());
			Iterator<NanoDissolveOperation> it = dissolveOperations.iterator();
			while (it.hasNext()) {
				if (it.next().doNanoDissolve()) {
					it.remove();
				}
				long nowMS=new Date().getTime();
				if ((nowMS<startMS) || (nowMS>startMS+2))
					break;
			}
		}
    }
}
package com.gmail.nerdx86.MiningToolKit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;

public class AutoLighter extends ToolBaseObject{
	public Map<String, Integer> autoLighterIntensity = new HashMap<String, Integer>();
	public AutoLighter(MiningToolKit aPlugin) {
		super(aPlugin);
		// TODO Auto-generated constructor stub
	}

	public boolean processCommandAutoLighter( Player aPlayer, String[] args){
		Integer intensity=getAutoLighterIntensity(aPlayer); 
		String playerName=aPlayer.getName();
		if (args.length > 0){
			intensity=Math.min(Integer.parseInt(args[0]),15);
		}
		else{
			if (intensity==0){
				intensity=8;
			}
			else{
				intensity=0;
			}
		}
		autoLighterIntensity.put(playerName,intensity);
		aPlayer.sendMessage("autolighter intensity set to "+intensity);
		return true;
	}

	public void doPlayerMoveEvent(PlayerMoveEvent event){
		Player player = event.getPlayer();
		int intensity=getAutoLighterIntensity(player);
		if (intensity>0){
			//getLogger().info("cur_intensity>0");
			//Get the player's location.
			int lightlevel = intensity;
			Location loc = event.getPlayer().getLocation();
			Location locHead = loc.clone().add(0, 1, 0);
			World world = loc.getWorld();
			Block blockbellowplayer=loc.getBlock();

			if(blockbellowplayer != null){
				lightlevel = blockbellowplayer.getLightLevel();
			}
			if (lightlevel<intensity){
				//getLogger().info("lightlevel("+lightlevel+")<cur_intensity("+cur_intensity+")");
				int xp = locHead.getBlockX(); 
				int yp = locHead.getBlockY();
				int zp = locHead.getBlockZ();

				HashSet<Block> blocks=new HashSet<Block>();

				for (int dist=1; dist<6; dist++){
					for (int idx=1-dist; idx<dist; idx++){
						calculateAutoLightLocations(xp, yp, zp, dist, idx, world, blocks);
					}
				}
				Block[] blockAry = blocks.toArray(new Block[0]);
				if (blockAry.length>0){
					int rndBlock=(int)(Math.random()*blockAry.length);
					blockAry[rndBlock].setType(Material.TORCH);
				}
			}
		}
	}

	public int getAutoLighterIntensity(Player aPlayer){
		String playerName=aPlayer.getName();
		if (!autoLighterIntensity.containsKey(playerName)) {
			autoLighterIntensity.put(playerName,0);
		}
		return autoLighterIntensity.get(playerName);
	}
	
	public void calculateAutoLightLocations(int xp, int yp, int zp, int dist, int idx, World world, HashSet<Block> blocks){
		int x1, x2, z1, z2;
		for (int i=0; i<4; i++){
			switch (i) {
			case 0:
				x1=xp-dist;
				x2=x1+1;
				z1=z2=zp+idx;
				break;
			case 1:
				x1=xp+dist;
				x2=x1-1;
				z1=z2=zp+idx;
				break;
			case 2:
				z1=zp-dist;
				z2=z1+1;
				x1=x2=xp+idx;
				break;
			default:
				z1=zp+dist;
				z2=z1-1;
				x1=x2=xp+idx;
				break;
			}

			Block attachBlock=world.getBlockAt(x1,yp,z1);
			if (!attachBlock.isEmpty() ){
				Block torchBlock=world.getBlockAt(x2,yp,z2);
				if (torchBlock.isEmpty()){
					blocks.add(torchBlock);
				}
			}
			if (dist==1){
				attachBlock=world.getBlockAt(x1,yp-1,z1);
				if (!attachBlock.isEmpty()){
					Block torchBlock=world.getBlockAt(x1,yp,z1);
					if (torchBlock.isEmpty()){
						blocks.add(torchBlock);
					}
				}
				attachBlock=world.getBlockAt(x1,yp-2,z1);
				if (!attachBlock.isEmpty()){
					Block torchBlock=world.getBlockAt(x1,yp-1,z1);
					if (torchBlock.isEmpty()){
						blocks.add(torchBlock);
					}
				}
			}
		}
	}
}

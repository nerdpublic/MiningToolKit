package com.gmail.nerdx86.MiningToolKit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Math;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class MiningToolKit extends JavaPlugin implements Listener{
	public MiningInventory miningInventory = new MiningInventory(this);
	public Map<String, Boolean> enableToTheSky = new HashMap<String, Boolean>();
	public Map<String, Integer> Autominer_Distance = new HashMap<String, Integer>();
	public Map<String, Integer> Autolighter_Intensity = new HashMap<String, Integer>();
	public Queue<NanoDissolver> Dissolvers = new LinkedList<NanoDissolver>();
	public Queue<BlockToDissolve> DissolveBlocks = new PriorityQueue<BlockToDissolve>(10, new Comparator<BlockToDissolve>() {
		public int compare(BlockToDissolve block1, BlockToDissolve block2) {
			if (block1.distance < block2.distance)
			{
				return -1;
			}
			if (block1.distance > block2.distance)
			{
				return 1;
			}
			return 0;
		}});

	final HashSet<Material> ignoreblocks=new HashSet<Material>( Arrays.asList(
			Material.AIR,
			Material.ANVIL,
			Material.BED,
			Material.BEDROCK,
			Material.BOOKSHELF,
			Material.BURNING_FURNACE,
			Material.CHEST,
			Material.DISPENSER,
			Material.DROPPER,
			Material.ENCHANTMENT_TABLE,
			Material.ENDER_CHEST,
			Material.ENDER_PORTAL,
			Material.ENDER_PORTAL_FRAME,
			Material.FURNACE,
			Material.HOPPER,
			Material.MOB_SPAWNER,
			Material.TORCH,
			Material.TRAPPED_CHEST,
			Material.WORKBENCH,
			Material.WALL_SIGN
			));
	public void logToTextFile(String aString){
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("c:\\Temp\\JavaLog.txt", true)));
			out.println(aString);
			out.close();
		} catch (IOException e) {
			getLogger().info("exception writing "+aString+" to c:\\JavaLog.txt");
		}
	}
	public class BlockToDissolveComparator implements Comparator<BlockToDissolve>{
		@Override
		public int compare(BlockToDissolve arg0, BlockToDissolve arg1) {
			if (arg0.distance < arg1.distance)
			{
				return -1;
			}
			if (arg0.distance > arg1.distance)
			{
				return 1;
			}
			return 0;
		}
	}

	public class BlockToDissolve{
		public BlockToDissolve(Player aPlayer, Block aBlock, double aDistance) {
			player=aPlayer;
			block=aBlock;
			distance=Math.round(aDistance);
		}
		Block block;
		Player player;
		long distance;
		public void doDissolve(){
			if (block != null){
				if (!ignoreblocks.contains(block.getType())) {
					Collection<ItemStack> items=block.getDrops();
					//logToTextFile(player+" Dissolving "+block+" @ Location: "+block.getLocation()+" Should drop: "+items);
					if (miningInventory.addItems(player, "", items)){
						block.setType(Material.AIR);
					} 
				}
			}
		}
	}

	public class NanoDissolver {
		public NanoDissolver(Player thePlayer, Location theCenter, int theRadius){
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
		Player player;
		Location center, head;
		double compareRadius;
		int  radius, xPoint, yPoint, zPoint, xStart, yStart, zStart, xStop, yStop, zStop;
		World world;
		public boolean doNanoDissolve() {
			double distance=0;
			int blocksToDissolve=10000;
			if (radius>0){
				while ((yPoint<=yStop)&& (blocksToDissolve>0)){
					while ((xPoint<=xStop) && (blocksToDissolve>0)){
						while ((zPoint<=zStop) && (blocksToDissolve>0)){
							Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
							if (yPoint==yStart){
								distance=center.distance(currentBlock.getLocation());
							}
							else {
								distance=head.distance(currentBlock.getLocation());
							}
							if ((!ignoreblocks.contains(currentBlock.getType())) &&	(distance<=compareRadius)) {
								DissolveBlocks.offer(new BlockToDissolve(player, currentBlock, distance));
								blocksToDissolve--;
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

	public void onEnable(){
		getLogger().info("onEnable has been invoked!");
		PluginManager PluginManager = getServer().getPluginManager();
		PluginManager.registerEvents(this, this);
		/*try{
			String path = getDataFolder() + File.separator + "miningInventory.bin";
			File file = new File(path);

			if(file.exists()) {// check if file exists before loading to avoid errors!
				miningInventory  = SLAPI.load(path);
			}
		}catch(Exception e){
			//handle the exception
			e.printStackTrace();
		}*/
	}

	public void onDisable(){
		getLogger().info("onDisable has been invoked!");
		try{
			//String path = getDataFolder() + File.separator + "miningInventory.bin";
			  // if the directory does not exist, create it
			  if (!getDataFolder().exists()) {
			    getDataFolder().mkdir();  
		     }
			//SLAPI.save(miningInventory,path);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public Location centerLocation(Location aLocation){
		aLocation.setX(aLocation.getBlockX()+0.5);
		aLocation.setY(aLocation.getBlockY());
		aLocation.setZ(aLocation.getBlockZ()+0.5);
		return aLocation;
	}
	
	public static boolean isInteger(String str)  
	{  
	  try  
	  {  
	    int i = Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (sender instanceof Player) {
			Player player = (Player) sender;
			// do something
			if (cmd.getName().equalsIgnoreCase("autominer")){ // If the player typed /autominer then do the following...
				Integer cur_distance=0; 
				if (Autominer_Distance.containsKey(player.getName())){
					cur_distance=Autominer_Distance.get(player.getName());
				}
				if (args.length > 0){
					cur_distance=Math.min(Integer.parseInt(args[0]),8);
				}
				else{
					if (cur_distance==0){
						cur_distance=2;
					}
					else{
						cur_distance=0;
					}
				}
				Autominer_Distance.put(player.getName(), cur_distance);
				player.sendMessage("autominer distance set to "+cur_distance);
				return true;
			} else if (cmd.getName().equalsIgnoreCase("autolighter")){ // If the player typed /autolighter then do the following...
				Integer cur_intensity=0; 
				if (Autolighter_Intensity.containsKey(player.getName())){
					cur_intensity=Autolighter_Intensity.get(player.getName());
				}
				if (args.length > 0){
					cur_intensity=Math.min(Integer.parseInt(args[0]),15);
				}
				else{
					if (cur_intensity==0){
						cur_intensity=8;
					}
					else{
						cur_intensity=0;
					}
				}
				Autolighter_Intensity.put(player.getName(), cur_intensity);
				player.sendMessage("autolighter intensity set to "+cur_intensity);
				return true;
			}else if (cmd.getName().equalsIgnoreCase("nanodissolve")){ // If the player typed /nanodissolve then do the following...
				Integer radius=10; 
				if (args.length > 0){
					radius=Math.min(Integer.parseInt(args[0]),1000);
				}
				Dissolvers.add(new NanoDissolver(player, player.getLocation(),radius));
				return true;
			}else if (cmd.getName().equalsIgnoreCase("tothesky")){ // If the player typed /tothesky then do the following...
				boolean currentState=false; 
				if (enableToTheSky.containsKey(player.getName())){
					currentState=enableToTheSky.get(player.getName());
				}
				if (args.length > 0){
					currentState=Boolean.parseBoolean(args[0]);
				}
				else{
					currentState=!currentState;
				}
				enableToTheSky.put(player.getName(), currentState);
				player.sendMessage("tothesky set to "+currentState);
				return true;
			}else if (cmd.getName().equalsIgnoreCase("makealake")){ // If the player typed /makealake then do the following...
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
			}else if (cmd.getName().equalsIgnoreCase("tothere")){ // If the player typed /tothere then do the following...
				Location start = player.getEyeLocation();
				Vector direction = start.getDirection();
				int stepsLeft=1000;
				getLogger().info("start "+start);

				direction.normalize();
				Location currentLocation = start.clone();
				currentLocation.setPitch(0);
				currentLocation.setYaw(0);

				Location nextLocation = currentLocation.clone().add(direction);
				Location nextLocationHead = nextLocation.clone().add(0,1,0);
				while ((stepsLeft>0) && nextLocation.getBlock().isEmpty() && nextLocationHead.getBlock().isEmpty()) {
					getLogger().info("next "+nextLocation);
					stepsLeft--;
					currentLocation=nextLocation.clone();
					nextLocation =nextLocation.add(direction);
					nextLocationHead = nextLocation.clone().add(0,1,0);
				}
				if (!nextLocation.getBlock().isEmpty())
					getLogger().info("feet hit "+nextLocation.getBlock().getType()+" at "+nextLocation);
				if (!nextLocationHead.getBlock().isEmpty())
					getLogger().info("head hit "+nextLocationHead.getBlock().getType()+" at "+nextLocationHead);

				//currentLocation.add(direction.multiply(-0.5));
				currentLocation.setDirection(direction);
				centerLocation(currentLocation);
				getLogger().info("current "+currentLocation);
				player.teleport(currentLocation, TeleportCause.COMMAND);
			}else if (cmd.getName().equalsIgnoreCase("mininginventory") || cmd.getName().equalsIgnoreCase("mi")){
				return miningInventory.processCommandMiningInventory(player, args);
			}

			return false;
		} else {
			sender.sendMessage("You must be a player!");
			return false;
		}
	}

	public void doToTheSky(PlayerMoveEvent aEvent) {
		Player player=aEvent.getPlayer();
		if (enableToTheSky.containsKey(player.getName())){
			if (enableToTheSky.get(player.getName())){
				Location loc = player.getLocation();
				World world = loc.getWorld();
				int highestY = world.getHighestBlockYAt(loc);
				int xp = loc.getBlockX(); 
				int yp = loc.getBlockY() + 2;
				int zp = loc.getBlockZ();
				for (int yPoint = yp; yPoint <= highestY; yPoint++) {
					Block currentBlock = world.getBlockAt(xp, yPoint, zp);
					if (!ignoreblocks.contains(currentBlock.getType())){ 
						DissolveBlocks.offer(new BlockToDissolve(player, currentBlock, 1+yPoint-yp));
					}
				}
			}
		}
	}


	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		//getLogger().info("onPlayerMove has been invoked!");
		AutoMiner(event);
		AutoLighter(event);
		DoDissolve(event);
		doToTheSky(event);

	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		//getLogger().info("onBlockBreakEvent has been invoked!");
	}

	public void DoDissolve(PlayerMoveEvent event) {
		int i=100;
		if (!Dissolvers.isEmpty()){
			if (Dissolvers.peek().doNanoDissolve())
				Dissolvers.poll();
		}
		while ((i>0) && (!DissolveBlocks.isEmpty())){
			//getLogger().info("!DissolveBlocks.isEmpty");
			DissolveBlocks.poll().doDissolve();
			i--;
		}
	}

	public void AutoMiner(PlayerMoveEvent event) {
		if (Autominer_Distance.containsKey(event.getPlayer().getName())){
			int radius=Autominer_Distance.get(event.getPlayer().getName());
			if (radius>0){
				double distance=0;
				double compareRadius=radius+0.5;
				Player player = event.getPlayer();
				//Get the player's location.
				Location loc = player.getLocation();
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

				for (int xPoint = x1; xPoint <= x2; xPoint++) { 
					// Loop over the cube in the y dimension.
					for (int yPoint = y1; yPoint <= y2; yPoint++) {
						// Loop over the cube in the z dimension.
						for (int zPoint = z1; zPoint <= z2; zPoint++) {
							// Get the block that we are currently looping over.
							Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
							// Set the block to type 57 (Diamond block!)
							//currentBlock.setTypeId(57);
							if (yPoint==y1){
								distance=loc.distance(currentBlock.getLocation());
							}
							else {
								distance=locHead.distance(currentBlock.getLocation());
							}
							if ((!ignoreblocks.contains(currentBlock.getType())) &&	(distance<=compareRadius)) {
								DissolveBlocks.offer(new BlockToDissolve(player, currentBlock, distance));

							}
						}
					}
				}
			}
		}

	}

	public void AutoLight(int xp, int yp, int zp, int dist, int idx, World world, HashSet<Block> blocks){
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
			if (!attachBlock.isEmpty()){
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

	public void AutoLighter(PlayerMoveEvent event) {
		if (Autolighter_Intensity.containsKey(event.getPlayer().getName())){
			int cur_intensity=Autolighter_Intensity.get(event.getPlayer().getName());
			if (cur_intensity>0){
				//getLogger().info("cur_intensity>0");
				//Get the player's location.
				int lightlevel = cur_intensity;
				Location loc = event.getPlayer().getLocation();
				Location locHead = loc.clone().add(0, 1, 0);
				World world = loc.getWorld();
				Block blockbellowplayer=loc.getBlock();

				if(blockbellowplayer != null){
					lightlevel = blockbellowplayer.getLightLevel();
				}
				if (lightlevel<cur_intensity){
					//getLogger().info("lightlevel("+lightlevel+")<cur_intensity("+cur_intensity+")");
					int xp = locHead.getBlockX(); 
					int yp = locHead.getBlockY();
					int zp = locHead.getBlockZ();

					HashSet<Block> blocks=new HashSet<Block>();

					for (int dist=1; dist<6; dist++){
						for (int idx=1-dist; idx<dist; idx++){
							AutoLight(xp, yp, zp, dist, idx, world, blocks);
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
	}
}

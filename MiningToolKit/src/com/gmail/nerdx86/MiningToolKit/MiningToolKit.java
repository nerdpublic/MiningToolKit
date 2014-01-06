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
	public MiningUtils miningUtils = new MiningUtils(this);
	public MiningOperations miningOperations = new MiningOperations(this);
	public MiningInventory miningInventory = new MiningInventory(this);
	public BlockDissolver blockDissolver = new BlockDissolver(this);
	public NanoDissolver nanoDissolver = new NanoDissolver(this);
	public AutoMiner autoMiner = new AutoMiner(this);
	public AutoLighter autoLighter = new AutoLighter(this);


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

	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (sender instanceof Player) {
			Player player = (Player) sender;
			// do something
			if (cmd.getName().equalsIgnoreCase("autominer") || cmd.getName().equalsIgnoreCase("am")){ // If the player typed /autominer then do the following...
				return autoMiner.processCommandAutoMiner(player, args);
			}else if (cmd.getName().equalsIgnoreCase("mininglaser") || cmd.getName().equalsIgnoreCase("ml")){ // If the player typed /tothesky then do the following...
				//return autoMiner.processCommandMiningLaser(player, args);
				return true;
			}else if (cmd.getName().equalsIgnoreCase("autolighter") || cmd.getName().equalsIgnoreCase("al")){ // If the player typed /autolighter then do the following...
				return autoLighter.processCommandAutoLighter(player, args);
			}else if (cmd.getName().equalsIgnoreCase("nanodissolve") || cmd.getName().equalsIgnoreCase("nd")){ // If the player typed /nanodissolve then do the following...
				return nanoDissolver.processCommandNanoDissolve(player, args);
			}else if (cmd.getName().equalsIgnoreCase("tothesky") || cmd.getName().equalsIgnoreCase("tts")){ // If the player typed /tothesky then do the following...
				return autoMiner.processCommandToTheSky(player, args);
			}else if (cmd.getName().equalsIgnoreCase("makealake")){ // If the player typed /makealake then do the following...
				//FillTools
				return true;
			}else if (cmd.getName().equalsIgnoreCase("tothere") || cmd.getName().equalsIgnoreCase("tt")){ // If the player typed /tothere then do the following...
				return miningOperations.processCommandToThere(player, args);
			}else if (cmd.getName().equalsIgnoreCase("mininginventory") || cmd.getName().equalsIgnoreCase("mi")){
				return miningInventory.processCommandMiningInventory(player, args);
			}
			return false;
		} else {
			sender.sendMessage("You must be a player!");
			return false;
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		//getLogger().info("onPlayerMove has been invoked!");
		nanoDissolver.doPlayerMoveEvent(event);
		autoMiner.doPlayerMoveEvent(event);
		autoLighter.doPlayerMoveEvent(event);
		blockDissolver.doPlayerMoveEvent(event);
	}

	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		//getLogger().info("onBlockBreakEvent has been invoked!");
	}

}

package com.gmail.nerdx86.MiningToolKit;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;

public class MiningUtils extends ToolBaseObject {
	public MiningUtils(MiningToolKit aPlugin) {
		super(aPlugin);
		// TODO Auto-generated constructor stub
	}

	final HashSet<Material> defaultIgnoreBlocks=new HashSet<Material>( Arrays.asList(
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
			plugin.getLogger().info("exception writing "+aString+" to c:\\JavaLog.txt");
		}
	}
	public static Location centerLocation(Location aLocation){
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

}

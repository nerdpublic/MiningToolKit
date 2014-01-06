package com.gmail.nerdx86.MiningToolKit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MiningInventory extends ToolBaseObject{
	public Map<String, List<Inventory>> inventoryMap = new HashMap<String, List<Inventory>>();
	public MiningInventory(MiningToolKit aPlugin) {
		super(aPlugin);
	}

 
	public boolean processCommandMiningInventory( Player aPlayer, String[] args){
		String inventoryName="";
		String inventoryKey="";
		int firstArg=0;
		if ((args.length>0) && args[0].equalsIgnoreCase("classify")){
			return plugin.blockDissolver.processCommandClassify(aPlayer, args);
		}
		if ((args.length>firstArg) && args[firstArg].equalsIgnoreCase("shared")){
			inventoryName=" "+args[firstArg];
			inventoryKey="_share_";
			firstArg++;
		}/*else check for player name to access other players?  Add Security?*/
		List<Inventory> inventoryList=getInventoryList(aPlayer, inventoryKey); 
		if ((args.length>firstArg) && args[firstArg].equalsIgnoreCase("dropall")){
			return doDropAll(aPlayer,inventoryList);
		}else if ((args.length>firstArg) && args[firstArg].equalsIgnoreCase("share")){
			//return doShare(plugin, aPlayer,inventoryList);
			/*int sharedMiningInventoryCount=0;
			List<Inventory> sharedMiningInventory=inventoryMap(args[0]).get(); 
			if (sharedMiningInventory!=null){
				miningInventoryCount=sharedMiningInventory.size();
			}
			if (sharedMiningInventoryCount==0){
				player.sendMessage("No shared miningInventory(s) availible!");
				Integer selectedIndex=0; 
				if (args.length > 0){
					selectedIndex=Integer.parseInt(args[0])-1;
				}
				if ((selectedIndex>=0) && (selectedIndex<sharedMiningInventoryCount)){
					player.openInventory(sharedMiningInventory.get(selectedIndex));
				}else{
					player.sendMessage((selectedIndex+1)+" is not a valid shared miningInventory. Please specify 1 to "+sharedMiningInventoryCount+" to open!");
				}
			}*/
		}else if ((args.length>firstArg) && args[firstArg].equalsIgnoreCase("sort")){
			return doSort(aPlayer,inventoryList);
		}else if ((args.length>firstArg) && args[firstArg].equalsIgnoreCase("buildchests")){

		}
		
		//Access specific Inventory
		int inventoryCount=inventoryList.size();
		if (inventoryCount==0){
			aPlayer.sendMessage("No"+inventoryName+" miningInventory(s) availible!");
		}else if ((args.length==firstArg) && (inventoryCount>1)){
			aPlayer.sendMessage(inventoryCount+inventoryName+" miningInventory(s) availible. Please specify which to open!");
		} else {
			Integer selectedIndex=0; 
			if (args.length > firstArg){
				try{
					selectedIndex=Integer.parseInt(args[firstArg])-1;
				}
				catch(NumberFormatException nfe){
					aPlayer.sendMessage(args[firstArg]+" is not a valid integer. Please specify 1 to "+inventoryCount+" to open!");
				}
			}
			if ((selectedIndex>=0) && (selectedIndex<inventoryCount)){
				aPlayer.openInventory(inventoryList.get(selectedIndex));
			}else{
				aPlayer.sendMessage((selectedIndex+1)+" is not a valid miningInventory. Please specify 1 to "+inventoryCount+" to open!");
			}
		}
		return true;
	}
	public boolean addItem(Player aPlayer,String aInventoryKey,ItemStack aItemStack){
		List<Inventory> inventoryList=getInventoryList( aPlayer, aInventoryKey);
		return addItem( aPlayer, inventoryList, aItemStack);
	}
	public boolean addItem(Player aPlayer,List<Inventory> inventoryList,ItemStack aItemStack){
		Collection<ItemStack> itemsToAdd=new ArrayList<ItemStack>();
		itemsToAdd.add(aItemStack);
		return addItems( aPlayer, inventoryList, itemsToAdd);
	}
	public boolean addItems(Player aPlayer,String aInventoryKey,Inventory aInventory){
		List<Inventory> inventoryList=getInventoryList( aPlayer, aInventoryKey);
		return addItems( aPlayer, inventoryList, aInventory);
	}
	public boolean addItems(Player aPlayer,List<Inventory> inventoryList,Inventory aInventory){
		Collection<ItemStack> itemsToAdd=new ArrayList<ItemStack>(Arrays.asList(aInventory.getContents()));
		return addItems( aPlayer, inventoryList, itemsToAdd);
	}
	public boolean addItems(Player aPlayer,String aInventoryKey,Collection<ItemStack> aItemStackCollection){
		List<Inventory> inventoryList=getInventoryList( aPlayer, aInventoryKey);
		return addItems( aPlayer, inventoryList, aItemStackCollection);
	}
	public boolean addItems(Player aPlayer,List<Inventory> inventoryList,Collection<ItemStack> aItemStackCollection){
		HashMap<Integer,ItemStack> remainingItems=new HashMap<Integer,ItemStack>();
		Collection<ItemStack> itemsToAdd=new ArrayList<ItemStack>(aItemStackCollection);
		if (!aItemStackCollection.isEmpty()){
			for (Inventory inventory : inventoryList) {
				remainingItems=inventory.addItem(itemsToAdd.toArray(new ItemStack[itemsToAdd.size()]));
				itemsToAdd=remainingItems.values();
				if (itemsToAdd.isEmpty())
					break;
			}
			while (!itemsToAdd.isEmpty()){
				Inventory inventory=plugin.getServer().createInventory(aPlayer, 54 /*9*9*9*9*/);
				inventoryList.add(inventory);
				remainingItems=inventory.addItem(itemsToAdd.toArray(new ItemStack[itemsToAdd.size()]));
				itemsToAdd=remainingItems.values();
			}
		}
		return true;
	}
	public boolean addItems(Player aPlayer,String aInventoryKey,HashMap<Integer,ItemStack> aItemStackMap){
		List<Inventory> inventoryList=getInventoryList(aPlayer, aInventoryKey);
		return addItems(aPlayer, inventoryList, aItemStackMap);
	}

	public boolean addItems(Player aPlayer,List<Inventory> inventoryList,HashMap<Integer,ItemStack> aItemStackMap){
		Collection<ItemStack> itemsToAdd=aItemStackMap.values();
		return addItems( aPlayer, inventoryList, itemsToAdd);
	}

	protected boolean doDropAll(Player aPlayer, List<Inventory> aInventoryList){
		Location dropLocation=aPlayer.getLocation();
		World world=dropLocation.getWorld();
		for (Inventory inventory:aInventoryList){
			for (ItemStack item:inventory){
				if (item!=null)
					world.dropItemNaturally(dropLocation, item);
			}
		}
		aInventoryList.clear();
		return true;
	}

	protected boolean doSort(Player aPlayer, List<Inventory> aInventoryList){
		List<ItemStack> priorityItems=new ArrayList<ItemStack>();
		List<ItemStack> normalItems=new ArrayList<ItemStack>();
		List<ItemStack> surplusItems=new ArrayList<ItemStack>();
		
		HashSet<Material> disposeBlocks=plugin.blockDissolver.getDisposeBlocks(aPlayer);
		HashSet<Material> surplusBlocks=plugin.blockDissolver.getSurplusBlocks(aPlayer);
		HashSet<Material> priorityBlocks=plugin.blockDissolver.getPriorityBlocks(aPlayer);

		for (Inventory inventory:aInventoryList){
			for (ItemStack item:inventory){
				if (item!=null){
					if (!disposeBlocks.contains(item.getType())){
						if (surplusBlocks.contains(item.getType())){
							surplusItems.add(item);
						}else if (priorityBlocks.contains(item.getType())){
							priorityItems.add(item);
						}else{
							normalItems.add(item);	
						}
					}
				}
			}
		}
		aInventoryList.clear();
		Collections.sort(surplusItems, new Comparator<ItemStack>() {
	        public int compare(ItemStack i1, ItemStack i2) {
	        	if (i1.getType()==i2.getType()) {
	        		return Integer.compare(i2.getAmount(), i1.getAmount()); 
	        	}
	            return i2.getType().compareTo(i1.getType());
	        }
	    });
		Collections.sort(normalItems, new Comparator<ItemStack>() {
	        public int compare(ItemStack i1, ItemStack i2) {
	        	if (i1.getType()==i2.getType()) {
	        		return Integer.compare(i2.getAmount(), i1.getAmount()); 
	        	}
	            return i2.getType().compareTo(i1.getType());
	        }
	    });
		Collections.sort(priorityItems, new Comparator<ItemStack>() {
	        public int compare(ItemStack i1, ItemStack i2) {
	        	if (i1.getType()==i2.getType()) {
	        		return Integer.compare(i2.getAmount(), i1.getAmount()); 
	        	}
	            return i2.getType().compareTo(i1.getType());
	        }
	    });
		//TODO Sorting is not working 100%!!  Need to debug!
		addItems(aPlayer, aInventoryList, priorityItems);
		addItems(aPlayer, aInventoryList, normalItems);
		addItems(aPlayer, aInventoryList, surplusItems);
		return true;
	}

	protected List<Inventory> getInventoryList(Player aPlayer, String aInventoryKey){
		if (aInventoryKey.equalsIgnoreCase("")){
			aInventoryKey=aPlayer.getName();
		}
		List<Inventory> inventoryList=inventoryMap.get(aInventoryKey);
		if (inventoryList==null){
			inventoryList=new ArrayList<Inventory>();
			inventoryMap.put(aInventoryKey, inventoryList);
		}
		return inventoryList;
	}
}

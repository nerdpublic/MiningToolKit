package com.gmail.nerdx86.MiningToolKit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.nerdx86.MiningToolKit.AutoMiner.AutoMinerConfiguration;
import com.gmail.nerdx86.MiningToolKit.NanoDissolver.NanoDissolveOperation;

public class BlockDissolver extends BukkitRunnable {
	public MiningToolKit plugin;

	public Queue<BlockToDissolve> blocksToDissolve = new PriorityQueue<BlockToDissolve>(
			10, new BlockToDissolveComparator());
	public Map<String, Map<String, HashSet<Material>>> blockMaps = new HashMap<String, Map<String, HashSet<Material>>>();

	public BlockDissolver(MiningToolKit aPlugin) {
		plugin = aPlugin;
	}

	public boolean addBlock(Player aPlayer, Block aBlock, double aDistance) {
		if (!plugin.blockDissolver.getIgnoreBlocks(aPlayer).contains(
				aBlock.getType())) {
			if (!blocksToDissolve.offer(new BlockToDissolve(aPlayer, aBlock,
					aDistance))) {
				plugin.miningUtils.logToTextFile(aPlayer
						+ " could not dissolve " + aBlock + " @ Location: "
						+ aBlock.getLocation());
				return false;
			}
		}
		return true; // Block was handled
	}

	public HashSet<Material> getBlocksByName(String aPlayerName,
			String aBlockMapName, HashSet<Material> aDefaultValue) {
		Map<String, HashSet<Material>> playerBlockMaps = blockMaps
				.get(aPlayerName);
		if (playerBlockMaps == null) {
			playerBlockMaps = new HashMap<String, HashSet<Material>>();
			blockMaps.put(aPlayerName, playerBlockMaps);
		}
		HashSet<Material> blocksMap = playerBlockMaps.get(aBlockMapName);
		if (blocksMap == null) {
			blocksMap = new HashSet<Material>(aDefaultValue);
			// TODO Load from disk if available..
			playerBlockMaps.put(aBlockMapName, blocksMap);
		}
		return blocksMap;
	}

	public Set<String> getBlockMapNames(String aPlayerName) {
		Map<String, HashSet<Material>> playerBlockMaps = blockMaps
				.get(aPlayerName);
		if (playerBlockMaps == null) {
			playerBlockMaps = new HashMap<String, HashSet<Material>>();
			blockMaps.put(aPlayerName, playerBlockMaps);
		}
		getBlocksByName(aPlayerName, "IgnoreBlocks", plugin.miningUtils.defaultIgnoreBlocks);
		getBlocksByName(aPlayerName, "PriorityBlocks", new HashSet<Material>());
		getBlocksByName(aPlayerName, "SurplusBlocks", new HashSet<Material>());
		getBlocksByName(aPlayerName, "DisposeBlocks", new HashSet<Material>());

		return blockMaps.keySet();
	}

	public HashSet<Material> getIgnoreBlocks(Player aPlayer) {
		return getBlocksByName(aPlayer.getName(), "IgnoreBlocks",
				plugin.miningUtils.defaultIgnoreBlocks);
	}

	public HashSet<Material> getPriorityBlocks(Player aPlayer) {
		return getBlocksByName(aPlayer.getName(), "PriorityBlocks",
				new HashSet<Material>());
	}

	public HashSet<Material> getSurplusBlocks(Player aPlayer) {
		return getBlocksByName(aPlayer.getName(), "SurplusBlocks",
				new HashSet<Material>());
	}

	public HashSet<Material> getDisposeBlocks(Player aPlayer) {
		return getBlocksByName(aPlayer.getName(), "DisposeBlocks",
				new HashSet<Material>());
	}

	public void loadBD(YamlConfiguration aConfiguration, List<String> aPlayerSet) {
		
	}

	public void saveBD(YamlConfiguration aConfiguration, HashSet<String> aPlayerSet) {
		Iterator<String> blocksMapKeyIterator = blockMaps.keySet().iterator();
		while (blocksMapKeyIterator.hasNext()) {
			aPlayerSet.add(blocksMapKeyIterator.next());
		}
		
		Iterator<String> playerIterator = aPlayerSet.iterator();
		while (playerIterator.hasNext()) {
			savePlayerBlocksMaps(aConfiguration, playerIterator.next());
		}
	}

	public void savePlayerBlocksMaps(YamlConfiguration aConfiguration, String aPlayerName) {
		Set<String> playerBlockMapNames=getBlockMapNames(aPlayerName);
		Iterator<String> playerBlockMapNamesIterator=playerBlockMapNames.iterator();
		while (playerBlockMapNamesIterator.hasNext()){
			String currentBlocksMapName=playerBlockMapNamesIterator.next();
			HashSet<Material> playerBlocksMap=getBlocksByName(aPlayerName, currentBlocksMapName, new HashSet<Material>());
			saveBlocksMap(aConfiguration, aPlayerName, currentBlocksMapName, playerBlocksMap);
		}
	}

	public void saveBlocksMap(YamlConfiguration aConfiguration,	String aPlayerName, String aMapName, HashSet<Material> aBlocksmap) {
		List<String> materialNames=new ArrayList<String>(); 	
		Iterator<Material> blocksMapIterator=aBlocksmap.iterator();
		while (blocksMapIterator.hasNext()){
			Material currentMaterial=blocksMapIterator.next();
			materialNames.add(currentMaterial.name());
		}
		aConfiguration.set(aPlayerName + ".BlockDissolver." + aMapName,	materialNames);
	}

	public boolean processCommandClassify(Player aPlayer, String[] args) {
		if (args.length == 1) {
			aPlayer.sendMessage("classes: show available classes");
			aPlayer.sendMessage("[class]: list materials in class");
			aPlayer.sendMessage("[class] add [material]: add material to class");
			aPlayer.sendMessage("[class] remove [material]: remove material from class");
			return true;
		}
		if ((args.length > 1) && args[1].equalsIgnoreCase("classes")) {
			aPlayer.sendMessage("Classes:");
			aPlayer.sendMessage(" Ignore");
			aPlayer.sendMessage(" Priority");
			aPlayer.sendMessage(" Surplus");
			aPlayer.sendMessage(" Dispose");
			return true;
		}

		HashSet<Material> selectedClassification = null;
		List<HashSet<Material>> nonselectedClassifications = new ArrayList<HashSet<Material>>();

		nonselectedClassifications.add(getIgnoreBlocks(aPlayer));
		nonselectedClassifications.add(getDisposeBlocks(aPlayer));
		nonselectedClassifications.add(getSurplusBlocks(aPlayer));
		nonselectedClassifications.add(getPriorityBlocks(aPlayer));
		if ((args.length > 1)
				&& (args[1].equalsIgnoreCase("ignore") || args[1]
						.equalsIgnoreCase("i"))) {
			selectedClassification = getIgnoreBlocks(aPlayer);
		} else if ((args.length > 1)
				&& (args[1].equalsIgnoreCase("dispose") || args[1]
						.equalsIgnoreCase("d"))) {
			selectedClassification = getDisposeBlocks(aPlayer);
		} else if ((args.length > 1)
				&& (args[1].equalsIgnoreCase("surplus") || args[1]
						.equalsIgnoreCase("s"))) {
			selectedClassification = getSurplusBlocks(aPlayer);
		} else if ((args.length > 1)
				&& (args[1].equalsIgnoreCase("priority") || args[1]
						.equalsIgnoreCase("p"))) {
			selectedClassification = getPriorityBlocks(aPlayer);
		} else {
			aPlayer.sendMessage("unknown class: " + args[1]);
			return true;
		}
		nonselectedClassifications.remove(selectedClassification);
		if (args.length == 2) {
			// List
			aPlayer.sendMessage(args[1] + ":");
			for (Material material : selectedClassification) {
				aPlayer.sendMessage(" " + material);
			}
		} else if ((args.length > 2)
				&& (args[2].equalsIgnoreCase("add") || args[2]
						.equalsIgnoreCase("a"))) {
			if (args.length == 3) {
				aPlayer.sendMessage("What do you want to add?");
				return true;
			}
			Material materialToAdd = Material.matchMaterial(args[3]);
			if (materialToAdd == null) {
				aPlayer.sendMessage(args[3] + " is not a valid material");
			} else {
				selectedClassification.add(materialToAdd);
				for (HashSet<Material> classification : nonselectedClassifications) {
					classification.remove(materialToAdd);
				}
			}
			aPlayer.sendMessage(args[1] + ":");
			for (Material material : selectedClassification) {
				aPlayer.sendMessage(" " + material);
			}
			return true;
		} else if ((args.length > 2)
				&& (args[2].equalsIgnoreCase("remove") || args[2]
						.equalsIgnoreCase("r"))) {
			if (args.length == 3) {
				aPlayer.sendMessage("What do you want to remove?");
				return true;
			}
			Material materialToRemove = Material.matchMaterial(args[3]);
			if (materialToRemove == null) {
				aPlayer.sendMessage(args[3] + " is not a valid material");
			} else {
				selectedClassification.remove(materialToRemove);
			}
			aPlayer.sendMessage(args[1] + ":");
			for (Material material : selectedClassification) {
				aPlayer.sendMessage(" " + material);
			}
			return true;
		}
		return true;
	}

	public class BlockToDissolve {
		Block block;
		Player player;
		double distance;

		public BlockToDissolve(Player aPlayer, Block aBlock, double aDistance) {
			player = aPlayer;
			block = aBlock;
			distance = aDistance;
		}

		public void doDissolve() {
			if (block != null) {
				if (!getIgnoreBlocks(player).contains(block.getType())) {
					Collection<ItemStack> items = block.getDrops();
					// logToTextFile(player+" Dissolving "+block+" @ Location: "+block.getLocation()+" Should drop: "+items);
					Iterator<ItemStack> it = items.iterator();
					while (it.hasNext()) {
						if (getDisposeBlocks(player).contains(
								it.next().getType())) {
							it.remove();
						}
					}
					if (plugin.miningInventory.addItems(player, "", items)) {
						block.setType(Material.AIR);
					}
				}
			}
		}
	}

	public class BlockToDissolveComparator implements
			Comparator<BlockToDissolve> {
		@Override
		public int compare(BlockToDissolve arg0, BlockToDissolve arg1) {
			if (arg0.distance < arg1.distance) {
				return -1;
			}
			if (arg0.distance > arg1.distance) {
				return 1;
			}
			return 0;
		}
	}

	@Override
	public void run() {
		long startMS = (new Date().getTime());
		while (!blocksToDissolve.isEmpty()) {
			// getLogger().info("!DissolveBlocks.isEmpty");
			blocksToDissolve.poll().doDissolve();
			long nowMS = new Date().getTime();
			if ((nowMS < startMS) || (nowMS > startMS + 1))
				break;
		}
	}
}

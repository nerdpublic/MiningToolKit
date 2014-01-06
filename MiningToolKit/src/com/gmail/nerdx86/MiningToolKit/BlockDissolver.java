package com.gmail.nerdx86.MiningToolKit;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;


public class BlockDissolver extends ToolBaseObject{
	public Queue<BlockToDissolve> blocksToDissolve = new PriorityQueue<BlockToDissolve>(10, new BlockToDissolveComparator());
	final HashSet<Material> ignoreBlocks=new HashSet<Material>(plugin.miningUtils.defaultIgnoreBlocks);
	final HashSet<Material> disposeBlocks=new HashSet<Material>();

	public BlockDissolver(MiningToolKit aPlugin) {
		super(aPlugin);
		// TODO Auto-generated constructor stub
	}

	public boolean addBlock(Player aPlayer, Block aBlock, double aDistance){
		if (!plugin.blockDissolver.ignoreBlocks.contains(aBlock.getType())) {
			if (!blocksToDissolve.offer(new BlockToDissolve(aPlayer, aBlock, aDistance))){
				plugin.miningUtils.logToTextFile(aPlayer+" could not dissolve "+aBlock+" @ Location: "+aBlock.getLocation());
				return false;
			}
		}
		return true; //Block was handled
	}

	public void doBlockDissolve(PlayerMoveEvent event) {
		int i=500;
		while ((i>0) && (!blocksToDissolve.isEmpty())){
			//getLogger().info("!DissolveBlocks.isEmpty");
			blocksToDissolve.poll().doDissolve();
			i--;
		}
	}

	public void doPlayerMoveEvent(PlayerMoveEvent event){
		doBlockDissolve(event);	
	}
	
	public class BlockToDissolve{
		Block block;
		Player player;
		double distance;
		public BlockToDissolve(Player aPlayer, Block aBlock, double aDistance) {
			player=aPlayer;
			block=aBlock;
			distance=aDistance;
		}
		public void doDissolve(){
			if (block != null){
				if (!ignoreBlocks.contains(block.getType())) {
					Collection<ItemStack> items=block.getDrops();
					//logToTextFile(player+" Dissolving "+block+" @ Location: "+block.getLocation()+" Should drop: "+items);
					if (plugin.miningInventory.addItems(player, "", items)){
						block.setType(Material.AIR);
					} 
				}
			}
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
}

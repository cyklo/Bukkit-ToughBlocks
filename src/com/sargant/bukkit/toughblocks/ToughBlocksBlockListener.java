// ToughBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.sargant.bukkit.toughblocks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

public class ToughBlocksBlockListener extends BlockListener
{
	private ToughBlocks parent;

	public ToughBlocksBlockListener(ToughBlocks instance)
	{
		parent = instance;
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) {
			return;
		}

		Block block = event.getBlock();
		ItemStack tool = event.getPlayer().getItemInHand();

		for(ToughBlocksContainer obc : parent.toughList) {

			// Check held item matches
			if(!obc.tool.contains(null) && !obc.tool.contains(tool.getType())) {
				continue;
			}

			// Check target block matches
			if(obc.original != event.getBlock().getType()) {
				continue;
			}
			
			// If block is not in the map already, add it
			if(!parent.blockHealth.containsKey(block.hashCode())) {
				parent.blockHealth.put(block.hashCode(), ToughBlocks.MAX_BLOCK_HEALTH);
			}
			
			// Now deduct the health
			Integer newHealth = parent.blockHealth.get(block.hashCode());
			newHealth = newHealth - obc.damage;
			
			if(newHealth <= 0) {
				parent.blockHealth.remove(block.hashCode());
			} else {
				parent.blockHealth.put(block.hashCode(), newHealth);
				
				block.setType(Material.DIRT); // Can't set to AIR for some reason
				block.setType(obc.original);
				event.setCancelled(true);
				
				if(parent.showProgress && obc.damage > 0) {
					event.getPlayer().sendMessage("Block " + Math.round(100 * ((double) newHealth / (double) ToughBlocks.MAX_BLOCK_HEALTH)) + "% intact...");
				}
			}
			break;
		}
	}
}

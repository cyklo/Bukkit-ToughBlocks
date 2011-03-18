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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public class ToughBlocks extends JavaPlugin
{
	public static final Integer MAX_BLOCK_HEALTH = 360;
	
	protected List<ToughBlocksContainer> toughList;
	protected Map<Integer, Integer> blockHealth;
	private final ToughBlocksBlockListener blockListener;
	protected final Logger log;
	protected Integer verbosity;
	protected Priority pri;
	protected boolean showProgress;

	public ToughBlocks() {

		toughList = new ArrayList<ToughBlocksContainer>();
		blockHealth = new HashMap<Integer, Integer>();
		blockListener = new ToughBlocksBlockListener(this);
		log = Logger.getLogger("Minecraft");
		verbosity = 2;
		pri = Priority.Lowest;
	}

	public void onDisable()
	{
		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
	}

	public void onEnable()
	{
		// Initialize and read in the YAML file

		getDataFolder().mkdirs();
		File yml = new File(getDataFolder(), "config.yml");

		if (!yml.exists())
		{
			try {
				yml.createNewFile();
				log.info("Created an empty file " + getDataFolder() +"/config.yml, please edit it!");
				getConfiguration().setProperty("toughblocks", null);
				getConfiguration().save();
			} catch (IOException ex){
				log.warning(getDescription().getName() + ": could not generate config.yml. Are the file permissions OK?");
			}
		}

		// Load in the values from the configuration file
		List <String> keys;
		try {
			keys = getConfiguration().getKeys(null);
		} catch(NullPointerException ex) {
			log.warning(getDescription().getName() + ": no parent key not found");
			return;
		}

		if(keys.contains("verbosity")) {
			String verb_string = getConfiguration().getString("verbosity", "normal");

			if(verb_string.equalsIgnoreCase("low")) { verbosity = 1; }
			else if(verb_string.equalsIgnoreCase("high")) { verbosity = 3; }
			else { verbosity = 2; }
		}

		if(keys.contains("priority")) {
			String priority_string = getConfiguration().getString("priority", "lowest");
			if(priority_string.equalsIgnoreCase("low")) { pri = Priority.Low; }
			else if(priority_string.equalsIgnoreCase("normal")) { pri = Priority.Normal; }
			else if(priority_string.equalsIgnoreCase("high")) { pri = Priority.High; }
			else if(priority_string.equalsIgnoreCase("highest")) { pri = Priority.Highest; }
			else { pri = Priority.Lowest; }
		}
		
		showProgress = getConfiguration().getBoolean("showprogress", true);

		if(!keys.contains("toughblocks"))
		{
			log.warning(getDescription().getName() + ": no 'toughblocks' key found");
			return;
		}

		keys.clear();
		keys = getConfiguration().getKeys("toughblocks");

		if(null == keys)
		{
			log.info(getDescription().getName() + ": no values found in config file!");
			return;
		}

		for(String s : keys) {
			List<Object> original_children = getConfiguration().getList("toughblocks."+s);

			if(original_children == null) {
				log.warning("Block \""+s+"\" has no children. Have you included the dash?");
				continue;
			}

			for(Object o : original_children) {
				if(o instanceof HashMap<?,?>) {

					ToughBlocksContainer tbc = new ToughBlocksContainer();

					try {
						HashMap<?, ?> m = (HashMap<?, ?>) o;

						// Source block
						tbc.original = Material.valueOf(s);

						// Tool used
						tbc.tool = new ArrayList<Material>();

						if(m.get("tool") instanceof String) {

							String toolString = (String) m.get("tool");

							if(toolString.equalsIgnoreCase("DYE")) {
								toolString = "INK_SACK";
							}

							if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
								tbc.tool.add((Material) null);
							} else {
								tbc.tool.add(Material.valueOf(toolString));
							}

						} else if (m.get("tool") instanceof List<?>) {

							for(Object listTool : (List<?>) m.get("tool")) {
								System.out.println("Listed tool " + ((String) listTool));
								tbc.tool.add(Material.valueOf((String) listTool));
							}

						} else {
							throw new Exception("Not a recognizable type");
						}
						
						// Damage incurred
						Integer damageDealt = Integer.class.cast(m.get("damage"));
						tbc.damage = (damageDealt == null || damageDealt < 0) ? MAX_BLOCK_HEALTH : damageDealt;

					} catch(Throwable ex) {
						if(verbosity > 1) {
							log.warning("Error while processing block " + s + ": " + ex.getMessage());
						}

						ex.printStackTrace();
						continue;
					}

					toughList.add(tbc);

					if(verbosity > 1) {
						log.info(getDescription().getName() + ": " +
								(tbc.tool.contains(null) ? "ALL TOOLS" : (tbc.tool.size() == 1 ? tbc.tool.get(0).toString() : tbc.tool.toString())) +
								" now does " + tbc.damage.toString() + " damage to " +
								tbc.original.toString());
					}
				}
			}
		}

		// Done setting up plugin

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, pri, this);

		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
	}
}

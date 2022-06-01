package com.github.longboyy.evolution;

import com.github.longboyy.evolution.commands.EvolutionCommandManager;
import com.github.longboyy.evolution.listeners.EntityListener;
import com.github.longboyy.evolution.listeners.PlayerListener;
import com.github.longboyy.evolution.traits.TraitLogicHandler;
import com.github.longboyy.evolution.traits.TraitManager;
import com.github.longboyy.evolution.util.pdc.StringDoubleMap;
import com.github.longboyy.evolution.util.TraitUtils;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.ACivMod;

public class Evolution extends ACivMod {

	/**
	 * Evolution is a plugin about mob genetics
	 *
	 * There is a list of possible traits a mob can have
	 * A mob will only inherit one trait per category from its parent
	 */

	public static final TextColor SUCCESS_GREEN = TextColor.color(40, 100, 0);
	public static final TextColor FAILURE_RED = TextColor.color(200, 0, 30);


	private static Evolution instance;

	public static Evolution getInstance(){
		return instance;
	}

	private EvolutionConfigParser configParser;
	private TraitManager traitManager;

	private EvolutionCommandManager commandManager;

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		this.traitManager = new TraitManager(this);
		this.configParser = new EvolutionConfigParser(this);
		if(!this.configParser.parse()){
			Bukkit.shutdown();
			return;
		}

		//this.traitManager.parseConfig(this.configParser.getConfig());
		TraitUtils.registerDefaultTraits(this.traitManager);

		this.registerListener(new EntityListener(this));
		this.registerListener(new PlayerListener(this));

		this.commandManager = new EvolutionCommandManager(this);
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	public EvolutionConfigParser getConfigParser(){
		return this.configParser;
	}

	public TraitManager getTraitManager(){
		return this.traitManager;
	}
}

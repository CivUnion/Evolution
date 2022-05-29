package com.github.longboyy.evolution;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.config.ConfigParser;

public class EvolutionConfigParser extends ConfigParser {

	private ConfigurationSection baseSection;

	private final Evolution evo;

	public EvolutionConfigParser(Evolution plugin) {
		super(plugin);
		this.evo = plugin;
	}

	public ConfigurationSection getConfig(){
		return baseSection;
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		this.baseSection = config;
		return true;
	}
}

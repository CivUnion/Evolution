package com.github.longboyy.evolution;

import com.github.longboyy.evolution.events.EvolutionReloadEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.config.ConfigParser;

import java.util.HashMap;
import java.util.Map;

public class EvolutionConfigParser extends ConfigParser {

	private final Evolution plugin;
	private final Map<String, Object> configOptions;
	//private ConfigurationSection baseSection;


	public EvolutionConfigParser(Evolution plugin) {
		super(plugin);
		this.plugin = plugin;
		this.configOptions = new HashMap<>();
	}

	public <T> T getOption(String key){
		if(!this.configOptions.containsKey(key)){
			return null;
		}

		Object rawValue = this.configOptions.get(key);

		try {
			@SuppressWarnings("unchecked")
			T value = (T) rawValue;
			return value;
		}catch(Exception e){
			return null;
		}
	}

	public <T> T getOption(String key, T defaultValue){
		T value = this.getOption(key);
		return value == null ? defaultValue : value;
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		this.configOptions.clear();
		if(config.isConfigurationSection("settings")){
			ConfigurationSection current = config.getConfigurationSection("settings");
			for(String key : current.getKeys(false)){
				Object val = current.get(key);
				if(val == null){
					continue;
				}
				this.configOptions.put(key, val);
			}
		}

		this.plugin.getTraitManager().parseConfig(config.getConfigurationSection("traits"));
		EvolutionReloadEvent event = new EvolutionReloadEvent(config);
		return event.callEvent();
	}
}

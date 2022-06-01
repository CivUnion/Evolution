package com.github.longboyy.evolution.events;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EvolutionReloadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final ConfigurationSection section;

	public EvolutionReloadEvent(ConfigurationSection section){
		this.section = section;
	}

	public ConfigurationSection getConfig(){
		return this.section;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return handlers;
	}
}

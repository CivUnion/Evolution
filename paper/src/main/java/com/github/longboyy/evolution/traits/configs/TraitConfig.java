package com.github.longboyy.evolution.traits.configs;

import org.bukkit.configuration.ConfigurationSection;


public class TraitConfig {

	protected double weight = 0.05D;
	protected double maxVariationPerGeneration = 0.005D;

	public TraitConfig(){
	}

	public void parse(ConfigurationSection section){
		this.weight = section.getDouble("weight", 1D);
		this.maxVariationPerGeneration = section.getDouble("maxVariationPerGeneration", 0.005D);
	}

	public double getWeight(){
		return this.weight;
	}

	public double getMaxVariationPerGeneration(){
		return this.maxVariationPerGeneration;
	}

}

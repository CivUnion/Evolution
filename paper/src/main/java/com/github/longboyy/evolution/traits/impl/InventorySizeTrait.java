package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.Trait;
import com.github.longboyy.evolution.traits.TraitCategory;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.objecthunter.exp4j.Expression;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

public class InventorySizeTrait extends Trait {

	private Expression variationExpression = TraitUtils.createVariationExpression("(log(1+x)/log(2))^0.7");

	public InventorySizeTrait() {
		super("inv_size", TraitCategory.UTILITY, ImmutableSet.copyOf(new EntityType[]{
				EntityType.LLAMA
		}));
	}

	@Override
	public boolean applyTrait(TraitEntity entity, double variation) {
		boolean success = super.applyTrait(entity, variation);
		if(success){
			int columns = this.getInventoryColumns(entity);
			// why the fuck is this called set strength????
			// it's literally just the number of columns in a llamas inventory reeeeee
			Llama llama = (Llama) entity;
			llama.setStrength(columns);
		}
		return success;
	}

	@Override
	public TextComponent.Builder displayInfo(TraitEntity entity) {
		TextComponent.Builder newBuilder = super.displayInfo(entity);
		newBuilder.append(Component.newline());
		newBuilder.append(Component.text("Columns:"));
		newBuilder.append(Component.space());
		int columns = this.getInventoryColumns(entity);
		newBuilder.append(Component.text(columns, Evolution.SUCCESS_GREEN));

		return newBuilder;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return "Inventory Size";
	}

	@Override
	public double getMaxVariation() {
		return 0;
	}

	@Override
	public void parseConfig(ConfigurationSection section) {
		super.parseConfig(section);
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 1D;
	}

	private int getInventoryColumns(TraitEntity entity){
		return Math.toIntExact(Math.round(1D + MoreMath.clamp(4D * this.getModifier(entity), 0D, 4D)));
	}

	private double getModifier(TraitEntity entity){
		double variation = this.getVariation(entity);
		if(variation >= 0D){
			return variationExpression.setVariable("x", variation).evaluate();
		}else{
			return 0D;
		}
	}
}

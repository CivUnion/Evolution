package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.traits.Trait;
import com.github.longboyy.evolution.traits.TraitCategory;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.objecthunter.exp4j.Expression;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.Map;

public class SpeedTrait extends Trait {

	private double defaultValue = 0.225D;

	private Map<EntityType, Double> maxSpeedMap = new HashMap<>();

	private Expression positiveExpression = TraitUtils.createVariationExpression("(log(1+x)/log(2))^0.7");
	private Expression negativeExpression = TraitUtils.createVariationExpression("-(log(1-x)/log(2))^0.7");

	public SpeedTrait() {
		super("speed", TraitCategory.UTILITY, ImmutableSet.copyOf(new EntityType[]{
				EntityType.HORSE,
				EntityType.MULE,
				EntityType.DONKEY,
				EntityType.LLAMA
		}));

		this.getAllowedTypes().forEach(type -> {
			this.maxSpeedMap.put(type, defaultValue);
		});
	}

	@Override
	public TextComponent.Builder displayInfo(TraitEntity entity) {
		TextComponent.Builder newBuilder = super.displayInfo(entity);
		newBuilder.append(Component.newline());
		newBuilder.append(Component.text("Speed:"));
		newBuilder.append(Component.space());
		double bps = this.getExtraSpeed(entity, this.getVariation(entity));
		double internalSpeed = this.bpsToInternal(bps);
		newBuilder.append(Component.text(String.format("%1.5f [%1.5fb/s]", internalSpeed, bps)));
		return newBuilder;
	}

	@Override
	public boolean applyTrait(TraitEntity entity, double variation) {
		boolean success = super.applyTrait(entity, variation);
		if(success){
			AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);

			for(AttributeModifier modifier : attribute.getModifiers()){
				if(modifier.getName().equalsIgnoreCase("speed_trait")){
					attribute.removeModifier(modifier);
				}
			}

			EntityType type = entity.getType();
			double defaultSpeed;

			switch(type){
				case HORSE:
				case DONKEY:
				case MULE:
				case LLAMA:
				case TRADER_LLAMA:
				case STRIDER:
					defaultSpeed = 0.225D;
					break;
				default:
					AttributeInstance defaultAttribute = type.getDefaultAttributes().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
					if(defaultAttribute == null){
						return false;
					}
					defaultSpeed = defaultAttribute.getBaseValue();
					break;
			}

			attribute.setBaseValue(defaultSpeed);

			double extraSpeed = this.bpsToInternal(this.getExtraSpeed(entity, variation));
			AttributeModifier modifier = new AttributeModifier("speed_trait", extraSpeed, AttributeModifier.Operation.ADD_NUMBER);
			attribute.addModifier(modifier);
		}

		return success;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return "Speed";
	}

	@Override
	public double getMaxVariation() {
		return 0.005D;
	}

	@Override
	public void parseConfig(ConfigurationSection section) {
		super.parseConfig(section);
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 1D;
	}

	private double bpsToInternal(double bps){
		return bps / 43.17D;
	}

	private double getExtraSpeed(TraitEntity entity, double variation){
		double modifiedSpeed = this.maxSpeedMap.getOrDefault(entity.getType(), defaultValue);

		if(variation >= 0){
			modifiedSpeed *= positiveExpression.setVariable("x", variation).evaluate();
		}else{
			modifiedSpeed *= negativeExpression.setVariable("x", variation).evaluate();
		}

		return modifiedSpeed;
	}
}

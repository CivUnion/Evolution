package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.traits.Trait;
import com.github.longboyy.evolution.traits.TraitCategory;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.github.longboyy.evolution.traits.configs.ExpressionTraitConfig;
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

public class SpeedTrait extends Trait<SpeedTrait.SpeedTraitConfig> {

	protected static double DEFAULT_SPEED = 10D;
	protected static final ImmutableSet<EntityType> APPLICABLE_ENTITY_TYPES = ImmutableSet.copyOf(new EntityType[]{
			EntityType.HORSE,
			EntityType.MULE,
			EntityType.DONKEY,
			EntityType.LLAMA
	});

	public static class SpeedTraitConfig extends ExpressionTraitConfig {

		protected double defaultSpeed = DEFAULT_SPEED;
		protected Map<EntityType, Double> speedMap = new HashMap<>();

		public SpeedTraitConfig(){
		}

		@Override
		public void parse(ConfigurationSection section) {
			super.parse(section);
			speedMap.clear();
			this.defaultSpeed = section.getDouble("defaultSpeed", DEFAULT_SPEED);

			APPLICABLE_ENTITY_TYPES.forEach(type -> {
				this.speedMap.put(type, this.defaultSpeed);
			});

			if(section.isConfigurationSection("overrides")){
				ConfigurationSection overrideSection = section.getConfigurationSection("overrides");
				APPLICABLE_ENTITY_TYPES.forEach(type -> {
					double speed = overrideSection.getDouble(type.name(), this.defaultSpeed);
					this.speedMap.put(type, speed);
				});
			}
		}
	}

	public SpeedTrait() {
		super("speed", TraitCategory.UTILITY, APPLICABLE_ENTITY_TYPES);
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
	protected Class<SpeedTraitConfig> getConfigClass() {
		return SpeedTraitConfig.class;
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 1D;
	}

	private double bpsToInternal(double bps){
		return bps / 43.17D;
	}

	private double getExtraSpeed(TraitEntity entity, double variation){
		double modifiedSpeed = this.config.speedMap.getOrDefault(entity.getType(), this.config.defaultSpeed);

		if(variation >= 0){
			modifiedSpeed *= this.config.getPositiveExpression().setVariable("x", variation).evaluate();
		}else{
			modifiedSpeed *= this.config.getNegativeExpression().setVariable("x", variation).evaluate();
		}

		return modifiedSpeed;
	}
}

package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.listeners.TraitListener;
import com.github.longboyy.evolution.traits.*;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

import java.util.HashMap;
import java.util.Map;

public class HealthTrait extends Trait {

	private double defaultValue = 15D;
	private Map<EntityType, Double> healthMap = new HashMap<>();

	private Expression positiveExpression = TraitUtils.createVariationExpression("(log(1+x)/log(2))^0.7");
	private Expression negativeExpression = TraitUtils.createVariationExpression("-(log(1-x)/log(2))^0.7");

	public HealthTrait() {
		super("health", 1D, TraitCategory.UTILITY, ImmutableSet.copyOf(new EntityType[]{
				EntityType.COW,
				EntityType.SHEEP,
				EntityType.PIG,
				EntityType.HORSE,
				EntityType.MULE,
				EntityType.DONKEY,
				EntityType.CHICKEN
		}));

		this.getAllowedTypes().forEach(type -> {
			this.healthMap.put(type, defaultValue);
		});

		TraitListener listener = Evolution.getInstance().getTraitManager().getListener();
		//listener.registerEvent(this, );
	}

	@Override
	public boolean applyTrait(LivingEntity entity, double variation){
		boolean success = super.applyTrait(entity, variation);
		if(success){
			AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);

			for(AttributeModifier modifier : attribute.getModifiers()){
				if(modifier.getName().equalsIgnoreCase("health_trait")){
					attribute.removeModifier(modifier);
				}
			}

			EntityType type = entity.getType();
			double defaultHealth;
			
			switch(type){
				case HORSE:
				case DONKEY:
				case MULE:
				case LLAMA:
				case TRADER_LLAMA:
				case STRIDER:
					defaultHealth = 20D;
					break;
				default:
					AttributeInstance defaultAttribute = type.getDefaultAttributes().getAttribute(Attribute.GENERIC_MAX_HEALTH);
					if(defaultAttribute == null){
						return false;
					}
					defaultHealth = defaultAttribute.getBaseValue();
					break;
			}

			attribute.setBaseValue(defaultHealth);

			//double defaultValue = entity.getType().getDefaultAttributes().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			//attribute.setBaseValue(defaultValue + extraHealth);

			double extraHealth = this.getExtraHealth(entity, variation);
			AttributeModifier modifier = new AttributeModifier("health_trait", extraHealth, AttributeModifier.Operation.ADD_NUMBER);
			attribute.addModifier(modifier);
			//attribute.addModifier();
			double newHealth = attribute.getValue();
			if(newHealth <= 0D){
				entity.damage(entity.getHealth());
			}else {
				entity.setHealth(attribute.getValue());
			}
		}

		return success;
	}

	@Override
	public TextComponent.Builder displayInfo(LivingEntity entity) {
		TextComponent.Builder newBuilder = super.displayInfo(entity);
		//int amount = Math.toIntExact(Math.round(MoreMath.clamp(this.maxValue * this.getMultiplier(entity), this.minValue, this.maxValue)));
		//newBuilder.hoverEvent(HoverEvent.showItem(Material.BONE.getKey(), amount, null));
		//newBuilder.hoverEvent(HoverEvent.showEntity(entity.getType().getKey(), entity.getUniqueId()));

		AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);

		double health = this.getExtraHealth(entity, this.getVariation(entity));
		newBuilder.append(Component.newline());
		newBuilder.append(Component.text("Base Health:"));
		newBuilder.append(Component.space());
		newBuilder.append(Component.text(String.format("%1.5f", attribute.getBaseValue()), health >= 0 ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED));
		newBuilder.append(Component.newline());
		newBuilder.append(Component.text("Extra Health:"));
		newBuilder.append(Component.space());
		newBuilder.append(Component.text(String.format("%1.5f", health), health >= 0 ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED));
		return newBuilder;
	}

	@Override
	public String getPrettyName() {
		return "Health";
	}

	@Override
	public double getMaxVariation() {
		return 0.005;
	}

	@Override
	public void parseConfig(ConfigurationSection section) {

	}

	private double getExtraHealth(LivingEntity entity){
		AttributeInstance attribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if(attribute == null){
			return 0D;
		}

		for(AttributeModifier modifier : attribute.getModifiers()){
			if(modifier.getName().equalsIgnoreCase("health_trait")){
				return modifier.getAmount();
			}
		}

		return 0D;
	}

	private double getExtraHealth(LivingEntity entity, double variation){
		double modifiedHealth = this.healthMap.getOrDefault(entity.getType(), defaultValue);

		if(variation >= 0){
			modifiedHealth *= positiveExpression.setVariable("x", variation).evaluate();
		}else{
			modifiedHealth *= negativeExpression.setVariable("x", variation).evaluate();
		}

		return modifiedHealth;
	}
}

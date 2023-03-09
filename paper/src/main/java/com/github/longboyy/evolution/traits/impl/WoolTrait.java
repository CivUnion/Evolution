package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.traits.ListenerTrait;
import com.github.longboyy.evolution.traits.TraitCategory;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.google.common.collect.ImmutableSet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
import com.github.longboyy.evolution.traits.configs.ExpressionTraitConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;
import org.bukkit.event.entity.SheepRegrowWoolEvent;

import java.util.HashMap;
import java.util.UUID;

public class WoolTrait extends ListenerTrait<WoolTrait.WoolTraitConfig> {

	public static class WoolTraitConfig extends ExpressionTraitConfig {

		protected int minRegrow = 2;
		protected int maxRegrow = 8;
		protected int regrowDifference = 6;

		protected HashMap<UUID, Integer> grassEatenMap = new HashMap<UUID, Integer>();

		public WoolTraitConfig(){
		}

		@Override
		public void parse(ConfigurationSection section) {
			super.parse(section);
			this.minRegrow = section.getInt("minRegrow", 2);
			this.maxRegrow = section.getInt("maxRegrow", 8);
			this.regrowDifference = maxRegrow - minRegrow;
		}
	}

	public WoolTrait() {
		super("Wool", TraitCategory.HUSBANDRY, ImmutableSet.copyOf(new EntityType[]{
			EntityType.SHEEP
	}));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onGrassEat(SheepRegrowWoolEvent e){
		UUID shepUUID = e.getEntity().getUniqueId();
		if (this.config.grassEatenMap.containsKey(shepUUID)) {
			TraitEntity entity = new TraitEntity(e.getEntity());
			int grassNeeded;

			if (!entity.hasTrait(this, TraitType.ACTIVE)) {
				grassNeeded = this.config.maxRegrow;
			} else {
				//long loveTime = Math.round(this.config.maxTimeBetweenBreeds - (this.config.differenceBetweenBreeds*modifier));
				grassNeeded = Math.toIntExact(Math.round(this.config.maxRegrow - (this.config.regrowDifference * this.getMultiplier(entity))));
				//grassNeeded = Math.toIntExact(Math.round(MoreMath.clamp(this.config.minRegrow * this.getMultiplier(entity), this.config.minRegrow, this.config.maxRegrow)));
			}

			if (this.config.grassEatenMap.get(shepUUID) < grassNeeded) {
				this.config.grassEatenMap.put(shepUUID, this.config.grassEatenMap.get(shepUUID) + 1);
				e.setCancelled(true);
			}
		} else {
			this.config.grassEatenMap.put(shepUUID, 1);
			e.setCancelled(true);
		}
	}

	@Override
	public TextComponent.Builder displayInfo(TraitEntity entity) {
		TextComponent.Builder newBuilder = super.displayInfo(entity);
		double realAmount = MoreMath.clamp(this.config.minRegrow * this.getMultiplier(entity), this.config.minRegrow, this.config.maxRegrow);
		int amount = Math.toIntExact(Math.round(realAmount));
		newBuilder.append(Component.newline());
		newBuilder.append(Component.text("Wool Regrowth Speed:"));
		newBuilder.append(Component.space());
		newBuilder.append(Component.text(String.format("%1.5f [~%s]", realAmount, amount), realAmount >= 0 ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED));
		return newBuilder;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return entity.getVariation(this) >= 0 ? "Fast Growing" : "Slow Growing";
	}

	@Override
	public double getMaxVariation() {
		return 0.005D;
	}

	@Override
	protected Class<WoolTraitConfig> getConfigClass() {
		return WoolTraitConfig.class;
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 1D;
	}

	private double getMultiplier(TraitEntity entity){
		double variation = this.getVariation(entity);
		if(variation > 0D){
			return 1/this.config.getPositiveExpression().setVariable("x", variation).evaluate();
		}else{
			return 0D;
		}
	}
}

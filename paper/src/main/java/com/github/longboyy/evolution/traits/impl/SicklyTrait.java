package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
import com.github.longboyy.evolution.traits.configs.ExpressionTraitConfig;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SicklyTrait extends Trait<SicklyTrait.SicklyTraitConfig> {
	protected static long SICKNESS_INTERVAL = 20L;

	public static class SicklyTraitConfig extends ExpressionTraitConfig {

		protected double sicknessRange = 3D;
		protected double sicknessSpreadChance = 0.0001D;

		public SicklyTraitConfig(){
		}

		@Override
		public void parse(ConfigurationSection section) {
			super.parse(section);
			this.sicknessRange = section.getDouble("spreadRadius", 3D);
			this.sicknessSpreadChance = section.getDouble("spreadChance", 0.0001D);
		}

	}

	private int taskId = -1;

	private TraitManager manager;

	public SicklyTrait() {
		super("sickly", TraitCategory.ILLNESS, ImmutableSet.copyOf(new EntityType[]{
				EntityType.COW,
				EntityType.SHEEP,
				EntityType.PIG,
				EntityType.HORSE,
				EntityType.MULE,
				EntityType.DONKEY,
				EntityType.CHICKEN
		}));

		this.manager = Evolution.getInstance().getTraitManager();

		this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Evolution.getInstance(), () -> {
			//ImmutableSet<LivingEntity> entities = Evolution.getInstance().getTraitManager().getEntitiesWith(this, TraitType.ACTIVE);
			ImmutableSet<TraitEntity> entities = TraitUtils.getEntitiesWithTrait(this, TraitType.ACTIVE);
			for(TraitEntity entity : entities){
				this.getDownWithTheSickness(entity);
			}
		}, 0L, SICKNESS_INTERVAL);
	}

	// Oh, ah, ah, ah, ah
	private void getDownWithTheSickness(TraitEntity entity){
		List<Entity> entityList = entity.entity.getNearbyEntities(this.config.sicknessRange, this.config.sicknessRange, this.config.sicknessRange);
		Set<TraitEntity> entities = entityList.stream()
				.filter(ent -> ent instanceof LivingEntity && manager.getTraits(ent.getType()).contains(this))
				.map(ent -> new TraitEntity(ent))
				.collect(Collectors.toSet());

		double selfVariation = MoreMath.clamp(this.getVariation(entity), 0D, 1D);

		for(TraitEntity ent : entities){
			if(ent.hasTrait(this)){
				continue;
			}

			//do we really need this?
			if(!manager.getTraits(ent.getType()).contains(this)){
				return;
			}

			double chance = this.config.sicknessSpreadChance * selfVariation;

			if(chance <= 0D){
				return;
			}

			//*MoreMath.clamp(selfVariation, 0D, 1D)
			if(Math.random() <= chance){
				Evolution.getInstance().info(String.format("Entity %s(%s[%s]) is getting down with the sickness due to Entity %s(%s[%s])",
						ent.getName(),
						ent.getType(),
						ent.getUniqueId(),
						entity.getName(),
						entity.getType(),
						entity.getUniqueId()));
				ent.addTrait(this, TraitType.ACTIVE);
				this.applyTrait(ent, (this.getVariation(entity)+this.getVariation(ent))*0.5D);
			}
		}

	}

	@Override
	public TextComponent.Builder displayInfo(TraitEntity entity) {
		TextComponent.Builder newBuilder = super.displayInfo(entity);
		double selfVariation = MoreMath.clamp(this.getVariation(entity), 0D, 1D);
		double chance = this.config.sicknessSpreadChance * selfVariation;

		if(chance > 0D) {
			newBuilder.append(Component.newline());
			newBuilder.append(Component.text("Spread chance:"));
			newBuilder.append(Component.space());
			newBuilder.append(Component.text(String.format("%1.5f", chance, chance > 0D ? Evolution.FAILURE_RED : Evolution.SUCCESS_GREEN)));
		}
		return newBuilder;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return entity != null && entity.hasTrait(this) ? (entity.getVariation(this) >= 0D ? "Sickly" : "Hardy") : "Sickly";
	}

	@Override
	public double getMaxVariation() {
		return 0.005;
	}

	@Override
	protected Class<SicklyTraitConfig> getConfigClass() {
		return SicklyTraitConfig.class;
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 1D;
	}
}

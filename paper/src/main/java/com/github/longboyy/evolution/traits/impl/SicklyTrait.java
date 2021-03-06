package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
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

public class SicklyTrait extends Trait {

	//The range in blocks to be used in the check for other animals near sick animals
	private double sicknessRange = 3D;
	// How often to check if an animal should now be sick
	private long sicknessInterval = 20L;

	private double sicknessChance = 0.001D;


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
		}, 0L, this.sicknessInterval);
	}

	// Oh, ah, ah, ah, ah
	private void getDownWithTheSickness(TraitEntity entity){
		List<Entity> entityList = entity.entity.getNearbyEntities(this.sicknessRange, this.sicknessRange, this.sicknessRange);
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

			double chance = this.sicknessChance * selfVariation;

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
		newBuilder.append(Component.newline());
		newBuilder.append(Component.text("Spread chance:"));
		newBuilder.append(Component.space());

		double selfVariation = MoreMath.clamp(this.getVariation(entity), 0D, 1D);
		double chance = this.sicknessChance * selfVariation;

		newBuilder.append(Component.text(String.format("%1.5f", chance, chance > 0D ? Evolution.FAILURE_RED : Evolution.SUCCESS_GREEN)));
		return newBuilder;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return "Sickly";
	}

	@Override
	public double getMaxVariation() {
		return 0.005;
	}

	@Override
	public void parseConfig(ConfigurationSection section) {
		super.parseConfig(section);
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 1D;
	}
}

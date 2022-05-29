package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.world.ChunkLoadEvent;
import org.checkerframework.checker.units.qual.C;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

import java.util.Arrays;
import java.util.HashSet;
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
		super("sickly", 1D, TraitCategory.ILLNESS, ImmutableSet.copyOf(new EntityType[]{
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
			ImmutableSet<LivingEntity> entities = Evolution.getInstance().getTraitManager().getEntitiesWith(this, TraitType.ACTIVE);
			for(LivingEntity entity : entities){
				this.getDownWithTheSickness(entity);
			}
		}, 0L, this.sicknessInterval);
	}

	// Oh, ah, ah, ah, ah
	private void getDownWithTheSickness(LivingEntity entity){
		List<Entity> entityList = entity.getNearbyEntities(this.sicknessRange, this.sicknessRange, this.sicknessRange);
		Set<LivingEntity> entities = entityList.stream()
				.filter(ent -> ent instanceof LivingEntity && manager.getTraitsByEntityType(ent.getType()).contains(this))
				.map(ent -> (LivingEntity)ent)
				.collect(Collectors.toSet());

		double selfVariation = MoreMath.clamp(this.getVariation(entity), 0D, 1D);

		for(LivingEntity ent : entities){
			if(manager.hasTrait(ent, this)){
				continue;
			}

			if(!manager.getTraitsByEntityType(ent.getType()).contains(this)){
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
				ImmutableSet<ITrait> entTraits = TraitType.ACTIVE.getTraitsOf(entity);
				Set<ITrait> newEntTraits;
				if(entTraits == null){
					newEntTraits = new HashSet<>();
				}else{
					newEntTraits = new HashSet<>(entTraits);
				}

				newEntTraits.add(this);
				manager.setActiveTraitsOf(ent, ImmutableSet.copyOf(newEntTraits));
				this.applyTrait(ent, (this.getVariation(entity)+this.getVariation(ent))*0.5D);
			}
		}

	}

	@Override
	public TextComponent.Builder displayInfo(LivingEntity entity) {
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
	public String getPrettyName() {
		return "Sickly";
	}

	@Override
	public double getMaxVariation() {
		return 0.005;
	}

	@Override
	public void parseConfig(ConfigurationSection section) {
	}
}

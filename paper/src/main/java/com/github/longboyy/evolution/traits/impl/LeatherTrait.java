package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.listeners.TraitListener;
import com.github.longboyy.evolution.traits.Trait;
import com.github.longboyy.evolution.traits.TraitCategory;
import com.github.longboyy.evolution.traits.TraitManager;
import com.github.longboyy.evolution.traits.TraitType;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.objecthunter.exp4j.Expression;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

public class LeatherTrait extends Trait {

	private double minValue = 1D;
	private double maxValue = 5D;
	private ItemStack leatherItem = new ItemStack(Material.LEATHER, 1);
	private Expression variationExpression = TraitUtils.createVariationExpression("(log(1+x)/log(2))^0.7");

	private final TraitManager manager;

	public LeatherTrait() {
		super("leather", 1D, TraitCategory.HUSBANDRY, ImmutableSet.copyOf(new EntityType[]{
				EntityType.COW,
				EntityType.MUSHROOM_COW,
				EntityType.PIG,
				EntityType.SHEEP,
				EntityType.HORSE,
				EntityType.MULE,
				EntityType.DONKEY
		}));

		this.manager = Evolution.getInstance().getTraitManager();

		TraitListener listener = this.manager.getListener();
		listener.registerEvent(EntityDeathEvent.class, _event -> {
			EntityDeathEvent event = (EntityDeathEvent) _event;

			LivingEntity entity = event.getEntity();

			if(!this.manager.hasTrait(entity, this, TraitType.ACTIVE)){
				return;
			}

			ItemStack item = new ItemStack(this.leatherItem);
			item.setAmount(Math.toIntExact(Math.round(MoreMath.clamp(this.maxValue * this.getMultiplier(entity), this.minValue, this.maxValue))));
			ItemMap dropMap = TraitUtils.addItem(new ItemMap(event.getDrops()), item);

			event.getDrops().clear();
			event.getDrops().addAll(dropMap.getItemStackRepresentation());
			//dropMap.getItemStackRepresentation().forEach(event.getDrops()::add);
		});
	}

	@Override
	public TextComponent.Builder displayInfo(LivingEntity entity) {
		TextComponent.Builder newBuilder = super.displayInfo(entity);
		double realAmount = MoreMath.clamp(this.maxValue * this.getMultiplier(entity), this.minValue, this.maxValue);
		int amount = Math.toIntExact(Math.round(realAmount));
		newBuilder.append(Component.newline());
		newBuilder.append(Component.text("Dropped Leather:"));
		newBuilder.append(Component.space());
		newBuilder.append(Component.text(String.format("%1.5f [~%s]", realAmount, amount), realAmount >= 0 ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED));
		return newBuilder;
	}

	@Override
	public String getPrettyName() {
		return "Leather";
	}

	@Override
	public double getMaxVariation() {
		return 0.005D;
	}

	@Override
	public void parseConfig(ConfigurationSection section) {

	}

	private double getMultiplier(LivingEntity entity){
		double variation = this.getVariation(entity);
		if(variation > 0D){
			return this.variationExpression.setVariable("x", variation).evaluate();
		}else{
			return 0D;
		}
	}
}

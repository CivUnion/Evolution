package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
import com.github.longboyy.evolution.util.TraitUtils;
import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.objecthunter.exp4j.Expression;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

public class BoneTrait extends Trait {

	private double minValue = 1D;
	private double maxValue = 5D;
	private ItemStack boneItem = new ItemStack(Material.BONE, 1);
	private Expression variationExpression = TraitUtils.createVariationExpression("(log(1+x)/log(2))^0.7");

	private final TraitManager manager;

	public BoneTrait() {
		super("bones", TraitCategory.HUSBANDRY, ImmutableSet.copyOf(new EntityType[]{
			EntityType.COW,
			EntityType.SHEEP,
			EntityType.PIG,
			EntityType.HORSE,
			EntityType.MULE,
			EntityType.DONKEY,
			EntityType.GOAT,
			EntityType.HOGLIN,
			EntityType.LLAMA,
			EntityType.TRADER_LLAMA
		}));

		this.manager = Evolution.getInstance().getTraitManager();

		/*
		TraitListener listener = this.manager.getListener();
		listener.registerEvent(EntityDeathEvent.class, _event -> {
			EntityDeathEvent event = (EntityDeathEvent) _event;

			TraitEntity entity = new TraitEntity(event.getEntity());

			if(!entity.hasTrait(this, TraitType.ACTIVE)){
				return;
			}

			ItemStack item = new ItemStack(this.boneItem);
			item.setAmount(Math.toIntExact(Math.round(MoreMath.clamp(this.maxValue * this.getMultiplier(entity), this.minValue, this.maxValue))));
			ItemMap dropMap = TraitUtils.addItem(new ItemMap(event.getDrops()), item);

			event.getDrops().clear();
			event.getDrops().addAll(dropMap.getItemStackRepresentation());
			//dropMap.getItemStackRepresentation().forEach(event.getDrops()::add);
		});
		 */
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(EntityDeathEvent event){
		TraitEntity entity = new TraitEntity(event.getEntity());

		if(!entity.hasTrait(this, TraitType.ACTIVE)){
			return;
		}

		ItemStack item = new ItemStack(this.boneItem);
		item.setAmount(Math.toIntExact(Math.round(MoreMath.clamp(this.maxValue * this.getMultiplier(entity), this.minValue, this.maxValue))));
		ItemMap dropMap = TraitUtils.addItem(new ItemMap(event.getDrops()), item);

		event.getDrops().clear();
		event.getDrops().addAll(dropMap.getItemStackRepresentation());
	}

	private double getMultiplier(TraitEntity entity){
		SicklyTrait sicklyTrait = this.manager.getTrait(SicklyTrait.class);
		if(sicklyTrait != null){

		}

		double variation = this.getVariation(entity);
		if(variation > 0D){
			return this.variationExpression.setVariable("x", variation).evaluate();
		}else{
			return 0D;
		}
	}

	@Override
	public TextComponent.Builder displayInfo(TraitEntity entity) {
		TextComponent.Builder newBuilder = super.displayInfo(entity);
		double realAmount = MoreMath.clamp(this.maxValue * this.getMultiplier(entity), this.minValue, this.maxValue);
		int amount = Math.toIntExact(Math.round(realAmount));
		newBuilder.append(Component.newline());
		newBuilder.append(Component.text("Dropped Bones:"));
		newBuilder.append(Component.space());
		newBuilder.append(Component.text(String.format("%1.5f [~%s]", realAmount, amount), realAmount >= 0 ? Evolution.SUCCESS_GREEN : Evolution.FAILURE_RED));
		return newBuilder;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return "Bones";
	}

	@Override
	public double getMaxVariation() {
		return 0.005D;
	}

	@Override
	public void parseConfig(ConfigurationSection section) {
		super.parseConfig(section);
		if(section != null){
			String exp = section.getString("expression", "(log(1+x)/log(2))^0.7");
			this.variationExpression = TraitUtils.createVariationExpression(exp);
		}
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return 1D;
	}
}

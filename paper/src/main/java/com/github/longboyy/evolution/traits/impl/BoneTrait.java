package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.Evolution;
import com.github.longboyy.evolution.traits.*;
import com.github.longboyy.evolution.traits.configs.ExpressionTraitConfig;
import com.github.longboyy.evolution.util.TraitEntityDropManager;
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
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.utilities.MoreMath;

import java.util.List;

public class BoneTrait extends Trait<BoneTrait.BoneTraitConfig> {

	public static class BoneTraitConfig extends ExpressionTraitConfig {
		protected double minDrop = 1D;
		protected double maxDrop = 5D;
		protected ItemStack boneItem = new ItemStack(Material.BONE, 1);
		//protected Expression variationExpression = TraitUtils.createVariationExpression("(log(1+x)/log(2))^0.7");

		public BoneTraitConfig(){

		}

		@Override
		public void parse(ConfigurationSection section) {
			super.parse(section);
			this.minDrop = section.getDouble("minDrop", 1D);
			this.maxDrop = section.getDouble("maxDrop", 1D);
			if(section.isConfigurationSection("item")){
				ItemMap map = ConfigHelper.parseItemMapDirectly(section.getConfigurationSection("item"));
				if(map != null){
					List<ItemStack> stacks = map.getItemStackRepresentation();
					if(stacks.size() > 0){
						this.boneItem = stacks.get(0);
						//CivModCorePlugin.getInstance().get
					}
				}
			}

			//String exp = section.getString("expression", "(log(1+x)/log(2))^0.7");
			//this.variationExpression = TraitUtils.createVariationExpression(exp);
		}
	}

	/*
	private double minValue = 1D;
	private double maxValue = 5D;
	private ItemStack boneItem = new ItemStack(Material.BONE, 1);
	private Expression variationExpression = TraitUtils.createVariationExpression("(log(1+x)/log(2))^0.7");
	 */

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

		TraitEntityDropManager.getInstance().registerDrop(this, (entity, map) -> {
			int dropAmount = Math.toIntExact(Math.round(this.config.minDrop));

			if(entity.hasTrait(this, TraitType.ACTIVE)){
				dropAmount = Math.toIntExact(Math.round(MoreMath.clamp(this.config.maxDrop * this.getMultiplier(entity), this.config.minDrop, this.config.maxDrop)));
			}

			ItemStack item = this.config.boneItem.clone();
			item.setAmount(dropAmount);

			map.removeItemStackCompletely(new ItemStack(Material.BONE));
			map.addItemStack(item);
		});
	}

	private double getMultiplier(TraitEntity entity){
		SicklyTrait sicklyTrait = this.manager.getTrait(SicklyTrait.class);
		if(sicklyTrait != null && entity.hasTrait(sicklyTrait, TraitType.ACTIVE)){
			double sicklyVariation = sicklyTrait.getVariation(entity);
		}

		double variation = this.getVariation(entity);
		if(variation > 0D){
			return this.config.getPositiveExpression().setVariable("x", variation).evaluate();
		}else{
			return 0D;
		}
	}

	@Override
	public TextComponent.Builder displayInfo(TraitEntity entity) {
		TextComponent.Builder newBuilder = super.displayInfo(entity);
		double realAmount = MoreMath.clamp(this.config.maxDrop * this.getMultiplier(entity), this.config.minDrop, this.config.maxDrop);
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

	/*
	@Override
	public void parseConfig(ConfigurationSection section) {
		super.parseConfig(section);
		if(section != null){
			String exp = section.getString("expression", "(log(1+x)/log(2))^0.7");
			this.variationExpression = TraitUtils.createVariationExpression(exp);
		}
	}
	 */

	@Override
	protected Class<BoneTraitConfig> getConfigClass() {
		return BoneTraitConfig.class;
	}

	@Override
	public double getWeight(TraitEntity entity) {
		return this.config.getWeight();
	}
}

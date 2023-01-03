package com.github.longboyy.evolution.traits.impl;

import com.github.longboyy.evolution.traits.ListenerTrait;
import com.github.longboyy.evolution.traits.TraitCategory;
import com.github.longboyy.evolution.traits.configs.TraitConfig;
import com.github.longboyy.evolution.traits.TraitEntity;
import com.google.common.collect.ImmutableSet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

public class WoolTrait extends ListenerTrait<WoolTrait.WoolTraitConfig> {

	public class WoolTraitConfig extends TraitConfig {

		@Override
		public void parse(ConfigurationSection section) {

		}

	}
	public WoolTrait() {
		super("wool", TraitCategory.HUSBANDRY, ImmutableSet.copyOf(new EntityType[]{
				EntityType.SHEEP
		}));
	}

	@Override
	protected Class<WoolTraitConfig> getConfigClass() {
		return WoolTraitConfig.class;
	}

	@Override
	public String getPrettyName(TraitEntity entity) {
		return "Wool";
	}
}

package com.github.longboyy.evolution.util.pdc;

import com.github.longboyy.evolution.Evolution;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.pdc.PersistentMapDataType;

import java.util.HashMap;
import java.util.Map;

public class StringLongMap extends PersistentMapDataType<String, Long> {

	public StringLongMap() {
		super(new StringLongKeyEncoder(), PersistentDataType.LONG);
	}

	@NotNull
	@Override
	protected Map<String, Long> newMap(int initialSize) {
		return new HashMap<>(initialSize);
	}

}


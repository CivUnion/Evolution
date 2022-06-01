package com.github.longboyy.evolution.util.pdc;

import com.github.longboyy.evolution.Evolution;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.pdc.PersistentMapDataType;

import java.util.HashMap;
import java.util.Map;

public class StringDoubleMap extends PersistentMapDataType<String,Double> {

	public static final StringDoubleMap STRING_DOUBLE_MAP = new StringDoubleMap();

	public StringDoubleMap(){
		super(new StringDoubleKeyEncoder(), PersistentDataType.DOUBLE);
	}

	@NotNull
	@Override
	protected Map<String, Double> newMap(int initialSize) {
		return new HashMap<>(initialSize);
	}
}

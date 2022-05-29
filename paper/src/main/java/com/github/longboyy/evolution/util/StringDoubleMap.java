package com.github.longboyy.evolution.util;

import com.github.longboyy.evolution.Evolution;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.pdc.PersistentMapDataType;

import java.util.HashMap;
import java.util.Map;

public class StringDoubleMap extends PersistentMapDataType<String,Double> {

	//public static final StringDoubleMap STRING_DOUBLE_MAP = new StringDoubleMap(new StringDoubleKeyEncoder(Evolution.getInstance()), new StringDoubleValueEncoder());
	private static StringDoubleMap type;

	public static StringDoubleMap getType(){
		return type;
	}

	public static void initType(Evolution plugin){
		type = new StringDoubleMap(plugin);
	}

	public StringDoubleMap(Evolution plugin){
		super(new StringDoubleKeyEncoder(plugin), PersistentDataType.DOUBLE);
	}

	@NotNull
	@Override
	protected Map<String, Double> newMap(int initialSize) {
		return new HashMap<>(initialSize);
	}
}

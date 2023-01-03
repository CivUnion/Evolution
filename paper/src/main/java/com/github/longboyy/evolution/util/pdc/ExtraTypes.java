package com.github.longboyy.evolution.util.pdc;

import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.pdc.PersistentMapDataType;

import java.util.HashMap;
import java.util.Map;

public class ExtraTypes {

	public static final StringDoubleMap STRING_DOUBLE_MAP = new StringDoubleMap();
	public static final StringLongMap STRING_LONG_MAP = new StringLongMap();

	static class StringDoubleMap extends PersistentMapDataType<String,Double> {
		public StringDoubleMap(){
			super(new StringDoubleKeyEncoder(), PersistentDataType.DOUBLE);
		}

		@NotNull
		@Override
		protected Map<String, Double> newMap(int initialSize) {
			return new HashMap<>(initialSize);
		}
	}

	static class StringLongMap extends PersistentMapDataType<String, Long> {

		public StringLongMap() {
			super(new StringLongKeyEncoder(), PersistentDataType.LONG);
		}

		@NotNull
		@Override
		protected Map<String, Long> newMap(int initialSize) {
			return new HashMap<>(initialSize);
		}
	}

}

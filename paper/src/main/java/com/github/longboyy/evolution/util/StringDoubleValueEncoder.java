package com.github.longboyy.evolution.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class StringDoubleValueEncoder implements PersistentDataType<Double, Double> {

	@Override
	public @NotNull Class<Double> getPrimitiveType() {
		return Double.class;
	}

	@Override
	public @NotNull Class<Double> getComplexType() {
		return Double.class;
	}

	@Override
	public @NotNull Double toPrimitive(@NotNull Double complex, @NotNull PersistentDataAdapterContext context) {
		return complex;
	}

	@Override
	public @NotNull Double fromPrimitive(@NotNull Double primitive, @NotNull PersistentDataAdapterContext context) {
		return primitive;
	}
}

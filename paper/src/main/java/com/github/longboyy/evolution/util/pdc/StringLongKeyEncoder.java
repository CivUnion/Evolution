package com.github.longboyy.evolution.util.pdc;

import com.github.longboyy.evolution.Evolution;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class StringLongKeyEncoder implements PersistentDataType<NamespacedKey, String> {

	@Override
	public @NotNull Class<NamespacedKey> getPrimitiveType() {
		return NamespacedKey.class;
	}

	@Override
	public @NotNull Class<String> getComplexType() {
		return String.class;
	}

	@Override
	public @NotNull NamespacedKey toPrimitive(@NotNull String complex, @NotNull PersistentDataAdapterContext context) {
		return NamespacedKey.fromString(complex, Evolution.getInstance());
	}

	@Override
	public @NotNull String fromPrimitive(@NotNull NamespacedKey primitive, @NotNull PersistentDataAdapterContext context) {
		return primitive.getKey();
	}
}

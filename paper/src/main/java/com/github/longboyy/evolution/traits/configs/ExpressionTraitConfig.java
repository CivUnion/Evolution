package com.github.longboyy.evolution.traits.configs;

import com.github.longboyy.evolution.util.TraitUtils;
import net.objecthunter.exp4j.Expression;
import org.bukkit.configuration.ConfigurationSection;

public class ExpressionTraitConfig extends TraitConfig {

	protected static final String DEFAULT_POSITIVE_EXPRESSION = "(log(1+x)/log(2))^0.7";
	protected static final String DEFAULT_NEGATIVE_EXPRESSION = "-(log(1-x)/log(2))^0.7";
	protected String positiveExpression = DEFAULT_POSITIVE_EXPRESSION;
	protected String negativeExpression = DEFAULT_NEGATIVE_EXPRESSION;

	@Override
	public void parse(ConfigurationSection section) {
		this.positiveExpression = section != null ? section.getString("positiveExpression", DEFAULT_POSITIVE_EXPRESSION) : DEFAULT_POSITIVE_EXPRESSION;
		this.negativeExpression = section != null ? section.getString("negativeExpression", DEFAULT_NEGATIVE_EXPRESSION) : DEFAULT_NEGATIVE_EXPRESSION;
	}

	public Expression getPositiveExpression(){
		return TraitUtils.createVariationExpression(positiveExpression);
	}

	public Expression getNegativeExpression(){
		return TraitUtils.createVariationExpression(negativeExpression);
	}

}

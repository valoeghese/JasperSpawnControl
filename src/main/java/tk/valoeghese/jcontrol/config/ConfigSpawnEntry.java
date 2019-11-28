package tk.valoeghese.jcontrol.config;

import tk.valoeghese.jcontrol.annotation.Required;
import tk.valoeghese.jcontrol.config.ConditionRegistry.ConditionConfig;

public class ConfigSpawnEntry {
	@Required
	public String condition = null;
	
	public ConditionConfig config = new ConditionConfig();
	
	public boolean defaultConditions = true;
	public float chance = 0.03f;
	public int minCount = 1;
	public int maxCount = 1;
}

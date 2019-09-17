package tk.valoeghese.jcontrol.config;

import tk.valoeghese.jcontrol.annotation.Required;

public class JControlConfig {
	
	@Required
	public String entity = null;
	
	@Required
	public ConfigSpawnEntry[] spawns = null;
	
}

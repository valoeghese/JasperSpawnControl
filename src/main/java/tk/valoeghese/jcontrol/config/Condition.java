package tk.valoeghese.jcontrol.config;

import net.minecraft.world.IWorld;
import tk.valoeghese.jcontrol.config.ConditionRegistry.ConditionConfig;

public interface Condition {
	public boolean test(IWorld world, int x, int y, int z, ConditionConfig config);
}

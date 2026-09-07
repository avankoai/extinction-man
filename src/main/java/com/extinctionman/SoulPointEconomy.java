package com.extinctionman;

import java.util.Collection;

final class SoulPointEconomy
{
	private SoulPointEconomy() {}

	static int earnedEnergy(Collection<String> completedSpecies)
	{
		if (completedSpecies == null) return 0;
		return completedSpecies.stream().mapToInt(name -> BossRegistry.isBoss(name) ? 10 : 1).sum();
	}

	static int availableEnergy(Collection<String> completedSpecies, int energySpent)
	{
		return Math.max(0, earnedEnergy(completedSpecies) - Math.max(0, energySpent));
	}

}

package com.extinctionman;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

final class KillCreditTracker<T>
{
	enum DeathClaim
	{
		CREDITED,
		CONTESTED,
		NOT_OURS
	}

	private final Map<T, Integer> lastOwnDamage = new IdentityHashMap<>();
	private final Set<T> contestedTargets = Collections.newSetFromMap(new IdentityHashMap<>());

	void markOwnDamage(T target, int tick)
	{
		lastOwnDamage.put(target, tick);
	}

	void markOtherDamage(T target)
	{
		contestedTargets.add(target);
	}

	DeathClaim claimDeath(T target, int tick)
	{
		return claimDeath(target, tick, false);
	}

	DeathClaim claimDeath(T target, int tick, boolean allowSharedCredit)
	{
		Integer damageTick = lastOwnDamage.remove(target);
		boolean contested = contestedTargets.remove(target);
		if (damageTick == null)
		{
			return DeathClaim.NOT_OURS;
		}
		return contested && !allowSharedCredit ? DeathClaim.CONTESTED : DeathClaim.CREDITED;
	}

	void forget(T target)
	{
		lastOwnDamage.remove(target);
		contestedTargets.remove(target);
	}

	void clear()
	{
		lastOwnDamage.clear();
		contestedTargets.clear();
	}
}

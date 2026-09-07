package com.extinctionman;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

/** Correlates server-confirmed NPC loot with a matching inventory increase or ground spawn. */
@Singleton
final class DirectSoulLootTracker
{
	private final Map<Integer, Observation> observations = new HashMap<>();
	private final Map<Integer, GroundObservation> recentGround = new HashMap<>();

	void observe(String source, int itemId, int currentTick)
	{
		GroundObservation ground = recentGround.get(itemId);
		boolean groundObserved = ground != null && source.equals(ground.sourceName)
			&& currentTick - ground.tick >= 0 && currentTick - ground.tick <= 1;
		observations.put(itemId, new Observation(source, currentTick, groundObserved));
	}

	void markGround(String source, int itemId, int currentTick)
	{
		recentGround.put(itemId, new GroundObservation(source, currentTick));
		Observation observation = observations.get(itemId);
		if (observation != null && source.equals(observation.sourceName))
		{
			observation.groundObserved = true;
		}
	}

	String claimInventoryIncrease(int itemId, int currentTick)
	{
		Observation observation = observations.get(itemId);
		if (observation == null || observation.groundObserved
			|| currentTick < observation.observedAtTick
			|| currentTick > observation.observedAtTick + 2) return null;
		observations.remove(itemId);
		return observation.sourceName;
	}

	void expire(int currentTick)
	{
		observations.entrySet().removeIf(entry -> currentTick > entry.getValue().observedAtTick + 2);
		recentGround.entrySet().removeIf(entry -> currentTick > entry.getValue().tick + 2);
	}

	void clear()
	{
		observations.clear();
		recentGround.clear();
	}

	private static final class Observation
	{
		private final String sourceName;
		private final int observedAtTick;
		private boolean groundObserved;
		private Observation(String sourceName, int observedAtTick, boolean groundObserved)
		{
			this.sourceName = sourceName;
			this.observedAtTick = observedAtTick;
			this.groundObserved = groundObserved;
		}
	}

	private static final class GroundObservation
	{
		private final String sourceName;
		private final int tick;
		private GroundObservation(String sourceName, int tick)
		{
			this.sourceName = sourceName;
			this.tick = tick;
		}
	}
}

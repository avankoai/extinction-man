package com.extinctionman;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.IdentityHashMap;
import javax.inject.Singleton;
import net.runelite.api.NPC;

@Singleton
final class ExtinctGhostTracker
{
	// Accessed on the client/render thread. NPC identity must survive index reuse.
	private final Map<NPC, SoulExceptionType> ghosts = new IdentityHashMap<>();
	private final Collection<NPC> view = Collections.unmodifiableSet(ghosts.keySet());

	void replace(Map<NPC, SoulExceptionType> npcs)
	{
		ghosts.clear();
		ghosts.putAll(npcs);
	}

	Collection<NPC> active()
	{
		return view;
	}

	void update(NPC npc, boolean ghost)
	{
		update(npc, ghost, SoulExceptionType.UNBOUND);
	}

	void update(NPC npc, boolean ghost, boolean golden)
	{
		update(npc, ghost, golden ? SoulExceptionType.UNBOUND : SoulExceptionType.SLAYER);
	}

	void update(NPC npc, boolean ghost, SoulExceptionType type)
	{
		if (ghost) ghosts.put(npc, type);
		else ghosts.remove(npc);
	}

	boolean isGolden(NPC npc)
	{
		return ghosts.get(npc) == SoulExceptionType.UNBOUND;
	}

	SoulExceptionType getType(NPC npc)
	{
		return ghosts.get(npc);
	}

	boolean contains(NPC npc)
	{
		return ghosts.containsKey(npc);
	}

	void remove(NPC npc)
	{
		ghosts.remove(npc);
	}

	void clear()
	{
		ghosts.clear();
	}
}

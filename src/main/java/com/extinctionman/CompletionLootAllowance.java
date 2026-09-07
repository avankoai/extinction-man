package com.extinctionman;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.inject.Singleton;
import net.runelite.api.NPC;

/** Allows all loot from the kill which first completed an extinction. */
@Singleton
final class CompletionLootAllowance
{
	private final Set<NPC> allowed = Collections.newSetFromMap(new IdentityHashMap<>());

	void mark(NPC npc)
	{
		if (npc != null) allowed.add(npc);
	}

	boolean consume(NPC npc)
	{
		return npc != null && allowed.remove(npc);
	}

	void forget(NPC npc) { allowed.remove(npc); }
	void clear() { allowed.clear(); }
}

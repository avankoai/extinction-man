package com.extinctionman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.inject.Singleton;
import net.runelite.api.NPC;

@Singleton
final class ExtinctGhostTracker
{
	private List<NPC> ghosts = Collections.emptyList();

	void replace(Collection<NPC> npcs)
	{
		ghosts = Collections.unmodifiableList(new ArrayList<>(npcs));
	}

	List<NPC> active()
	{
		return ghosts;
	}

	boolean contains(NPC npc)
	{
		return ghosts.contains(npc);
	}

	void remove(NPC npc)
	{
		if (!ghosts.contains(npc)) return;
		List<NPC> updated = new ArrayList<>(ghosts);
		updated.remove(npc);
		ghosts = Collections.unmodifiableList(updated);
	}

	void clear()
	{
		ghosts = Collections.emptyList();
	}
}

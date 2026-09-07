package com.extinctionman;

import java.lang.reflect.Proxy;
import java.util.IdentityHashMap;
import java.util.Map;
import net.runelite.api.NPC;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExtinctGhostTrackerTest
{
	@Test
	public void updatesOnlyTheSpawnedNpcAndRemovesItOnDespawn()
	{
		ExtinctGhostTracker tracker = new ExtinctGhostTracker();
		NPC first = npc();
		NPC second = npc();
		tracker.update(first, true);
		tracker.update(second, true, false);
		tracker.update(first, true);
		assertEquals(2, tracker.active().size());
		assertTrue(tracker.isGolden(first));
		assertFalse(tracker.isGolden(second));
		assertEquals(SoulExceptionType.UNBOUND, tracker.getType(first));
		assertEquals(SoulExceptionType.SLAYER, tracker.getType(second));
		tracker.update(first, false);
		assertFalse(tracker.contains(first));
		assertTrue(tracker.contains(second));
		tracker.remove(second);
		assertTrue(tracker.active().isEmpty());
		assertFalse(tracker.contains(npc()));
	}

	@Test
	public void changingExceptionReplacesPreviousMembership()
	{
		ExtinctGhostTracker tracker = new ExtinctGhostTracker();
		NPC old = npc();
		NPC replacement = npc();
		tracker.update(old, true);
		Map<NPC, SoulExceptionType> replacements = new IdentityHashMap<>();
		replacements.put(replacement, SoulExceptionType.SLAYER);
		tracker.replace(replacements);
		assertFalse(tracker.contains(old));
		assertTrue(tracker.contains(replacement));
		assertFalse(tracker.isGolden(replacement));
		tracker.clear();
		assertTrue(tracker.active().isEmpty());
	}

	private static NPC npc()
	{
		return (NPC) Proxy.newProxyInstance(NPC.class.getClassLoader(), new Class<?>[]{NPC.class},
			(proxy, method, args) -> { throw new AssertionError("NPC value equality must not be used"); });
	}
}

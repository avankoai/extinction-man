package com.extinctionman;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPCComposition;
import net.runelite.client.util.Text;

@Singleton
final class BestiaryRegistry
{
	private static final int NPC_CONFIG_ARCHIVE = 9;
	private final Set<String> attackableNames = createInitialNames();
	private final Set<String> excludedSpecialNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	private int cursor;
	private boolean complete;
	private int[] npcIds;

	synchronized boolean scanBatch(Client client, int batchSize)
	{
		if (complete)
		{
			return false;
		}
		if (npcIds == null)
		{
			if (client.getIndexConfig() == null)
			{
				return false;
			}
			npcIds = client.getIndexConfig().getFileIds(NPC_CONFIG_ARCHIVE);
			if (npcIds == null)
			{
				return false;
			}
			Arrays.sort(npcIds);
		}

		int end = Math.min(npcIds.length, cursor + batchSize);
		for (; cursor < end; cursor++)
		{
			NPCComposition composition = client.getNpcDefinition(npcIds[cursor]);
			if (composition != null && isAttackable(
				composition.getName(), composition.getActions(), composition.getCombatLevel()))
			{
				String cleanName = Text.removeTags(composition.getName()).trim();
				if (!cleanName.isEmpty() && !SpecialEncounterRules.alwaysIgnoredTechnicalNpc(cleanName))
				{
					if (!excludedSpecialNames.contains(cleanName)) attackableNames.add(cleanName);
				}
			}
		}

		if (cursor >= npcIds.length)
		{
			complete = true;
			return true;
		}
		return false;
	}

	synchronized Set<String> getAttackableNames()
	{
		return Collections.unmodifiableSet(new TreeSet<>(attackableNames));
	}

	synchronized boolean isComplete()
	{
		return complete;
	}

	synchronized boolean excludeSpecialNpc(String npcName)
	{
		if (npcName == null || npcName.trim().isEmpty()
			|| SpecialEncounterRules.isRaidName(npcName)) return false;
		boolean changed = excludedSpecialNames.add(npcName);
		attackableNames.remove(npcName);
		return changed;
	}

	synchronized boolean isExcludedSpecialNpc(String npcName)
	{
		return npcName != null && excludedSpecialNames.contains(npcName);
	}

	synchronized String findExactName(String requestedName)
	{
		if (requestedName == null) return null;
		return attackableNames.stream()
			.filter(name -> name.equalsIgnoreCase(requestedName.trim()))
			.findFirst().orElse(null);
	}

	static boolean isAttackable(String name, String[] actions, int combatLevel)
	{
		if (name == null || name.trim().isEmpty() || "null".equalsIgnoreCase(name.trim())
			|| actions == null || combatLevel <= 0)
		{
			return false;
		}
		return Arrays.stream(actions).anyMatch(action -> "Attack".equalsIgnoreCase(action));
	}

	private static Set<String> createInitialNames()
	{
		Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		names.addAll(SpecialEncounterRules.raidNames());
		return names;
	}
}

package com.extinctionman;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPCComposition;
import net.runelite.client.util.Text;
import net.runelite.client.config.ConfigManager;

@Singleton
final class BestiaryRegistry
{
	private static final int NPC_CONFIG_ARCHIVE = 9;
	private static final String CATALOG_KEY = "monsterCatalogV1";
	private static final String NEW_MONSTERS_KEY = "newMonstersV1";
	private final ConfigManager configManager;
	private final Set<String> attackableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	private final Set<String> scannedAttackableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	private final Set<String> newNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	private final Set<String> excludedSpecialNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	private final Set<String> savedNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

	synchronized void prioritizeProgress(java.util.Collection<SoulProgress> progress)
	{
		savedNames.clear();
		for (SoulProgress entry : progress)
		{
			String name = entry.getNpcName();
			if (entry.getSouls() > 0 && !excludedSpecialNames.contains(name)
				&& !SpecialEncounterRules.alwaysIgnoredTechnicalNpc(name)) savedNames.add(name);
		}
	}
	private int cursor;
	private boolean complete;
	private int[] npcIds;

	BestiaryRegistry()
	{
		this(null);
	}

	@Inject
	BestiaryRegistry(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

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
		boolean changed = false;
		for (; cursor < end; cursor++)
		{
			NPCComposition composition = client.getNpcDefinition(npcIds[cursor]);
			if (composition != null && isAttackable(
				composition.getName(), composition.getActions(), composition.getCombatLevel()))
			{
				String cleanName = Text.removeTags(composition.getName()).trim();
				if (!cleanName.isEmpty() && !SpecialEncounterRules.alwaysIgnoredTechnicalNpc(cleanName))
				{
					scannedAttackableNames.add(cleanName);
					if (!excludedSpecialNames.contains(cleanName)) changed |= attackableNames.add(cleanName);
				}
			}
		}

		if (cursor >= npcIds.length)
		{
			attackableNames.addAll(SpecialEncounterRules.raidNames());
			scannedAttackableNames.addAll(SpecialEncounterRules.raidNames());
			updateVersionCatalogue();
			complete = true;
			return true;
		}
		return changed;
	}

	synchronized boolean isNew(String npcName)
	{
		return npcName != null && newNames.contains(npcName);
	}

	synchronized void markValidated(String npcName)
	{
		if (npcName != null && newNames.remove(npcName) && configManager != null)
		{
			configManager.setConfiguration(ExtinctionManConfig.GROUP, NEW_MONSTERS_KEY,
				MonsterCatalogCodec.encode(newNames));
		}
	}

	private void updateVersionCatalogue()
	{
		if (configManager == null) return;
		String stored = configManager.getConfiguration(ExtinctionManConfig.GROUP, CATALOG_KEY);
		if (stored == null)
		{
			configManager.setConfiguration(ExtinctionManConfig.GROUP, CATALOG_KEY,
				MonsterCatalogCodec.encode(scannedAttackableNames));
			return;
		}
		Set<String> previous = MonsterCatalogCodec.decode(stored);
		newNames.addAll(MonsterCatalogCodec.decode(
			configManager.getConfiguration(ExtinctionManConfig.GROUP, NEW_MONSTERS_KEY)));
		for (String name : scannedAttackableNames)
		{
			if (!previous.contains(name)) newNames.add(name);
		}
		configManager.setConfiguration(ExtinctionManConfig.GROUP, CATALOG_KEY,
			MonsterCatalogCodec.encode(scannedAttackableNames));
		configManager.setConfiguration(ExtinctionManConfig.GROUP, NEW_MONSTERS_KEY,
			MonsterCatalogCodec.encode(newNames));
	}

	synchronized Set<String> getAttackableNames()
	{
		Set<String> names = new TreeSet<>(attackableNames);
		names.addAll(savedNames);
		return Collections.unmodifiableSet(names);
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
		savedNames.remove(npcName);
		return changed;
	}

	synchronized boolean isExcludedSpecialNpc(String npcName)
	{
		return npcName != null && excludedSpecialNames.contains(npcName);
	}

	synchronized String findExactName(String requestedName)
	{
		if (requestedName == null) return null;
		return getAttackableNames().stream()
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

}

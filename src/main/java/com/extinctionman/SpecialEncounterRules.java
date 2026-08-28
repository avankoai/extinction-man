package com.extinctionman;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

final class SpecialEncounterRules
{
	static final String CHAMBERS_OF_XERIC = "Chambers of Xeric";
	static final String THEATRE_OF_BLOOD = "Theatre of Blood";
	static final String TOMBS_OF_AMASCUT = "Tombs of Amascut";
	static final String TZTOK_JAD = "TzTok-Jad";
	static final String TZKAL_ZUK = "TzKal-Zuk";
	static final int FIGHT_CAVES_REGION = 9551;
	static final int INFERNO_REGION = 9043;
	static final int FORTIS_COLOSSEUM_REGION = 7216;
	static final int GAUNTLET_REGION = 7512;
	static final int CORRUPTED_GAUNTLET_REGION = 7768;
	static final int NIGHTMARE_ZONE_REGION = 9033;
	private static final Set<Integer> GOD_WARS_DUNGEON_REGIONS = setOf(
		11346, 11347, 11601, 11602, 11603);
	private static final Set<Integer> SOUL_WARS_REGIONS = setOf(8493, 8749, 9005);
	private static final Set<Integer> BARBARIAN_ASSAULT_REGIONS = setOf(7508, 7509, 10322);
	private static final Set<String> GWD_GENERALS = setOfNames(
		"Kree'arra", "General Graardor", "Commander Zilyana", "K'ril Tsutsaroth", "Nex");
	private static final Set<String> TECHNICAL_NPCS = setOfNames(
		"Respiratory system", "Enormous Tentacle", "Hueycoatl's tail",
		"Hueycoatl tail", "Abyssal portal");


	private static final Set<Integer> COX_REGIONS = setOf(
		12889, 13136, 13137, 13138, 13139, 13140, 13141, 13145,
		13393, 13394, 13395, 13396, 13397, 13401);
	private static final Set<Integer> TOB_REGIONS = setOf(
		12611, 12612, 12613, 12867, 12869, 13122, 13123, 13125, 13379, 14642);
	private static final Set<Integer> TOA_REGIONS = setOf(
		14160, 14162, 14164, 14674, 14676, 15184, 15186, 15188, 15696, 15698, 15700);
	private static final Set<String> RAID_NAMES = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
		CHAMBERS_OF_XERIC, THEATRE_OF_BLOOD, TOMBS_OF_AMASCUT)));

	private SpecialEncounterRules() {}

	static String raidAtRegion(int regionId)
	{
		if (COX_REGIONS.contains(regionId)) return CHAMBERS_OF_XERIC;
		if (TOB_REGIONS.contains(regionId)) return THEATRE_OF_BLOOD;
		if (TOA_REGIONS.contains(regionId)) return TOMBS_OF_AMASCUT;
		return null;
	}

	static boolean shouldIgnoreNpcKill(int regionId, String npcName)
	{
		if (raidAtRegion(regionId) != null) return true;
		if (regionId == FIGHT_CAVES_REGION) return !TZTOK_JAD.equals(npcName);
		if (regionId == INFERNO_REGION) return !TZKAL_ZUK.equals(npcName);
		if (regionId == FORTIS_COLOSSEUM_REGION) return !"Sol Heredit".equals(npcName);
		if (regionId == GAUNTLET_REGION) return !"Crystalline Hunllef".equals(npcName);
		if (regionId == CORRUPTED_GAUNTLET_REGION) return !"Corrupted Hunllef".equals(npcName);
		if (SOUL_WARS_REGIONS.contains(regionId)) return true;
		if (BARBARIAN_ASSAULT_REGIONS.contains(regionId)) return !"Penance Queen".equals(npcName);
		if (alwaysIgnoredTechnicalNpc(npcName)) return true;
		return false;
	}

	static boolean shouldExcludeEncounterNpc(int regionId, String npcName)
	{
		if (alwaysIgnoredTechnicalNpc(npcName)) return true;
		if (regionId == FORTIS_COLOSSEUM_REGION) return !"Sol Heredit".equals(npcName);
		if (regionId == GAUNTLET_REGION) return !"Crystalline Hunllef".equals(npcName);
		if (regionId == CORRUPTED_GAUNTLET_REGION) return !"Corrupted Hunllef".equals(npcName);
		if (SOUL_WARS_REGIONS.contains(regionId)) return true;
		if (BARBARIAN_ASSAULT_REGIONS.contains(regionId)) return !"Penance Queen".equals(npcName);
		return false;
	}

	static boolean shouldAlwaysRemainVisible(int regionId, String npcName)
	{
		if (regionId == FORTIS_COLOSSEUM_REGION) return !"Sol Heredit".equals(npcName);
		if (SOUL_WARS_REGIONS.contains(regionId)) return true;
		if (BARBARIAN_ASSAULT_REGIONS.contains(regionId)) return !"Penance Queen".equals(npcName);
		return alwaysIgnoredTechnicalNpc(npcName);
	}

	static boolean isGodWarsSupportNpc(int regionId, String npcName)
	{
		return GOD_WARS_DUNGEON_REGIONS.contains(regionId) && npcName != null
			&& !GWD_GENERALS.contains(npcName);
	}

	static String gauntletBossForRegion(int regionId)
	{
		if (regionId == GAUNTLET_REGION) return "Crystalline Hunllef";
		if (regionId == CORRUPTED_GAUNTLET_REGION) return "Corrupted Hunllef";
		return null;
	}

	static boolean alwaysIgnoredTechnicalNpc(String npcName)
	{
		return npcName != null && TECHNICAL_NPCS.contains(npcName);
	}

	static boolean isRaidName(String name) { return RAID_NAMES.contains(name); }
	static Set<String> raidNames() { return RAID_NAMES; }

	static String canonicalRaidBoss(String raidName, String npcName)
	{
		if (raidName == null || npcName == null) return null;
		String lower = npcName.toLowerCase(java.util.Locale.ENGLISH);
		if (CHAMBERS_OF_XERIC.equals(raidName))
		{
			if (lower.contains("great olm")) return "Great Olm";
			if (lower.contains("tekton")) return "Tekton";
			if (lower.contains("muttadile")) return "Muttadile";
			if (lower.equals("guardian")) return "Guardian";
			if (lower.contains("vespula")) return "Vespula";
			if (lower.contains("lizardman shaman")) return "Lizardman shaman";
			if (lower.contains("vasa nistirio")) return "Vasa Nistirio";
			if (lower.contains("vanguard")) return "Vanguard";
			if (lower.equals("deathly ranger")) return "Deathly ranger";
			if (lower.equals("deathly mage")) return "Deathly mage";
			if (lower.contains("ice demon")) return "Ice demon";
		}
		if (THEATRE_OF_BLOOD.equals(raidName))
		{
			if (lower.contains("maiden of sugadinti")) return "The Maiden of Sugadinti";
			if (lower.contains("pestilent bloat")) return "Pestilent Bloat";
			if (lower.contains("nylocas vasilias")) return "Nylocas Vasilias";
			if (lower.contains("sotetseg")) return "Sotetseg";
			if (lower.contains("xarpus")) return "Xarpus";
			if (lower.contains("verzik vitur")) return "Verzik Vitur";
		}
		if (TOMBS_OF_AMASCUT.equals(raidName))
		{
			if (lower.equals("ba-ba")) return "Ba-Ba";
			if (lower.equals("kephri")) return "Kephri";
			if (lower.equals("zebak")) return "Zebak";
			if (lower.equals("akkha")) return "Akkha";
			if (lower.contains("elidinis' warden")) return "Elidinis' Warden";
			if (lower.contains("tumeken's warden")) return "Tumeken's Warden";
		}
		return null;
	}

	private static Set<Integer> setOf(Integer... values)
	{
		return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
	}

	private static Set<String> setOfNames(String... values)
	{
		Set<String> names = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		names.addAll(Arrays.asList(values));
		return Collections.unmodifiableSet(names);
	}
}

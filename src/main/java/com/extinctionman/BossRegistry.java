package com.extinctionman;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Bestiary names belonging to an activity represented on the official OSRS boss Hiscores.
 * Component bosses use their in-game Bestiary name so filtering and permits remain exact.
 */
final class BossRegistry
{
	private static final Set<String> BOSSES = names(
		"Abyssal Sire", "Alchemical Hydra", "Amoxliatl", "Araxxor", "Artio",
		"Ahrim the Blighted", "Dharok the Wretched", "Guthan the Infested",
		"Karil the Tainted", "Torag the Corrupted", "Verac the Defiled",
		"Brutus", "Bryophyta", "Callisto", "Calvar'ion", "Cerberus",
		SpecialEncounterRules.CHAMBERS_OF_XERIC,
		"Chaos Elemental", "Chaos Fanatic", "Commander Zilyana", "Corporeal Beast",
		"Crazy archaeologist", "Dagannoth Prime", "Dagannoth Rex", "Dagannoth Supreme",
		"Deranged archaeologist", "Doom of Mokhaiotl",
		"Duke Sucellus", "General Graardor", "Giant Mole", "Dawn", "Dusk",
		"Hespori", "Kalphite Queen", "King Black Dragon", "Kraken", "Kree'arra",
		"K'ril Tsutsaroth", "Mad Angel", "Maggot King", "The Mimic", "Nex",
		"The Nightmare", "Phosani's Nightmare",
		"Obor", "Phantom Muspah", "Sarachnis", "Scorpia", "Scurrius",
		"Shellbane Gryphon", "Skotizo",
		"Sol Heredit", "Spindel", "Tempoross", "Crystalline Hunllef",
		"Corrupted Hunllef", "The Hueycoatl", "The Leviathan", "The Whisperer",
		"Blood Moon", "Blue Moon", "Eclipse Moon",
		"Branda, Queen of Fire", "Eldric, King of Ice",
		SpecialEncounterRules.THEATRE_OF_BLOOD,
		"Thermonuclear smoke devil", SpecialEncounterRules.TOMBS_OF_AMASCUT,
		"TzKal-Zuk", "TzTok-Jad", "Vardorvis", "Venenatis", "Vet'ion", "Vorkath",
		"Wintertodt", "Yama", "Zalcano", "Zulrah"
	);

	private BossRegistry() {}

	static boolean isBoss(String name)
	{
		return name != null && BOSSES.contains(name);
	}

	static Set<String> names()
	{
		return BOSSES;
	}

	private static Set<String> names(String... values)
	{
		Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		result.addAll(Arrays.asList(values));
		return Collections.unmodifiableSet(result);
	}
}

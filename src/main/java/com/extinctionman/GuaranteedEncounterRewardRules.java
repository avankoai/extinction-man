package com.extinctionman;

final class GuaranteedEncounterRewardRules
{
	private GuaranteedEncounterRewardRules() {}

	static boolean matches(String sourceNpcName, String itemName)
	{
		return (SpecialEncounterRules.TZTOK_JAD.equals(sourceNpcName) && "Fire cape".equalsIgnoreCase(itemName))
			|| (SpecialEncounterRules.TZKAL_ZUK.equals(sourceNpcName) && "Infernal cape".equalsIgnoreCase(itemName));
	}
}

package com.extinctionman;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExtinctionLogPanelTest
{
	@Test
	public void formatsOverallExtinctionProgress()
	{
		assertEquals("<html><div style='text-align:center'>Monsters<br>1/3 Completed (33.33%)<br><br>"
			+ "Bosses<br>0/0 Completed (0.00%)</div></html>", ExtinctionLogPanel.formatProgressSummary(Arrays.asList(
			new SoulProgress("Goblin", 100),
			new SoulProgress("Guard", 42),
			new SoulProgress("Rat", 0))));
	}

	@Test
	public void formatsEmptyBestiaryWithoutDividingByZero()
	{
		assertEquals("<html><div style='text-align:center'>Monsters<br>0/0 Completed (0.00%)<br><br>"
				+ "Bosses<br>0/0 Completed (0.00%)</div></html>",
			ExtinctionLogPanel.formatProgressSummary(Collections.emptyList()));
	}

	@Test
	public void separatesBossAndMonsterCompletionInTheHeader()
	{
		assertEquals("<html><div style='text-align:center'>Monsters<br>1/1 Completed (100.00%)<br><br>"
				+ "Bosses<br>1/2 Completed (50.00%)</div></html>",
			ExtinctionLogPanel.formatProgressSummary(Arrays.asList(
				new SoulProgress("Goblin", 100),
				new SoulProgress("Vorkath", 100),
				new SoulProgress("Zulrah", 20))));
	}

	@Test
	public void infoExplainsTheCoreProgressionAndSafetyRules()
	{
		String info = ExtinctionLogPanel.infoHtml();
		assertTrue(info.contains("Every individual monster kill awards <b>1 Soul.</b>"));
		assertTrue(info.contains("Making a monster extinct awards <b>1 Soul Energy</b>"));
		assertTrue(info.contains("100 Soul Energy - 1 Unbound Soul (Item)"));
		assertTrue(info.contains("password is <b>Soul</b>"));
		assertTrue(info.contains("Monsters are not actually removed from the game"));
		assertTrue(info.contains("Active Unbound Soul"));
	}
}

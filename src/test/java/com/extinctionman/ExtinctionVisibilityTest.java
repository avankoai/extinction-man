package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExtinctionVisibilityTest
{
	@Test
	public void hidesNaturallyExtinctNpc()
	{
		assertTrue(ExtinctionVisibility.shouldHide("Goblin", true, true, false));
	}

	@Test
	public void livingNpcRemainsVisible()
	{
		assertFalse(ExtinctionVisibility.shouldHide("Goblin", true, false, false));
	}

	@Test
	public void masterToggleKeepsNpcVisible()
	{
		assertFalse(ExtinctionVisibility.shouldHide("Goblin", false, true, false));
	}

	@Test
	public void slayerExceptionRevealsExtinctNpc()
	{
		assertFalse(ExtinctionVisibility.shouldHide("Goblin", true, true, true));
	}
}

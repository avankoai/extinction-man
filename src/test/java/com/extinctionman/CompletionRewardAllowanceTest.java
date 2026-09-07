package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompletionRewardAllowanceTest
{
	@Test
	public void waitsForAndClaimsOnlyTheMatchingRewardSource()
	{
		CompletionRewardAllowance allowance = new CompletionRewardAllowance();
		allowance.allow("Barrows", null);
		assertTrue(allowance.allows("Barrows"));
		allowance.claim("Moons");
		assertTrue(allowance.allows("Barrows"));
		allowance.claim("Barrows");
		assertTrue(allowance.allows("Barrows"));
		allowance.close("Barrows");
		assertFalse(allowance.allows("Barrows"));
	}

	@Test
	public void activatesImmediatelyForAnAlreadyOpenRewardInterface()
	{
		CompletionRewardAllowance allowance = new CompletionRewardAllowance();
		allowance.allow("Raid", "Raid");
		assertTrue(allowance.allows("Raid"));
		allowance.clear();
		assertFalse(allowance.allows("Raid"));
	}
}

package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KillCreditTrackerTest
{
	@Test
	public void ownDamageAllowsExactlyOneDeathClaim()
	{
		KillCreditTracker<Object> tracker = new KillCreditTracker<>();
		Object npc = new Object();
		tracker.markOwnDamage(npc, 10);
		assertEquals(KillCreditTracker.DeathClaim.CREDITED, tracker.claimDeath(npc, 12));
		assertEquals(KillCreditTracker.DeathClaim.NOT_OURS, tracker.claimDeath(npc, 12));
	}

	@Test
	public void remembersOwnDamageUntilThatNpcDies()
	{
		KillCreditTracker<Object> tracker = new KillCreditTracker<>();
		Object npc = new Object();
		assertEquals(KillCreditTracker.DeathClaim.NOT_OURS, tracker.claimDeath(npc, 12));
		tracker.markOwnDamage(npc, 1);
		assertEquals(KillCreditTracker.DeathClaim.CREDITED, tracker.claimDeath(npc, 300));
	}

	@Test
	public void despawnClearsRememberedCombatParticipation()
	{
		KillCreditTracker<Object> tracker = new KillCreditTracker<>();
		Object npc = new Object();
		tracker.markOwnDamage(npc, 10);
		tracker.forget(npc);
		assertEquals(KillCreditTracker.DeathClaim.NOT_OURS, tracker.claimDeath(npc, 12));
	}

	@Test
	public void otherPlayerDamageMakesKillContested()
	{
		KillCreditTracker<Object> tracker = new KillCreditTracker<>();
		Object npc = new Object();
		tracker.markOtherDamage(npc);
		tracker.markOwnDamage(npc, 10);
		assertEquals(KillCreditTracker.DeathClaim.CONTESTED, tracker.claimDeath(npc, 12));
		assertEquals(KillCreditTracker.DeathClaim.NOT_OURS, tracker.claimDeath(npc, 12));
	}

	@Test
	public void otherPlayerDamageWithoutOwnDamageIsNotOurKill()
	{
		KillCreditTracker<Object> tracker = new KillCreditTracker<>();
		Object npc = new Object();
		tracker.markOtherDamage(npc);
		assertEquals(KillCreditTracker.DeathClaim.NOT_OURS, tracker.claimDeath(npc, 12));
	}

	@Test
	public void sharedBossAllowsCreditAfterOtherPlayerDamage()
	{
		KillCreditTracker<Object> tracker = new KillCreditTracker<>();
		Object npc = new Object();
		tracker.markOtherDamage(npc);
		tracker.markOwnDamage(npc, 10);
		assertEquals(KillCreditTracker.DeathClaim.CREDITED, tracker.claimDeath(npc, 12, true));
	}
}

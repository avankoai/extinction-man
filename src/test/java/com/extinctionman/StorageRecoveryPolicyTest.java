package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StorageRecoveryPolicyTest
{
	@Test
	public void recoverySnapshotWinsEvenWhenItRepresentsAReset()
	{
		assertEquals("", StorageRecoveryPolicy.newestSnapshot("old progress", ""));
		assertEquals("old progress", StorageRecoveryPolicy.newestSnapshot("old progress", null));
	}

	@Test
	public void writeVerificationRequiresTheExactNewValue()
	{
		assertTrue(StorageRecoveryPolicy.writeWasVerified("new", "new"));
		assertFalse(StorageRecoveryPolicy.writeWasVerified("new", "old"));
		assertFalse(StorageRecoveryPolicy.writeWasVerified("new", null));
	}
}

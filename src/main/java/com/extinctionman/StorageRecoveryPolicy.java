package com.extinctionman;

final class StorageRecoveryPolicy
{
	private StorageRecoveryPolicy() {}

	static String newestSnapshot(String profileStorage, String recoveryStorage)
	{
		return recoveryStorage != null ? recoveryStorage : profileStorage;
	}

	static boolean writeWasVerified(String expected, String stored)
	{
		return expected != null && expected.equals(stored);
	}
}

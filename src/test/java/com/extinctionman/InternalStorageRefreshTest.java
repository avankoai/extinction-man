package com.extinctionman;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InternalStorageRefreshTest
{
	@Test
	public void storageWritesDoNotTriggerFullSidebarRebuilds()
	{
		assertTrue(ExtinctionManPlugin.isInternalStorageKey("soulLogV2"));
		assertTrue(ExtinctionManPlugin.isInternalStorageKey("soulPointsMetaV1"));
		assertTrue(ExtinctionManPlugin.isInternalStorageKey("unsyncedSoulLogV2"));
		assertTrue(ExtinctionManPlugin.isInternalStorageKey("unsyncedSoulLogV2_YWNjb3VudA"));
		assertTrue(ExtinctionManPlugin.isInternalStorageKey("unsyncedSoulPointsMetaV1_YWNjb3VudA"));
		assertTrue(ExtinctionManPlugin.isInternalStorageKey("souls_R29ibGlu"));
		assertFalse(ExtinctionManPlugin.isInternalStorageKey("showExtinctionPopup"));
		assertFalse(ExtinctionManPlugin.isInternalStorageKey(null));
	}
}

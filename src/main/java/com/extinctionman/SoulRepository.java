package com.extinctionman;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.client.config.ConfigManager;

final class SoulRepository
{
	private static final String LEGACY_KEY_PREFIX = "souls_";
	private static final String PROFILE_LOG_KEY = "soulLogV2";
	private static final String LEGACY_MIGRATION_KEY = "legacySoulsMigratedV2";
	private static final String LEGACY_RECOVERY_LOG_KEY = "unsyncedSoulLogV2";
	private static final String RECOVERY_LOG_KEY_PREFIX = "unsyncedSoulLogV2_";
	private final ConfigManager configManager;
	private final Map<String, SoulProgress> cache = new HashMap<>();
	private SoulProgress lastEncountered;
	private boolean loaded;
	private boolean profileStorageAvailable;

	SoulRepository(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	synchronized SoulProgress gainSoul(String exactNpcName)
	{
		ensureLoaded();
		SoulProgress current = get(exactNpcName);
		SoulProgress updated = current.gainSoul();
		cache.put(exactNpcName, updated);
		lastEncountered = updated;
		saveProfile();
		return updated;
	}

	synchronized void reset(String exactNpcName)
	{
		ensureLoaded();
		cache.remove(exactNpcName);
		if (lastEncountered != null && lastEncountered.getNpcName().equals(exactNpcName))
		{
			lastEncountered = null;
		}
		saveProfile();
	}

	synchronized void resetAll()
	{
		ensureLoaded();
		cache.clear();
		lastEncountered = null;
		saveProfile();
	}

	synchronized void markComplete(String exactNpcName)
	{
		ensureLoaded();
		SoulProgress completed = new SoulProgress(exactNpcName, SoulProgress.EXTINCTION_TARGET);
		cache.put(exactNpcName, completed);
		lastEncountered = completed;
		saveProfile();
	}

	synchronized SoulProgress get(String exactNpcName)
	{
		ensureLoaded();
		return cache.computeIfAbsent(exactNpcName, name -> new SoulProgress(name, 0));
	}

	synchronized boolean isExtinctCached(String exactNpcName)
	{
		ensureLoaded();
		SoulProgress progress = cache.get(exactNpcName);
		return progress != null && progress.isExtinct();
	}

	synchronized SoulProgress getLastEncountered()
	{
		return lastEncountered;
	}

	synchronized List<SoulProgress> getAll()
	{
		ensureLoaded();
		List<SoulProgress> all = new ArrayList<>();
		for (SoulProgress progress : cache.values())
		{
			if (progress.getSouls() > 0)
			{
				all.add(progress);
			}
		}
		all.sort(Comparator.comparing(SoulProgress::isExtinct).reversed()
			.thenComparing(SoulProgress::getNpcName, String.CASE_INSENSITIVE_ORDER));
		return all;
	}

	synchronized String createBackup()
	{
		ensureLoaded();
		return SoulLogCodec.encodeBackup(cache);
	}

	synchronized String exportStorage()
	{
		ensureLoaded();
		return SoulLogCodec.encodeStorage(cache);
	}

	synchronized void importStorage(String storage)
	{
		Map<String, Integer> restored = SoulLogCodec.decodeStorage(storage);
		cache.clear();
		loadValues(restored);
		lastEncountered = null;
		loaded = true;
		saveProfile();
	}

	synchronized void restoreBackup(String backup)
	{
		Map<String, Integer> restored = SoulLogCodec.decodeBackup(backup);
		cache.clear();
		for (Map.Entry<String, Integer> entry : restored.entrySet())
		{
			cache.put(entry.getKey(), new SoulProgress(entry.getKey(), entry.getValue()));
		}
		lastEncountered = null;
		loaded = true;
		saveProfile();
	}

	synchronized void reloadProfile()
	{
		cache.clear();
		lastEncountered = null;
		loaded = false;
		profileStorageAvailable = false;
		ensureLoaded();
	}

	synchronized boolean isProfileStorageAvailable()
	{
		ensureLoaded();
		return profileStorageAvailable;
	}

	private void ensureLoaded()
	{
		if (loaded)
		{
			return;
		}
		loaded = true;
		String stored = configManager.getRSProfileConfiguration(
			ExtinctionManConfig.GROUP, PROFILE_LOG_KEY);
		if (stored != null)
		{
			loadValues(SoulLogCodec.decodeStorage(stored));
			profileStorageAvailable = true;
			String recovery = readRecovery();
			if (recovery != null)
			{
				mergeValues(SoulLogCodec.decodeStorage(recovery));
				saveProfile();
			}
			return;
		}

		Boolean migrated = configManager.getConfiguration(
			ExtinctionManConfig.GROUP, LEGACY_MIGRATION_KEY, Boolean.class);
		Map<String, Integer> legacy = Boolean.TRUE.equals(migrated)
			? new HashMap<>() : readLegacyValues();
		loadValues(legacy);
		String recovery = readRecovery();
		if (recovery != null)
		{
			mergeValues(SoulLogCodec.decodeStorage(recovery));
		}
		configManager.setRSProfileConfiguration(
			ExtinctionManConfig.GROUP, PROFILE_LOG_KEY, SoulLogCodec.encodeStorage(cache));
		String verification = configManager.getRSProfileConfiguration(
			ExtinctionManConfig.GROUP, PROFILE_LOG_KEY);
		profileStorageAvailable = verification != null;
		if (profileStorageAvailable && !Boolean.TRUE.equals(migrated))
		{
			configManager.setConfiguration(
				ExtinctionManConfig.GROUP, LEGACY_MIGRATION_KEY, true);
		}
		if (!profileStorageAvailable)
		{
			configManager.setConfiguration(ExtinctionManConfig.GROUP,
				recoveryKey(), SoulLogCodec.encodeStorage(cache));
		}
	}

	private void saveProfile()
	{
		configManager.setRSProfileConfiguration(
			ExtinctionManConfig.GROUP, PROFILE_LOG_KEY, SoulLogCodec.encodeStorage(cache));
		profileStorageAvailable = configManager.getRSProfileConfiguration(
			ExtinctionManConfig.GROUP, PROFILE_LOG_KEY) != null;
		if (profileStorageAvailable)
		{
			clearRecovery();
		}
		else
		{
			configManager.setConfiguration(ExtinctionManConfig.GROUP,
				recoveryKey(), SoulLogCodec.encodeStorage(cache));
		}
	}

	private String readRecovery()
	{
		String recovery = configManager.getConfiguration(
			ExtinctionManConfig.GROUP, recoveryKey());
		return recovery != null ? recovery : configManager.getConfiguration(
			ExtinctionManConfig.GROUP, LEGACY_RECOVERY_LOG_KEY);
	}

	private void clearRecovery()
	{
		configManager.unsetConfiguration(ExtinctionManConfig.GROUP, recoveryKey());
		configManager.unsetConfiguration(ExtinctionManConfig.GROUP, LEGACY_RECOVERY_LOG_KEY);
	}

	private String recoveryKey()
	{
		String profileKey = configManager.getRSProfileKey();
		String identity = profileKey == null ? "unbound" : Base64.getUrlEncoder().withoutPadding()
			.encodeToString(profileKey.getBytes(StandardCharsets.UTF_8));
		return RECOVERY_LOG_KEY_PREFIX + identity;
	}

	private void loadValues(Map<String, Integer> values)
	{
		for (Map.Entry<String, Integer> entry : values.entrySet())
		{
			if (entry.getKey() != null && !entry.getKey().trim().isEmpty() && entry.getValue() > 0)
			{
				cache.put(entry.getKey(), new SoulProgress(entry.getKey(), entry.getValue()));
			}
		}
	}

	private void mergeValues(Map<String, Integer> values)
	{
		for (Map.Entry<String, Integer> entry : values.entrySet())
		{
			SoulProgress current = cache.get(entry.getKey());
			if (current == null || entry.getValue() > current.getSouls())
			{
				cache.put(entry.getKey(), new SoulProgress(entry.getKey(), entry.getValue()));
			}
		}
	}

	private Map<String, Integer> readLegacyValues()
	{
		Map<String, Integer> values = new HashMap<>();
		String fullPrefix = ExtinctionManConfig.GROUP + "." + LEGACY_KEY_PREFIX;
		for (String fullKey : configManager.getConfigurationKeys(fullPrefix))
		{
			String exactNpcName = nameFromFullKey(fullKey);
			Integer souls = exactNpcName == null ? null : configManager.getConfiguration(
				ExtinctionManConfig.GROUP, key(exactNpcName), Integer.class);
			if (souls != null && souls > 0)
			{
				values.put(exactNpcName, souls);
			}
		}
		return values;
	}

	static String key(String exactNpcName)
	{
		String encoded = Base64.getUrlEncoder().withoutPadding()
			.encodeToString(exactNpcName.getBytes(StandardCharsets.UTF_8));
		return LEGACY_KEY_PREFIX + encoded;
	}

	static String nameFromFullKey(String fullKey)
	{
		String fullPrefix = ExtinctionManConfig.GROUP + "." + LEGACY_KEY_PREFIX;
		if (fullKey == null || !fullKey.startsWith(fullPrefix))
		{
			return null;
		}
		try
		{
			byte[] decoded = Base64.getUrlDecoder().decode(fullKey.substring(fullPrefix.length()));
			return new String(decoded, StandardCharsets.UTF_8);
		}
		catch (IllegalArgumentException ex)
		{
			return null;
		}
	}
}

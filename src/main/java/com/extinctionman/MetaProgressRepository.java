package com.extinctionman;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
final class MetaProgressRepository
{
	static final int EXTERMINATIONS_PER_SOUL_POINT = SoulPointEconomy.EXTERMINATIONS_PER_POINT;
	private static final String PROFILE_KEY = "soulPointsMetaV1";
	private static final String LEGACY_RECOVERY_KEY = "unsyncedSoulPointsMetaV1";
	private static final String RECOVERY_KEY_PREFIX = "unsyncedSoulPointsMetaV1_";
	private final ConfigManager configManager;
	private final Set<String> rewardedSpecies = new HashSet<>();
	private final List<WhitelistUnlock> unlocks = new ArrayList<>();
	private int forfeitedPoints;
	private boolean loaded;
	private boolean initialReconciliationNeeded;

	@Inject
	MetaProgressRepository(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	synchronized SoulPointAward award(String exactNpcName)
	{
		ensureLoaded();
		int pointsBefore = SoulPointEconomy.earnedPoints(rewardedSpecies.size());
		if (!rewardedSpecies.add(exactNpcName))
		{
			return new SoulPointAward(false, false);
		}
		save();
		int pointsAfter = SoulPointEconomy.earnedPoints(rewardedSpecies.size());
		return new SoulPointAward(true, pointsAfter > pointsBefore);
	}

	synchronized boolean revokeAward(String exactNpcName)
	{
		ensureLoaded();
		if (!rewardedSpecies.remove(exactNpcName))
		{
			return false;
		}
		save();
		return true;
	}

	synchronized int getCompletedExterminations()
	{
		ensureLoaded();
		return rewardedSpecies.size();
	}

	synchronized int getAvailablePoints()
	{
		ensureLoaded();
		return SoulPointEconomy.availablePoints(rewardedSpecies.size(),
			unlocks.size() + forfeitedPoints);
	}

	synchronized int getProgressToNextPoint()
	{
		ensureLoaded();
		return SoulPointEconomy.progressToNextPoint(rewardedSpecies.size());
	}

	synchronized boolean canCreateUnlock()
	{
		return getAvailablePoints() >= 1 && getActiveHunt() == null;
	}

	synchronized void createUnlock(int itemId, String itemName, String sourceNpcName)
	{
		if (!canCreateUnlock())
		{
			throw new IllegalStateException("No Soul Item unlock is currently available.");
		}
		unlocks.add(new WhitelistUnlock(itemId, itemName, sourceNpcName, false));
		save();
	}

	synchronized WhitelistUnlock getActiveHunt()
	{
		ensureLoaded();
		return unlocks.stream().filter(unlock -> !unlock.isAcquired()).findFirst().orElse(null);
	}

	synchronized boolean completeActiveHunt()
	{
		WhitelistUnlock active = getActiveHunt();
		if (active == null)
		{
			return false;
		}
		int index = unlocks.indexOf(active);
		unlocks.set(index, active.acquired());
		save();
		return true;
	}

	synchronized boolean isWhitelisted(int canonicalItemId)
	{
		ensureLoaded();
		return unlocks.stream().anyMatch(unlock -> unlock.getItemId() == canonicalItemId);
	}

	synchronized boolean isWhitelisted(int canonicalItemId, String itemName)
	{
		ensureLoaded();
		return unlocks.stream().anyMatch(unlock -> unlock.matchesItem(canonicalItemId, itemName));
	}

	synchronized List<WhitelistUnlock> getUnlocks()
	{
		ensureLoaded();
		return new ArrayList<>(unlocks);
	}

	synchronized boolean removeUnlock(int canonicalItemId, boolean refundPoint)
	{
		ensureLoaded();
		int index = -1;
		for (int i = 0; i < unlocks.size(); i++)
		{
			if (unlocks.get(i).getItemId() == canonicalItemId)
			{
				index = i;
				break;
			}
		}
		if (index < 0) return false;
		unlocks.remove(index);
		if (!refundPoint)
		{
			forfeitedPoints++;
		}
		save();
		return true;
	}

	synchronized Set<String> getRewardedSpecies()
	{
		ensureLoaded();
		return new HashSet<>(rewardedSpecies);
	}

	synchronized void resetSoulPointProgress()
	{
		ensureLoaded();
		rewardedSpecies.clear();
		save();
	}

	synchronized boolean needsInitialReconciliation()
	{
		ensureLoaded();
		return initialReconciliationNeeded;
	}

	synchronized void completeInitialReconciliation(Set<String> completedSpecies)
	{
		ensureLoaded();
		if (!initialReconciliationNeeded) return;
		rewardedSpecies.addAll(completedSpecies);
		initialReconciliationNeeded = false;
		save();
	}

	synchronized String exportData()
	{
		ensureLoaded();
		return MetaProgressCodec.encode(rewardedSpecies, unlocks, forfeitedPoints);
	}

	synchronized void importData(String data)
	{
		MetaProgressCodec.Decoded decoded = MetaProgressCodec.decode(data);
		rewardedSpecies.clear();
		rewardedSpecies.addAll(decoded.getRewardedSpecies());
		unlocks.clear();
		unlocks.addAll(decoded.getUnlocks());
		forfeitedPoints = decoded.getForfeitedPoints();
		loaded = true;
		initialReconciliationNeeded = false;
		save();
	}

	synchronized void reloadProfile()
	{
		loaded = false;
		rewardedSpecies.clear();
		unlocks.clear();
		forfeitedPoints = 0;
		initialReconciliationNeeded = false;
		ensureLoaded();
	}

	private void ensureLoaded()
	{
		if (loaded) return;
		loaded = true;
		String stored = configManager.getRSProfileConfiguration(ExtinctionManConfig.GROUP, PROFILE_KEY);
		String recovery = readRecovery();
		String source = stored != null ? stored : recovery;
		initialReconciliationNeeded = source == null;
		if (source != null)
		{
			MetaProgressCodec.Decoded decoded = MetaProgressCodec.decode(source);
			rewardedSpecies.addAll(decoded.getRewardedSpecies());
			unlocks.addAll(decoded.getUnlocks());
			forfeitedPoints = decoded.getForfeitedPoints();
		}
		if (stored == null) save();
	}

	private void save()
	{
		String encoded = MetaProgressCodec.encode(rewardedSpecies, unlocks, forfeitedPoints);
		configManager.setRSProfileConfiguration(ExtinctionManConfig.GROUP, PROFILE_KEY, encoded);
		if (configManager.getRSProfileConfiguration(ExtinctionManConfig.GROUP, PROFILE_KEY) != null)
		{
			clearRecovery();
		}
		else
		{
			configManager.setConfiguration(ExtinctionManConfig.GROUP, recoveryKey(), encoded);
		}
	}

	private String readRecovery()
	{
		String recovery = configManager.getConfiguration(ExtinctionManConfig.GROUP, recoveryKey());
		return recovery != null ? recovery : configManager.getConfiguration(
			ExtinctionManConfig.GROUP, LEGACY_RECOVERY_KEY);
	}

	private void clearRecovery()
	{
		configManager.unsetConfiguration(ExtinctionManConfig.GROUP, recoveryKey());
		configManager.unsetConfiguration(ExtinctionManConfig.GROUP, LEGACY_RECOVERY_KEY);
	}

	private String recoveryKey()
	{
		String profileKey = configManager.getRSProfileKey();
		String identity = profileKey == null ? "unbound" : Base64.getUrlEncoder().withoutPadding()
			.encodeToString(profileKey.getBytes(StandardCharsets.UTF_8));
		return RECOVERY_KEY_PREFIX + identity;
	}
}

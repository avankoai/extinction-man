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
	static final int SOUL_ITEM_COST = 100;
	private static final String PROFILE_KEY = "soulPointsMetaV1";
	private static final String LEGACY_RECOVERY_KEY = "unsyncedSoulPointsMetaV1";
	private static final String RECOVERY_KEY_PREFIX = "unsyncedSoulPointsMetaV1_";
	private final ConfigManager configManager;
	private final Set<String> rewardedSpecies = new HashSet<>();
	private final List<WhitelistUnlock> unlocks = new ArrayList<>();
	private int forfeitedPoints;
	private int bonusPoints;
	private final List<SoulPermit> activePermits = new ArrayList<>();
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
		int pointsBefore = SoulPointEconomy.earnedEnergy(rewardedSpecies);
		if (!rewardedSpecies.add(exactNpcName))
		{
			return new SoulPointAward(false, 0);
		}
		save();
		int pointsAfter = SoulPointEconomy.earnedEnergy(rewardedSpecies);
		return new SoulPointAward(true, pointsAfter - pointsBefore);
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
		int spent = unlocks.stream().mapToInt(WhitelistUnlock::getPointCost).sum() + forfeitedPoints;
		return Math.max(0, SoulPointEconomy.earnedEnergy(rewardedSpecies) + bonusPoints - spent);
	}

	synchronized int getSpentPoints()
	{
		ensureLoaded();
		return unlocks.stream().mapToInt(WhitelistUnlock::getPointCost).sum() + forfeitedPoints;
	}

	synchronized boolean canCreateUnlock()
	{
		return getAvailablePoints() >= getNewUnlockCost();
	}

	int getNewUnlockCost()
	{
		return SOUL_ITEM_COST;
	}

	synchronized SoulPermit getActivePermit()
	{
		ensureLoaded();
		return activePermits.isEmpty() ? null : activePermits.get(0);
	}

	synchronized List<SoulPermit> getActivePermits()
	{
		ensureLoaded();
		return new ArrayList<>(activePermits);
	}

	synchronized SoulPermit findActivePermit(String sourceName)
	{
		ensureLoaded();
		return activePermits.stream().filter(permit -> permit.matches(sourceName))
			.findFirst().orElse(null);
	}

	synchronized boolean canCreatePermit(boolean boss, int kills)
	{
		if (kills < 1) return false;
		return getAvailablePoints() >= (boss ? 10 : 1) * kills;
	}

	synchronized boolean canCreatePermit(boolean boss)
	{
		return canCreatePermit(boss, 1);
	}

	synchronized void createPermit(String sourceName, boolean boss, int kills)
	{
		if (!canCreatePermit(boss, kills)) throw new IllegalStateException("Not enough Soul Energy.");
		SoulPermit created = new SoulPermit(sourceName, boss, kills);
		for (int i = 0; i < activePermits.size(); i++)
		{
			SoulPermit existing = activePermits.get(i);
			if (existing.matches(sourceName) && existing.isBoss() == boss)
			{
				activePermits.set(i, existing.addKills(kills));
				forfeitedPoints += created.getPointCost();
				save();
				return;
			}
		}
		activePermits.add(created);
		forfeitedPoints += created.getPointCost();
		save();
	}

	synchronized void createPermit(String sourceName, boolean boss)
	{
		createPermit(sourceName, boss, 1);
	}

	synchronized boolean consumePermit(String sourceName)
	{
		ensureLoaded();
		for (int i = 0; i < activePermits.size(); i++)
		{
			SoulPermit permit = activePermits.get(i);
			if (!permit.matches(sourceName)) continue;
			SoulPermit remaining = permit.consumeOne();
			if (remaining == null) activePermits.remove(i);
			else activePermits.set(i, remaining);
			save();
			return true;
		}
		return false;
	}

	synchronized void createUnlock(int itemId, String itemName, String sourceNpcName)
	{
		if (!canCreateUnlock())
		{
			throw new IllegalStateException("Not enough Soul Energy for an Unbound Soul.");
		}
		unlocks.add(new WhitelistUnlock(itemId, itemName, sourceNpcName, false, getNewUnlockCost()));
		save();
	}

	synchronized WhitelistUnlock getActiveHunt()
	{
		ensureLoaded();
		return unlocks.stream().filter(unlock -> !unlock.isAcquired()).findFirst().orElse(null);
	}

	synchronized List<WhitelistUnlock> getActiveHunts()
	{
		ensureLoaded();
		List<WhitelistUnlock> active = new ArrayList<>();
		for (WhitelistUnlock unlock : unlocks)
		{
			if (!unlock.isAcquired()) active.add(unlock);
		}
		return active;
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
		WhitelistUnlock removed = unlocks.remove(index);
		if (!refundPoint)
		{
			forfeitedPoints += removed.getPointCost();
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
		bonusPoints = 0;
		activePermits.clear();
		save();
	}

	synchronized boolean removeUnlock(WhitelistUnlock unlock, boolean refundEnergy)
	{
		ensureLoaded();
		int index = unlocks.indexOf(unlock);
		if (index < 0) return false;
		WhitelistUnlock removed = unlocks.remove(index);
		if (!refundEnergy) forfeitedPoints += removed.getPointCost();
		save();
		return true;
	}

	synchronized boolean completeHunt(WhitelistUnlock active)
	{
		ensureLoaded();
		if (active == null || active.isAcquired()) return false;
		int index = unlocks.indexOf(active);
		if (index < 0) return false;
		unlocks.set(index, active.acquired());
		save();
		return true;
	}

	synchronized void grantBonusSoulPoint()
	{
		grantBonusSoulPoints(1);
	}

	synchronized void grantBonusSoulPoints(int amount)
	{
		ensureLoaded();
		bonusPoints += Math.max(0, amount);
		save();
	}

	synchronized void setAvailableSoulEnergy(int amount)
	{
		ensureLoaded();
		if (amount < 0) throw new IllegalArgumentException("Soul Energy cannot be negative.");
		int spent = unlocks.stream().mapToInt(WhitelistUnlock::getPointCost).sum() + forfeitedPoints;
		bonusPoints = amount + spent - SoulPointEconomy.earnedEnergy(rewardedSpecies);
		save();
	}

	synchronized void resetAllProgress()
	{
		ensureLoaded();
		rewardedSpecies.clear();
		unlocks.clear();
		forfeitedPoints = 0;
		bonusPoints = 0;
		activePermits.clear();
		initialReconciliationNeeded = false;
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

	synchronized boolean synchronizeExtinctionAwards(Set<String> completedSpecies)
	{
		ensureLoaded();
		Set<String> expected = completedSpecies == null
			? java.util.Collections.emptySet() : new HashSet<>(completedSpecies);
		if (!initialReconciliationNeeded && rewardedSpecies.equals(expected))
		{
			return false;
		}
		rewardedSpecies.clear();
		rewardedSpecies.addAll(expected);
		initialReconciliationNeeded = false;
		save();
		return true;
	}

	synchronized String exportData()
	{
		ensureLoaded();
		return MetaProgressCodec.encode(rewardedSpecies, unlocks, forfeitedPoints, bonusPoints, activePermits);
	}

	synchronized void importData(String data)
	{
		MetaProgressCodec.Decoded decoded = MetaProgressCodec.decode(data);
		rewardedSpecies.clear();
		rewardedSpecies.addAll(decoded.getRewardedSpecies());
		unlocks.clear();
		unlocks.addAll(decoded.getUnlocks());
		forfeitedPoints = decoded.getForfeitedPoints();
		bonusPoints = decoded.getBonusPoints();
		activePermits.clear();
		activePermits.addAll(decoded.getActivePermits());
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
		bonusPoints = 0;
		activePermits.clear();
		initialReconciliationNeeded = false;
		ensureLoaded();
	}

	private void ensureLoaded()
	{
		if (loaded) return;
		loaded = true;
		String stored = configManager.getRSProfileConfiguration(ExtinctionManConfig.GROUP, PROFILE_KEY);
		String recovery = readRecovery();
		String source = StorageRecoveryPolicy.newestSnapshot(stored, recovery);
		initialReconciliationNeeded = source == null;
		boolean migratedEconomy = false;
		if (source != null)
		{
			MetaProgressCodec.Decoded decoded = MetaProgressCodec.decode(source);
			migratedEconomy = decoded.isLegacyEconomy();
			rewardedSpecies.addAll(decoded.getRewardedSpecies());
			unlocks.addAll(decoded.getUnlocks());
			forfeitedPoints = decoded.getForfeitedPoints();
			bonusPoints = decoded.getBonusPoints();
			activePermits.addAll(decoded.getActivePermits());
		}
		if (recovery != null || stored == null || migratedEconomy) save();
	}

	private void save()
	{
		String encoded = MetaProgressCodec.encode(rewardedSpecies, unlocks, forfeitedPoints, bonusPoints, activePermits);
		configManager.setConfiguration(ExtinctionManConfig.GROUP, recoveryKey(), encoded);
		configManager.setRSProfileConfiguration(ExtinctionManConfig.GROUP, PROFILE_KEY, encoded);
		String verification = configManager.getRSProfileConfiguration(
			ExtinctionManConfig.GROUP, PROFILE_KEY);
		if (StorageRecoveryPolicy.writeWasVerified(encoded, verification))
		{
			clearRecovery();
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

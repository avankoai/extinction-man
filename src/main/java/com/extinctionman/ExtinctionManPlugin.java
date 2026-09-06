package com.extinctionman;

import com.google.inject.Provides;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Collections;
import java.util.IdentityHashMap;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Renderable;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.slayer.SlayerConfig;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Extinction Man",
	description = "Collect souls from loot-eligible NPC kills and mark species extinct at 100",
	tags = {"extinction", "souls", "kills", "tracker"}
)
public class ExtinctionManPlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ExtinctionManConfig config;
	@Inject private ConfigManager configManager;
	@Inject private OverlayManager overlayManager;
	@Inject private ExtinctionManOverlay overlay;
	@Inject private ForbiddenLootOverlay forbiddenLootOverlay;
	@Inject private ForbiddenLootTracker forbiddenLootTracker;
	@Inject private SoulAnimationOverlay soulAnimationOverlay;
	@Inject private SoulAnimationTracker soulAnimationTracker;
	@Inject private ExtinctionCelebrationOverlay extinctionCelebrationOverlay;
	@Inject private ExtinctionCelebrationTracker extinctionCelebrationTracker;
	@Inject private SoulPointCelebrationOverlay soulPointCelebrationOverlay;
	@Inject private SoulPointCelebrationTracker soulPointCelebrationTracker;
	@Inject private ExterminationWaveTracker exterminationWaveTracker;
	@Inject private BestiaryRegistry bestiaryRegistry;
	@Inject private ExtinctionLogPanel extinctionLogPanel;
	@Inject private ClientToolbar clientToolbar;
	@Inject private ClientThread clientThread;
	@Inject private RenderCallbackManager renderCallbackManager;
	@Inject private MetaProgressRepository metaProgressRepository;
	@Inject private ItemRegistry itemRegistry;
	@Inject private ItemManager itemManager;
	@Inject private WhitelistHuntService whitelistHuntService;
	@Inject private WhitelistLootOverlay whitelistLootOverlay;
	@Inject private WhitelistLootTracker whitelistLootTracker;
	@Inject private ExtinctGhostOverlay extinctGhostOverlay;
	@Inject private ExtinctGhostTracker extinctGhostTracker;
	@Inject private ExtinctGhostModelStyler extinctGhostModelStyler;

	private SoulRepository repository;
	private String currentSlayerTask;
	private NavigationButton navigationButton;
	private NPC pendingWaveNpc;
	private String pendingWaveNpcName;
	private int pendingWavePreviousAnimation;
	private int pendingWaveExpiresAtTick;
	private boolean pendingWaveSoulPointAwarded;
	private int pendingWaveBankedSoulPoints;
	private int activeHuntItemId = -1;
	private int activeHuntInventoryBaseline;
	private boolean raidRewardClaimed;
	private boolean lastLoadingIntoInstance;
	private final Set<String> raidBossesSeen = new LinkedHashSet<>();
	private final Set<NPC> gwdCompletionLootAllowed =
		Collections.newSetFromMap(new IdentityHashMap<>());
	private final Set<NPC> knownNpcs = Collections.newSetFromMap(new IdentityHashMap<>());
	private final KillCreditTracker<NPC> killCreditTracker = new KillCreditTracker<>();
	private final RenderCallback renderCallback = new RenderCallback()
	{
		@Override
		public boolean addEntity(Renderable renderable, boolean ui)
		{
			return shouldDraw(renderable, ui);
		}
	};

	@Override
	protected void startUp()
	{
		if (repository == null)
		{
			repository = new SoulRepository(configManager);
		}
		overlayManager.add(overlay);
		overlayManager.add(forbiddenLootOverlay);
		overlayManager.add(soulAnimationOverlay);
		overlayManager.add(extinctionCelebrationOverlay);
		overlayManager.add(soulPointCelebrationOverlay);
		overlayManager.add(whitelistLootOverlay);
		overlayManager.add(extinctGhostOverlay);
		navigationButton = NavigationButton.builder()
			.tooltip("Extinction Log")
			.icon(ExtinctionLogIcon.create())
			.priority(5)
			.panel(extinctionLogPanel)
			.build();
		clientToolbar.addNavigation(navigationButton);
		extinctionLogPanel.refresh();
		renderCallbackManager.register(renderCallback);
		clientThread.invokeLater(() ->
		{
			if (repository != null)
			{
				initializeKnownNpcs();
			}
		});
		log.debug("Extinction Man started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(forbiddenLootOverlay);
		overlayManager.remove(soulAnimationOverlay);
		overlayManager.remove(extinctionCelebrationOverlay);
		overlayManager.remove(soulPointCelebrationOverlay);
		overlayManager.remove(whitelistLootOverlay);
		overlayManager.remove(extinctGhostOverlay);
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
			navigationButton = null;
		}
		renderCallbackManager.unregister(renderCallback);
		forbiddenLootTracker.clear();
		soulAnimationTracker.clear();
		extinctionCelebrationTracker.clear();
		soulPointCelebrationTracker.clear();
		exterminationWaveTracker.clear();
		extinctGhostTracker.clear();
		extinctGhostModelStyler.restore();
		knownNpcs.clear();
		killCreditTracker.clear();
		clearPendingWave();
		whitelistLootTracker.clear();
		repository = null;
		log.debug("Extinction Man stopped");
	}

	@Subscribe(priority = -1)
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		knownNpcs.add(npc);
		String exactNpcName = npc.getName();
		if (repository != null && exactNpcName != null)
		{
			repository.get(exactNpcName);
		}
		discoverSpecialNpc(npc);
		refreshExtinctGhosts();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (ExtinctionManConfig.GROUP.equals(event.getGroup())
			&& !isInternalStorageKey(event.getKey()))
		{
			extinctionLogPanel.refresh();
			clientThread.invokeLater(() ->
			{
				if (repository != null)
				{
					refreshExtinctGhosts();
				}
			});
		}
	}

	static boolean isInternalStorageKey(String key)
	{
		return key != null && (key.equals("soulLogV2")
			|| key.equals("soulPointsMetaV1")
			|| key.startsWith("unsyncedSoulLogV2")
			|| key.startsWith("unsyncedSoulPointsMetaV1")
			|| key.equals("legacySoulsMigratedV2")
			|| key.startsWith("souls_"));
	}

	@Subscribe(priority = -1)
	public void onGameTick(GameTick event)
	{
		extinctGhostModelStyler.restore();
		if (pendingWaveNpc != null && client.getTickCount() >= pendingWaveExpiresAtTick)
		{
			showPendingCelebrations();
			clearPendingWave();
		}
		String updatedSlayerTask = configManager.getRSProfileConfiguration(
			SlayerConfig.GROUP_NAME, SlayerConfig.TASK_NAME_KEY);
		if (!java.util.Objects.equals(currentSlayerTask, updatedSlayerTask))
		{
			currentSlayerTask = updatedSlayerTask;
			refreshExtinctGhosts();
		}
		if (client.getGameState() == GameState.LOGGED_IN && bestiaryRegistry.scanBatch(client, 300))
		{
			extinctionLogPanel.refresh();
		}
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			itemRegistry.scanBatch(500);
			reconcileSoulPointRewards();
			if (initializeActiveHuntBaseline())
			{
				refreshExtinctGhosts();
			}
		}
	}

	private void refreshExtinctGhosts()
	{
		if (repository == null || client.getGameState() != GameState.LOGGED_IN
			|| client.getTopLevelWorldView() == null)
		{
		extinctGhostTracker.clear();
		extinctGhostModelStyler.restore();
			return;
		}
		Set<NPC> ghosts = Collections.newSetFromMap(new IdentityHashMap<>());
		for (NPC npc : knownNpcs)
		{
			String name = npc.getName();
			if (name != null && repository.isExtinctCached(name)
				&& exceptionLabel(npc) != null && !shouldHideNpc(npc))
			{
				ghosts.add(npc);
			}
		}
		extinctGhostTracker.replace(ghosts);
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!(event.getActor() instanceof NPC) || event.getHitsplat().getAmount() <= 0)
		{
			return;
		}

		NPC npc = (NPC) event.getActor();
		if (event.getHitsplat().isMine() || isIndirectPlayerDamage(npc, event))
		{
			killCreditTracker.markOwnDamage(npc, client.getTickCount());
		}
		else if (event.getHitsplat().isOthers())
		{
			killCreditTracker.markOtherDamage(npc);
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (event.getActor() != pendingWaveNpc)
		{
			return;
		}

		int animation = pendingWaveNpc.getAnimation();
		if (animation >= 0 && animation != pendingWavePreviousAnimation)
		{
			startExterminationWave(pendingWaveNpcName, animation);
			showPendingCelebrations();
			clearPendingWave();
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		knownNpcs.remove(npc);
		extinctGhostTracker.remove(npc);
		killCreditTracker.forget(npc);
		if (npc == pendingWaveNpc)
		{
			showPendingCelebrations();
			clearPendingWave();
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (event.getActor() instanceof NPC)
		{
			NPC npc = (NPC) event.getActor();
			if (SpecialEncounterRules.shouldIgnoreNpcKill(currentRegionId(), npc.getName()))
			{
				killCreditTracker.forget(npc);
				return;
			}
			if (isGodWarsSupportException(npc))
			{
				killCreditTracker.forget(npc);
				return;
			}
			KillCreditTracker.DeathClaim claim = killCreditTracker.claimDeath(npc, client.getTickCount());
			if (claim == KillCreditTracker.DeathClaim.CREDITED)
			{
				recordKill(npc);
			}
			else if (claim == KillCreditTracker.DeathClaim.CONTESTED && config.showChatMessages())
			{
				String npcName = npc.getName() == null ? "NPC" : npc.getName();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
					"<col=ff981f>Contested kill: no soul awarded for " + npcName + ".</col>", null);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		boolean inInstancedRegion = client.getTopLevelWorldView() != null
			&& client.getTopLevelWorldView().isInstance();
		if (event.getGameState() == GameState.LOADING
			&& inInstancedRegion != lastLoadingIntoInstance)
		{
			lastLoadingIntoInstance = inInstancedRegion;
			raidRewardClaimed = false;
			if (inInstancedRegion) raidBossesSeen.clear();
		}
		if (event.getGameState() == GameState.HOPPING || event.getGameState() == GameState.LOGIN_SCREEN)
		{
			currentSlayerTask = null;
			forbiddenLootTracker.clear();
			whitelistLootTracker.clear();
			soulAnimationTracker.clear();
			extinctionCelebrationTracker.clear();
			soulPointCelebrationTracker.clear();
			exterminationWaveTracker.clear();
			extinctGhostTracker.clear();
			killCreditTracker.clear();
			clearPendingWave();
			activeHuntItemId = -1;
			raidRewardClaimed = false;
			lastLoadingIntoInstance = false;
			raidBossesSeen.clear();
			gwdCompletionLootAllowed.clear();
			knownNpcs.clear();
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			initializeKnownNpcs();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (raidRewardClaimed || repository == null)
		{
			return;
		}

		String raidName;
		int rewardInventory;
		switch (event.getGroupId())
		{
			case InterfaceID.RAIDS_REWARDS:
				raidName = SpecialEncounterRules.CHAMBERS_OF_XERIC;
				rewardInventory = InventoryID.RAIDS_REWARDS;
				break;
			case InterfaceID.TOB_CHESTS:
				raidName = SpecialEncounterRules.THEATRE_OF_BLOOD;
				rewardInventory = InventoryID.TOB_CHESTS;
				break;
			case InterfaceID.TOA_CHESTS:
				raidName = SpecialEncounterRules.TOMBS_OF_AMASCUT;
				rewardInventory = InventoryID.TOA_CHESTS;
				break;
			case InterfaceID.COLOSSEUM_REWARD_CHEST_2:
				ItemContainer colosseumRewards = client.getItemContainer(
					InventoryID.COLOSSEUM_REWARDS);
				if (colosseumRewards != null)
				{
					raidRewardClaimed = true;
					completeSpecialHuntIfRewarded("Sol Heredit", colosseumRewards);
				}
				return;
			default:
				return;
		}

		if (!raidName.equals(currentRaidName()))
		{
			return;
		}
		ItemContainer rewards = client.getItemContainer(rewardInventory);
		if (rewards == null)
		{
			return;
		}
		raidRewardClaimed = true;
		recordRaidReward(raidName);
		completeSpecialHuntIfRewarded(raidName, rewards);
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		if (repository != null)
		{
			repository.reloadProfile();
			metaProgressRepository.reloadProfile();
			activeHuntItemId = -1;
			refreshExtinctGhosts();
			extinctionLogPanel.refresh();
		}
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived event)
	{
		NPC npc = event.getNpc();
		if (npc != null && gwdCompletionLootAllowed.remove(npc))
		{
			return;
		}
		if (currentRaidName() != null)
		{
			return;
		}
		if (npc != null && !event.getItems().isEmpty() && exceptionLabel(npc) != null)
		{
			for (ItemStack item : event.getItems())
			{
				WorldPoint worldPoint = npc.getWorldLocation();
				WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
				int canonicalItemId = itemManager.canonicalize(item.getId());
				if (hunt != null && hunt.getSourceNpcName().equals(npc.getName())
					&& matchesSoulItem(hunt, canonicalItemId))
				{
					whitelistLootTracker.add(worldPoint, canonicalItemId,
						hunt.getItemName(), client.getTickCount());
				}
				if (isLootAllowed(npc, item.getId()))
				{
					continue;
				}
				String itemName = itemManager.getItemComposition(item.getId()).getName();
				forbiddenLootTracker.add(worldPoint, item.getId(), itemName, client.getTickCount());
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV)
		{
			return;
		}
		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		if (hunt == null || activeHuntItemId != hunt.getItemId())
		{
			return;
		}
		int currentCount = countSoulItem(event.getItemContainer(), hunt);
		if (currentCount > activeHuntInventoryBaseline && whitelistHuntService.completeActiveHunt())
		{
			activeHuntItemId = -1;
			whitelistLootTracker.clear();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				"<col=4bcd5f>SOUL ITEM OBTAINED — " + hunt.getItemName()
					+ ". Soul Item hunt complete.</col>", null);
			extinctionLogPanel.refresh();
		}
	}

	private void recordKill(NPC npc)
	{
		String exactNpcName = npc == null ? null : npc.getName();
		if (repository == null || exactNpcName == null || exactNpcName.trim().isEmpty())
		{
			return;
		}
		if (SpecialEncounterRules.shouldIgnoreNpcKill(currentRegionId(), exactNpcName))
		{
			return;
		}

		String exceptionLabel = exceptionLabel(npc);
		if (exceptionLabel != null)
		{
			boolean lootForbidden = forbidsLoot(npc);
			String lootRule = "Soul Item hunt".equals(exceptionLabel)
				? "Only the selected Soul Item may be taken."
				: (lootForbidden ? "Any loot from this kill is FORBIDDEN."
					: "Loot is allowed for this mandatory kill.");
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				"<col=ff2d2d>" + exceptionLabel + ": " + exactNpcName
					+ " grants no soul. " + lootRule + "</col>", null);
			return;
		}

		SoulProgress before = repository.get(exactNpcName);
		SoulProgress after = repository.gainSoul(exactNpcName);
		if (!before.isExtinct() && after.isExtinct()
			&& SpecialEncounterRules.isGodWarsSupportNpc(currentRegionId(), exactNpcName))
		{
			gwdCompletionLootAllowed.add(npc);
		}
		if (!before.isExtinct())
		{
			soulAnimationTracker.add(npc.getWorldLocation(), System.currentTimeMillis());
		}
		if (!before.isExtinct() && after.isExtinct())
		{
			SoulPointAward award = metaProgressRepository.award(exactNpcName);
			queueExterminationWave(npc, exactNpcName, award.isSoulPointEarned(),
				metaProgressRepository.getAvailablePoints());
		}
		extinctionLogPanel.refresh();
		if (!config.showChatMessages() || before.isExtinct())
		{
			return;
		}

		String message = after.isExtinct()
			? "<col=dc5050>" + exactNpcName + " is EXTINCT — 100/100 souls.</col>"
			: "<col=aae1ff>+1 " + exactNpcName + " Soul — " + after.getSouls() + "/100</col>";
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
	}

	private boolean isIndirectPlayerDamage(NPC npc, HitsplatApplied event)
	{
		if (event.getHitsplat().isOthers() || client.getLocalPlayer() == null)
		{
			return false;
		}
		return npc.getInteracting() == client.getLocalPlayer()
			|| client.getLocalPlayer().getInteracting() == npc;
	}

	private void showExtinctionCelebration(String npcName, boolean soulPointAwarded)
	{
		extinctionCelebrationTracker.show(npcName, System.currentTimeMillis(), soulPointAwarded);
	}

	private void showPendingCelebrations()
	{
		showExtinctionCelebration(pendingWaveNpcName, pendingWaveSoulPointAwarded);
		if (pendingWaveSoulPointAwarded)
		{
			soulPointCelebrationTracker.show(pendingWaveBankedSoulPoints,
				System.currentTimeMillis());
		}
	}

	private void queueExterminationWave(NPC npc, String exactNpcName,
		boolean soulPointAwarded, int bankedSoulPoints)
	{
		pendingWaveNpc = npc;
		pendingWaveNpcName = exactNpcName;
		pendingWavePreviousAnimation = npc.getAnimation();
		pendingWaveExpiresAtTick = client.getTickCount() + 3;
		pendingWaveSoulPointAwarded = soulPointAwarded;
		pendingWaveBankedSoulPoints = bankedSoulPoints;
	}

	private void clearPendingWave()
	{
		pendingWaveNpc = null;
		pendingWaveNpcName = null;
		pendingWavePreviousAnimation = -1;
		pendingWaveExpiresAtTick = 0;
		pendingWaveSoulPointAwarded = false;
		pendingWaveBankedSoulPoints = 0;
	}

	private void startExterminationWave(String exactNpcName, int deathAnimation)
	{
		if (deathAnimation < 0 || client.getTopLevelWorldView() == null)
		{
			return;
		}

		exterminationWaveTracker.start(exactNpcName, client.getTickCount());
		for (NPC nearbyNpc : client.getTopLevelWorldView().npcs())
		{
			if (exactNpcName.equals(nearbyNpc.getName()))
			{
				nearbyNpc.setAnimation(deathAnimation);
				nearbyNpc.setAnimationFrame(0);
			}
		}
	}

	private boolean shouldDraw(Renderable renderable, boolean drawingUi)
	{
		if (!(renderable instanceof NPC) || repository == null)
		{
			return true;
		}

		NPC npc = (NPC) renderable;
		if (extinctGhostTracker.contains(npc))
		{
			extinctGhostModelStyler.style(npc);
		}
		if (exterminationWaveTracker.isAnimating(npc.getName(), client.getTickCount()))
		{
			return true;
		}
		return !shouldHideNpc(npc);
	}

	private boolean shouldHideNpc(NPC npc)
	{
		String exactNpcName = npc.getName();
		int regionId = currentRegionId();
		String gauntletBoss = SpecialEncounterRules.gauntletBossForRegion(regionId);
		if (gauntletBoss != null)
		{
			return repository.isExtinctCached(gauntletBoss)
				&& !isActiveHuntSource(gauntletBoss);
		}
		if (SpecialEncounterRules.raidAtRegion(regionId) != null
			|| SpecialEncounterRules.shouldAlwaysRemainVisible(regionId, exactNpcName)
			|| (regionId == SpecialEncounterRules.FIGHT_CAVES_REGION
				&& !SpecialEncounterRules.TZTOK_JAD.equals(exactNpcName))
			|| (regionId == SpecialEncounterRules.INFERNO_REGION
				&& !SpecialEncounterRules.TZKAL_ZUK.equals(exactNpcName)))
		{
			return false;
		}
		return ExtinctionVisibility.shouldHide(
			exactNpcName,
			config.hideExtinctNpcs(),
			repository.isExtinctCached(exactNpcName),
			exceptionLabel(npc) != null);
	}

	private String exceptionLabel(NPC npc)
	{
		if (isGodWarsSupportException(npc))
		{
			return "God Wars essence exception";
		}
		if (isMandatoryException(npc))
		{
			return "Mandatory exception";
		}
		if (isWhitelistHuntException(npc))
		{
			return "Soul Item hunt";
		}
		if (isSlayerException(npc))
		{
			return "Slayer exception";
		}
		return null;
	}

	private boolean forbidsLoot(NPC npc)
	{
		return isGodWarsSupportException(npc)
			|| isSlayerException(npc)
			|| (isMandatoryException(npc) && !config.allowMandatoryExceptionLoot());
	}

	private boolean isLootAllowed(NPC npc, int itemId)
	{
		if (isMandatoryException(npc) && config.allowMandatoryExceptionLoot())
		{
			return true;
		}
		int canonicalItemId = itemManager.canonicalize(itemId);
		String itemName = cleanItemName(canonicalItemId);
		return whitelistHuntService.isWhitelisted(canonicalItemId, itemName);
	}

	private boolean isWhitelistHuntException(NPC npc)
	{
		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		return npc != null && hunt != null && (hunt.getSourceNpcName().equals(npc.getName())
			|| hunt.getSourceNpcName().equals(currentRaidName()));
	}

	private boolean isGodWarsSupportException(NPC npc)
	{
		return npc != null && repository != null
			&& repository.isExtinctCached(npc.getName())
			&& SpecialEncounterRules.isGodWarsSupportNpc(currentRegionId(), npc.getName());
	}

	private boolean isActiveHuntSource(String sourceName)
	{
		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		return hunt != null && sourceName.equals(hunt.getSourceNpcName());
	}

	private int currentRegionId()
	{
		if (client.getLocalPlayer() == null || client.getLocalPlayer().getLocalLocation() == null)
		{
			return -1;
		}
		return WorldPoint.fromLocalInstance(client,
			client.getLocalPlayer().getLocalLocation()).getRegionID();
	}

	private String currentRaidName()
	{
		return SpecialEncounterRules.raidAtRegion(currentRegionId());
	}

	private void recordRaidReward(String raidName)
	{
		String newlyExtinctTarget = null;
		boolean soulPointEarned = false;
		for (String bossName : raidBossesSeen)
		{
			SoulProgress bossBefore = repository.get(bossName);
			SoulProgress bossAfter = repository.gainSoul(bossName);
			if (!bossBefore.isExtinct() && bossAfter.isExtinct())
			{
				SoulPointAward bossAward = metaProgressRepository.award(bossName);
				if (newlyExtinctTarget == null) newlyExtinctTarget = bossName;
				soulPointEarned |= bossAward.isSoulPointEarned();
			}
		}
		SoulProgress before = repository.get(raidName);
		SoulProgress after = before.isExtinct() ? before : repository.gainSoul(raidName);
		if (!before.isExtinct() && after.isExtinct())
		{
			SoulPointAward award = metaProgressRepository.award(raidName);
			newlyExtinctTarget = raidName;
			soulPointEarned |= award.isSoulPointEarned();
		}
		if (newlyExtinctTarget != null)
		{
			long now = System.currentTimeMillis();
			if (SpecialEncounterRules.isRaidName(newlyExtinctTarget))
				extinctionCelebrationTracker.showRaid(newlyExtinctTarget, now, soulPointEarned);
			else
				extinctionCelebrationTracker.show(newlyExtinctTarget, now, soulPointEarned);
			if (soulPointEarned)
				soulPointCelebrationTracker.show(metaProgressRepository.getAvailablePoints(), now);
		}
		raidBossesSeen.clear();
		extinctionLogPanel.refresh();
		if (config.showChatMessages() && !before.isExtinct())
		{
			String message = after.isExtinct()
				? "<col=dc5050>" + raidName + " is EXTINCT — 100/100 rewards.</col>"
				: "<col=aae1ff>+1 " + raidName + " Reward — " + after.getSouls() + "/100</col>";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		}
	}

	private void completeSpecialHuntIfRewarded(String sourceName, ItemContainer rewards)
	{
		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		if (hunt == null || !sourceName.equals(hunt.getSourceNpcName())) return;
		for (Item item : rewards.getItems())
		{
			if (item.getId() >= 0 && matchesSoulItem(hunt, item.getId()))
			{
				if (whitelistHuntService.completeActiveHunt())
				{
					activeHuntItemId = -1;
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
						"<col=4bcd5f>SOUL ITEM OBTAINED — " + hunt.getItemName()
							+ ". Soul Item hunt complete.</col>", null);
					extinctionLogPanel.refresh();
				}
				return;
			}
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
			"<col=ff2d2d>SOUL ITEM NOT FOUND — all rewards from this raid are FORBIDDEN.</col>", null);
	}

	private void reconcileSoulPointRewards()
	{
		if (!metaProgressRepository.needsInitialReconciliation())
		{
			return;
		}
		Set<String> completedSpecies = new LinkedHashSet<>();
		for (SoulProgress progress : repository.getAll())
		{
			if (progress.isExtinct()
				&& !bestiaryRegistry.isExcludedSpecialNpc(progress.getNpcName()))
			{
				completedSpecies.add(progress.getNpcName());
			}
		}
		metaProgressRepository.completeInitialReconciliation(completedSpecies);
	}

	private void initializeKnownNpcs()
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.getTopLevelWorldView() == null)
		{
			return;
		}
		knownNpcs.clear();
		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			knownNpcs.add(npc);
			discoverSpecialNpc(npc);
		}
		refreshExtinctGhosts();
	}

	private void discoverSpecialNpc(NPC npc)
	{
		int regionId = currentRegionId();
		boolean raid = SpecialEncounterRules.raidAtRegion(regionId) != null;
		boolean fightCaves = regionId == SpecialEncounterRules.FIGHT_CAVES_REGION;
		boolean inferno = regionId == SpecialEncounterRules.INFERNO_REGION;
		boolean specialActivity = SpecialEncounterRules.shouldExcludeEncounterNpc(regionId, "Test NPC");
		if (!raid && !fightCaves && !inferno && !specialActivity
			&& regionId != SpecialEncounterRules.FORTIS_COLOSSEUM_REGION
			&& regionId != SpecialEncounterRules.GAUNTLET_REGION
			&& regionId != SpecialEncounterRules.CORRUPTED_GAUNTLET_REGION) return;

		String raidName = SpecialEncounterRules.raidAtRegion(regionId);
		String name = npc.getName();
		String raidBoss = SpecialEncounterRules.canonicalRaidBoss(raidName, name);
		if (raidBoss != null)
		{
			raidBossesSeen.add(raidBoss);
			return;
		}
		boolean keepBoss = (fightCaves && SpecialEncounterRules.TZTOK_JAD.equals(name))
			|| (inferno && SpecialEncounterRules.TZKAL_ZUK.equals(name));
		if (!keepBoss && (raid || SpecialEncounterRules.shouldExcludeEncounterNpc(regionId, name))
			&& bestiaryRegistry.excludeSpecialNpc(name))
		{
			extinctionLogPanel.refresh();
		}
	}

	private boolean initializeActiveHuntBaseline()
	{
		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		if (hunt == null)
		{
			boolean changed = activeHuntItemId != -1;
			activeHuntItemId = -1;
			return changed;
		}
		if (activeHuntItemId != hunt.getItemId())
		{
			activeHuntItemId = hunt.getItemId();
			ItemContainer inventory = client.getItemContainer(InventoryID.INV);
			activeHuntInventoryBaseline = countSoulItem(inventory, hunt);
			return true;
		}
		return false;
	}

	private int countSoulItem(ItemContainer container, WhitelistUnlock hunt)
	{
		if (container == null) return 0;
		int count = 0;
		for (Item item : container.getItems())
		{
			if (item.getId() >= 0 && matchesSoulItem(hunt, item.getId()))
			{
				count += item.getQuantity();
			}
		}
		return count;
	}

	private boolean matchesSoulItem(WhitelistUnlock hunt, int itemId)
	{
		int canonicalItemId = itemManager.canonicalize(itemId);
		return hunt.matchesItem(canonicalItemId, cleanItemName(canonicalItemId));
	}

	private String cleanItemName(int itemId)
	{
		return Text.removeTags(itemManager.getItemComposition(itemId).getName()).trim();
	}

	private boolean isMandatoryException(NPC npc)
	{
		return npc != null && MandatoryExceptionMatcher.matches(
			config.mandatoryExceptionMode(), config.mandatoryExceptionNpc(), npc.getName());
	}

	private boolean isSlayerException(NPC npc)
	{
		if (npc == null)
		{
			return false;
		}
		String exactNpcName = npc.getName();
		boolean extinct = repository != null && repository.isExtinctCached(exactNpcName);
		if (!extinct)
		{
			return false;
		}

		return SlayerTaskMatcher.matches(currentSlayerTask, exactNpcName);
	}

	@Provides
	ExtinctionManConfig provideConfig(ConfigManager manager)
	{
		return manager.getConfig(ExtinctionManConfig.class);
	}

	@Provides
	SoulRepository provideSoulRepository(ConfigManager manager)
	{
		if (repository == null)
		{
			repository = new SoulRepository(manager);
		}
		return repository;
	}
}

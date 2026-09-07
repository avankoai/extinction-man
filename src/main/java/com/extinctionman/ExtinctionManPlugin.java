package com.extinctionman;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import net.runelite.api.ScriptID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.NpcLootReceived;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.events.ServerNpcLoot;
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
	private static final String BARROWS_REWARD_ACTIVITY = "Barrows reward chest";
	private static final String MOONS_REWARD_ACTIVITY = "Lunar Chest";
	@Inject private Client client;
	@Inject @javax.inject.Named("developerMode") private boolean developerMode;
	@Inject private MilestoneAudio milestoneAudio;
	@Inject private CompletionLootAllowance completionLootAllowance;
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
	@Inject private SoulItemFoundOverlay soulItemFoundOverlay;
	@Inject private SoulItemFoundTracker soulItemFoundTracker;
	@Inject private SoulItemPickupAuthorization soulItemPickupAuthorization;
	@Inject private SoulItemRewardAuthorization soulItemRewardAuthorization;
	@Inject private DirectSoulLootTracker directSoulLootTracker;
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
	@Inject private NextKillTestBoost nextKillTestBoost;


	private SoulRepository repository;
	private String currentSlayerTask;
	private NavigationButton navigationButton;
	private NPC pendingWaveNpc;
	private String pendingWaveNpcName;
	private int pendingWavePreviousAnimation;
	private int pendingWaveExpiresAtTick;
	private boolean pendingWaveSoulPointAwarded;
	private boolean pendingWavePermit;
	private final Map<Integer, Integer> activeHuntInventoryBaselines = new java.util.HashMap<>();
	private String activeSoulState;
	private boolean raidRewardClaimed;
	private boolean lastLoadingIntoInstance;
	private int activeRewardGroupId = -1;
	private String activeRewardSource;
	private final CompletionRewardAllowance completionRewardAllowance = new CompletionRewardAllowance();
	private final SoulItemPopupGate soulItemPopupGate = new SoulItemPopupGate();
	private final Set<String> raidBossesSeen = new LinkedHashSet<>();
	private final Set<NPC> gwdCompletionLootAllowed =
		Collections.newSetFromMap(new IdentityHashMap<>());
	private final Set<NPC> knownNpcs = Collections.newSetFromMap(new IdentityHashMap<>());
	private final Set<NPC> deadNpcsAwaitingAnimation =
		Collections.newSetFromMap(new IdentityHashMap<>());
	private final Map<String, Integer> lastDeathAnimationByNpcName = new java.util.HashMap<>();
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
		overlayManager.add(soulItemFoundOverlay);
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
		milestoneAudio.clear();
		overlayManager.remove(overlay);
		overlayManager.remove(forbiddenLootOverlay);
		overlayManager.remove(soulAnimationOverlay);
		overlayManager.remove(extinctionCelebrationOverlay);
		overlayManager.remove(soulItemFoundOverlay);
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
		soulItemFoundTracker.clear();
		soulItemPickupAuthorization.clear();
		soulItemRewardAuthorization.clear();
		soulItemPopupGate.clear();
		directSoulLootTracker.clear();
		exterminationWaveTracker.clear();
		extinctGhostTracker.clear();
		nextKillTestBoost.clear();

		knownNpcs.clear();
		killCreditTracker.clear();
		clearPendingWave();
		whitelistLootTracker.clear();
		completionLootAllowance.clear();
		activeHuntInventoryBaselines.clear();
		activeSoulState = null;
		activeRewardGroupId = -1;
		activeRewardSource = null;
		completionRewardAllowance.clear();
		raidRewardClaimed = false;
		raidBossesSeen.clear();
		gwdCompletionLootAllowed.clear();
		deadNpcsAwaitingAnimation.clear();
		lastDeathAnimationByNpcName.clear();
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
		extinctGhostTracker.update(npc, isExtinctGhost(npc), exceptionSoulType(npc));
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (ExtinctionManConfig.GROUP.equals(event.getGroup())
			&& !isInternalStorageKey(event.getKey()))
		{
			clientThread.invoke(() ->
			{
				if (repository != null)
				{
					refreshExtinctGhosts();
				}
			});
			extinctionLogPanel.refresh();
		}
	}

	static boolean isInternalStorageKey(String key)
	{
		return key != null && (key.equals("soulLogV2")
			|| key.equals("soulPointsMetaV1")
			|| key.equals("monsterCatalogV1")
			|| key.equals("newMonstersV1")
			|| key.startsWith("unsyncedSoulLogV2")
			|| key.startsWith("unsyncedSoulPointsMetaV1")
			|| key.equals("legacySoulsMigratedV2")
			|| key.startsWith("souls_"));
	}

	@Subscribe(priority = -1)
	public void onGameTick(GameTick event)
	{
		completePendingDirectSoulLoot();

		if (pendingWaveNpc != null && client.getTickCount() >= pendingWaveExpiresAtTick)
		{
			int animation = pendingWaveNpc.getAnimation();
			if (animation < 0)
			{
				Integer remembered = lastDeathAnimationByNpcName.get(
					normalizedNpcName(pendingWaveNpcName));
				animation = remembered == null ? -1 : remembered;
			}
			finishPendingWave(animation);
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
			String soulState = activeSoulStateSignature();
			boolean soulsChanged = !java.util.Objects.equals(activeSoulState, soulState);
			activeSoulState = soulState;
			if (initializeActiveHuntBaselines() || soulsChanged)
			{
				refreshExtinctGhosts();
			}
		}
	}

	@Subscribe
	public void onClientTick(net.runelite.api.events.ClientTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;
		long now = System.currentTimeMillis();
		milestoneAudio.update(extinctionCelebrationTracker.active(now),
			soulItemFoundTracker.active(now));
	}

	private void refreshExtinctGhosts()
	{

		if (repository == null || client.getGameState() != GameState.LOGGED_IN
			|| client.getTopLevelWorldView() == null)
		{
			extinctGhostTracker.clear();
			return;
		}
		Map<NPC, SoulExceptionType> ghosts = new IdentityHashMap<>();
		for (NPC npc : knownNpcs)
		{
			if (isExtinctGhost(npc))
			{
				ghosts.put(npc, exceptionSoulType(npc));
			}
		}
		extinctGhostTracker.replace(ghosts);
	}

	private boolean isExtinctGhost(NPC npc)
	{
		String name = npc.getName();
		if (pendingWavePermit && pendingWaveNpc != null
			&& java.util.Objects.equals(pendingWaveNpcName, name))
		{
			return true;
		}
		return repository != null && name != null && ExtinctionVisibility.shouldGhost(
			repository.isExtinctCached(name), exceptionLabel(npc) != null, shouldHideNpc(npc));
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
		if (event.getActor() instanceof NPC)
		{
			NPC npc = (NPC) event.getActor();
			if (deadNpcsAwaitingAnimation.remove(npc) && npc.getAnimation() >= 0
				&& npc.getName() != null)
			{
				lastDeathAnimationByNpcName.put(normalizedNpcName(npc.getName()), npc.getAnimation());
			}
		}
		if (event.getActor() != pendingWaveNpc)
		{
			return;
		}

		int animation = pendingWaveNpc.getAnimation();
		if (animation >= 0 && animation != pendingWavePreviousAnimation)
		{
			finishPendingWave(animation);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		knownNpcs.remove(npc);
		deadNpcsAwaitingAnimation.remove(npc);
		extinctGhostTracker.remove(npc);
		killCreditTracker.forget(npc);
		if (npc == pendingWaveNpc)
		{
			Integer remembered = lastDeathAnimationByNpcName.get(normalizedNpcName(pendingWaveNpcName));
			finishPendingWave(remembered == null ? npc.getAnimation() : remembered);
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (event.getActor() instanceof NPC)
		{
			NPC npc = (NPC) event.getActor();
			deadNpcsAwaitingAnimation.add(npc);
			if (npc.getAnimation() >= 0 && npc.getName() != null)
			{
				lastDeathAnimationByNpcName.put(normalizedNpcName(npc.getName()), npc.getAnimation());
			}
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
			KillCreditTracker.DeathClaim claim = killCreditTracker.claimDeath(npc, client.getTickCount(),
				SpecialEncounterRules.allowsSharedKillCredit(npc.getName()));
			if (claim == KillCreditTracker.DeathClaim.CREDITED)
			{
				authorizeGuaranteedEncounterReward(npc);
				if (!consumeSoulPermitKill(npc))
				{
					recordKill(npc);
				}
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
			milestoneAudio.clear();
			currentSlayerTask = null;
			forbiddenLootTracker.clear();
			whitelistLootTracker.clear();
			soulAnimationTracker.clear();
			extinctionCelebrationTracker.clear();
			soulItemFoundTracker.clear();
			soulItemPickupAuthorization.clear();
			soulItemRewardAuthorization.clear();
			soulItemPopupGate.clear();
			directSoulLootTracker.clear();
			exterminationWaveTracker.clear();
			extinctGhostTracker.clear();
			nextKillTestBoost.clear();
			killCreditTracker.clear();
			clearPendingWave();
			activeHuntInventoryBaselines.clear();
			activeSoulState = null;
			activeRewardGroupId = -1;
			activeRewardSource = null;
			completionRewardAllowance.clear();
			raidRewardClaimed = false;
			lastLoadingIntoInstance = false;
			raidBossesSeen.clear();
			gwdCompletionLootAllowed.clear();
			completionLootAllowance.clear();
			knownNpcs.clear();
			deadNpcsAwaitingAnimation.clear();
			lastDeathAnimationByNpcName.clear();
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			initializeKnownNpcs();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (repository == null)
		{
			return;
		}
		if (event.getGroupId() == InterfaceID.BARROWS_REWARD)
		{
			setActiveReward(event.getGroupId(), BARROWS_REWARD_ACTIVITY);
			completeActivityChestHunt(InventoryID.TRAIL_REWARDINV, true);
			return;
		}
		if (event.getGroupId() == InterfaceID.PMOON_REWARD)
		{
			setActiveReward(event.getGroupId(), MOONS_REWARD_ACTIVITY);
			completeActivityChestHunt(InventoryID.PMOON_REWARDINV, false);
			return;
		}
		if (event.getGroupId() == (InterfaceID.DomEndLevelUi.UNIVERSE >>> 16))
		{
			setActiveReward(event.getGroupId(), SpecialEncounterRules.DOOM_OF_MOKHAIOTL);
			ItemContainer doomRewards = client.getItemContainer(InventoryID.DOM_LOOTPILE);
			if (doomRewards == null)
			{
				doomRewards = client.getItemContainer(InventoryID.DOM_LOOTPILE_DURING);
			}
			if (doomRewards != null)
			{
				completeSpecialHuntIfRewarded(SpecialEncounterRules.DOOM_OF_MOKHAIOTL,
					doomRewards);
			}
			return;
		}
		if (raidRewardClaimed)
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
				setActiveReward(event.getGroupId(), SpecialEncounterRules.SOL_HEREDIT);
				ItemContainer colosseumRewards = client.getItemContainer(
					InventoryID.COLOSSEUM_REWARDS);
				if (colosseumRewards != null)
				{
					raidRewardClaimed = true;
					completeSpecialHuntIfRewarded(SpecialEncounterRules.SOL_HEREDIT,
						colosseumRewards);
				}
				return;
			default:
				return;
		}
		setActiveReward(event.getGroupId(), raidName);

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
		recordEncounterReward(raidName);
		completeSpecialHuntIfRewarded(raidName, rewards);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (repository == null || event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}
		String source = SpecialEncounterRules.rewardClaimSourceForChatMessage(
			Text.removeTags(event.getMessage()));
		if (source != null)
		{
			recordEncounterReward(source);
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (repository != null && event.getScriptId() == ScriptID.DOM_LOOT_CLAIM)
		{
			recordEncounterReward(SpecialEncounterRules.DOOM_OF_MOKHAIOTL);
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == activeRewardGroupId)
		{
			completionRewardAllowance.close(activeRewardSource);
			activeRewardGroupId = -1;
			activeRewardSource = null;
		}
	}

	private void authorizeGuaranteedEncounterReward(NPC npc)
	{
		if (npc.getName() == null) return;
		for (WhitelistUnlock hunt : activeHuntsForSource(npc.getName()))
		{
			if (GuaranteedEncounterRewardRules.matches(npc.getName(), hunt.getItemName()))
			{
				soulItemRewardAuthorization.authorize(
					hunt.getItemId(), hunt.getSourceNpcName(), client.getTickCount());
			}
		}
	}

	private void completeActivityChestHunt(int inventoryId, boolean barrows)
	{
		ItemContainer rewards = client.getItemContainer(inventoryId);
		if (rewards == null) return;
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			if (barrows ? !SpecialEncounterRules.isBarrowsBrother(hunt.getSourceNpcName())
				: !SpecialEncounterRules.isMoonBoss(hunt.getSourceNpcName())) continue;
			int killedVarbit = rewardSourceKilledVarbit(hunt.getSourceNpcName());
			if (killedVarbit >= 0 && client.getVarbitValue(killedVarbit) != 0)
				completeSpecialHuntIfRewarded(hunt.getSourceNpcName(), rewards);
		}
	}

	private static int rewardSourceKilledVarbit(String sourceName)
	{
		switch (sourceName)
		{
			case "Ahrim the Blighted": return VarbitID.BARROWS_KILLED_AHRIM;
			case "Dharok the Wretched": return VarbitID.BARROWS_KILLED_DHAROK;
			case "Guthan the Infested": return VarbitID.BARROWS_KILLED_GUTHAN;
			case "Karil the Tainted": return VarbitID.BARROWS_KILLED_KARIL;
			case "Torag the Corrupted": return VarbitID.BARROWS_KILLED_TORAG;
			case "Verac the Defiled": return VarbitID.BARROWS_KILLED_VERAC;
			case "Blood Moon": return VarbitID.PMOON_BOSS_BLOOD_DEAD;
			case "Blue Moon": return VarbitID.PMOON_BOSS_BLUE_DEAD;
			case "Eclipse Moon": return VarbitID.PMOON_BOSS_ECLIPSE_DEAD;
			default: return -1;
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		nextKillTestBoost.clear();
		if (repository != null)
		{
			repository.reloadProfile();
			metaProgressRepository.reloadProfile();
			prioritizeSavedProgress();
			activeHuntInventoryBaselines.clear();
			activeSoulState = null;
			soulItemPopupGate.clear();
			refreshExtinctGhosts();
			extinctionLogPanel.refresh();
		}
	}

	@Subscribe
	public void onServerNpcLoot(ServerNpcLoot event)
	{
		if (event.getComposition() == null) return;
		String rawSourceName = event.getComposition().getName();
		if (rawSourceName == null) return;
		String sourceName = Text.removeTags(rawSourceName).trim();
		for (ItemStack item : event.getItems())
		{
			int canonicalItemId = itemManager.canonicalize(item.getId());
			WhitelistUnlock hunt = activeHuntForItem(sourceName, canonicalItemId);
			if (hunt != null)
			{
				directSoulLootTracker.observe(sourceName, canonicalItemId, client.getTickCount());
			}
		}
	}

	@Subscribe
	public void onNpcLootReceived(NpcLootReceived event)
	{
		NPC npc = event.getNpc();
		rememberDeathAnimation(npc);
		if (npc != null && (completionLootAllowance.consume(npc)
			|| gwdCompletionLootAllowed.remove(npc)))
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
				int canonicalItemId = itemManager.canonicalize(item.getId());
				WhitelistUnlock hunt = activeHuntForItem(npc.getName(), canonicalItemId);
				if (hunt != null)
				{
					directSoulLootTracker.markGround(npc.getName(), canonicalItemId,
						client.getTickCount());
					whitelistLootTracker.add(worldPoint, item.getId(),
						hunt.getItemName(), hunt.getSourceNpcName(), client.getTickCount());
					showSoulItemFoundOnce(hunt, hunt.getSourceNpcName());
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
	public void onItemDespawned(ItemDespawned event)
	{
		WorldPoint worldPoint = event.getTile().getWorldLocation();
		int itemId = event.getItem().getId();
		whitelistLootTracker.remove(worldPoint, itemId);
		forbiddenLootTracker.remove(worldPoint, itemId);
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		int canonicalItemId = itemManager.canonicalize(event.getItem().getId());
		String source = soulItemRewardAuthorization.source(canonicalItemId, client.getTickCount());
		WhitelistUnlock hunt = activeHuntForItem(source, canonicalItemId);
		if (hunt == null) return;
		whitelistLootTracker.add(event.getTile().getWorldLocation(), event.getItem().getId(),
			hunt.getItemName(), hunt.getSourceNpcName(), client.getTickCount());
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV)
		{
			return;
		}
		for (WhitelistUnlock representative : distinctActiveHuntsByItem())
		{
			int itemId = representative.getItemId();
			int currentCount = countSoulItem(event.getItemContainer(), representative);
			int previousCount = activeHuntInventoryBaselines.getOrDefault(itemId, currentCount);
			activeHuntInventoryBaselines.put(itemId, currentCount);
			if (currentCount <= previousCount) continue;
			String markedSource = soulItemPickupAuthorization.consumeSource(itemId,
				client.getTickCount());
			boolean markedGroundPickup = markedSource != null;
			String verifiedRewardSource = soulItemRewardAuthorization.consume(itemId,
				client.getTickCount());
			String verifiedDirectSource = directSoulLootTracker.claimInventoryIncrease(itemId,
				client.getTickCount());
			String source = verifiedRewardSource != null ? verifiedRewardSource
				: verifiedDirectSource != null ? verifiedDirectSource
				: markedSource == null || markedSource.isEmpty() ? null : markedSource;
			WhitelistUnlock hunt = source == null ? firstActiveHuntForItem(itemId)
				: activeHuntForItem(source, itemId);
			if (hunt != null && (markedGroundPickup || source != null))
			{
				completeVerifiedSoulHunt(hunt, source);
			}
		}
	}

	private void forbidRemainingSoulItems(WhitelistUnlock completed)
	{
		for (WhitelistLootMarker marker : whitelistLootTracker.drainMatching(
			completed.getSourceNpcName(), completed.getItemId(), client.getTickCount()))
		{
			forbiddenLootTracker.add(marker.getWorldPoint(), marker.getItemId(),
				marker.getItemName(), client.getTickCount());
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
			return;
		}

		SoulProgress before = repository.get(exactNpcName);
		bestiaryRegistry.markValidated(exactNpcName);
		boolean boostedKill = developerMode && nextKillTestBoost.consume();
		SoulProgress after = repository.gainSouls(exactNpcName,
			killSoulAmount(developerMode, boostedKill));
		if (!before.isExtinct() && after.isExtinct()
			&& SpecialEncounterRules.isGodWarsSupportNpc(currentRegionId(), exactNpcName))
		{
			gwdCompletionLootAllowed.add(npc);
		}
		if (!before.isExtinct() && !after.isExtinct())
		{
			soulAnimationTracker.add(npc.getWorldLocation(), System.currentTimeMillis());
		}
		if (!before.isExtinct() && after.isExtinct())
		{
			completionLootAllowance.mark(npc);
			String rewardSource = rewardSourceForCompletedNpc(exactNpcName);
			if (rewardSource != null && isRewardSourceExtinct(rewardSource))
			{
				allowCompletionReward(rewardSource);
			}
			SoulPointAward award = metaProgressRepository.award(exactNpcName);
			queueExterminationWave(npc, exactNpcName, award.isSoulPointEarned());
		}
		extinctionLogPanel.refresh();
		if (!config.showChatMessages() || before.isExtinct())
		{
			return;
		}

		String message = after.isExtinct()
			? ExtinctionChatMessage.random(exactNpcName)
			: "<col=005f73>+" + (after.getSouls() - before.getSouls()) + " " + exactNpcName
				+ " Soul — " + after.getSouls() + "/100</col>";
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
	}

	private boolean consumeSoulPermitKill(NPC npc)
	{
		if (npc != null && isActiveHuntSource(npc.getName())) return false;
		SoulPermit permit = npc == null ? null : metaProgressRepository.findActivePermit(npc.getName());
		if (npc == null || permit == null || !permit.matches(npc.getName())) return false;
		completionLootAllowance.mark(npc);
		String rewardSource = rewardSourceForCompletedNpc(permit.getSourceName());
		if (rewardSource != null) allowCompletionReward(rewardSource);
		if (!metaProgressRepository.consumePermit(permit.getSourceName())) return false;
		SoulPermit remaining = metaProgressRepository.findActivePermit(permit.getSourceName());
		if (remaining == null) queuePermitWave(npc, permit.getSourceName());
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
			"<col=9b6fd3>" + (permit.isBoss() ? "STRONG" : "WEAK") + " SOUL USED — "
				+ permit.getSourceName() + (remaining == null ? " is extinct again."
				: " has " + remaining.getRemainingKills() + " kill(s) remaining.")
				+ " Loot from this kill remains allowed.</col>", null);
		extinctionLogPanel.refresh();
		return true;
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

	private static String rewardSourceForCompletedNpc(String npcName)
	{
		if (SpecialEncounterRules.isBarrowsBrother(npcName)) return BARROWS_REWARD_ACTIVITY;
		if (SpecialEncounterRules.isMoonBoss(npcName)) return MOONS_REWARD_ACTIVITY;
		if (SpecialEncounterRules.isGauntletBoss(npcName)
			|| SpecialEncounterRules.SOL_HEREDIT.equals(npcName)) return npcName;
		return null;
	}

	private net.runelite.api.coords.WorldPoint menuWorldPoint(net.runelite.api.MenuEntry entry)
	{
		net.runelite.api.WorldView worldView = client.getWorldView(entry.getWorldViewId());
		if (worldView == null) worldView = client.getTopLevelWorldView();
		return worldView == null ? null : net.runelite.api.coords.WorldPoint.fromScene(
			worldView, entry.getParam0(), entry.getParam1(), worldView.getPlane());
	}

	private boolean shouldBlockHuntTake(net.runelite.api.MenuEntry entry)
	{
		if (!SoulHuntMenuFilter.isGroundTake(entry.getType(), entry.getOption())
			|| isMarkedSoulItemTake(entry)) return false;
		net.runelite.api.coords.WorldPoint tile = menuWorldPoint(entry);
		return tile != null && forbiddenLootTracker.isForbidden(
			tile, entry.getIdentifier(), client.getTickCount());
	}

	private net.runelite.api.MenuEntry[] filterHuntMenu(net.runelite.api.MenuEntry[] entries)
	{
		return java.util.Arrays.stream(entries)
			.filter(entry -> !shouldBlockHuntTake(entry)
				&& !shouldBlockRewardChestAction(entry)
				&& !shouldBlockRewardItemAction(entry))
			.peek(this::colorSoulItemTarget)
			.toArray(net.runelite.api.MenuEntry[]::new);
	}

	private void colorSoulItemTarget(net.runelite.api.MenuEntry entry)
	{
		if (isMarkedSoulItemTake(entry))
		{
			entry.setTarget(SoulHuntMenuFilter.goldenTarget(entry.getTarget()));
		}
	}

	private boolean isMarkedSoulItemTake(net.runelite.api.MenuEntry entry)
	{
		if (!SoulHuntMenuFilter.isGroundTake(entry.getType(), entry.getOption())) return false;
		WorldPoint tile = menuWorldPoint(entry);
		if (tile == null || !whitelistLootTracker.contains(
			tile, entry.getIdentifier(), client.getTickCount())) return false;
		int canonicalId = itemManager.canonicalize(entry.getIdentifier());
		return firstActiveHuntForItem(canonicalId) != null;
	}

	@Subscribe(priority = -1000)
	public void onPostMenuSort(net.runelite.api.events.PostMenuSort event)
	{
		net.runelite.api.MenuEntry[] entries = client.getMenu().getMenuEntries();
		net.runelite.api.MenuEntry[] filtered = filterHuntMenu(entries);
		if (filtered.length != entries.length) client.getMenu().setMenuEntries(filtered);
	}

	@Subscribe(priority = -1000)
	public void onMenuOpened(net.runelite.api.events.MenuOpened event)
	{
		net.runelite.api.MenuEntry[] entries = event.getMenuEntries();
		net.runelite.api.MenuEntry[] filtered = filterHuntMenu(entries);
		if (filtered.length != entries.length) event.setMenuEntries(filtered);
	}

	@Subscribe(priority = -1000)
	public void onMenuOptionClicked(net.runelite.api.events.MenuOptionClicked event)
	{
		net.runelite.api.MenuEntry entry = event.getMenuEntry();
		String rewardSource = rewardChestSource(entry);
		claimCompletionRewardAllowance(rewardSource);
		if (entry != null)
		{
			for (WhitelistUnlock hunt : eligibleHuntsForRewardSource(rewardSource))
				soulItemRewardAuthorization.authorize(hunt.getItemId(),
					hunt.getSourceNpcName(), client.getTickCount());
		}
		if (entry != null && activeRewardGroupId >= 0 && entry.isItemOp()
			&& entry.getParam1() >>> 16 == activeRewardGroupId)
		{
			int rewardItemId = entry.getItemId() >= 0 ? entry.getItemId() : entry.getIdentifier();
			for (WhitelistUnlock hunt : eligibleHuntsForRewardSource(activeRewardSource))
			{
				if (matchesSoulItem(hunt, rewardItemId)) soulItemRewardAuthorization.authorize(
					hunt.getItemId(), hunt.getSourceNpcName(), client.getTickCount());
			}
		}
		if (entry != null)
		{
			for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
			{
				if (isGauntletRewardOpen(entry, hunt)) soulItemRewardAuthorization.authorize(
					hunt.getItemId(), hunt.getSourceNpcName(), client.getTickCount());
			}
		}
		if (entry != null && isMarkedSoulItemTake(entry))
		{
			WorldPoint tile = menuWorldPoint(entry);
			String source = tile == null ? null : whitelistLootTracker.sourceAt(tile,
				entry.getIdentifier(), client.getTickCount());
			soulItemPickupAuthorization.authorize(itemManager.canonicalize(entry.getIdentifier()),
				source == null ? "" : source, client.getTickCount());
		}
		if (entry != null && (shouldBlockHuntTake(entry)
			|| shouldBlockRewardChestAction(entry)
			|| shouldBlockRewardItemAction(entry)))
		{
			event.consume();
		}
	}

	private boolean shouldBlockRewardChestAction(net.runelite.api.MenuEntry entry)
	{
		String rewardSource = rewardChestSource(entry);
		return rewardSource != null && RewardAccessPolicy.blocksSource(
			isRewardSourceExtinct(rewardSource), hasCompletionRewardAllowance(rewardSource),
			!eligibleHuntsForRewardSource(rewardSource).isEmpty()
				|| permitIsEligibleForRewardSource(rewardSource));
	}

	private boolean shouldBlockRewardItemAction(net.runelite.api.MenuEntry entry)
	{
		if (activeRewardGroupId < 0 || entry == null || !entry.isItemOp()
			|| entry.getParam1() >>> 16 != activeRewardGroupId) return false;
		boolean extinct = isRewardSourceExtinct(activeRewardSource);
		boolean completionAllowed = hasCompletionRewardAllowance(activeRewardSource);
		boolean eligiblePermit = permitIsEligibleForRewardSource(activeRewardSource);
		List<WhitelistUnlock> eligibleHunts = eligibleHuntsForRewardSource(activeRewardSource);
		boolean eligibleHunt = !eligibleHunts.isEmpty()
			|| eligiblePermit;
		if (RewardAccessPolicy.blocksSource(extinct, completionAllowed, eligibleHunt)) return true;
		if (!extinct || completionAllowed || eligiblePermit) return false;
		int itemId = entry.getItemId() >= 0 ? entry.getItemId() : entry.getIdentifier();
		for (WhitelistUnlock hunt : eligibleHunts) if (matchesSoulItem(hunt, itemId)) return false;
		return true;
	}

	private String rewardChestSource(net.runelite.api.MenuEntry entry)
	{
		if (entry == null) return null;
		String widgetSource = SpecialEncounterRules.rewardClaimSourceForWidget(entry.getParam1());
		if (widgetSource != null) return widgetSource;
		if (!isRewardObjectAction(entry.getType())) return null;
		String option = Text.removeTags(entry.getOption() == null ? "" : entry.getOption()).trim();
		if (!(option.equalsIgnoreCase("Open") || option.equalsIgnoreCase("Search")
			|| option.equalsIgnoreCase("Loot") || option.equalsIgnoreCase("Claim"))) return null;
		String target = Text.removeTags(entry.getTarget() == null ? "" : entry.getTarget())
			.trim().toLowerCase(java.util.Locale.ENGLISH);
		String raid = currentRaidName();
		if (raid != null && (target.contains("chest") || target.contains("reward")
			|| target.contains("monumental"))) return raid;
		int region = currentRegionId();
		if ((region == SpecialEncounterRules.GAUNTLET_REGION
			|| region == SpecialEncounterRules.CORRUPTED_GAUNTLET_REGION)
			&& target.contains("reward chest")) return SpecialEncounterRules.gauntletBossForRegion(region);
		if (region == SpecialEncounterRules.FORTIS_COLOSSEUM_REGION && target.contains("chest"))
			return SpecialEncounterRules.SOL_HEREDIT;
		if (target.contains("lunar chest")) return MOONS_REWARD_ACTIVITY;
		if (target.equals("chest") && hasAnyBarrowsBrotherKilled())
			return BARROWS_REWARD_ACTIVITY;
		return null;
	}

	private static boolean isRewardObjectAction(net.runelite.api.MenuAction action)
	{
		return action == net.runelite.api.MenuAction.GAME_OBJECT_FIRST_OPTION
			|| action == net.runelite.api.MenuAction.GAME_OBJECT_SECOND_OPTION
			|| action == net.runelite.api.MenuAction.GAME_OBJECT_THIRD_OPTION
			|| action == net.runelite.api.MenuAction.GAME_OBJECT_FOURTH_OPTION
			|| action == net.runelite.api.MenuAction.GAME_OBJECT_FIFTH_OPTION;
	}

	private boolean hasAnyBarrowsBrotherKilled()
	{
		return client.getVarbitValue(VarbitID.BARROWS_KILLED_AHRIM) != 0
			|| client.getVarbitValue(VarbitID.BARROWS_KILLED_DHAROK) != 0
			|| client.getVarbitValue(VarbitID.BARROWS_KILLED_GUTHAN) != 0
			|| client.getVarbitValue(VarbitID.BARROWS_KILLED_KARIL) != 0
			|| client.getVarbitValue(VarbitID.BARROWS_KILLED_TORAG) != 0
			|| client.getVarbitValue(VarbitID.BARROWS_KILLED_VERAC) != 0;
	}

	private boolean isRewardSourceExtinct(String rewardSource)
	{
		if (BARROWS_REWARD_ACTIVITY.equals(rewardSource))
			return SpecialEncounterRules.barrowsBrothers().stream().allMatch(repository::isExtinctCached);
		if (MOONS_REWARD_ACTIVITY.equals(rewardSource))
			return SpecialEncounterRules.moonBosses().stream().allMatch(repository::isExtinctCached);
		return repository != null && repository.isExtinctCached(rewardSource);
	}

	private static boolean huntMatchesRewardSource(WhitelistUnlock hunt, String rewardSource)
	{
		if (hunt == null || rewardSource == null) return false;
		if (BARROWS_REWARD_ACTIVITY.equals(rewardSource))
			return SpecialEncounterRules.isBarrowsBrother(hunt.getSourceNpcName());
		if (MOONS_REWARD_ACTIVITY.equals(rewardSource))
			return SpecialEncounterRules.isMoonBoss(hunt.getSourceNpcName());
		return rewardSource.equals(hunt.getSourceNpcName());
	}

	private boolean huntIsEligibleForRewardSource(WhitelistUnlock hunt, String rewardSource)
	{
		if (!huntMatchesRewardSource(hunt, rewardSource)) return false;
		if (BARROWS_REWARD_ACTIVITY.equals(rewardSource)
			|| MOONS_REWARD_ACTIVITY.equals(rewardSource))
		{
			int killedVarbit = rewardSourceKilledVarbit(hunt.getSourceNpcName());
			return killedVarbit >= 0 && client.getVarbitValue(killedVarbit) != 0;
		}
		return true;
	}

	private List<WhitelistUnlock> eligibleHuntsForRewardSource(String rewardSource)
	{
		List<WhitelistUnlock> eligible = new ArrayList<>();
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			if (huntIsEligibleForRewardSource(hunt, rewardSource)) eligible.add(hunt);
		}
		return eligible;
	}

	private boolean permitIsEligibleForRewardSource(String rewardSource)
	{
		if (rewardSource == null) return false;
		for (SoulPermit permit : metaProgressRepository.getActivePermits())
		{
		if (BARROWS_REWARD_ACTIVITY.equals(rewardSource))
		{
			int killedVarbit = rewardSourceKilledVarbit(permit.getSourceName());
			if (SpecialEncounterRules.isBarrowsBrother(permit.getSourceName())
				&& killedVarbit >= 0 && client.getVarbitValue(killedVarbit) != 0) return true;
		}
		else if (MOONS_REWARD_ACTIVITY.equals(rewardSource))
		{
			int killedVarbit = rewardSourceKilledVarbit(permit.getSourceName());
			if (SpecialEncounterRules.isMoonBoss(permit.getSourceName())
				&& killedVarbit >= 0 && client.getVarbitValue(killedVarbit) != 0) return true;
		}
		else if (permit.matches(rewardSource)) return true;
		}
		return false;
	}

	private void setActiveReward(int groupId, String source)
	{
		activeRewardGroupId = groupId;
		activeRewardSource = source;
	}

	private void allowCompletionReward(String source)
	{
		completionRewardAllowance.allow(source,
			activeRewardGroupId >= 0 ? activeRewardSource : null);
	}

	private boolean hasCompletionRewardAllowance(String source)
	{
		return completionRewardAllowance.allows(source);
	}

	private void claimCompletionRewardAllowance(String source)
	{
		completionRewardAllowance.claim(source);
	}

	private boolean isGauntletRewardOpen(net.runelite.api.MenuEntry entry, WhitelistUnlock hunt)
	{
		if (hunt == null || client.getVarbitValue(VarbitID.GAUNTLET_REWARD_AVAILABLE) == 0)
		{
			return false;
		}
		boolean corrupted = client.getVarbitValue(VarbitID.GAUNTLET_CORRUPTED) != 0;
		String option = entry.getOption();
		String target = entry.getTarget();
		return SpecialEncounterRules.isGauntletRewardSource(hunt.getSourceNpcName(), corrupted)
			&& option != null && "Open".equalsIgnoreCase(Text.removeTags(option).trim())
			&& target != null && Text.removeTags(target).toLowerCase(java.util.Locale.ENGLISH)
				.contains("reward chest");
	}

	static int killSoulAmount(boolean developerMode, boolean instantExtinction)
	{
		return developerMode && instantExtinction ? SoulProgress.EXTINCTION_TARGET : 1;
	}

	private void showExtinctionCelebration(String npcName, boolean soulPointAwarded)
	{
		extinctionCelebrationTracker.show(npcName, System.currentTimeMillis(), soulPointAwarded);
	}

	private void showPendingCelebrations()
	{
		// One timestamp keeps every orb in the extinction wave in the same phase.
		long now = System.currentTimeMillis();
		boolean victimIncluded = false;
		if (client.getTopLevelWorldView() != null)
		{
			for (NPC npc : client.getTopLevelWorldView().npcs())
			{
				if (java.util.Objects.equals(pendingWaveNpcName, npc.getName()))
				{
					soulAnimationTracker.add(npc.getWorldLocation(), now, true);
					victimIncluded |= npc == pendingWaveNpc;
				}
			}
		}
		if (!victimIncluded && pendingWaveNpc != null)
		{
			soulAnimationTracker.add(pendingWaveNpc.getWorldLocation(), now, true);
		}
		if (!pendingWavePermit)
		{
			showExtinctionCelebration(pendingWaveNpcName, pendingWaveSoulPointAwarded);
		}
	}

	private void queueExterminationWave(NPC npc, String exactNpcName,
		boolean soulPointAwarded)
	{
		pendingWaveNpc = npc;
		pendingWaveNpcName = exactNpcName;
		pendingWavePreviousAnimation = npc.getAnimation();
		pendingWaveExpiresAtTick = client.getTickCount() + 3;
		pendingWaveSoulPointAwarded = soulPointAwarded;
		pendingWavePermit = false;
	}

	private void queuePermitWave(NPC npc, String exactNpcName)
	{
		pendingWaveNpc = npc;
		pendingWaveNpcName = exactNpcName;
		pendingWavePreviousAnimation = npc.getAnimation();
		pendingWaveExpiresAtTick = client.getTickCount() + 3;
		pendingWaveSoulPointAwarded = false;
		pendingWavePermit = true;
	}

	private void finishPendingWave(int deathAnimation)
	{
		String waveName = pendingWaveNpcName;
		showPendingCelebrations();
		clearPendingWave();
		startExterminationWave(waveName, deathAnimation);
	}

	private void clearPendingWave()
	{
		pendingWaveNpc = null;
		pendingWaveNpcName = null;
		pendingWavePreviousAnimation = -1;
		pendingWaveExpiresAtTick = 0;
		pendingWaveSoulPointAwarded = false;
		pendingWavePermit = false;
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
		// Keep the scene stable while waiting for the final victim's death animation.
		// Previously extinction hid these NPCs immediately, then the wave revealed them.
		if (pendingWaveNpc != null && java.util.Objects.equals(pendingWaveNpcName, npc.getName()))
		{
			return true;
		}
		if (exterminationWaveTracker.isAnimating(npc.getName(), client.getTickCount()))
		{
			extinctGhostTracker.remove(npc);
			return true;
		}
		boolean hidden = shouldHideNpc(npc);
		boolean ghost = !hidden && isExtinctGhost(npc);
		// Gold is reserved for Soul Items. Permits and other exceptions stay blue.
		extinctGhostTracker.update(npc, ghost, exceptionSoulType(npc));
		// Keep the real model and clickbox; the overlay only adds the exception tint.
		return !hidden;
	}

	private boolean shouldHideNpc(NPC npc)
	{
		String exactNpcName = npc.getName();
		int regionId = currentRegionId();
		String raidName = SpecialEncounterRules.raidAtRegion(regionId);
		if (raidName != null)
		{
			net.runelite.api.NPCComposition composition = npc.getComposition();
			return composition != null && SpecialEncounterRules.shouldHideRaidNpc(
				repository.isExtinctCached(raidName), isActiveExceptionSource(raidName),
				exactNpcName, composition.getActions(), composition.getCombatLevel());
		}
		String gauntletBoss = SpecialEncounterRules.gauntletBossForRegion(regionId);
		if (gauntletBoss != null)
		{
			return repository.isExtinctCached(gauntletBoss)
				&& !isActiveExceptionSource(gauntletBoss);
		}
		if (SpecialEncounterRules.shouldAlwaysRemainVisible(regionId, exactNpcName)
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
			return "Unbound Soul";
		}
		if (isSoulPermitException(npc))
		{
			SoulPermit permit = metaProgressRepository.findActivePermit(npc.getName());
			return permit != null && permit.isBoss() ? "Strong Soul" : "Weak Soul";
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
		int canonicalItemId = itemManager.canonicalize(itemId);
		String itemName = cleanItemName(canonicalItemId);
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			if (SoulItemLootRules.allowsActiveHuntItem(
				hunt, npc.getName(), canonicalItemId, itemName)) return true;
		}
		if (isWhitelistHuntException(npc)) return false;
		if (isSoulPermitException(npc))
		{
			return true;
		}
		if (isMandatoryException(npc) && config.allowMandatoryExceptionLoot())
		{
			return true;
		}
		return false;
	}

	private SoulExceptionType exceptionSoulType(NPC npc)
	{
		if (isWhitelistHuntException(npc)) return SoulExceptionType.UNBOUND;
		if (isSlayerException(npc)) return SoulExceptionType.SLAYER;
		SoulPermit permit = npc == null ? null : metaProgressRepository.findActivePermit(npc.getName());
		if (permit == null && currentRaidName() != null)
			permit = metaProgressRepository.findActivePermit(currentRaidName());
		if (permit != null) return permit.isBoss() ? SoulExceptionType.STRONG : SoulExceptionType.WEAK;
		return npc != null && BossRegistry.isBoss(npc.getName())
			? SoulExceptionType.STRONG : SoulExceptionType.WEAK;
	}

	private boolean isWhitelistHuntException(NPC npc)
	{
		if (npc == null) return false;
		String npcName = npc.getName();
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			String sourceName = hunt.getSourceNpcName();
			if (sourceName.equals(npcName) || sourceName.equals(currentRaidName())
				|| (repository != null && repository.isExtinctCached(npcName)
					&& SpecialEncounterRules.sharesRewardActivity(sourceName, npcName))) return true;
		}
		return false;
	}

	private boolean isGodWarsSupportException(NPC npc)
	{
		return npc != null && repository != null
			&& repository.isExtinctCached(npc.getName())
			&& SpecialEncounterRules.isGodWarsSupportNpc(currentRegionId(), npc.getName());
	}

	private boolean isActiveHuntSource(String sourceName)
	{
		return !activeHuntsForSource(sourceName).isEmpty();
	}

	private boolean isActiveExceptionSource(String sourceName)
	{
		if (isActiveHuntSource(sourceName)) return true;
		return metaProgressRepository.findActivePermit(sourceName) != null;
	}

	private boolean isSoulPermitException(NPC npc)
	{
		if (npc == null) return false;
		String npcName = npc.getName();
		if (metaProgressRepository.findActivePermit(npcName) != null) return true;
		String raid = currentRaidName();
		return raid != null && metaProgressRepository.findActivePermit(raid) != null;
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

	private void recordEncounterReward(String raidName)
	{
		bestiaryRegistry.markValidated(raidName);
		SoulPermit permit = metaProgressRepository.findActivePermit(raidName);
		if (permit != null && permit.matches(raidName) && !isActiveHuntSource(raidName))
		{
			allowCompletionReward(raidName);
			metaProgressRepository.consumePermit(raidName);
			SoulPermit remaining = metaProgressRepository.findActivePermit(raidName);
			raidBossesSeen.clear();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
				"<col=9b6fd3>STRONG SOUL USED — " + raidName
					+ (remaining == null ? " is extinct again."
					: " has " + remaining.getRemainingKills() + " completion(s) remaining.")
					+ " This reward remains allowed.</col>", null);
			refreshExtinctGhosts();
			extinctionLogPanel.refresh();
			return;
		}
		String newlyExtinctTarget = null;
		boolean soulPointEarned = false;
		SoulProgress before = repository.get(raidName);
		SoulProgress after = before.isExtinct() ? before : repository.gainSoul(raidName);
		if (!before.isExtinct() && after.isExtinct())
		{
			SoulPointAward award = metaProgressRepository.award(raidName);
			allowCompletionReward(raidName);
			newlyExtinctTarget = raidName;
			soulPointEarned |= award.isSoulPointEarned();
		}
		if (newlyExtinctTarget != null)
		{
			long now = System.currentTimeMillis();
			if (SpecialEncounterRules.isClaimBasedActivity(newlyExtinctTarget))
				extinctionCelebrationTracker.showRaid(newlyExtinctTarget, now, soulPointEarned);
			else
				extinctionCelebrationTracker.show(newlyExtinctTarget, now, soulPointEarned);
		}
		raidBossesSeen.clear();
		extinctionLogPanel.refresh();
		if (config.showChatMessages() && !before.isExtinct())
		{
			String message = after.isExtinct()
				? RewardCompletionChatMessage.random(raidName)
				: "<col=005f73>+1 " + raidName + " Reward — " + after.getSouls() + "/100</col>";
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
		}
	}

	private void completeSpecialHuntIfRewarded(String sourceName, ItemContainer rewards)
	{
		List<WhitelistUnlock> hunts = activeHuntsForSource(sourceName);
		if (hunts.isEmpty()) return;
		boolean found = false;
		for (WhitelistUnlock hunt : hunts)
		{
			for (Item item : rewards.getItems())
			{
				if (item.getId() >= 0 && matchesSoulItem(hunt, item.getId()))
				{
					showSoulItemFoundOnce(hunt, sourceName);
					found = true;
					break;
				}
			}
		}
		if (!found) client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
			"<col=ff2d2d>UNBOUND SOUL NOT FOUND — all other rewards are FORBIDDEN.</col>", null);
	}

	private void completePendingDirectSoulLoot()
	{
		directSoulLootTracker.expire(client.getTickCount());
	}

	private boolean completeVerifiedSoulHunt(WhitelistUnlock hunt, String visualSource)
	{
		if (hunt == null) return false;
		List<NPC> remainingHuntNpcs = new ArrayList<>();
		for (NPC npc : knownNpcs)
		{
			if (!npc.isDead() && npc.getName() != null
				&& npc.getName().equalsIgnoreCase(hunt.getSourceNpcName()))
			{
				remainingHuntNpcs.add(npc);
			}
		}
		if (!whitelistHuntService.completeHunt(hunt)) return false;
		List<WhitelistUnlock> remainingForSource = activeHuntsForSource(hunt.getSourceNpcName());
		if (remainingForSource.isEmpty()) startSoulItemCompletionWave(
			hunt.getSourceNpcName(), remainingHuntNpcs);
		activeHuntInventoryBaselines.remove(hunt.getItemId());
		boolean sameItemStillActive = remainingForSource.stream()
			.anyMatch(other -> other.matchesItem(hunt.getItemId(), hunt.getItemName()));
		if (!sameItemStillActive) forbidRemainingSoulItems(hunt);
		soulItemPopupGate.release(hunt.getItemId());
		if (visualSource != null)
		{
			showSoulItemFoundOnce(hunt, visualSource);
		}
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
			"<col=c48a28>Unbound item obtained: " + hunt.getItemName() + ".</col>", null);
		refreshExtinctGhosts();
		extinctionLogPanel.refresh();
		return true;
	}

	private void showSoulItemFoundOnce(WhitelistUnlock hunt, String sourceName)
	{
		if (hunt != null && sourceName != null && soulItemPopupGate.claim(hunt.getItemId()))
		{
			soulItemFoundTracker.show(hunt.getItemName(), System.currentTimeMillis());
		}
	}

	private void startSoulItemCompletionWave(String sourceName, List<NPC> remainingHuntNpcs)
	{
		if (remainingHuntNpcs.isEmpty()) return;
		long now = System.currentTimeMillis();
		for (NPC npc : remainingHuntNpcs)
		{
			soulAnimationTracker.addGolden(npc.getWorldLocation(), now);
		}

		Integer deathAnimation = lastDeathAnimationByNpcName.get(normalizedNpcName(sourceName));
		if (deathAnimation == null || deathAnimation < 0) return;
		String exactNpcName = remainingHuntNpcs.get(0).getName();
		exterminationWaveTracker.start(exactNpcName, client.getTickCount());
		for (NPC npc : remainingHuntNpcs)
		{
			npc.setAnimation(deathAnimation);
			npc.setAnimationFrame(0);
		}
	}

	private static String normalizedNpcName(String npcName)
	{
		return npcName == null ? "" : npcName.trim().toLowerCase(java.util.Locale.ENGLISH);
	}

	private void rememberDeathAnimation(NPC npc)
	{
		if (npc != null && npc.getAnimation() >= 0 && npc.getName() != null)
		{
			lastDeathAnimationByNpcName.put(normalizedNpcName(npc.getName()), npc.getAnimation());
		}
	}

	private void reconcileSoulPointRewards()
	{
		Set<String> completedSpecies = new LinkedHashSet<>();
		for (SoulProgress progress : repository.getAll())
		{
			if (progress.isExtinct()
				&& !bestiaryRegistry.isExcludedSpecialNpc(progress.getNpcName()))
			{
				completedSpecies.add(progress.getNpcName());
			}
		}
		if (metaProgressRepository.synchronizeExtinctionAwards(completedSpecies))
		{
			extinctionLogPanel.refresh();
		}
	}

	private void initializeKnownNpcs()
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.getTopLevelWorldView() == null)
		{
			return;
		}
		prioritizeSavedProgress();
		extinctionLogPanel.refresh();
		knownNpcs.clear();
		deadNpcsAwaitingAnimation.clear();
		lastDeathAnimationByNpcName.clear();
		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			knownNpcs.add(npc);
			discoverSpecialNpc(npc);
		}
		refreshExtinctGhosts();
	}

	private void prioritizeSavedProgress()
	{
		if (repository == null) return;
		bestiaryRegistry.prioritizeProgress(repository.getAll());
		itemRegistry.prioritizeProgress(metaProgressRepository.getUnlocks());
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

	private boolean initializeActiveHuntBaselines()
	{
		List<WhitelistUnlock> hunts = whitelistHuntService.getActiveHunts();
		Set<Integer> activeItemIds = new java.util.HashSet<>();
		for (WhitelistUnlock hunt : hunts) activeItemIds.add(hunt.getItemId());
		boolean changed = activeHuntInventoryBaselines.keySet().retainAll(activeItemIds);
		if (hunts.isEmpty())
		{
			soulItemPickupAuthorization.clear();
			soulItemRewardAuthorization.clear();
			soulItemPopupGate.clear();
			directSoulLootTracker.clear();
			return changed;
		}
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		for (WhitelistUnlock hunt : hunts)
		{
			if (!activeHuntInventoryBaselines.containsKey(hunt.getItemId()))
			{
				activeHuntInventoryBaselines.put(hunt.getItemId(), countSoulItem(inventory, hunt));
				changed = true;
			}
		}
		return changed;
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

	private List<WhitelistUnlock> distinctActiveHuntsByItem()
	{
		Map<Integer, WhitelistUnlock> distinct = new java.util.LinkedHashMap<>();
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			distinct.putIfAbsent(hunt.getItemId(), hunt);
		}
		return new ArrayList<>(distinct.values());
	}

	private WhitelistUnlock firstActiveHuntForItem(int canonicalItemId)
	{
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			if (hunt.matchesItem(canonicalItemId, cleanItemName(canonicalItemId))) return hunt;
		}
		return null;
	}

	private WhitelistUnlock activeHuntForItem(String sourceName, int canonicalItemId)
	{
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			if (hunt.getSourceNpcName().equals(sourceName)
				&& hunt.matchesItem(canonicalItemId, cleanItemName(canonicalItemId))) return hunt;
		}
		return null;
	}

	private List<WhitelistUnlock> activeHuntsForSource(String sourceName)
	{
		List<WhitelistUnlock> matches = new ArrayList<>();
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			if (sourceName != null && sourceName.equals(hunt.getSourceNpcName())) matches.add(hunt);
		}
		return matches;
	}

	private String activeSoulStateSignature()
	{
		StringBuilder state = new StringBuilder();
		for (SoulPermit permit : metaProgressRepository.getActivePermits())
		{
			state.append('P').append(permit.getSourceName()).append(':')
				.append(permit.getRemainingKills()).append(';');
		}
		for (WhitelistUnlock hunt : whitelistHuntService.getActiveHunts())
		{
			state.append('U').append(hunt.getSourceNpcName()).append(':')
				.append(hunt.getItemId()).append(';');
		}
		return state.toString();
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

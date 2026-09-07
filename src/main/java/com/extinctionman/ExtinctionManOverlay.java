package com.extinctionman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

final class ExtinctionManOverlay extends OverlayPanel
{
	private static final Color SOUL_COLOR = new Color(170, 225, 255);
	private static final Color EXCEPTION_COLOR = new Color(220, 80, 80);
	private final ExtinctionManConfig config;
	private final SoulRepository repository;
	private final WhitelistHuntService whitelistHuntService;
	private final MetaProgressRepository metaProgressRepository;

	@Inject
	ExtinctionManOverlay(ExtinctionManConfig config, SoulRepository repository,
		WhitelistHuntService whitelistHuntService, MetaProgressRepository metaProgressRepository)
	{
		this.config = config;
		this.repository = repository;
		this.whitelistHuntService = whitelistHuntService;
		this.metaProgressRepository = metaProgressRepository;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay()
			|| (repository.getLastEncountered() == null && !config.mandatoryExceptionMode()
				&& whitelistHuntService.getActiveHunt() == null
				&& metaProgressRepository.getActivePermit() == null)) return null;
		panelComponent.setPreferredSize(new Dimension(preferredWidth(graphics), 0));
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Extinction Man").color(SOUL_COLOR).build());

		if (config.mandatoryExceptionMode())
		{
			String npcName = config.mandatoryExceptionNpc().trim();
			panelComponent.getChildren().add(LineComponent.builder()
				.left("MANDATORY EXCEPTION")
				.right(npcName.isEmpty() ? "NO NPC SET" : npcName
					+ (config.allowMandatoryExceptionLoot() ? " (LOOT ALLOWED)" : ""))
				.leftColor(EXCEPTION_COLOR).rightColor(EXCEPTION_COLOR).build());
		}

		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		if (hunt != null)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("UNBOUND SOUL").right(hunt.getItemName())
				.leftColor(SoulStatusColors.IN_PROGRESS)
				.rightColor(SoulStatusColors.IN_PROGRESS).build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Source").right(hunt.getSourceNpcName())
				.leftColor(Color.LIGHT_GRAY).rightColor(Color.LIGHT_GRAY).build());
		}
		java.util.List<SoulPermit> permits = metaProgressRepository.getActivePermits();
		for (SoulPermit permit : permits)
		{
			int kills = permit.getRemainingKills();
			panelComponent.getChildren().add(LineComponent.builder()
				.left(permit.getSourceName()).right(kills + (kills == 1 ? " kill" : " kills"))
				.leftColor(SoulStatusColors.IN_PROGRESS)
				.rightColor(SoulStatusColors.IN_PROGRESS).build());
		}

		SoulProgress progress = repository.getLastEncountered();
		if (progress != null)
		{
			Color statusColor = SoulStatusColors.forProgress(progress);
			String unit = SpecialEncounterRules.isRaidName(progress.getNpcName()) ? " rewards" : " souls";
			panelComponent.getChildren().add(LineComponent.builder()
				.left(progress.getNpcName())
				.right(progress.isExtinct() ? "EXTINCT" : progress.getSouls() + "/100" + unit)
				.leftColor(statusColor).rightColor(statusColor).build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Soul Energy").right(Integer.toString(metaProgressRepository.getAvailablePoints()))
				.leftColor(SOUL_COLOR).rightColor(SOUL_COLOR).build());
		}
		return super.render(graphics);
	}

	private int preferredWidth(Graphics2D graphics)
	{
		int width = 160;
		SoulProgress progress = repository.getLastEncountered();
		if (progress != null)
		{
			String value = progress.isExtinct() ? "EXTINCT" : progress.getSouls() + "/100"
				+ (SpecialEncounterRules.isRaidName(progress.getNpcName()) ? " rewards" : " souls");
			width = Math.max(width, lineWidth(graphics, progress.getNpcName(), value));
		}
		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		if (hunt != null)
		{
			width = Math.max(width, lineWidth(graphics, "UNBOUND SOUL", hunt.getItemName()));
			width = Math.max(width, lineWidth(graphics, "Source", hunt.getSourceNpcName()));
		}
		java.util.List<SoulPermit> permits = metaProgressRepository.getActivePermits();
		for (SoulPermit permit : permits)
		{
			int kills = permit.getRemainingKills();
			width = Math.max(width, lineWidth(graphics, permit.getSourceName(),
				kills + (kills == 1 ? " kill" : " kills")));
		}
		if (config.mandatoryExceptionMode())
		{
			String npc = config.mandatoryExceptionNpc().trim();
			width = Math.max(width, lineWidth(graphics, "MANDATORY EXCEPTION",
				npc.isEmpty() ? "NO NPC SET" : npc));
		}
		return width;
	}

	private static int lineWidth(Graphics2D graphics, String left, String right)
	{
		return graphics.getFontMetrics().stringWidth(left)
			+ graphics.getFontMetrics().stringWidth(right) + 32;
	}
}

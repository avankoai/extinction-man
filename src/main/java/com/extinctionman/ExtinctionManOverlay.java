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

	@Inject
	ExtinctionManOverlay(ExtinctionManConfig config, SoulRepository repository,
		WhitelistHuntService whitelistHuntService)
	{
		this.config = config;
		this.repository = repository;
		this.whitelistHuntService = whitelistHuntService;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay()
			|| (repository.getLastEncountered() == null && !config.mandatoryExceptionMode()
				&& whitelistHuntService.getActiveHunt() == null))
		{
			return null;
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Extinction Man")
			.color(SOUL_COLOR)
			.build());

		if (config.mandatoryExceptionMode())
		{
			String npcName = config.mandatoryExceptionNpc().trim();
			panelComponent.getChildren().add(LineComponent.builder()
				.left("MANDATORY EXCEPTION")
				.right(npcName.isEmpty() ? "NO NPC SET" : npcName
					+ (config.allowMandatoryExceptionLoot() ? " (LOOT ALLOWED)" : ""))
				.leftColor(EXCEPTION_COLOR)
				.rightColor(EXCEPTION_COLOR)
				.build());
		}

		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		if (hunt != null)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("SOUL ITEM")
				.right(hunt.getItemName())
				.leftColor(SoulStatusColors.IN_PROGRESS)
				.rightColor(SoulStatusColors.IN_PROGRESS)
				.build());
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Source")
				.right(hunt.getSourceNpcName())
				.leftColor(Color.LIGHT_GRAY)
				.rightColor(Color.LIGHT_GRAY)
				.build());
		}

		SoulProgress progress = repository.getLastEncountered();
		if (progress != null)
		{
			Color statusColor = SoulStatusColors.forProgress(progress);
			String unit = SpecialEncounterRules.isRaidName(progress.getNpcName()) ? " rewards" : " souls";
			panelComponent.getChildren().add(LineComponent.builder()
				.left(progress.getNpcName())
				.right(progress.isExtinct() ? "EXTINCT" : progress.getSouls() + "/100" + unit)
				.leftColor(statusColor)
				.rightColor(statusColor)
				.build());
		}
		return super.render(graphics);
	}
}

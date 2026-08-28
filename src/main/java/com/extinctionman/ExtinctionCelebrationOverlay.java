package com.extinctionman;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class ExtinctionCelebrationOverlay extends Overlay
{
	private final ExtinctionManConfig config;
	private final ExtinctionCelebrationTracker tracker;

	@Inject
	ExtinctionCelebrationOverlay(ExtinctionManConfig config, ExtinctionCelebrationTracker tracker)
	{
		this.config = config;
		this.tracker = tracker;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ALWAYS_ON_TOP);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showExtinctionPopup()) return null;
		long now = System.currentTimeMillis();
		ExtinctionCelebration celebration = tracker.active(now);
		if (celebration == null) return null;
		Dimension canvas = graphics.getClipBounds().getSize();
		int width = Math.min(330, canvas.width - 30);
		int height = 132;
		int x = (canvas.width - width) / 2;
		int y = Math.max(35, canvas.height / 9);
		Composite old = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
			celebration.alpha(now, ExtinctionCelebrationTracker.DURATION_MILLIS)));
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		CollectionLogPopupRenderer.drawFrame(graphics, x, y, width, height, "Exterminated");
		CollectionLogPopupRenderer.drawCentered(graphics, celebration.getNpcName(), x, width, y + 83,
			CollectionLogPopupRenderer.MAIN_FONT, CollectionLogPopupRenderer.WHITE);
		CollectionLogPopupRenderer.drawCentered(graphics, celebration.getCompletionText(), x, width, y + 109,
			CollectionLogPopupRenderer.DETAIL_FONT, CollectionLogPopupRenderer.SOUL_BLUE);
		graphics.setComposite(old);
		return null;
	}
}

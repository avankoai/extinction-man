package com.extinctionman;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ButtonModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

final class ExtinctionLogPanel extends PluginPanel
{
	private static final Color SOUL_COLOR = new Color(170, 225, 255);
	private final SoulRepository repository;
	private final BestiaryRegistry bestiaryRegistry;
	private final MetaProgressRepository metaProgressRepository;
	private final ItemRegistry itemRegistry;
	private final WhitelistHuntService whitelistHuntService;
	private final WhitelistLootTracker whitelistLootTracker;
	private final ExtinctionCelebrationTracker extinctionCelebrationTracker;
	private final SoulItemFoundTracker soulItemFoundTracker;
	private final ForbiddenLootTracker forbiddenLootTracker;
	private final ExtinctGhostTracker extinctGhostTracker;
	private final NextKillTestBoost nextKillTestBoost;
	private final boolean developerMode;
	private final JLabel summary = new JLabel();
	private final JLabel soulPointsSummary = new JLabel();
	private final JLabel soulPointsSpent = new JLabel();
	private final JTextField search = new JTextField();
	private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{
		"All monsters", "Extinct", "In progress", "Not started", "Bosses"
	});
	private boolean updatingStatusFilter;
	private boolean devUnlocked;
	private boolean changingProtectedTab;
	private final JPanel entries = new JPanel();
	private final JPanel vaultEntries = new JPanel();
	private final Map<String, RowLabels> visibleRows = new HashMap<>();
	private final JButton copyLog = new JButton("Copy Progress");
	private final JButton copyBackup = new JButton("Backup");
	private final JButton restoreBackup = new JButton("Restore");
	private final JButton forgeSoul = new JButton("Forge Soul");
	private final JButton previewSoulItem = new JButton("Preview Unbound Item");
	private final JButton adjustSouls = new JButton("Set Monster Souls");
	private final JButton removeSoulItem = new JButton("Remove Unbound Soul");
	private final JButton resetAccount = new JButton("Reset All Progress");
	private final JButton nextKillBoost = new JButton("Next Kill: Extinction");
	private final JButton grantSoulPoint = new JButton("Set Soul Energy");
	private final JButton previewExtinction = new JButton("Preview Extinction");

	@Inject
	ExtinctionLogPanel(SoulRepository repository, BestiaryRegistry bestiaryRegistry,
		MetaProgressRepository metaProgressRepository, ItemRegistry itemRegistry,
		WhitelistHuntService whitelistHuntService, WhitelistLootTracker whitelistLootTracker,
		ExtinctionCelebrationTracker extinctionCelebrationTracker,
		SoulItemFoundTracker soulItemFoundTracker,
		ForbiddenLootTracker forbiddenLootTracker, ExtinctGhostTracker extinctGhostTracker,
		NextKillTestBoost nextKillTestBoost,
		@javax.inject.Named("developerMode") boolean developerMode)
	{
		super(false);
		this.repository = repository;
		this.bestiaryRegistry = bestiaryRegistry;
		this.metaProgressRepository = metaProgressRepository;
		this.itemRegistry = itemRegistry;
		this.whitelistHuntService = whitelistHuntService;
		this.whitelistLootTracker = whitelistLootTracker;
		this.extinctionCelebrationTracker = extinctionCelebrationTracker;
		this.soulItemFoundTracker = soulItemFoundTracker;
		this.forbiddenLootTracker = forbiddenLootTracker;
		this.extinctGhostTracker = extinctGhostTracker;
		this.nextKillTestBoost = nextKillTestBoost;
		this.developerMode = developerMode;
		setLayout(new BorderLayout(0, 8));
		setOpaque(true);
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(3, 4, 5)),
			BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(62, 66, 71)),
				BorderFactory.createEmptyBorder(8, 8, 8, 8))));

		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel title = new JLabel("EXTINCTION MAN", SwingConstants.CENTER);
		title.setForeground(SOUL_COLOR);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		summary.setForeground(Color.LIGHT_GRAY);
		summary.setAlignmentX(Component.CENTER_ALIGNMENT);
		summary.setHorizontalAlignment(SwingConstants.CENTER);
		summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
		soulPointsSummary.setForeground(SOUL_COLOR);
		soulPointsSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
		soulPointsSpent.setForeground(Color.GRAY);
		soulPointsSpent.setAlignmentX(Component.CENTER_ALIGNMENT);
		search.setToolTipText("Search monsters and raids");
		search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		statusFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		statusFilter.setToolTipText("Filter monsters by soul progress");
		statusFilter.setFocusable(false);
		statusFilter.setSelectedItem("In progress");
		statusFilter.addActionListener(event ->
		{
			if (!updatingStatusFilter)
			{
				rebuild();
			}
		});
		copyLog.setAlignmentX(Component.CENTER_ALIGNMENT);
		copyLog.setFocusable(false);
		copyLog.addActionListener(event -> copyLogToClipboard());
		copyBackup.setFocusable(false);
		copyBackup.setToolTipText("Copy an account backup to the clipboard");
		copyBackup.addActionListener(event -> copyBackupToClipboard());
		restoreBackup.setFocusable(false);
		restoreBackup.setToolTipText("Restore an Extinction Man backup from the clipboard");
		restoreBackup.addActionListener(event -> restoreBackupFromClipboard());
		forgeSoul.setFocusable(false);
		forgeSoul.setAlignmentX(Component.CENTER_ALIGNMENT);
		forgeSoul.addActionListener(event -> forgeSoul());
		previewSoulItem.setFocusable(false);
		previewSoulItem.setToolTipText("Preview the verified reward overlay without changing progress");
		previewSoulItem.addActionListener(event -> previewSoulItemFound());
		adjustSouls.setFocusable(false);
		adjustSouls.setToolTipText("Correct one monster's exact soul count between 0 and 100");
		adjustSouls.addActionListener(event -> chooseProgressAdjustment());
		removeSoulItem.setFocusable(false);
		removeSoulItem.setToolTipText("Remove an active or collected Unbound Soul record");
		removeSoulItem.addActionListener(event -> chooseSoulItemRemoval());
		resetAccount.setFocusable(false);
		resetAccount.setToolTipText("Erase all monster Souls, Soul Energy, and forged Souls for this account");
		resetAccount.addActionListener(event -> confirmResetEntireAccount());
		nextKillBoost.setFocusable(false);
		nextKillBoost.setToolTipText("Make the next valid kill immediately drive that monster to extinction");
		nextKillBoost.addActionListener(event -> armNextKillBoost());
		grantSoulPoint.setFocusable(false);
		grantSoulPoint.setToolTipText("Replace the currently available Soul Energy with an exact value");
		grantSoulPoint.addActionListener(event -> setTestSoulPoints());
		previewExtinction.setFocusable(false);
		previewExtinction.addActionListener(event -> previewExtinctionPopup());
		stylePrimaryButton(forgeSoul);
		styleSecondaryButton(copyLog);
		styleSecondaryButton(copyBackup);
		styleSecondaryButton(restoreBackup);
		styleSecondaryButton(previewSoulItem);
		styleSecondaryButton(adjustSouls);
		styleSecondaryButton(removeSoulItem);
		styleDangerButton(resetAccount);
		styleSecondaryButton(nextKillBoost);
		styleSecondaryButton(grantSoulPoint);
		styleSecondaryButton(previewExtinction);
		header.add(title);
		header.add(Box.createVerticalStrut(4));
		header.add(summary);

		entries.setLayout(new BoxLayout(entries, BoxLayout.Y_AXIS));
		entries.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane bestiaryScroll = createScrollPane(entries);
		JPanel bestiaryTab = new JPanel(new BorderLayout(0, 8));
		bestiaryTab.setOpaque(false);
		JPanel bestiaryTools = verticalPanel();
		bestiaryTools.add(search);
		bestiaryTools.add(Box.createVerticalStrut(5));
		bestiaryTools.add(statusFilter);
		bestiaryTab.add(bestiaryTools, BorderLayout.NORTH);
		bestiaryTab.add(bestiaryScroll, BorderLayout.CENTER);

		vaultEntries.setLayout(new BoxLayout(vaultEntries, BoxLayout.Y_AXIS));
		vaultEntries.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JPanel vaultTab = new JPanel(new BorderLayout(0, 8));
		vaultTab.setOpaque(false);
		JPanel vaultTools = verticalPanel();
		vaultTools.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		vaultTools.add(soulPointsSummary);
		vaultTools.add(Box.createVerticalStrut(2));
		vaultTools.add(soulPointsSpent);
		vaultTools.add(Box.createVerticalStrut(7));
		JPanel soulsHelp = verticalPanel();
		String[] helpLines = {
			"<html><div style='text-align:center'>Each extinct monster earns<br>1 Soul Energy.</div></html>",
			"<html><div style='text-align:center'>Each extinct boss earns<br>10 Soul Energy.</div></html>", "",
			"1 Energy: Weak Soul", "10 Energy: Strong Soul",
			"100 Energy: Unbound Soul"
		};
		for (String line : helpLines)
		{
			if (line.isEmpty())
			{
				soulsHelp.add(Box.createVerticalStrut(5));
				continue;
			}
			JLabel helpLine = new JLabel(line, SwingConstants.CENTER);
			helpLine.setForeground(Color.LIGHT_GRAY);
			soulsHelp.add(fullWidthLabel(helpLine));
		}
		vaultTools.add(soulsHelp);
		vaultTools.add(Box.createVerticalStrut(7));
		vaultTools.add(forgeSoul);
		vaultTab.add(vaultTools, BorderLayout.NORTH);
		vaultTab.add(createScrollPane(vaultEntries), BorderLayout.CENTER);

		JPanel editTab = verticalPanel();
		editTab.setOpaque(true);
		editTab.setBackground(ColorScheme.DARK_GRAY_COLOR);
		editTab.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 2));
		JLabel editTitle = new JLabel("Manual corrections", SwingConstants.CENTER);
		editTitle.setForeground(SOUL_COLOR);
		JLabel editHelp = new JLabel("<html><div style='text-align:center'>"
			+ "Use these controls only to correct<br>or recover saved progress.</div></html>",
			SwingConstants.CENTER);
		editHelp.setForeground(Color.GRAY);
		editTab.add(fullWidthLabel(editTitle));
		editTab.add(Box.createVerticalStrut(5));
		editTab.add(fullWidthLabel(editHelp));
		editTab.add(Box.createVerticalStrut(12));
		if (developerMode)
		{
			addEditHeading(editTab, "Test Tools");
			editTab.add(fullWidthButton(nextKillBoost));
			editTab.add(Box.createVerticalStrut(12));
		}
		addEditHeading(editTab, "Soul Progress");
		editTab.add(fullWidthButton(adjustSouls));
		editTab.add(Box.createVerticalStrut(5));
		editTab.add(fullWidthButton(grantSoulPoint));
		editTab.add(Box.createVerticalStrut(5));
		editTab.add(fullWidthButton(removeSoulItem));
		if (developerMode)
		{
			editTab.add(Box.createVerticalStrut(12));
			addEditHeading(editTab, "Popup Previews");
			editTab.add(fullWidthButton(previewExtinction));
			editTab.add(Box.createVerticalStrut(5));
			editTab.add(fullWidthButton(previewSoulItem));
		}
		editTab.add(Box.createVerticalStrut(12));
		addEditHeading(editTab, "Backup & Restore");
		editTab.add(fullWidthButton(copyLog));
		editTab.add(Box.createVerticalStrut(6));
		editTab.add(fullWidthButton(copyBackup));
		editTab.add(Box.createVerticalStrut(6));
		editTab.add(fullWidthButton(restoreBackup));
		editTab.add(Box.createVerticalStrut(12));
		addEditHeading(editTab, "Full reset");
		editTab.add(fullWidthButton(resetAccount));

		JEditorPane infoTab = createInfoPane();

		JTabbedPane tabs = new JTabbedPane();
		tabs.setFocusable(false);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabs.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tabs.setForeground(Color.LIGHT_GRAY);
		addCompactTab(tabs, "Bestiary", bestiaryTab);
		addCompactTab(tabs, "Souls", vaultTab);
		addCompactTab(tabs, "Info", createScrollPane(infoTab));
		addCompactTab(tabs, "Dev", createScrollPane(editTab));
		tabs.addChangeListener(event ->
		{
			if (changingProtectedTab || tabs.getSelectedIndex() != 3 || devUnlocked) return;
			JPasswordField password = new JPasswordField();
			int answer = JOptionPane.showConfirmDialog(this, password,
				"Enter the Dev password", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);
			if (answer == JOptionPane.OK_OPTION
				&& "Soul".equals(new String(password.getPassword())))
			{
				devUnlocked = true;
				return;
			}
			if (answer == JOptionPane.OK_OPTION)
			{
				JOptionPane.showMessageDialog(this, "Incorrect password.",
					"Dev locked", JOptionPane.ERROR_MESSAGE);
			}
			changingProtectedTab = true;
			tabs.setSelectedIndex(2);
			changingProtectedTab = false;
		});

		add(header, BorderLayout.NORTH);
		add(tabs, BorderLayout.CENTER);
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent event)
			{
				rebuild();
			}
		});
		search.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override public void insertUpdate(DocumentEvent event) { rebuild(); }
			@Override public void removeUpdate(DocumentEvent event) { rebuild(); }
			@Override public void changedUpdate(DocumentEvent event) { rebuild(); }
		});
		rebuild();
	}

	void refresh()
	{
		if (!isShowing())
		{
			return;
		}
		if (SwingUtilities.isEventDispatchThread())
		{
			rebuild();
		}
		else
		{
			SwingUtilities.invokeLater(this::rebuild);
		}
	}

	private void rebuild()
	{
		List<SoulProgress> encountered = repository.getAll();
		bestiaryRegistry.prioritizeProgress(encountered);
		List<SoulProgress> all = combinedEntries(encountered);
		updateProgressSummary(all);
		updateStatusFilterCounts(all);
		updateMetaSection();
		entries.removeAll();
		visibleRows.clear();
		String query = search.getText().trim().toLowerCase(Locale.ENGLISH);
		int selectedStatus = statusFilter.getSelectedIndex();
		int shown = 0;
		for (SoulProgress progress : all)
		{
			if (!matches(progress, query, selectedStatus))
			{
				continue;
			}
			entries.add(createRow(progress));
			entries.add(Box.createVerticalStrut(4));
			shown++;
		}
		if (shown == 0)
		{
			JLabel empty = new JLabel(bestiaryRegistry.isComplete()
				|| selectedStatus == 1 || selectedStatus == 2
				? "No matching monsters."
				: "Scanning monster list...");
			empty.setForeground(Color.GRAY);
			entries.add(empty);
		}
		entries.revalidate();
		entries.repaint();
	}

	private void updateMetaSection()
	{
		int availablePoints = metaProgressRepository.getAvailablePoints();
		soulPointsSummary.setText("Soul Energy: " + availablePoints);
		soulPointsSpent.setText("Soul Energy Spent: " + metaProgressRepository.getSpentPoints());
		List<WhitelistUnlock> activeHunts = whitelistHuntService.getActiveHunts();
		forgeSoul.setEnabled(metaProgressRepository.getAvailablePoints() >= 1);
		removeSoulItem.setEnabled(!metaProgressRepository.getUnlocks().isEmpty());
		nextKillBoost.setText(nextKillTestBoost.isArmed()
			? "Next Kill: Extinction (Armed)" : "Next Kill: Extinction");
		rebuildVault(activeHunts, metaProgressRepository.getActivePermits());
	}

	private void updateProgressSummary(List<SoulProgress> all)
	{
		summary.setText(formatProgressSummary(all));
	}

	private void updateProgressSummary()
	{
		updateProgressSummary(combinedEntries(repository.getAll()));
	}

	static String formatProgressSummary(List<SoulProgress> all)
	{
		long bossTotal = all.stream().filter(progress -> BossRegistry.isBoss(progress.getNpcName())).count();
		long bossExtinct = all.stream().filter(progress -> BossRegistry.isBoss(progress.getNpcName())
			&& progress.isExtinct()).count();
		long monsterTotal = all.size() - bossTotal;
		long monsterExtinct = all.stream().filter(progress -> !BossRegistry.isBoss(progress.getNpcName())
			&& progress.isExtinct()).count();
		double monsterPercentage = monsterTotal == 0 ? 0.0 : monsterExtinct * 100.0 / monsterTotal;
		double bossPercentage = bossTotal == 0 ? 0.0 : bossExtinct * 100.0 / bossTotal;
		return String.format(Locale.ENGLISH,
			"<html><div style='text-align:center'>Monsters<br>%d/%d Completed (%.2f%%)<br><br>"
				+ "Bosses<br>%d/%d Completed (%.2f%%)</div></html>",
			monsterExtinct, monsterTotal, monsterPercentage, bossExtinct, bossTotal, bossPercentage);
	}

	private void rebuildVault(List<WhitelistUnlock> activeHunts, List<SoulPermit> activePermits)
	{
		vaultEntries.removeAll();
		List<WhitelistUnlock> unlocks = metaProgressRepository.getUnlocks();
		if (!activeHunts.isEmpty())
		{
			addVaultHeading("Active Unbound Souls (" + activeHunts.size() + ")");
			for (WhitelistUnlock activeHunt : activeHunts) addVaultItem(activeHunt);
			vaultEntries.add(Box.createVerticalStrut(8));
		}
		if (!activePermits.isEmpty())
		{
			addVaultHeading("Active Souls (" + activePermits.size() + ")");
			for (SoulPermit permit : activePermits)
			{
				JLabel soul = new JLabel((permit.isBoss() ? "Strong: " : "Weak: ")
					+ permit.getSourceName() + " — " + permit.getRemainingKills()
					+ (permit.getRemainingKills() == 1 ? " kill" : " kills"));
				soul.setForeground(SoulStatusColors.IN_PROGRESS);
				soul.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
				vaultEntries.add(fullWidthLabel(soul));
			}
			vaultEntries.add(Box.createVerticalStrut(8));
		}
		long collectedItems = unlocks.stream().filter(WhitelistUnlock::isAcquired).count();
		addVaultHeading("Collected Unbound Souls (" + collectedItems + ")", SwingConstants.CENTER);
		boolean hasCollected = false;
		for (WhitelistUnlock unlock : unlocks)
		{
			if (unlock.isAcquired())
			{
				addVaultItem(unlock);
				hasCollected = true;
			}
		}
		if (!hasCollected)
		{
			JLabel empty = new JLabel("No Unbound Souls collected yet.", SwingConstants.CENTER);
			empty.setForeground(Color.GRAY);
			empty.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
			vaultEntries.add(fullWidthLabel(empty));
		}
		vaultEntries.revalidate();
		vaultEntries.repaint();
	}

	private void addVaultHeading(String text)
	{
		addVaultHeading(text, SwingConstants.LEFT);
	}

	private void addVaultHeading(String text, int alignment)
	{
		JLabel heading = new JLabel(text, alignment);
		heading.setForeground(SOUL_COLOR);
		heading.setFont(FontManager.getDefaultBoldFont().deriveFont(13f));
		heading.setBorder(BorderFactory.createEmptyBorder(4, 3, 5, 3));
		vaultEntries.add(fullWidthLabel(heading));
	}

	private void addVaultItem(WhitelistUnlock unlock)
	{
		JPanel row = new JPanel(new BorderLayout(4, 2));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
		JLabel item = new JLabel(unlock.getItemName());
		item.setForeground(unlock.isAcquired() ? SoulStatusColors.COMPLETE
			: SoulStatusColors.IN_PROGRESS);
		JLabel source = new JLabel((unlock.isAcquired() ? "Obtained from " : "Source: ")
			+ unlock.getSourceNpcName());
		source.setForeground(Color.GRAY);
		JPanel details = verticalPanel();
		details.add(item);
		details.add(source);
		row.add(details, BorderLayout.CENTER);
		vaultEntries.add(row);
		vaultEntries.add(Box.createVerticalStrut(4));
	}

	private void confirmRemoveWhitelistItem(WhitelistUnlock unlock)
	{
		if (unlock.getPointCost() == 0)
		{
			int answer = JOptionPane.showConfirmDialog(this,
				"Remove free test item " + unlock.getItemName() + "?\nNo Soul Energy will be refunded.",
				"Remove Unbound Soul", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION)
			{
				metaProgressRepository.removeUnlock(unlock, false);
				whitelistLootTracker.clear();
				updateMetaSection();
			}
			return;
		}
		Object[] choices = {"Remove and refund Soul Energy", "Remove without refund", "Cancel"};
		int choice = JOptionPane.showOptionDialog(this,
			"Remove " + unlock.getItemName() + " from your Unbound Souls?\n\n"
				+ "Source: " + unlock.getSourceNpcName() + "\n"
				+ "If this is an active Unbound Soul, it will also be cancelled.",
			"Remove Unbound Soul", JOptionPane.DEFAULT_OPTION,
			JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
		if (choice == 0 || choice == 1)
		{
			metaProgressRepository.removeUnlock(unlock, choice == 0);
			whitelistLootTracker.clear();
			updateMetaSection();
		}
	}

	private static JPanel verticalPanel()
	{
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		return panel;
	}

	private static JPanel fullWidthButton(JButton button)
	{
		JPanel holder = new JPanel(new BorderLayout());
		holder.setOpaque(false);
		holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
		holder.add(button, BorderLayout.CENTER);
		return holder;
	}

	private static JPanel fullWidthLabel(JLabel label)
	{
		JPanel holder = new JPanel(new BorderLayout());
		holder.setOpaque(false);
		holder.setAlignmentX(Component.CENTER_ALIGNMENT);
		holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getPreferredSize().height));
		holder.add(label, BorderLayout.CENTER);
		return holder;
	}

	private static JScrollPane createScrollPane(Component content)
	{
		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		return scrollPane;
	}

	private static void addCompactTab(JTabbedPane tabs, String title, Component content)
	{
		tabs.addTab(title, content);
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		label.setFont(FontManager.getDefaultFont().deriveFont(11f));
		label.setForeground(Color.LIGHT_GRAY);
		label.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		tabs.setTabComponentAt(tabs.getTabCount() - 1, label);
	}

	private static JEditorPane createInfoPane()
	{
		JEditorPane info = new JEditorPane("text/html", infoHtml());
		info.setEditable(false);
		info.setFocusable(false);
		info.setOpaque(true);
		info.setBackground(ColorScheme.DARK_GRAY_COLOR);
		info.setForeground(Color.LIGHT_GRAY);
		info.setFont(FontManager.getDefaultFont());
		info.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		info.setBorder(BorderFactory.createEmptyBorder(7, 7, 12, 7));
		return info;
	}

	static String infoHtml()
	{
		return "<html><head><style>"
			+ "body{margin:0;color:#d3d3d3;font-family:sans-serif;font-size:11px;}"
			+ "h2{margin:0 0 6px 0;color:#aae1ff;font-size:14px;}"
			+ "h3{margin:12px 0 4px 0;color:#aae1ff;font-size:12px;}"
			+ "p{margin:0 0 7px 0;}"
			+ "b{color:#ffffff;} .gold{color:#c48a28;} .blue{color:#579ac4;}"
			+ "</style></head><body>"
			+ "<h2>Extinction Man</h2>"
			+ "<p>Your goal is to defeat every monster in Gielinor 100 times and drive "
			+ "the entire world to extinction.</p>"
			+ "<h3>How extinction works</h3>"
			+ "<p>After killing a monster 100 times, it becomes extinct and its entire species "
			+ "disappears from the game.</p>"
			+ "<p>This means you only have 100 loot opportunities per monster to obtain everything "
			+ "you need. Many valuable drops are much rarer than 1/100, so don’t expect to get "
			+ "everything you want. If you miss a drop, that chance is gone. Another monster may "
			+ "provide a different way forward.</p>"
			+ "<p>Example: killing <b>Abyssal demon</b> 100 times removes every monster named Abyssal "
			+ "demon. An Abyssal walker is a separate species and remains available.</p>"
			+ "<p><i>Note: Monsters are not actually removed from the game as this would be breaking "
			+ "the rules. They are only hidden on the player’s client side.</i></p>"
			+ "<h3>Soul Energy</h3>"
			+ "<p>Every individual monster kill awards <b>1 Soul.</b> Making a monster extinct awards "
			+ "<b>1 Soul Energy</b>. Making a boss extinct awards <b>10 Soul Energy</b>. Soul Energy "
			+ "can be spent on the following:</p>"
			+ "<p>1 Soul Energy - 1 Weak Soul (Monster)</p>"
			+ "<p>10 Soul Energy - 1 Strong Soul (Boss)</p>"
			+ "<p>100 Soul Energy - 1 Unbound Soul (Item)</p>"
			+ "<p><b>A weak or strong soul</b> permits one single kill of a monster or boss that is "
			+ "already extinct. By purchasing a soul, the monster you choose will come back to "
			+ "Gielinor, and become extinct again after all purchased kills are used. You may forge "
			+ "several kills at once and keep multiple Souls active.</p>"
			+ "<p><i>Example: You spend one soul energy to purchase a weak soul. This temporarily "
			+ "brings back a non-boss monster from your choice. After 1 kill, the creature becomes "
			+ "extinct again.</i></p>"
			+ "<p><b>An Unbound Soul</b> is an extremely rare, powerful soul which brings back a "
			+ "monster permanently until one specific item of choice is dropped. During this time, "
			+ "all other drops from this monster remain locked.</p>"
			+ "<p><i>Example: After 100 abyssal demon kills you did not receive the Abyssal Whip. An "
			+ "Unbound Soul can be attached to Abyssal demons to make them come back from the dead. "
			+ "No drops can be picked up, but if your chosen item appears (unbound abyssal whip), a "
			+ "golden loot beam will appear. After picking up the unbound abyssal whip, all remaining "
			+ "Abyssal demons will die again.</i></p>"
			+ "<h3>Slayer Tasks</h3>"
			+ "<p>If you get a Slayer task that includes an extinct monster, it will temporarily "
			+ "return so the task can be completed. Looting items is not allowed if the monster is "
			+ "extinct, so it’s only for XP purposes.</p>"
			+ "<hr>"
			+ "<p>Plugin info:</p>"
			+ "<h3>Bestiary</h3>"
			+ "<p>This is your complete extinction log. The header separately shows completed normal "
			+ "monsters and bosses with their percentages. Use Search to find an exact monster "
			+ "name. Use the filter to view In progress, Extinct, Not started, Bosses or All monsters; "
			+ "each option shows its current entry count.</p>"
			+ "<p>Each row shows the exact species name and its Souls out of 100. In progress is the "
			+ "default view, so the monsters you are currently hunting appear first. New game "
			+ "content is marked <b>(NEW)</b> until you validate it with a credited kill.</p>"
			+ "<h3>Souls</h3>"
			+ "<p>The top lines show your available and spent Soul Energy. Choose <b>Forge Soul</b> "
			+ "to spend Soul Energy. For a weak or strong soul, choose the specific monster or boss "
			+ "you would like to come back from the dead. For an unbound soul, search for the exact "
			+ "item, then enter the exact name of the desired source monster that can provide it. "
			+ "Review both choices before confirming.</p>"
			+ "<p>Your current targets appear under Active Unbound Souls. Multiple Unbound Souls may "
			+ "be active at once, including more than one for the same monster. Completed choices "
			+ "move to Collected Unbound Souls, where you can "
			+ "review the item and its source.</p>"
			+ "<h3>Dev</h3>"
			+ "<p>The <b>Dev</b> tab is for recovery and development, not normal play. Its password "
			+ "is <b>Soul</b> and is required once per RuneLite session. Monster progress can "
			+ "correct a missed or incorrect kill. Unbound Soul controls can manually complete or "
			+ "remove a record, and Soul Energy controls can correct energy progress.</p>"
			+ "<p>Developer builds also show one-kill, Extinction popup and Unbound Soul popup test tools. "
			+ "Backup copies the full account state to your clipboard; Restore replaces the current "
			+ "state with that backup. Reset All Progress erases the entire character profile after "
			+ "confirmation. Always make a backup before editing or resetting data.</p>"
			+ "<h3>Info</h3>"
			+ "<p>This page is the rulebook and quick-start guide. Return here whenever you are unsure "
			+ "whether a kill, exception or item claim is allowed.</p>"
			+ "</body></html>";
	}

	private static void stylePrimaryButton(JButton button)
	{
		Color normal = new Color(91, 67, 39);
		button.setBackground(normal);
		button.setForeground(new Color(255, 190, 72));
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(151, 111, 54)),
			BorderFactory.createEmptyBorder(5, 8, 5, 8)));
		installButtonStates(button, normal, new Color(119, 88, 50), new Color(62, 45, 28));
	}

	private static void styleSecondaryButton(JButton button)
	{
		Color normal = ColorScheme.DARKER_GRAY_COLOR;
		button.setBackground(normal);
		button.setForeground(Color.LIGHT_GRAY);
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(80, 80, 80)),
			BorderFactory.createEmptyBorder(4, 7, 4, 7)));
		installButtonStates(button, normal, new Color(61, 64, 68), new Color(20, 21, 23));
	}

	private static void styleDangerButton(JButton button)
	{
		Color normal = new Color(91, 34, 34);
		button.setBackground(normal);
		button.setForeground(new Color(255, 205, 205));
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(150, 58, 58)),
			BorderFactory.createEmptyBorder(4, 7, 4, 7)));
		installButtonStates(button, normal, new Color(122, 43, 43), new Color(61, 21, 21));
	}

	private static void installButtonStates(JButton button, Color normal, Color hover, Color pressed)
	{
		button.setOpaque(true);
		button.setContentAreaFilled(true);
		button.setRolloverEnabled(true);
		button.getModel().addChangeListener(event ->
		{
			ButtonModel model = button.getModel();
			button.setBackground(model.isPressed() && model.isArmed()
				? pressed : model.isRollover() ? hover : normal);
		});
	}

	private JPanel createRow(SoulProgress progress)
	{
		JPanel row = new JPanel(new BorderLayout(4, 2));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

		JLabel name = new JLabel(displayMonsterName(progress.getNpcName()));
		Color statusColor = SoulStatusColors.forProgress(progress);
		name.setForeground(statusColor);
		boolean raid = SpecialEncounterRules.isRaidName(progress.getNpcName());
		String unit = raid ? "rewards" : "souls";
		String statusText = progress.isExtinct()
			? "EXTINCT"
			: progress.getSouls() + "/100 " + unit + " - " + progress.getRemaining() + " remaining";
		JLabel status = new JLabel(statusText);
		status.setForeground(statusColor);

		JPanel details = new JPanel();
		details.setOpaque(false);
		details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
		details.add(name);
		details.add(status);

		row.add(details, BorderLayout.CENTER);
		visibleRows.put(progress.getNpcName(), new RowLabels(name, status));
		return row;
	}

	private void updateVisibleRow(String npcName)
	{
		RowLabels labels = visibleRows.get(npcName);
		if (labels == null) return;
		SoulProgress progress = repository.get(npcName);
		Color color = SoulStatusColors.forProgress(progress);
		labels.name.setForeground(color);
		labels.name.setText(displayMonsterName(npcName));
		labels.status.setForeground(color);
		boolean raid = SpecialEncounterRules.isRaidName(npcName);
		labels.status.setText(progress.isExtinct() ? "EXTINCT"
			: progress.getSouls() + "/100 " + (raid ? "rewards" : "souls")
				+ " - " + progress.getRemaining() + " remaining");
	}

	private static final class RowLabels
	{
		private final JLabel name;
		private final JLabel status;
		private RowLabels(JLabel name, JLabel status)
		{
			this.name = name;
			this.status = status;
		}
	}

	private List<SoulProgress> combinedEntries(List<SoulProgress> encountered)
	{
		Map<String, SoulProgress> byName = new HashMap<>();
		for (String name : bestiaryRegistry.getAttackableNames())
		{
			byName.put(name, new SoulProgress(name, 0));
		}
		for (SoulProgress progress : encountered)
		{
			if (!bestiaryRegistry.isExcludedSpecialNpc(progress.getNpcName())
				&& !SpecialEncounterRules.alwaysIgnoredTechnicalNpc(progress.getNpcName()))
			{
				byName.put(progress.getNpcName(), progress);
			}
		}
		List<SoulProgress> combined = new ArrayList<>(byName.values());
		combined.sort(java.util.Comparator.comparing(SoulProgress::isExtinct).reversed()
			.thenComparing((SoulProgress progress) -> progress.getSouls() > 0 ? 0 : 1)
			.thenComparing(SoulProgress::getNpcName, String.CASE_INSENSITIVE_ORDER));
		return combined;
	}

	private void updateStatusFilterCounts(List<SoulProgress> all)
	{
		int extinct = 0;
		int inProgress = 0;
		int notStarted = 0;
		int bosses = 0;
		for (SoulProgress progress : all)
		{
			if (BossRegistry.isBoss(progress.getNpcName())) bosses++;
			if (progress.isExtinct())
			{
				extinct++;
			}
			else if (progress.getSouls() > 0)
			{
				inProgress++;
			}
			else
			{
				notStarted++;
			}
		}

		String[] labels = new String[]{
			"All monsters (" + all.size() + ")",
			"Extinct (" + extinct + ")",
			"In progress (" + inProgress + ")",
			"Not started (" + notStarted + ")",
			"Bosses (" + bosses + ")"
		};
		int selectedIndex = Math.max(0, statusFilter.getSelectedIndex());
		updatingStatusFilter = true;
		try
		{
			statusFilter.setModel(new DefaultComboBoxModel<>(labels));
			statusFilter.setSelectedIndex(Math.min(selectedIndex, labels.length - 1));
		}
		finally
		{
			updatingStatusFilter = false;
		}
	}

	private String displayMonsterName(String npcName)
	{
		return bestiaryRegistry.isNew(npcName) ? npcName + " (NEW)" : npcName;
	}

	private static boolean matches(SoulProgress progress, String query, int selectedStatus)
	{
		if (!progress.getNpcName().toLowerCase(Locale.ENGLISH).contains(query))
		{
			return false;
		}
		if (selectedStatus == 1)
		{
			return progress.isExtinct();
		}
		if (selectedStatus == 2)
		{
			return progress.getSouls() > 0 && !progress.isExtinct();
		}
		if (selectedStatus == 3)
		{
			return progress.getSouls() == 0;
		}
		if (selectedStatus == 4)
		{
			return BossRegistry.isBoss(progress.getNpcName());
		}
		return true;
	}

	private void armNextKillBoost()
	{
		if (!developerMode) return;
		nextKillTestBoost.arm();
		updateMetaSection();
	}

	private void setTestSoulPoints()
	{
		if (!developerMode) return;
		String requested = JOptionPane.showInputDialog(this,
			"Enter the Soul Energy balance you want to have:",
			Integer.toString(metaProgressRepository.getAvailablePoints()));
		if (requested == null) return;
		final int amount;
		try
		{
			amount = Integer.parseInt(requested.trim());
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Enter a whole number of zero or more.",
				"Invalid Soul Energy", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (amount < 0)
		{
			JOptionPane.showMessageDialog(this, "Enter a whole number of zero or more.",
				"Invalid Soul Energy", JOptionPane.ERROR_MESSAGE);
			return;
		}
		metaProgressRepository.setAvailableSoulEnergy(amount);
		updateMetaSection();
	}

	private void confirmResetEntireAccount()
	{
		int answer = JOptionPane.showConfirmDialog(this,
			"Reset ALL Extinction Man progress for this account?\n\n"
				+ "This erases every monster soul, extinction, Soul Energy, active forged Soul, "
				+ "and collected Unbound Soul.\nCreate a backup first if you may need this data.",
			"Reset entire account progress", JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);
		if (answer != JOptionPane.YES_OPTION) return;
		String confirmation = JOptionPane.showInputDialog(this,
			"Type RESET to permanently erase all Extinction Man progress:",
			"Final reset confirmation", JOptionPane.WARNING_MESSAGE);
		if (!"RESET".equals(confirmation)) return;

		repository.resetAll();
		metaProgressRepository.resetAllProgress();
		whitelistLootTracker.clear();
		forbiddenLootTracker.clear();
		extinctGhostTracker.clear();
		extinctionCelebrationTracker.clear();
		soulItemFoundTracker.clear();
		nextKillTestBoost.clear();
		rebuild();
		JOptionPane.showMessageDialog(this,
			"All Extinction Man progress for this account has been reset.",
			"Account progress reset", JOptionPane.INFORMATION_MESSAGE);
	}

	private void chooseProgressAdjustment()
	{
		String query = JOptionPane.showInputDialog(this,
			"Search for the monster whose soul count you want to correct:",
			"Set monster souls", JOptionPane.QUESTION_MESSAGE);
		if (query == null || query.trim().isEmpty()) return;
		String lowerQuery = query.trim().toLowerCase(Locale.ENGLISH);
		List<SoulProgress> matches = new ArrayList<>();
		for (SoulProgress progress : combinedEntries(repository.getAll()))
		{
			if (progress.getNpcName().toLowerCase(Locale.ENGLISH).contains(lowerQuery))
			{
				matches.add(progress);
				if (matches.size() >= 100) break;
			}
		}
		if (matches.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "No matching monster was found.",
				"Monster not found", JOptionPane.ERROR_MESSAGE);
			return;
		}
		SoulProgress selected = (SoulProgress) JOptionPane.showInputDialog(this,
			"Choose the exact monster:", "Set monster souls", JOptionPane.QUESTION_MESSAGE,
			null, matches.toArray(), matches.get(0));
		if (selected == null) return;
		String requested = JOptionPane.showInputDialog(this,
			"Enter the corrected soul count for " + selected.getNpcName() + " (0–100):",
			Integer.toString(selected.getSouls()));
		if (requested == null) return;
		final int souls;
		try
		{
			souls = Integer.parseInt(requested.trim());
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Enter a whole number from 0 through 100.",
				"Invalid soul count", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (souls < 0 || souls > SoulProgress.EXTINCTION_TARGET)
		{
			JOptionPane.showMessageDialog(this, "Enter a whole number from 0 through 100.",
				"Invalid soul count", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (souls == selected.getSouls()) return;
		int answer = JOptionPane.showConfirmDialog(this,
			"Change " + selected.getNpcName() + " from " + selected.getSouls()
				+ "/100 to " + souls + "/100 souls?\n\n"
				+ "Soul Energy will be reconciled if extinction status changes.",
			"Confirm progress correction", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer != JOptionPane.YES_OPTION) return;
		SoulProgress edited = repository.setSouls(selected.getNpcName(), souls);
		if (!selected.isExtinct() && edited.isExtinct())
		{
			metaProgressRepository.award(selected.getNpcName());
		}
		else if (selected.isExtinct() && !edited.isExtinct())
		{
			metaProgressRepository.revokeAward(selected.getNpcName());
		}
		rebuild();
	}

	private void chooseSoulItemRemoval()
	{
		List<WhitelistUnlock> unlocks = metaProgressRepository.getUnlocks();
		if (unlocks.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "There are no Unbound Souls to remove.",
				"Nothing to remove", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String[] labels = new String[unlocks.size()];
		for (int i = 0; i < unlocks.size(); i++)
		{
			WhitelistUnlock unlock = unlocks.get(i);
			labels[i] = (unlock.isAcquired() ? "Collected: " : "Active Unbound Soul: ")
				+ unlock.getItemName() + " — " + unlock.getSourceNpcName();
		}
		String selected = (String) JOptionPane.showInputDialog(this,
			"Choose the Unbound Soul record to remove:", "Remove Unbound Soul",
			JOptionPane.WARNING_MESSAGE, null, labels, labels[0]);
		if (selected == null) return;
		for (int i = 0; i < labels.length; i++)
		{
			if (labels[i].equals(selected))
			{
				confirmRemoveWhitelistItem(unlocks.get(i));
				return;
			}
		}
	}

	private void copyLogToClipboard()
	{
		String text = SoulLogExporter.format(repository.getAll());
		try
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
			copyLog.setText("Copied!");
			javax.swing.Timer timer = new javax.swing.Timer(1500, event -> copyLog.setText("Copy Progress"));
			timer.setRepeats(false);
			timer.start();
		}
		catch (IllegalStateException ex)
		{
			JOptionPane.showMessageDialog(this, "The clipboard is currently unavailable.",
				"Copy failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void copyBackupToClipboard()
	{
		try
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				new StringSelection(FullBackupCodec.encode(
					repository.exportStorage(), metaProgressRepository.exportData())), null);
			copyBackup.setText("Copied!");
			javax.swing.Timer timer = new javax.swing.Timer(1500, event -> copyBackup.setText("Backup"));
			timer.setRepeats(false);
			timer.start();
		}
		catch (IllegalStateException ex)
		{
			JOptionPane.showMessageDialog(this, "The clipboard is currently unavailable.",
				"Backup failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void restoreBackupFromClipboard()
	{
		try
		{
			Object clipboardValue = Toolkit.getDefaultToolkit().getSystemClipboard()
				.getData(DataFlavor.stringFlavor);
			String backup = clipboardValue == null ? "" : clipboardValue.toString().trim();
			boolean legacyBackup = backup.startsWith("EXTINCTION-MAN-BACKUP-V2\n");
			FullBackupCodec.Decoded fullBackup = legacyBackup ? null : FullBackupCodec.decode(backup);
			if (legacyBackup)
			{
				SoulLogCodec.decodeBackup(backup);
			}
			int answer = JOptionPane.showConfirmDialog(
				this,
				"Replace this account's complete soul log with the backup from your clipboard?\n"
					+ "Create a fresh backup first if you may need the current log.",
				"Confirm backup restore",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.YES_OPTION)
			{
				if (legacyBackup)
				{
					repository.restoreBackup(backup);
				}
				else
				{
					repository.importStorage(fullBackup.getSoulStorage());
					metaProgressRepository.importData(fullBackup.getMetaStorage());
				}
				rebuild();
				JOptionPane.showMessageDialog(this, "The soul log was restored successfully.",
					"Restore complete", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(this,
				ex.getMessage() == null ? "The clipboard does not contain a valid backup." : ex.getMessage(),
				"Restore failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void createWhitelistHunt()
	{
		itemRegistry.prioritizeProgress(metaProgressRepository.getUnlocks());
		String query = JOptionPane.showInputDialog(this,
			"Search for the item you want to bind to an Unbound Soul:"
				+ (itemRegistry.isComplete() ? "" : "\nSaved items are ready; other items are still loading."),
			"Forge Unbound Soul", JOptionPane.QUESTION_MESSAGE);
		if (query == null || query.trim().isEmpty()) return;
		List<WhitelistItemOption> matches = itemRegistry.search(query, 50);
		if (matches.isEmpty())
		{
			JOptionPane.showMessageDialog(this, itemRegistry.isComplete() ? "No matching item was found."
				: "No match in the loaded items yet. The database is still loading; try again shortly.",
				"Item not found", JOptionPane.ERROR_MESSAGE);
			return;
		}
		WhitelistItemOption item = (WhitelistItemOption) JOptionPane.showInputDialog(this,
			"Select the exact item:", "Forge Unbound Soul", JOptionPane.QUESTION_MESSAGE,
			null, matches.toArray(), matches.get(0));
		if (item == null) return;

		String requestedNpc = JOptionPane.showInputDialog(this,
			"Enter the exact source monster or raid name:",
			"Choose extinct source", JOptionPane.QUESTION_MESSAGE);
		if (requestedNpc == null) return;
		String sourceNpc = bestiaryRegistry.findExactName(requestedNpc);
		if (sourceNpc == null || !repository.get(sourceNpc).isExtinct())
		{
			JOptionPane.showMessageDialog(this,
				"The source must be an exact monster or raid name that is already extinct.",
				"Invalid source", JOptionPane.ERROR_MESSAGE);
			return;
		}

		int answer = JOptionPane.showConfirmDialog(this,
			(metaProgressRepository.getNewUnlockCost() == 0 ? "Create free test Unbound Soul?" : "Spend 100 Soul Energy?")
				+ "\n\nItem: " + item.getItemName()
				+ "\nSource: " + sourceNpc
				+ "\n\nOnly this Unbound Soul item may be taken until it is obtained.",
			"Confirm Unbound Soul", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			metaProgressRepository.createUnlock(item.getItemId(), item.getItemName(), sourceNpc);
			rebuild();
		}
	}

	private void markActiveHuntObtained()
	{
		List<WhitelistUnlock> hunts = whitelistHuntService.getActiveHunts();
		if (hunts.isEmpty()) return;
		WhitelistUnlock hunt = hunts.get(0);
		if (hunts.size() > 1)
		{
			String[] labels = new String[hunts.size()];
			for (int i = 0; i < hunts.size(); i++) labels[i] = hunts.get(i).getItemName()
				+ " — " + hunts.get(i).getSourceNpcName();
			String selected = (String) JOptionPane.showInputDialog(this,
				"Choose the Unbound Soul to complete:", "Complete Unbound Soul",
				JOptionPane.QUESTION_MESSAGE, null, labels, labels[0]);
			if (selected == null) return;
			for (int i = 0; i < labels.length; i++) if (labels[i].equals(selected))
			{
				hunt = hunts.get(i);
				break;
			}
		}
		int answer = JOptionPane.showConfirmDialog(this,
			"Mark " + hunt.getItemName() + " as obtained and complete this Unbound Soul?",
			"Confirm Unbound Soul completion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			whitelistHuntService.completeHunt(hunt);
			whitelistLootTracker.clear();
			rebuild();
		}
	}

	private void forgeSoul()
	{
		String[] types = {"Weak Soul — 1 Soul Energy per monster kill",
			"Strong Soul — 10 Soul Energy per boss kill",
			"Unbound Soul — 100 Soul Energy per item"};
		String selectedType = (String) JOptionPane.showInputDialog(this,
			"What kind of Soul do you want to forge?", "Forge Soul",
			JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
		if (selectedType == null) return;
		if (selectedType.startsWith("Unbound"))
		{
			if (!metaProgressRepository.canCreateUnlock())
			{
				JOptionPane.showMessageDialog(this, "You need 100 Soul Energy.",
					"Unbound Soul unavailable", JOptionPane.ERROR_MESSAGE);
				return;
			}
			createWhitelistHunt();
			return;
		}
		createSoulPermit(selectedType.startsWith("Strong"));
	}

	private void createSoulPermit(boolean boss)
	{
		int energyPerKill = boss ? 10 : 1;

		List<String> candidates = new ArrayList<>();
		for (SoulProgress progress : combinedEntries(repository.getAll()))
		{
			if (progress.isExtinct() && BossRegistry.isBoss(progress.getNpcName()) == boss)
			{
				candidates.add(progress.getNpcName());
			}
		}
		if (candidates.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "There are no extinct "
				+ (boss ? "bosses" : "monsters") + " available.",
				"No Permit sources", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		JComboBox<String> choice = new JComboBox<>(candidates.toArray(new String[0]));
		choice.setEditable(true);
		choice.setSelectedItem("");
		int chosen = JOptionPane.showConfirmDialog(this, choice,
			"Type or choose the exact " + (boss ? "boss" : "monster") + " name",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (chosen != JOptionPane.OK_OPTION) return;
		Object requested = choice.getEditor().getItem();
		String exact = requested == null ? null : bestiaryRegistry.findExactName(requested.toString());
		if (exact == null || !repository.get(exact).isExtinct() || BossRegistry.isBoss(exact) != boss)
		{
			JOptionPane.showMessageDialog(this, "Choose an exact extinct "
				+ (boss ? "boss" : "monster") + " from the list.",
				"Invalid Permit source", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String amountText = JOptionPane.showInputDialog(this,
			"How many kills do you want to purchase?\nCost per kill: " + energyPerKill
				+ " Soul Energy", "1");
		if (amountText == null) return;
		final int kills;
		try
		{
			kills = Integer.parseInt(amountText.trim());
		}
		catch (NumberFormatException ex)
		{
			JOptionPane.showMessageDialog(this, "Enter a positive whole number.",
				"Invalid kill amount", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (kills < 1 || kills > Integer.MAX_VALUE / energyPerKill)
		{
			JOptionPane.showMessageDialog(this, "Enter a positive whole number.",
				"Invalid kill amount", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int cost = kills * energyPerKill;
		if (!metaProgressRepository.canCreatePermit(boss, kills))
		{
			JOptionPane.showMessageDialog(this, "You need " + cost + " Soul Energy.",
				"Soul unavailable", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int answer = JOptionPane.showConfirmDialog(this,
			"Spend " + cost + " Soul Energy?\n\nSoul: " + (boss ? "Strong" : "Weak")
				+ "\nSource: " + exact + "\nAllowed kills: " + kills
				+ "\nAll loot from these kills is allowed.",
			"Confirm Soul", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			metaProgressRepository.createPermit(exact, boss, kills);
			rebuild();
		}
	}

	private static void addEditHeading(JPanel panel, String text)
	{
		JLabel heading = new JLabel(text, SwingConstants.CENTER);
		heading.setForeground(SOUL_COLOR);
		heading.setFont(FontManager.getDefaultBoldFont().deriveFont(13f));
		heading.setBorder(BorderFactory.createEmptyBorder(0, 3, 5, 3));
		panel.add(fullWidthLabel(heading));
	}

	private void previewSoulItemFound()
	{
		if (!developerMode) return;
		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		String itemName = hunt == null ? "Soul Hammer" : hunt.getItemName();
		soulItemFoundTracker.preview(itemName, System.currentTimeMillis());
	}

	private void previewExtinctionPopup()
	{
		if (!developerMode) return;
		extinctionCelebrationTracker.preview("Goblin", System.currentTimeMillis());
	}

}

package com.extinctionman;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.config.ConfigManager;

final class ExtinctionLogPanel extends PluginPanel
{
	private static final Color SOUL_COLOR = new Color(170, 225, 255);
	private static final int MAX_VISIBLE_ROWS = 300;
	private final SoulRepository repository;
	private final BestiaryRegistry bestiaryRegistry;
	private final MetaProgressRepository metaProgressRepository;
	private final ItemRegistry itemRegistry;
	private final WhitelistHuntService whitelistHuntService;
	private final ExtinctionManConfig config;
	private final ConfigManager configManager;
	private final WhitelistLootTracker whitelistLootTracker;
	private final ExtinctionCelebrationTracker extinctionCelebrationTracker;
	private final SoulPointCelebrationTracker soulPointCelebrationTracker;
	private final JLabel summary = new JLabel();
	private final JLabel totalSummary = new JLabel();
	private final JLabel storageSummary = new JLabel();
	private final JLabel soulPointsSummary = new JLabel();
	private final JLabel huntSummary = new JLabel();
	private final JLabel whitelistSummary = new JLabel();
	private final JTextField search = new JTextField();
	private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{
		"All monsters", "Completed", "In progress", "Not started"
	});
	private final JPanel entries = new JPanel();
	private final JPanel vaultEntries = new JPanel();
	private final Map<String, RowLabels> visibleRows = new HashMap<>();
	private final JButton copyLog = new JButton("Copy Log");
	private final JButton resetSouls = new JButton("Reset Souls");
	private final JButton markCompleted = new JButton("Mark Completed");
	private final JButton copyBackup = new JButton("Backup");
	private final JButton restoreBackup = new JButton("Restore");
	private final JButton createHunt = new JButton("Create Soul Item");
	private final JButton markObtained = new JButton("Mark Item Obtained");
	private final JButton resetSoulPoints = new JButton("Reset Soul Point Progress");

	@Inject
	ExtinctionLogPanel(SoulRepository repository, BestiaryRegistry bestiaryRegistry,
		MetaProgressRepository metaProgressRepository, ItemRegistry itemRegistry,
		WhitelistHuntService whitelistHuntService, ExtinctionManConfig config,
		ConfigManager configManager, WhitelistLootTracker whitelistLootTracker,
		ExtinctionCelebrationTracker extinctionCelebrationTracker,
		SoulPointCelebrationTracker soulPointCelebrationTracker)
	{
		this.repository = repository;
		this.bestiaryRegistry = bestiaryRegistry;
		this.metaProgressRepository = metaProgressRepository;
		this.itemRegistry = itemRegistry;
		this.whitelistHuntService = whitelistHuntService;
		this.config = config;
		this.configManager = configManager;
		this.whitelistLootTracker = whitelistLootTracker;
		this.extinctionCelebrationTracker = extinctionCelebrationTracker;
		this.soulPointCelebrationTracker = soulPointCelebrationTracker;
		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel title = new JLabel("EXTINCTION MAN", SwingConstants.CENTER);
		title.setForeground(SOUL_COLOR);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		summary.setForeground(Color.LIGHT_GRAY);
		summary.setAlignmentX(Component.CENTER_ALIGNMENT);
		totalSummary.setForeground(Color.GRAY);
		totalSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
		storageSummary.setForeground(Color.GRAY);
		storageSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
		soulPointsSummary.setForeground(SOUL_COLOR);
		soulPointsSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
		huntSummary.setForeground(SoulStatusColors.IN_PROGRESS);
		huntSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
		whitelistSummary.setForeground(Color.LIGHT_GRAY);
		whitelistSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
		search.setToolTipText("Search monsters and raids");
		search.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		statusFilter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		statusFilter.setToolTipText("Filter monsters by soul progress");
		statusFilter.setFocusable(false);
		statusFilter.addActionListener(event -> rebuild());
		copyLog.setAlignmentX(Component.CENTER_ALIGNMENT);
		copyLog.setFocusable(false);
		copyLog.addActionListener(event -> copyLogToClipboard());
		resetSouls.setFocusable(false);
		resetSouls.setToolTipText("Reset one monster or the complete soul log");
		resetSouls.addActionListener(event -> chooseReset());
		markCompleted.setFocusable(false);
		markCompleted.setToolTipText("Manually set one monster to 100/100 souls");
		markCompleted.addActionListener(event -> chooseManualCompletion());
		copyBackup.setFocusable(false);
		copyBackup.setToolTipText("Copy an account backup to the clipboard");
		copyBackup.addActionListener(event -> copyBackupToClipboard());
		restoreBackup.setFocusable(false);
		restoreBackup.setToolTipText("Restore an Extinction Man backup from the clipboard");
		restoreBackup.addActionListener(event -> restoreBackupFromClipboard());
		createHunt.setFocusable(false);
		createHunt.setAlignmentX(Component.CENTER_ALIGNMENT);
		createHunt.addActionListener(event -> createWhitelistHunt());
		markObtained.setFocusable(false);
		markObtained.setAlignmentX(Component.CENTER_ALIGNMENT);
		markObtained.setToolTipText("Manually complete the active hunt for special item delivery cases");
		markObtained.addActionListener(event -> markActiveHuntObtained());
		resetSoulPoints.setFocusable(false);
		resetSoulPoints.setAlignmentX(Component.CENTER_ALIGNMENT);
		resetSoulPoints.setToolTipText("Reset earned Soul Point progress without removing Soul Items");
		resetSoulPoints.addActionListener(event -> confirmResetSoulPoints());
		stylePrimaryButton(createHunt);
		styleSecondaryButton(markObtained);
		styleSecondaryButton(copyLog);
		styleSecondaryButton(resetSouls);
		styleSecondaryButton(markCompleted);
		styleSecondaryButton(resetSoulPoints);
		styleSecondaryButton(copyBackup);
		styleSecondaryButton(restoreBackup);
		header.add(title);
		header.add(Box.createVerticalStrut(4));
		header.add(summary);
		header.add(Box.createVerticalStrut(2));
		header.add(totalSummary);
		header.add(Box.createVerticalStrut(2));
		header.add(storageSummary);

		entries.setLayout(new BoxLayout(entries, BoxLayout.Y_AXIS));
		entries.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane bestiaryScroll = createScrollPane(entries);
		JPanel bestiaryTab = new JPanel(new BorderLayout(0, 8));
		bestiaryTab.setOpaque(false);
		JPanel bestiaryTools = verticalPanel();
		bestiaryTools.add(search);
		bestiaryTools.add(Box.createVerticalStrut(5));
		bestiaryTools.add(statusFilter);
		bestiaryTools.add(Box.createVerticalStrut(5));
		JPanel logButtons = new JPanel(new GridLayout(1, 2, 5, 0));
		logButtons.setOpaque(false);
		logButtons.add(copyLog);
		logButtons.add(resetSouls);
		bestiaryTools.add(logButtons);
		bestiaryTools.add(Box.createVerticalStrut(4));
		bestiaryTools.add(fullWidthButton(markCompleted));
		bestiaryTab.add(bestiaryTools, BorderLayout.NORTH);
		bestiaryTab.add(bestiaryScroll, BorderLayout.CENTER);

		vaultEntries.setLayout(new BoxLayout(vaultEntries, BoxLayout.Y_AXIS));
		vaultEntries.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JPanel vaultTab = new JPanel(new BorderLayout(0, 8));
		vaultTab.setOpaque(false);
		JPanel vaultTools = verticalPanel();
		vaultTools.add(soulPointsSummary);
		vaultTools.add(Box.createVerticalStrut(4));
		vaultTools.add(huntSummary);
		vaultTools.add(Box.createVerticalStrut(2));
		vaultTools.add(whitelistSummary);
		vaultTools.add(Box.createVerticalStrut(7));
		vaultTools.add(createHunt);
		vaultTools.add(Box.createVerticalStrut(4));
		vaultTools.add(markObtained);
		vaultTools.add(Box.createVerticalStrut(7));
		vaultTools.add(fullWidthButton(resetSoulPoints));
		vaultTab.add(vaultTools, BorderLayout.NORTH);
		vaultTab.add(createScrollPane(vaultEntries), BorderLayout.CENTER);

		JPanel dataTab = verticalPanel();
		dataTab.setBorder(BorderFactory.createEmptyBorder(8, 2, 8, 2));
		JLabel dataTitle = new JLabel("Backup & Restore");
		dataTitle.setForeground(SOUL_COLOR);
		dataTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel dataHelp = new JLabel("<html><div style='text-align:center;width:170px'>"
			+ "Keep your progress safe or move it to another PC.</div></html>");
		dataHelp.setForeground(Color.GRAY);
		dataHelp.setAlignmentX(Component.CENTER_ALIGNMENT);
		dataTab.add(dataTitle);
		dataTab.add(Box.createVerticalStrut(5));
		dataTab.add(dataHelp);
		dataTab.add(Box.createVerticalStrut(10));
		dataTab.add(fullWidthButton(copyBackup));
		dataTab.add(Box.createVerticalStrut(6));
		dataTab.add(fullWidthButton(restoreBackup));
		dataTab.add(Box.createVerticalGlue());

		JTabbedPane tabs = new JTabbedPane();
		tabs.setFocusable(false);
		tabs.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tabs.setForeground(Color.LIGHT_GRAY);
		tabs.addTab("Bestiary", bestiaryTab);
		tabs.addTab("Vault", vaultTab);
		tabs.addTab("Data", dataTab);

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
		List<SoulProgress> all = combinedEntries(encountered);
		updateProgressSummary(encountered);
		totalSummary.setText(bestiaryRegistry.isComplete()
			? all.size() + " total monsters"
			: "Scanning monster list...");
		storageSummary.setText(repository.isProfileStorageAvailable()
			? "Account profile storage active"
			: "Log in to activate account storage");
		updateMetaSection();
		entries.removeAll();
		visibleRows.clear();
		String query = search.getText().trim().toLowerCase(Locale.ENGLISH);
		String selectedStatus = (String) statusFilter.getSelectedItem();
		int shown = 0;
		for (SoulProgress progress : all)
		{
			if (!matches(progress, query, selectedStatus))
			{
				continue;
			}
			if (shown >= MAX_VISIBLE_ROWS)
			{
				break;
			}
			entries.add(createRow(progress));
			entries.add(Box.createVerticalStrut(4));
			shown++;
		}
		if (shown == 0)
		{
			JLabel empty = new JLabel(bestiaryRegistry.isComplete()
				? "No matching monsters."
				: "Scanning monster list...");
			empty.setForeground(Color.GRAY);
			entries.add(empty);
		}
		else if (matchingCount(all, query, selectedStatus) > MAX_VISIBLE_ROWS)
		{
			JLabel limit = new JLabel("Showing first " + MAX_VISIBLE_ROWS + " results. Refine your search.");
			limit.setForeground(Color.GRAY);
			entries.add(limit);
		}
		entries.revalidate();
		entries.repaint();
	}

	private void updateMetaSection()
	{
		int availablePoints = metaProgressRepository.getAvailablePoints();
		soulPointsSummary.setText("Soul Points: " + availablePoints
			+ "  -  Next: " + metaProgressRepository.getProgressToNextPoint() + "/"
			+ MetaProgressRepository.EXTERMINATIONS_PER_SOUL_POINT);
		WhitelistUnlock activeHunt = whitelistHuntService.getActiveHunt();
		huntSummary.setText(activeHunt == null ? "No active Soul items"
			: "Hunting: " + activeHunt.getItemName() + " from " + activeHunt.getSourceNpcName());
		long collectedItems = metaProgressRepository.getUnlocks().stream()
			.filter(WhitelistUnlock::isAcquired)
			.count();
		whitelistSummary.setText("Soul Items collected: " + collectedItems);
		createHunt.setEnabled(activeHunt == null && metaProgressRepository.canCreateUnlock());
		markObtained.setVisible(activeHunt != null);
		markObtained.setEnabled(activeHunt != null);
		rebuildVault(activeHunt);
	}

	private void updateProgressSummary(List<SoulProgress> encountered)
	{
		long extinctCount = encountered.stream().filter(SoulProgress::isExtinct).count();
		summary.setText(extinctCount + " extinct  -  " + encountered.size() + " encountered");
	}

	private void rebuildVault(WhitelistUnlock activeHunt)
	{
		vaultEntries.removeAll();
		List<WhitelistUnlock> unlocks = metaProgressRepository.getUnlocks();
		if (activeHunt != null)
		{
			addVaultHeading("Active Soul Hunt");
			addVaultItem(activeHunt);
			vaultEntries.add(Box.createVerticalStrut(8));
		}
		addVaultHeading("Collected Soul Items");
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
			JLabel empty = new JLabel("No Soul Items collected yet.");
			empty.setForeground(Color.GRAY);
			empty.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
			vaultEntries.add(empty);
		}
		vaultEntries.revalidate();
		vaultEntries.repaint();
	}

	private void addVaultHeading(String text)
	{
		JLabel heading = new JLabel(text);
		heading.setForeground(SOUL_COLOR);
		heading.setFont(heading.getFont().deriveFont(12f));
		heading.setBorder(BorderFactory.createEmptyBorder(4, 3, 5, 3));
		vaultEntries.add(heading);
	}

	private void addVaultItem(WhitelistUnlock unlock)
	{
		JPanel row = new JPanel(new BorderLayout(4, 2));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
		JLabel item = new JLabel(unlock.getItemName());
		item.setForeground(unlock.isAcquired() ? SoulStatusColors.COMPLETE
			: SoulStatusColors.IN_PROGRESS);
		JLabel source = new JLabel((unlock.isAcquired() ? "Obtained from " : "Hunting from ")
			+ unlock.getSourceNpcName());
		source.setForeground(Color.GRAY);
		JPanel details = verticalPanel();
		details.add(item);
		details.add(source);
		row.add(details, BorderLayout.CENTER);
		JButton remove = new JButton("Remove Item");
		remove.setFocusable(false);
		remove.setToolTipText("Remove " + unlock.getItemName() + " from your Soul Items");
		styleSecondaryButton(remove);
		remove.addActionListener(event -> confirmRemoveWhitelistItem(unlock));
		row.add(fullWidthButton(remove), BorderLayout.SOUTH);
		vaultEntries.add(row);
		vaultEntries.add(Box.createVerticalStrut(4));
	}

	private void confirmRemoveWhitelistItem(WhitelistUnlock unlock)
	{
		Object[] choices = {"Remove and refund Soul Point", "Remove without refund", "Cancel"};
		int choice = JOptionPane.showOptionDialog(this,
			"Remove " + unlock.getItemName() + " from your Soul Items?\n\n"
				+ "Source: " + unlock.getSourceNpcName() + "\n"
				+ "If this is the active hunt, that hunt will also end.",
			"Remove Soul Item", JOptionPane.DEFAULT_OPTION,
			JOptionPane.WARNING_MESSAGE, null, choices, choices[0]);
		if (choice == 0 || choice == 1)
		{
			metaProgressRepository.removeUnlock(unlock.getItemId(), choice == 0);
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

	private static JScrollPane createScrollPane(JPanel content)
	{
		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		return scrollPane;
	}

	private static void stylePrimaryButton(JButton button)
	{
		button.setBackground(new Color(91, 67, 39));
		button.setForeground(new Color(255, 190, 72));
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(151, 111, 54)),
			BorderFactory.createEmptyBorder(5, 8, 5, 8)));
	}

	private static void styleSecondaryButton(JButton button)
	{
		button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		button.setForeground(Color.LIGHT_GRAY);
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(80, 80, 80)),
			BorderFactory.createEmptyBorder(4, 7, 4, 7)));
	}

	private JPanel createRow(SoulProgress progress)
	{
		JPanel row = new JPanel(new BorderLayout(4, 2));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

		JLabel name = new JLabel(progress.getNpcName());
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

	private static long matchingCount(List<SoulProgress> all, String query, String selectedStatus)
	{
		return all.stream()
			.filter(progress -> matches(progress, query, selectedStatus))
			.count();
	}

	private static boolean matches(SoulProgress progress, String query, String selectedStatus)
	{
		if (!progress.getNpcName().toLowerCase(Locale.ENGLISH).contains(query))
		{
			return false;
		}
		if ("Completed".equals(selectedStatus))
		{
			return progress.isExtinct();
		}
		if ("In progress".equals(selectedStatus))
		{
			return progress.getSouls() > 0 && !progress.isExtinct();
		}
		if ("Not started".equals(selectedStatus))
		{
			return progress.getSouls() == 0;
		}
		return true;
	}

	private void confirmReset(SoulProgress progress)
	{
		String npcName = progress.getNpcName();
		int answer = JOptionPane.showConfirmDialog(
			this,
			"Reset all souls for " + npcName + "?\nThis cannot be undone.",
			"Confirm soul reset",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			if (progress.isExtinct())
			{
				metaProgressRepository.revokeAward(npcName);
			}
			repository.reset(npcName);
			updateProgressSummary(repository.getAll());
			updateMetaSection();
			updateVisibleRow(npcName);
		}
	}

	private void chooseReset()
	{
		Object[] choices = {"Specific monster", "Entire soul log", "Cancel"};
		int choice = JOptionPane.showOptionDialog(this,
			"What would you like to reset?",
			"Reset Souls",
			JOptionPane.DEFAULT_OPTION,
			JOptionPane.WARNING_MESSAGE,
			null,
			choices,
			choices[0]);
		if (choice == 0)
		{
			chooseSpecificReset();
		}
		else if (choice == 1)
		{
			confirmResetAll();
		}
	}

	private void chooseSpecificReset()
	{
		List<SoulProgress> progress = repository.getAll();
		if (progress.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "There are no collected souls to reset.",
				"Nothing to reset", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SoulProgress selected = (SoulProgress) JOptionPane.showInputDialog(this,
			"Choose the monster whose souls you want to reset:",
			"Reset specific monster",
			JOptionPane.WARNING_MESSAGE,
			null,
			progress.toArray(),
			progress.get(0));
		if (selected != null)
		{
			confirmReset(selected);
		}
	}

	private void confirmResetAll()
	{
		int answer = JOptionPane.showConfirmDialog(this,
			"Reset the ENTIRE soul log to zero?\n\n"
				+ "Soul Point progress earned from completed monsters will also be reset.\n"
				+ "Existing Soul Items will be kept.\n"
				+ "Create a backup first if you may need this progress later.",
			"Confirm complete soul reset",
			JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			repository.resetAll();
			metaProgressRepository.resetSoulPointProgress();
			updateProgressSummary(repository.getAll());
			updateMetaSection();
			for (String npcName : new ArrayList<>(visibleRows.keySet()))
			{
				updateVisibleRow(npcName);
			}
		}
	}

	private void confirmResetSoulPoints()
	{
		int answer = JOptionPane.showConfirmDialog(this,
			"Reset all earned Soul Point progress?\n\n"
				+ "Available Soul Points and progress toward the next point will return to zero.\n"
				+ "Existing Soul Items and monster souls will be kept.",
			"Confirm Soul Point reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			metaProgressRepository.resetSoulPointProgress();
			updateMetaSection();
		}
	}

	private void chooseManualCompletion()
	{
		String query = JOptionPane.showInputDialog(this,
			"Search for the monster you want to set to 100/100:",
			"Mark monster completed", JOptionPane.QUESTION_MESSAGE);
		if (query == null || query.trim().isEmpty()) return;
		String lowerQuery = query.trim().toLowerCase(Locale.ENGLISH);
		List<SoulProgress> matches = new ArrayList<>();
		for (SoulProgress progress : combinedEntries(repository.getAll()))
		{
			if (!progress.isExtinct()
				&& progress.getNpcName().toLowerCase(Locale.ENGLISH).contains(lowerQuery))
			{
				matches.add(progress);
				if (matches.size() >= 100) break;
			}
		}
		if (matches.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "No incomplete matching monster was found.",
				"Monster not found", JOptionPane.ERROR_MESSAGE);
			return;
		}
		SoulProgress selected = (SoulProgress) JOptionPane.showInputDialog(this,
			"Choose the exact monster:", "Mark monster completed",
			JOptionPane.WARNING_MESSAGE, null, matches.toArray(), matches.get(0));
		if (selected == null) return;
		int answer = JOptionPane.showConfirmDialog(this,
			"Set " + selected.getNpcName() + " to 100/100 souls?\n\n"
				+ "This awards one completed extermination toward Soul Point progress.\n"
				+ "Resetting this monster later will remove that progress again.",
			"Confirm manual completion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			repository.markComplete(selected.getNpcName());
			SoulPointAward award = metaProgressRepository.award(selected.getNpcName());
			long now = System.currentTimeMillis();
			extinctionCelebrationTracker.show(selected.getNpcName(), now,
				award.isSoulPointEarned());
			if (award.isSoulPointEarned())
			{
				soulPointCelebrationTracker.show(metaProgressRepository.getAvailablePoints(), now);
			}
			updateProgressSummary(repository.getAll());
			updateMetaSection();
			updateVisibleRow(selected.getNpcName());
		}
	}

	private void copyLogToClipboard()
	{
		String text = SoulLogExporter.format(repository.getAll());
		try
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
			copyLog.setText("Copied!");
			javax.swing.Timer timer = new javax.swing.Timer(1500, event -> copyLog.setText("Copy Log"));
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
		if (!itemRegistry.isComplete())
		{
			JOptionPane.showMessageDialog(this, "The item list is still loading. Please try again shortly.",
				"Item list loading", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String query = JOptionPane.showInputDialog(this,
			"Search for the item you want to unlock as a Soul Item:",
			"Choose Soul Item", JOptionPane.QUESTION_MESSAGE);
		if (query == null || query.trim().isEmpty()) return;
		List<WhitelistItemOption> matches = itemRegistry.search(query, 50);
		if (matches.isEmpty())
		{
			JOptionPane.showMessageDialog(this, "No matching item was found.",
				"Item not found", JOptionPane.ERROR_MESSAGE);
			return;
		}
		WhitelistItemOption item = (WhitelistItemOption) JOptionPane.showInputDialog(this,
			"Select the exact item:", "Choose Soul Item", JOptionPane.QUESTION_MESSAGE,
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
			"Spend 1 Soul Point?\n\nItem: " + item.getItemName()
				+ "\nSource: " + sourceNpc
				+ "\n\nOnly this Soul Item may be taken during the hunt.",
			"Confirm Soul Item", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			metaProgressRepository.createUnlock(item.getItemId(), item.getItemName(), sourceNpc);
			rebuild();
		}
	}

	private void markActiveHuntObtained()
	{
		WhitelistUnlock hunt = whitelistHuntService.getActiveHunt();
		if (hunt == null) return;
		int answer = JOptionPane.showConfirmDialog(this,
			"Mark " + hunt.getItemName() + " as obtained and close this hunt?",
			"Confirm hunt completion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION)
		{
			whitelistHuntService.completeActiveHunt();
			whitelistLootTracker.clear();
			rebuild();
		}
	}
}

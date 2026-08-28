# Extinction Man

Extinction Man is a self-imposed Old School RuneScape challenge plugin for RuneLite.
It records valid NPC kills as Souls. At 100 Souls, that exact monster name becomes
**EXTINCT**.

The plugin is a client-side tracker. It does not create items, change server state,
automate input, or guarantee that a player follows the challenge rules.

## Core rules

- One valid, uncontested death grants one Soul.
- Progress is stored by exact visible NPC name and stops at 100/100.
- Empty drops still count because Souls are awarded from death participation rather
  than loot events.
- Player damage delivered through poison, recoil, burn, or similar delayed effects
  remains eligible after earlier player-owned damage.
- A kill contested by another player does not grant a Soul.
- Extinct NPCs are hidden unless a supported exception makes them necessary.
- Exception kills grant no additional Souls.
- Forbidden loot is marked visually. The public plugin does not remove the game's
  **Take** option or prevent a player from picking an item up.

## Exceptions and activities

- Slayer tasks temporarily reveal matching extinct targets.
- One configurable mandatory-kill exception supports quests and unlock requirements.
- Soul Item hunts temporarily reveal an extinct source monster until the selected
  item is obtained.
- God Wars support monsters remain available inside God Wars Dungeon, but their loot
  is forbidden after extinction.
- Raid completions advance the combined raid and encountered raid bosses instead of
  counting individual room deaths.
- Fight Caves and Inferno count only TzTok-Jad and TzKal-Zuk respectively.
- The normal and Corrupted Gauntlet count only their own Hunllef.
- Nightmare Zone monsters use the normal 100-Soul extinction rule.

## Soul Points and Soul Items

Every 100 unique completed exterminations awards one Soul Point. One Soul Point can
unlock one Soul Item. The active extinct source becomes accessible while the hunt is
active, and the selected drop receives a spectral loot beam. Obtained Soul Items are
kept in the Soul Vault history.

## Interface

The sidebar contains three tabs:

- **Bestiary** — search and filter the complete monster database.
- **Vault** — view Soul Points, the active hunt, and collected Soul Items.
- **Data** — copy or restore a complete backup.

The gameplay overlay shows only the most recently credited monster. Extinction and
Soul Point milestones use collection-log-style popups. Extinct monsters revealed by
an exception receive a cold spectral appearance.

## Storage and backups

Soul progress, Soul Points, and Soul Items use RuneLite RuneScape-profile storage so
characters remain separate. Local emergency recovery data is also separated by
RuneScape profile. Clipboard backups include the complete Soul log and Soul Item
economy. Existing V2 Soul-only backups remain importable.

## Development

Requirements: Java 11 or newer and IntelliJ IDEA Community Edition or another Java IDE.

1. Run `./gradlew test`.
2. Run `./gradlew run` to open the RuneLite development client.
3. Enable **Extinction Man** in the plugin list.
4. Perform all RuneScape actions manually.

Automated tests cover counters, matching, special-activity rules, storage codecs,
backup validation, marker lifetimes, popup trackers, and Soul Point calculations.
They never control the game client or provide automated gameplay.

## Language

All player-facing plugin text is English. Development discussion and support may use
other languages.

## Jagex attribution

This is an unofficial, non-commercial fan project. It is not endorsed by or
affiliated with Jagex. Old School RuneScape and RuneScape are trademarks of Jagex
Ltd. This project uses Jagex property under the Jagex Fan Content Policy.

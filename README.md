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
- Forbidden ground loot is labelled `Item name (Locked)`. During an active Unbound Soul, this build
  removes **Take** only from forbidden drops registered at a later exception-kill's
  tile, while keeping the selected item. Other ground items remain available.
  All drops from the 100th kill remain available; restrictions begin with later
  kills made after that monster is already extinct. Normal menus return when the hunt ends.
  This is a client-side menu restriction, not server-side enforcement; spell
  targeting, inventory actions and containers are not filtered. Plugin Hub
  acceptance of this conditional menu restriction has not been confirmed.

## Exceptions and activities

- Slayer tasks temporarily reveal matching extinct targets.
- One configurable mandatory-kill exception supports quests and unlock requirements.
- Unbound Souls temporarily reveal an extinct source monster until the selected
  item is obtained.
- God Wars support monsters remain available inside God Wars Dungeon, but their loot
  is forbidden after extinction.
- Raid completions advance one combined raid entry instead of counting individual
  room deaths. After 100 full completions, attackable monsters inside that raid are
  hidden together; an eligible forged Soul temporarily restores the raid.
- Fight Caves and Inferno count only TzTok-Jad and TzKal-Zuk respectively.
- The normal and Corrupted Gauntlet count only their own Hunllef.
- Nightmare Zone monsters use the normal 100-Soul extinction rule.

## Soul Energy and forged Souls

Extinguishing a normal monster species awards one Soul Energy; extinguishing a boss
awards ten. One Energy forges a Weak Soul for one extra normal-monster kill, ten Energy
forges a Strong Soul for one extra boss kill, and 100 Energy forges one Unbound Soul
bound to a chosen item. Weak and Strong Souls can purchase several kills at once and
multiple Souls may remain active together. Each source becomes extinct again when its
purchased kill count reaches zero, while loot from those exact kills remains allowed.
An active Unbound source remains accessible until its chosen item is obtained, and
multiple Unbound Souls may be active together. Every selected ground drop receives a
golden loot beam with a gold item name in
its **Take** menu entry. Each beam disappears with its ground item. Picking up one
completes one matching Unbound Soul. The source is hidden when no other forged Soul
keeps it active. Inventory changes only complete a ground-loot target after the player
clicked that marked drop; withdrawing an existing copy from a bank does not count.
Supported raid, Colosseum, Barrows, and Moons of Peril reward containers instead
show a golden **Unbound Soul Found** popup after verifying both the required source and
item. Gauntlet reward-chest and server-confirmed direct-inventory drops use the same
popup and completion path. Bank withdrawals cannot complete a hunt. Bundled rewards
cannot be split client-side: the game awards their incidental contents together with
the selected item, so those extras must still be treated as forbidden challenge loot.
Obtained Unbound Souls are kept in the Souls history.

## Interface

The sidebar contains four tabs:

- **Bestiary** — search and filter the complete monster database, including the boss view.
- **Souls** — view available and spent Energy, forge Weak, Strong or Unbound Souls, and review them.
- **Info** — read the challenge rules and the short guide for every tab.
- **Dev** — correct or back up saved progress after entering the session password `Soul`.

The gameplay overlay shows the most recently credited monster and current Soul Energy.
An extinction uses one collection-log-style popup whose final line reports **1 Soul
Energy Collected** for a monster or **10 Soul Energy Collected** for a boss. Unbound
Souls retain their own found popup. Extinct monsters revealed by an Unbound Soul receive
a small animated golden Soul orb that hovers at the monster's
centre. Only the small sparks circle the orb itself. The marker leaves the NPC's real
model, colours and clickbox intact. Weak Souls use a white orb, Strong Souls a black
orb, Slayer exceptions a blue orb, and extinction animations remain red.

Mandatory exceptions match the full NPC name without regard to capitalization:
`goblin`, `Goblin`, and `GOBLIN` all select Goblin, but not Hobgoblin.

**Milestone sounds** plays the original Slayer level-up jingle for extinction and
the Prayer level-up jingle for found Unbound Souls.
**Milestone volume** is independent of the game's music slider; set it to zero to
mute these jingles. Windows application volume still applies. Valid kills display
a rising blue orb with a pulsing glow and small sparkling particles.

## Storage and backups

Soul progress, Soul Energy, and forged Souls use RuneLite RuneScape-profile storage so
characters remain separate. Local emergency recovery data is also separated by
RuneScape profile. Clipboard backups include the complete Soul log and Soul Energy
economy. Existing V2 Soul-only backups remain importable.

## Development

Requirements: Java 11 or newer and IntelliJ IDEA Community Edition or another Java IDE.

1. Run `./gradlew test`.
2. Run `./gradlew run` to open the RuneLite development client.
3. Enable **Extinction Man** in the plugin list.
4. Perform all RuneScape actions manually.

Automated tests cover counters, matching, special-activity rules, storage codecs,
backup validation, marker lifetimes, popup trackers, and Soul Energy calculations.
They never control the game client or provide automated gameplay.

The development client's **Dev** tab contains targeted test tools. **Next Kill:
Extinction** affects only the next valid, non-exception kill. **Set Soul Energy**
replaces the available Soul Energy with the entered value without inventing monster
progress. Test changes are saved to
the current character, so create a backup first if you may want to undo them.

Developer builds also provide Extinction and Unbound Soul popup previews under Dev.
These previews do not change saved progress. Metadata version 6 stores multiple active
forged Souls and remaining kill counts; older backups remain supported.

Reward support is source-specific. Ordinary NPC ground drops, server-reported direct
NPC loot, raids, Colosseum, Barrows, Moons of Peril, and both Gauntlets are covered.
Item transformations, token exchanges, and unrelated minigame or quest reward screens
need their own verified adapter before they can complete an Unbound Soul.

## Language

All player-facing plugin text is English. Development discussion and support may use
other languages.

## Jagex attribution

This is an unofficial, non-commercial fan project. It is not endorsed by or
affiliated with Jagex. Old School RuneScape and RuneScape are trademarks of Jagex
Ltd. This project uses Jagex property under the Jagex Fan Content Policy.

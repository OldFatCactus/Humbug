Humbug
======

Minecraft server plug-in: Simply disables various functionality

- Disables Anvil use.
- Disables Ender Chest use.
- Disables Villager trading.
- Disables Wither block destruction radius.
- Disables Wither and Wither Skull explosions.

Config file settings:
- debug: Boolean, Turns on debug logging (currently there is none)
- anvil: Boolean, Turns on anvil use
- ender_chest: Boolean, Turns on ender chest use
- villager_trades: Boolean, Turns on villager trades
- wither: Boolean, Turns on the wither
- wither_explosions: Boolean, Turns on wither explosions destroying blocks
-     Wither/Wither Skull explosions will always occur to damage players,
-     this only effects block breakage.
- wither_insta_break: Boolean, Turns on the wither insta-break ability

Default configuration:
    debug: false
    anvil: false
    ender_chest: false
    villager_trades: false
    wither: true
    wither_explosions: false
    wither_insta_break: false

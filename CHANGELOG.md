# 2.2.1

- Replaced all sound recordings with original synthesized sounds (tools/generate_sounds.py). No Valve audio remains.
- Removed an unused leftover sound file.

# 2.2.0

- Added a Mod Menu settings screen: range, cooldown, volume, cross-dimension, teleport mobs and items. Changes save to config/portalgunremastered.json.
- Added option to limit teleporting to players only.
- No fall damage after passing through a portal.
- Thread-safe config loading; cooldown state cleared when the server stops.

# 2.1.0

- Fixed portals not teleporting players; players are now checked every tick.
- Portals now link across dimensions (toggle in config).
- Added keybinds: Switch Portal Color, Fire Blue, Fire Orange, Clear Portals.
- Added config/portalgunremastered.json: range, cooldownTicks, soundVolume, crossDimension.
- Renamed to Portal Gun Remastered (mod id unchanged).
- Portal links are now stored as dimension + position; portals placed with 2.0.0 must be re-fired once.

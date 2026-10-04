# Portal Gun Remastered (Fabric, Minecraft 26.2)

Mod id stays `portalgunclassic` so existing worlds keep their portal guns and blocks.

Controls
- Right-click: fire the current portal color
- Keybinds (Options > Controls > Portal Gun Remastered): Switch Portal Color (R), Fire Blue, Fire Orange, Clear Portals (last three unbound by default)

Walk into either portal and you come out of the other one. Players are detected every tick; other entities by the portal blocks.
Config: Mod Menu settings screen, or edit config/portalgunremastered.json (range, cooldownTicks, soundVolume, crossDimension, teleportOtherEntities). Mod Menu is optional.
Debug: launch with -Dportalgun.debug=true to log every teleport.

Compatibility: no mixins, only public Fabric API, so no conflicts with Sodium, Lithium, Iris, etc.

Licensed LGPL-3.0-or-later. Original mod by iChun.
Sounds are original and synthesized by tools/generate_sounds.py (needs python3, numpy, scipy, ffmpeg); rerun it to regenerate them.

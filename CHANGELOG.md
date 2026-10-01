# Grab Villager 1.0.5 (Minecraft 26.1)

### Bug Fixes & Security Hardening:
- **Anti-Griefing & Claim Protection**: Players can no longer pick up or eject entities in protected areas (WorldGuard, FTB Chunks, GriefPrevention) or out of reach.
- **Anti-Noclip & Anti-Suffocation**: Throwing or dropping entities against walls or closed doors now uses collision raycasting, safely placing entities without suffocation or wall glitches.
- **Flight & Fall Damage Protection**: Fixed issue where entities falling longer than 5 seconds took fall damage. Thrown entities now receive fall damage immunity throughout the entire flight until touching the ground.
- **Sunlight Protection for Zombie Villagers**: Zombie Villagers carried by players no longer ignite or burn under direct sunlight, and pre-existing fire is cleared upon pickup.
- **Network Rate Limiting**: Added server-side cooldown on drop/throw packets to prevent spam.
- **Throw Power Client-Server Sync**: The client's chosen throw multiplier in `/grabvillager` is now properly transmitted and honored by the server.
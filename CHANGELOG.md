# Grab Villager 1.0.12 (Minecraft 26.2)

### New Features:
- **Carried Villager HUD Badge**: Displays a discreet badge above the hotbar showing the carried villager's profession and level (or Baby / Unemployed / Zombie Villager / Wandering Trader), along with a heart icon and HP readout when injured.

### Bug Fixes & Improvements:
- Fixed soft-block water bounce cutting off immediately on the next tick and prevented lava/vehicle landings from triggering bounce effects.
- Improved mid-air Catch Mechanic with line-of-sight checks, valid player state checks (no catching while sleeping, gliding, or riding), and proper thrower reset.
- Improved Trajectory Preview to stop on water surfaces, check the villager's hitbox width, respect the server's throw multiplier cap, and cancel cleanly when opening a GUI.
- Added server-to-client config sync (`throwMultiplier` cap and `allowTools`) for consistent multiplayer behavior.
- Checked vehicle capacity before dismounting in targeted vehicle placement and properly respected datapack `grabbable` tag overrides.
- Extended mid-air panic animation to Wandering Traders and Zombie Villagers, and rounded config slider values cleanly.

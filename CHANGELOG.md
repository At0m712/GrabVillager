# Grab Villager NeoForge 1.0.8 (Minecraft 26.3)

### New Features (Full Fabric Parity + HUD Badge):
- **Carried Villager HUD Badge**: Displays a discreet badge above the hotbar showing the carried villager's profession and level (or Baby / Unemployed / Zombie Villager / Wandering Trader), along with a heart icon and HP readout when injured.
- **Targeted Vehicle Placement**: Shift + Right-click directly on a Boat or Minecart while carrying a villager to seat them inside immediately.
- **Festive Baby Villager Reactions**: Baby villagers emit festive particles and cheerful celebration sounds when picked up and thrown.
- **Trajectory Preview**: Displays a dotted parabolic arc and impact marker while charging a throw.
- **Multiplayer Catch Mechanic**: Throw a villager toward an empty-handed friend to catch them mid-air.
- **Soft Block Cushion & Bounce**: Landing on Hay Bales, Beds, Water, Mud, Wool, or Slime prevents impact damage and triggers a playful bounce with custom sounds and particles.

### Bug Fixes & Improvements:
- Added aggro/target protection (`CarriedMobTargetMixin`) and target clearing when picking up mobs.
- Added auto-dismount when sleeping, gliding with Elytra, or entering a vehicle, plus improved first-person hiding.
- Added claim checks for dropping/throwing, disconnect cleanup, server-to-client config sync, and datapack tag override support.
- Fixed water bounce cutoffs, lava/vehicle landing edge cases, line-of-sight catch checks, GUI charge cancellation, and extended mid-air panic animations to Wandering Traders and Zombie Villagers.

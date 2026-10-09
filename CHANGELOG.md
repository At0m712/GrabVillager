# Grab Villager 1.0.9 (Minecraft 26.3)

### Bug Fixes & Improvements:
- **Claim Protection on Drop & Throw Destination**: Validated `mayInteract` at drop and throw destination blocks to prevent ejecting mobs into claimed or protected areas.
- **Auto Dismount on Sleeping, Elytra & Vehicle Entry**: Carried mobs are now automatically safely placed down when the player sleeps, glides with an elytra, or mounts a vehicle.
- **Infinite Flight Loop Fix**: Added a 15-second (300 ticks) maximum flight duration cap to prevent infinite flight audio and particle loops in cobwebs, powdered snow, or levitation.
- **Mob AI & Golem Aggro Protection**: Carried mobs can no longer target or attack anything, and hostile mobs or Iron Golems will not target or attack carried mobs while held.
- **Custom Sounds for Trader Llamas**: Trader Llamas now play proper llama ambient and hurt sounds instead of human villager sounds when held, thrown, or dropped.
- **First-Person View While Charging**: The carried mob is now visible in first-person view while charging a throw to provide visual feedback of the windup animation.
- **Memory Leak Fix**: Cleaned up disconnect listener to purge player rate-limiting data when disconnecting.
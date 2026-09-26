package atom.grabvillager.client.mixin;

import atom.grabvillager.config.GrabVillagerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class HideHandMixin {

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void grabvillager$hideHandWhenCarrying(CallbackInfo ci) {

        // 1. SÉCURITÉ : Si la configuration AUTORISE l'usage des outils, on ne cache rien du tout.
        if (GrabVillagerConfig.allowTools) {
            return;
        }

        // 2. Si les outils sont INTERDITS, on vérifie si on porte une entité grabbable
        var player = Minecraft.getInstance().player;
        if (player != null && player.getPassengers().stream().anyMatch(atom.grabvillager.logic.GrabVillagerLogic::isGrabbable)) {
            // On annule l'affichage de la main et de l'objet
            ci.cancel();
        }
    }
}
package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.BlockHider;
import kr.pyke.blockhider.transform.HitboxOwner;
import kr.pyke.blockhider.transform.PlayerTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Interaction.class)
public class InteractionMixin implements HitboxOwner {
    @Unique private Player blockhider$owner;

    @Override public void blockhider$setOwner(Player player) { this.blockhider$owner = player; }

    @Override public Player blockhider$getOwner() { return this.blockhider$owner; }

    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void blockhider$ignoreOwnHitbox(CallbackInfoReturnable<Boolean> cir) {
        Interaction self = (Interaction) (Object) this;
        if (self.level().isClientSide()) {
            Player localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null) {
                PlayerTransform transform = (PlayerTransform) localPlayer;
                if (transform.blockhider$getTransformedBlock() != null && transform.blockhider$getTransformedPos() != null) {
                    double expectedX = transform.blockhider$getTransformedPos().getX() + 0.5d;
                    double expectedY = transform.blockhider$getTransformedPos().getY() + 1.d;
                    double expectedZ = transform.blockhider$getTransformedPos().getZ() + 0.5d;

                    if (self.distanceToSqr(expectedX, expectedY, expectedZ) < 0.01) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }

    @Inject(method = "skipAttackInteraction(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void blockhider$redirectAttack(Entity source, CallbackInfoReturnable<Boolean> cir) {
        Interaction self = (Interaction) (Object) this;
        BlockHider.LOGGER.info("skipAttackInteraction called: attacker={}, owner={}, clientSide={}", source, this.blockhider$owner, self.level().isClientSide());

        if (self.level().isClientSide()) { return; }
        if (!(source instanceof Player player)) { return; }
        if (this.blockhider$owner == null || this.blockhider$owner == player || !this.blockhider$owner.isAlive()) { return; }

        BlockHider.LOGGER.info("Redirecting to owner");
        player.attack(this.blockhider$owner);
        cir.setReturnValue(true);
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void blockhider$redirectHurt(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        BlockHider.LOGGER.info("[BlockHider] hurtServer called: source={}, damage={}", source.getEntity(), damage);
        if (this.blockhider$owner == null || !this.blockhider$owner.isAlive()) { return; }

        Entity attacker = source.getEntity();
        if (attacker == this.blockhider$owner) { return; }

        boolean hit = this.blockhider$owner.hurtServer(level, source, damage);
        cir.setReturnValue(hit);
    }
}
package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.HitboxOwner;
import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.transform.TransformableBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerTransform {
    @Unique private BlockState blockhider$transformedBlock;
    @Unique private BlockPos blockhider$transformedPos;
    @Unique private Interaction blockhider$hitboxEntity;

    @Override public BlockState blockhider$getTransformedBlock() { return this.blockhider$transformedBlock; }

    @Override public BlockPos blockhider$getTransformedPos() { return this.blockhider$transformedPos; }

    @Override public Interaction blockhider$getHitboxEntity() { return this.blockhider$hitboxEntity; }

    @Override public void blockhider$setHitboxEntity(Interaction entity) { this.blockhider$hitboxEntity = entity; }

    @Override
    public void blockhider$setTransformedBlock(BlockState blockState, BlockPos blockPos) {
        if (blockState == this.blockhider$transformedBlock && Objects.equals(blockPos, this.blockhider$transformedPos)) { return; }

        this.blockhider$transformedBlock = blockState;
        this.blockhider$transformedPos = blockPos;

        Player player = (Player) (Object) this;

        if (!player.level().isClientSide()) {
            if (this.blockhider$hitboxEntity != null && !this.blockhider$hitboxEntity.isRemoved()) {
                this.blockhider$hitboxEntity.discard();
                this.blockhider$hitboxEntity = null;
            }

            if (blockState != null && blockPos != null) {
                EntityDimensions dimensions = TransformableBlocks.getDimensions(player.level(), blockPos, blockState);
                Interaction hitbox = EntityType.INTERACTION.create(player.level(), EntitySpawnReason.COMMAND);

                if (hitbox != null) {
                    hitbox.setWidth(dimensions.width());
                    hitbox.setHeight(dimensions.height());
                    hitbox.setPos(blockPos.getX() + 0.5d, blockPos.getY() + 1.d, blockPos.getZ() + 0.5d);

                    if (hitbox instanceof HitboxOwner owner) {
                        owner.blockhider$setOwner(player);
                    }

                    player.level().addFreshEntity(hitbox);
                    this.blockhider$hitboxEntity = hitbox;
                }
            }
        }

        player.refreshDimensions();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void blockhider$tickHitbox(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide() && this.blockhider$hitboxEntity != null) {
            if (this.blockhider$transformedBlock == null || player.isRemoved() || !player.isAlive() || this.blockhider$transformedPos == null) {
                this.blockhider$hitboxEntity.discard();
                this.blockhider$hitboxEntity = null;
            }
            else {
                BlockPos pos = this.blockhider$transformedPos;
                this.blockhider$hitboxEntity.setPos(pos.getX() + 0.5d, pos.getY() + 1.d, pos.getZ() + 0.5d);
            }
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void blockhider$redirectAttack(Entity target, CallbackInfo ci) {
        if (target instanceof HitboxOwner hitbox && hitbox.blockhider$getOwner() != null) {
            Player owner = hitbox.blockhider$getOwner();
            if (owner != (Object) this && owner.isAlive()) {
                ((Player) (Object) this).attack(owner);
                ci.cancel();
            }
        }
    }
}
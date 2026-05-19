package kr.pyke.blockhider.mixin.server;

import kr.pyke.blockhider.transform.HitboxOwner;
import kr.pyke.blockhider.transform.PlayerTransform;
import kr.pyke.blockhider.transform.TransformableBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
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

    @Unique private static final float HITBOX_PADDING = 0.025f;

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
                    hitbox.setWidth(dimensions.width() + (HITBOX_PADDING * 2));
                    hitbox.setHeight(dimensions.height() + (HITBOX_PADDING * 2));
                    hitbox.setPos(blockPos.getX() + 0.5d, blockPos.getY() + 1.d - HITBOX_PADDING, blockPos.getZ() + 0.5d);

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
        if (player.level().isClientSide() || this.blockhider$hitboxEntity == null) { return; }

        if (this.blockhider$transformedBlock == null || this.blockhider$transformedPos == null || player.isRemoved() || !player.isAlive()) {
            this.blockhider$hitboxEntity.discard();
            this.blockhider$hitboxEntity = null;
        }
    }
}
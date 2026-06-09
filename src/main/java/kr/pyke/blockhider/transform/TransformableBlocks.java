package kr.pyke.blockhider.transform;

import kr.pyke.blockhider.registry.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TransformableBlocks {
    private static final EntityDimensions DEFAULT_BLOCK_DIMENSIONS = EntityDimensions.fixed(1.f, 1.f);
    private static final float MIN_HITBOX_WIDTH = 0.25f;
    private static final float MIN_HITBOX_HEIGHT = 0.25f;

    private TransformableBlocks() { }

    public static boolean isTransformable(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.isAir()) { return false; }
        if (state.is(ModBlocks.GHOST_BLOCK)) { return false; }
        if (state.isCollisionShapeFullBlock(level, pos)) { return true; }
        if (state.hasBlockEntity()) { return true; }
        if (state.getBlock() instanceof IronBarsBlock) { return true; }
        if (state.is(BlockTags.FENCES)) { return true; }
        if (state.is(BlockTags.FENCE_GATES)) { return true; }
        if (state.is(BlockTags.WALLS)) { return true; }
        if (state.is(BlockTags.LEAVES)) { return true; }
        if (state.is(BlockTags.STAIRS)) { return true; }
        if (state.is(BlockTags.SLABS)) { return true; }

        return false;
    }

    public static EntityDimensions getDimensions(BlockGetter level, BlockPos pos, BlockState state) {
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) { return DEFAULT_BLOCK_DIMENSIONS; }

        AABB bounds = shape.bounds();
        float width = Math.max(MIN_HITBOX_WIDTH, (float)Math.max(bounds.getXsize(), bounds.getZsize()));
        float height = Math.max(MIN_HITBOX_HEIGHT, (float)bounds.getYsize());

        return EntityDimensions.fixed(width, height);
    }
}
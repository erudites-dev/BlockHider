package kr.pyke.blockhider.transform;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TransformableBlocks {
    private static final EntityDimensions DEFAULT_BLOCK_DIMENSIONS = EntityDimensions.fixed(1.f, 1.f);

    private TransformableBlocks() { }

    public static boolean isTransformable(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.isAir()) { return false; }
        if (state.isCollisionShapeFullBlock(level, pos)) { return true; }
        if (state.hasBlockEntity()) { return true; }
        if (state.is(BlockTags.FENCES)) { return true; }
        if (state.is(BlockTags.LEAVES)) { return true; }
        if (state.is(BlockTags.STAIRS)) { return true; }
        if (state.is(BlockTags.SLABS)) { return true; }

        return false;
    }

    public static EntityDimensions getDimensions(BlockGetter level, BlockPos pos, BlockState state) {
        VoxelShape shape = state.getCollisionShape(level, pos, CollisionContext.empty());
        if (shape.isEmpty()) { return DEFAULT_BLOCK_DIMENSIONS; }

        AABB bounds = shape.bounds();
        float width = (float)Math.max(bounds.getXsize(), bounds.getZsize());
        float height = (float)bounds.getYsize();

        return EntityDimensions.fixed(width, height);
    }
}

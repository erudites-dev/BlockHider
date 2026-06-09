package kr.pyke.blockhider.registry.block.ghost;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class GhostBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<GhostBlock> CODEC = simpleCodec(GhostBlock::new);

    private static final VoxelShape SHAPE_NS = Block.box(4, 0, 0, 12, 16, 16);
    private static final VoxelShape SHAPE_EW = Block.box(0, 0, 4, 16, 16, 12);

    public GhostBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<GhostBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        Direction facing = state.getValue(FACING);

        return facing.getAxis() == Direction.Axis.X ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        boolean isPlayer = context instanceof EntityCollisionContext && ((EntityCollisionContext)context).getEntity() instanceof Player;

        return isPlayer ? Shapes.empty() : this.getShape(state, world, pos, context);
    }
}
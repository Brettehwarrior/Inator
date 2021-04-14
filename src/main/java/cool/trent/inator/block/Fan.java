package cool.trent.inator.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class Fan extends Block  {
    public static final DirectionProperty FACING;
    static { // Probably unnecessary but I don't know any better lol
        FACING = FacingBlock.FACING;
    }

    public Fan() {
        super(FabricBlockSettings
                .of(Material.PISTON)
                .strength(1.2f, 19.0f)
                .breakByHand(true)
                .sounds(BlockSoundGroup.METAL)
        );
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Fan places in direction like dispenser
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}

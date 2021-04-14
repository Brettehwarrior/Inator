package cool.trent.inator.common;

import cool.trent.inator.block.entity.ChuteEntity;
import cool.trent.inator.InatorMod;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class BlockEntities {
    public static void init() {System.out.println("Initializing Inator Block Entities");}

    public static BlockEntityType<ChuteEntity> CHUTE_ENTITY;

    static {
        CHUTE_ENTITY = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                InatorMod.MOD_ID+"chute",
                BlockEntityType.Builder.create(ChuteEntity::new, Blocks.CHUTE).build(null));
    }
}

package cool.trent.inator.common;

import cool.trent.inator.InatorMod;
import cool.trent.inator.block.Chute;
import cool.trent.inator.block.Fan;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Blocks {
    public static void init() {System.out.println("Initializing Inator Blocks");}

    // Block declaration
    public static final Block CHUTE;
    public static final Block FAN;

    // Register methods
    public static Block register(String name, Block block) {
        register(name, new BlockItem(block, new Item.Settings().group(InatorMod.ITEM_GROUP)));
        return Registry.register(Registry.BLOCK, new Identifier(InatorMod.MOD_ID, name), block);
    }
    public static BlockItem register(String name, BlockItem blockItem) {
        return Registry.register(Registry.ITEM, new Identifier(InatorMod.MOD_ID, name), blockItem);
    }

    // Registration
    static {
        CHUTE = register("chute", new Chute());
        FAN = register("fan", new Fan());
    }
}

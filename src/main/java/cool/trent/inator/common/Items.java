package cool.trent.inator.common;

import cool.trent.inator.InatorMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Items {
    public static void init() {System.out.println("Initializing Inator Items");}

    public static final Item IRON_SHEET;

    public static Item register(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(InatorMod.MOD_ID, name), item);
    }

    static {
        IRON_SHEET = register("iron_sheet", new Item(new FabricItemSettings().group(InatorMod.ITEM_GROUP)));
    }
}

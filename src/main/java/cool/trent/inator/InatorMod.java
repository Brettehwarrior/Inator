package cool.trent.inator;

import cool.trent.inator.common.BlockEntities;
import cool.trent.inator.common.Blocks;
import cool.trent.inator.common.Items;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class InatorMod implements ModInitializer {
	public static final String MOD_ID = "inator";
	public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
			new Identifier(MOD_ID, "general"),
			() -> new ItemStack(Items.IRON_SHEET)
	);

	@Override
	public void onInitialize() {
		Items.init();
		Blocks.init();
		BlockEntities.init();
	}
}

package net.gospi.mountsofmayhem.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import net.minecraft.world.item.Item;

import net.gospi.mountsofmayhem.item.WoodenSpearItem;
import net.gospi.mountsofmayhem.item.StoneSpearItem;
import net.gospi.mountsofmayhem.item.NetheriteSpearItem;
import net.gospi.mountsofmayhem.item.NetheriteNautilusShellItem;
import net.gospi.mountsofmayhem.item.NetheriteHorseArmorItem;
import net.gospi.mountsofmayhem.item.IronSpearItem;
import net.gospi.mountsofmayhem.item.IronNautilusShellItem;
import net.gospi.mountsofmayhem.item.GoldenSpearItem;
import net.gospi.mountsofmayhem.item.GoldenNautilusShellItem;
import net.gospi.mountsofmayhem.item.DiamondSpearItem;
import net.gospi.mountsofmayhem.item.DiamondNautilusShellItem;
import net.gospi.mountsofmayhem.item.CopperSpearItem;
import net.gospi.mountsofmayhem.item.CopperNautilusShellItem;
import net.gospi.mountsofmayhem.MountsOfMayhemMod;

public class MountsOfMayhemModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MountsOfMayhemMod.MODID);
	public static final DeferredItem<Item> NAUTILUS_SPAWN_EGG = REGISTRY.register("nautilus_spawn_egg", () -> new DeferredSpawnEggItem(MountsOfMayhemModEntities.NAUTILUS, -65806, -1089459, new Item.Properties()));
	public static final DeferredItem<Item> COPPER_NAUTILUS_SHELL = REGISTRY.register("copper_nautilus_shell", CopperNautilusShellItem::new);
	public static final DeferredItem<Item> GOLDEN_NAUTILUS_SHELL = REGISTRY.register("golden_nautilus_shell", GoldenNautilusShellItem::new);
	public static final DeferredItem<Item> IRON_NAUTILUS_SHELL = REGISTRY.register("iron_nautilus_shell", IronNautilusShellItem::new);
	public static final DeferredItem<Item> DIAMOND_NAUTILUS_SHELL = REGISTRY.register("diamond_nautilus_shell", DiamondNautilusShellItem::new);
	public static final DeferredItem<Item> NETHERITE_NAUTILUS_SHELL = REGISTRY.register("netherite_nautilus_shell", NetheriteNautilusShellItem::new);
	public static final DeferredItem<Item> ZOMBIE_NAUTILUS_SPAWN_EGG = REGISTRY.register("zombie_nautilus_spawn_egg", () -> new DeferredSpawnEggItem(MountsOfMayhemModEntities.ZOMBIE_NAUTILUS, -3031631, -15847123, new Item.Properties()));
	public static final DeferredItem<Item> PARCHED_SPAWN_EGG = REGISTRY.register("parched_spawn_egg", () -> new DeferredSpawnEggItem(MountsOfMayhemModEntities.PARCHED, -1, -1, new Item.Properties()));
	public static final DeferredItem<Item> WOODEN_SPEAR = REGISTRY.register("wooden_spear", WoodenSpearItem::new);
	public static final DeferredItem<Item> STONE_SPEAR = REGISTRY.register("stone_spear", StoneSpearItem::new);
	public static final DeferredItem<Item> COPPER_SPEAR = REGISTRY.register("copper_spear", CopperSpearItem::new);
	public static final DeferredItem<Item> GOLDEN_SPEAR = REGISTRY.register("golden_spear", GoldenSpearItem::new);
	public static final DeferredItem<Item> IRON_SPEAR = REGISTRY.register("iron_spear", IronSpearItem::new);
	public static final DeferredItem<Item> DIAMOND_SPEAR = REGISTRY.register("diamond_spear", DiamondSpearItem::new);
	public static final DeferredItem<Item> NETHERITE_SPEAR = REGISTRY.register("netherite_spear", NetheriteSpearItem::new);
	public static final DeferredItem<Item> CAMEL_HUSK_SPAWN_EGG = REGISTRY.register("camel_husk_spawn_egg", () -> new DeferredSpawnEggItem(MountsOfMayhemModEntities.CAMEL_HUSK, -13284010, -15398399, new Item.Properties()));
	public static final DeferredItem<Item> NETHERITE_HORSE_ARMOR = REGISTRY.register("netherite_horse_armor", () -> new NetheriteHorseArmorItem());
}

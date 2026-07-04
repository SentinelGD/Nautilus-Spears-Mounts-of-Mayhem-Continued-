package net.gospi.mountsofmayhem.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;

import net.gospi.mountsofmayhem.MountsOfMayhemMod;

@EventBusSubscriber(modid = MountsOfMayhemMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class MountsOfMayhemModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MountsOfMayhemMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(MountsOfMayhemModItems.NAUTILUS_SPAWN_EGG.get());
			tabData.accept(MountsOfMayhemModItems.ZOMBIE_NAUTILUS_SPAWN_EGG.get());
			tabData.accept(MountsOfMayhemModItems.PARCHED_SPAWN_EGG.get());
			tabData.accept(MountsOfMayhemModItems.CAMEL_HUSK_SPAWN_EGG.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			tabData.insertAfter(Items.NETHERITE_SWORD.getDefaultInstance(), MountsOfMayhemModItems.WOODEN_SPEAR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.WOODEN_SPEAR.get().getDefaultInstance(), MountsOfMayhemModItems.STONE_SPEAR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.STONE_SPEAR.get().getDefaultInstance(), MountsOfMayhemModItems.COPPER_SPEAR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.COPPER_SPEAR.get().getDefaultInstance(), MountsOfMayhemModItems.GOLDEN_SPEAR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.GOLDEN_SPEAR.get().getDefaultInstance(), MountsOfMayhemModItems.IRON_SPEAR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.IRON_SPEAR.get().getDefaultInstance(), MountsOfMayhemModItems.DIAMOND_SPEAR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.DIAMOND_SPEAR.get().getDefaultInstance(), MountsOfMayhemModItems.NETHERITE_SPEAR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(Items.DIAMOND_HORSE_ARMOR.getDefaultInstance(), MountsOfMayhemModItems.NETHERITE_HORSE_ARMOR.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.NETHERITE_HORSE_ARMOR.get().getDefaultInstance(), MountsOfMayhemModItems.COPPER_NAUTILUS_SHELL.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.COPPER_NAUTILUS_SHELL.get().getDefaultInstance(), MountsOfMayhemModItems.IRON_NAUTILUS_SHELL.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.IRON_NAUTILUS_SHELL.get().getDefaultInstance(), MountsOfMayhemModItems.GOLDEN_NAUTILUS_SHELL.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.GOLDEN_NAUTILUS_SHELL.get().getDefaultInstance(), MountsOfMayhemModItems.DIAMOND_NAUTILUS_SHELL.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			tabData.insertAfter(MountsOfMayhemModItems.DIAMOND_NAUTILUS_SHELL.get().getDefaultInstance(), MountsOfMayhemModItems.NETHERITE_NAUTILUS_SHELL.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
		}
	}
}

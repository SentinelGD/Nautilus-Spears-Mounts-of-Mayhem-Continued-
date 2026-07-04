/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.gospi.mountsofmayhem.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

import net.gospi.mountsofmayhem.MountsOfMayhemMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MountsOfMayhemModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MountsOfMayhemMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(MountsOfMayhemModItems.NAUTILUS_SPAWN_EGG.get());
			tabData.accept(MountsOfMayhemModItems.ZOMBIE_NAUTILUS_SPAWN_EGG.get());
			tabData.accept(MountsOfMayhemModItems.PARCHED_SPAWN_EGG.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			tabData.accept(MountsOfMayhemModItems.COPPER_NAUTILUS_SHELL.get());
			tabData.accept(MountsOfMayhemModItems.IRON_NAUTILUS_SHELL.get());
			tabData.accept(MountsOfMayhemModItems.GOLDEN_NAUTILUS_SHELL.get());
			tabData.accept(MountsOfMayhemModItems.DIAMOND_NAUTILUS_SHELL.get());
			tabData.accept(MountsOfMayhemModItems.NETHERITE_NAUTILUS_SHELL.get());
			tabData.accept(MountsOfMayhemModItems.WOODEN_SPEAR.get());
			tabData.accept(MountsOfMayhemModItems.STONE_SPEAR.get());
			tabData.accept(MountsOfMayhemModItems.COPPER_SPEAR.get());
			tabData.accept(MountsOfMayhemModItems.GOLDEN_SPEAR.get());
			tabData.accept(MountsOfMayhemModItems.IRON_SPEAR.get());
			tabData.accept(MountsOfMayhemModItems.DIAMOND_SPEAR.get());
			tabData.accept(MountsOfMayhemModItems.NETHERITE_SPEAR.get());
		}
	}
}
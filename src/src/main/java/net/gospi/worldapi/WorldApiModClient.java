package net.gospi.worldapi;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class WorldApiModClient {
    public static void init() {
        ModList.get().getModContainerById("worldapi").ifPresent(container -> {
            Supplier<IConfigScreenFactory> supplier = () -> (modContainer, parent) -> new WorldApiConfigScreen(parent);
            container.registerExtensionPoint(IConfigScreenFactory.class, supplier);
        });
        GhostBlockRenderer.register();
    }
}

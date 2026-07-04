package net.gospi.mountsofmayhem.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.Item;

public class NetheriteHorseArmorItem extends AnimalArmorItem {
    private static final ResourceLocation HORSE_ARMOR_TEXTURE =
        ResourceLocation.parse("mounts_of_mayhem:textures/entity/horse/armor/horse_armor_netherite.png");

    public NetheriteHorseArmorItem() {
        super(ArmorMaterials.NETHERITE, AnimalArmorItem.BodyType.EQUESTRIAN, false,
              new Item.Properties().stacksTo(1).fireResistant());
    }

    @Override
    public ResourceLocation getTexture() {
        return HORSE_ARMOR_TEXTURE;
    }
}

package de.ellpeck.slingshot;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.BasicTrade;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber
public final class Events {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            List<VillagerTrades.ITrade> expert = event.getTrades().get(5);
            expert.add(new BasicTrade(6, EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(Registry.capacityEnchantment, 4)), 3, 4));
            expert.add(new BasicTrade(10, EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(Registry.capacityEnchantment, 5)), 2, 5));
        }
    }
}

package de.ellpeck.slingshot;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Registry {

    private static final List<SlingshotBehavior> BEHAVIORS = new ArrayList<>();
    public static Item slingshot;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(slingshot = new ItemSlingshot());
    }

    public static void init() {
        addBehavior(new SlingshotBehavior("carrot", new ItemStack(Items.CARROT), 40, 3, 0.45F));
        addBehavior(new SlingshotBehavior("potato", new ItemStack(Items.POTATO), 40, 3, 0.45F));
    }

    public static void addBehavior(SlingshotBehavior behavior) {
        BEHAVIORS.add(behavior);
        slingshot.addPropertyOverride(new ResourceLocation(Slingshot.ID, behavior.name), (stack, world, entity) -> entity != null && ItemSlingshot.getChargedItem(stack).isItemEqual(behavior.item) ? 1 : 0);
    }

    public static SlingshotBehavior getBehavior(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        for (SlingshotBehavior behavior : BEHAVIORS) {
            if (behavior.item.isItemEqual(stack))
                return behavior;
        }
        return null;
    }
}

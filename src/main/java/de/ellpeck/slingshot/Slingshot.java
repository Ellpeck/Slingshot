package de.ellpeck.slingshot;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Slingshot.ID)
public final class Slingshot {

    public static final String ID = "slingshot";

    public Slingshot() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(Registry::setup);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> bus.addListener(Registry.Client::setup));
    }
}

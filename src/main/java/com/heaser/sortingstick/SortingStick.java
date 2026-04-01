package com.heaser.sortingstick;

import com.heaser.sortingstick.client.ClientEvents;
import com.heaser.sortingstick.command.SortingTestCommand;
import com.heaser.sortingstick.config.SortingStickConfig;
import com.heaser.sortingstick.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(SortingStick.MODID)
public class SortingStick {

    public static final String MODID = "sortingstick";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SortingStick(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModParticleTypes.PARTICLE_TYPES.register(modEventBus);

        modEventBus.addListener(NetworkHandler::register);
        modEventBus.addListener(this::clientSetup);

        NeoForge.EVENT_BUS.addListener(SortingTestCommand::onRegisterCommands);

        modContainer.registerConfig(ModConfig.Type.COMMON, SortingStickConfig.SPEC, "sortingstick-common.toml");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(ClientEvents.class);
    }
}

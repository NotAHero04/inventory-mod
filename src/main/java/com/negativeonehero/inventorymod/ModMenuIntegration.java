package com.negativeonehero.inventorymod;

import com.negativeonehero.inventorymod.utils.InventoryModConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.of("Inventorium Config"))
                    .setSavingRunnable(() -> InventoryModConfig.writeConfig(false));
            ConfigCategory category = builder.getOrCreateCategory(Text.of("Options"));
            category.addEntry(builder.entryBuilder()
                    .startBooleanToggle(Text.of("Enable BigInteger support"), InventoryModConfig.bigInteger)
                    .setTooltip(Text.of("""
                            Enable even larger stack size, up to 2^(2^31-1)
                            which is 646.5 million digits long!
                            Performance may be degraded.
                            (NOT YET IMPLEMENTED)"""))
                    .setDefaultValue(false)
                    .setSaveConsumer(value -> InventoryModConfig.bigInteger = value)
                    .requireRestart()
                    .build());
            return builder.build();
        };
    }
}

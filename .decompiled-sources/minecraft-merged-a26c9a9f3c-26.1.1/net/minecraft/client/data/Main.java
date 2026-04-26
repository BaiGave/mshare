/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.client.ClientBootstrap;
import net.minecraft.client.data.AtlasProvider;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.WaypointStyleProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class Main {
    @SuppressForbidden(reason="System.out needed before bootstrap")
    public static void main(String[] args) throws IOException {
        SharedConstants.tryDetectVersion();
        OptionParser parser = new OptionParser();
        AbstractOptionSpec helpOption = parser.accepts("help", "Show the help menu").forHelp();
        OptionSpecBuilder clientOption = parser.accepts("client", "Include client generators");
        OptionSpecBuilder allOption = parser.accepts("all", "Include all generators");
        ArgumentAcceptingOptionSpec<String> outputOption = parser.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated", (String[])new String[0]);
        OptionSet optionSet = parser.parse(args);
        if (optionSet.has(helpOption) || !optionSet.hasOptions()) {
            parser.printHelpOn(System.out);
            return;
        }
        Path output = Paths.get((String)outputOption.value(optionSet), new String[0]);
        boolean allOptions = optionSet.has(allOption);
        boolean client = allOptions || optionSet.has(clientOption);
        Bootstrap.bootStrap();
        ClientBootstrap.bootstrap();
        DataGenerator.Cached generator = new DataGenerator.Cached(output, SharedConstants.getCurrentVersion(), true);
        Main.addClientProviders(generator, client);
        ((DataGenerator)generator).run();
        Util.shutdownExecutors();
    }

    public static void addClientProviders(DataGenerator generator, boolean client) {
        DataGenerator.PackGenerator clientVanillaPack = generator.getVanillaPack(client);
        clientVanillaPack.addProvider(ModelProvider::new);
        clientVanillaPack.addProvider(EquipmentAssetProvider::new);
        clientVanillaPack.addProvider(WaypointStyleProvider::new);
        clientVanillaPack.addProvider(AtlasProvider::new);
    }
}


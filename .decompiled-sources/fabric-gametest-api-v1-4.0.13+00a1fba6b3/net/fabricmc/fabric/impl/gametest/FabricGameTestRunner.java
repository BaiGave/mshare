/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gametest;

import java.io.File;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import net.fabricmc.fabric.impl.gametest.SavingXmlTestReporter;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FabricGameTestRunner {
    public static final boolean ENABLED = System.getProperty("fabric-api.gametest") != null;
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricGameTestRunner.class);
    private static final String GAMETEST_STRUCTURE_PATH = "gametest/structure";
    public static final FileToIdConverter GAMETEST_STRUCTURE_FINDER = new FileToIdConverter("gametest/structure", ".snbt");

    private FabricGameTestRunner() {
    }

    public static void runHeadlessServer(LevelStorageSource.LevelStorageAccess storageAccess, PackRepository packRepository) {
        String reportPath = System.getProperty("fabric-api.gametest.report-file");
        if (reportPath != null) {
            try {
                GlobalTestReporter.replaceWith(new SavingXmlTestReporter(new File(reportPath)));
            }
            catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Starting test server");
        Optional<String> filter = Optional.ofNullable(System.getProperty("fabric-api.gametest.filter"));
        boolean verify = Boolean.getBoolean("fabric-api.gametest.verify");
        int repeat = 0;
        MinecraftServer.spin(thread -> GameTestServer.create(thread, storageAccess, packRepository, filter, verify, repeat));
    }
}


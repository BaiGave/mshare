/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.client.indigo.IndigoMixinConfigPlugin;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Indigo
implements ClientModInitializer {
    public static final AoConfig AMBIENT_OCCLUSION_MODE;
    public static final boolean DEBUG_COMPARE_LIGHTING;
    public static final boolean FIX_SMOOTH_LIGHTING_OFFSET;
    public static final boolean FIX_MEAN_LIGHT_CALCULATION;
    public static final boolean FIX_EXTERIOR_VERTEX_LIGHTING;
    public static final boolean FIX_LUMINOUS_AO_SHADE;
    private static final Logger LOGGER;
    private static final boolean GENERATE_CONFIG_FILE;
    public static final ScopedValue<MultiBufferSource> LEVEL_RENDERER_BUFFER_SOURCE;

    private static boolean asBoolean(@Nullable String property, boolean defValue) {
        return Indigo.asTriState(property).orElse(defValue);
    }

    private static <T extends Enum> T asEnum(@Nullable String property, T defValue) {
        if (property != null && !property.isEmpty()) {
            for (Enum obj : (Enum[])defValue.getClass().getEnumConstants()) {
                if (!property.equalsIgnoreCase(obj.name())) continue;
                return (T)obj;
            }
        }
        return defValue;
    }

    private static TriState asTriState(@Nullable String property) {
        if (property == null || property.isEmpty()) {
            return TriState.DEFAULT;
        }
        return switch (property.toLowerCase(Locale.ROOT)) {
            case "true" -> TriState.TRUE;
            case "false" -> TriState.FALSE;
            default -> TriState.DEFAULT;
        };
    }

    @Override
    public void onInitializeClient() {
        if (IndigoMixinConfigPlugin.shouldApplyIndigo()) {
            LOGGER.info("[Indigo] Registering Indigo renderer!");
            Renderer.register(IndigoRenderer.INSTANCE);
        } else {
            LOGGER.info("[Indigo] Different rendering plugin detected; not applying Indigo.");
        }
    }

    static {
        LOGGER = LoggerFactory.getLogger(Indigo.class);
        GENERATE_CONFIG_FILE = System.getProperty("fabric.indigo.generateConfigFile") != null;
        LEVEL_RENDERER_BUFFER_SOURCE = ScopedValue.newInstance();
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("fabric");
        Path configFile = configDir.resolve("indigo-renderer.properties");
        boolean configExists = Files.exists(configFile, new LinkOption[0]);
        Properties properties = new Properties();
        if (configExists) {
            try (InputStream stream = Files.newInputStream(configFile, new OpenOption[0]);){
                properties.load(stream);
            }
            catch (IOException e) {
                LOGGER.warn("[Indigo] Could not read property file '{}'", (Object)configFile.toAbsolutePath(), (Object)e);
            }
        }
        AMBIENT_OCCLUSION_MODE = Indigo.asEnum((String)properties.computeIfAbsent("ambient-occlusion-mode", (Function<? super Object, ?>)((Function<Object, Object>)object -> "hybrid")), AoConfig.HYBRID);
        DEBUG_COMPARE_LIGHTING = Indigo.asBoolean((String)properties.computeIfAbsent("debug-compare-lighting", (Function<? super Object, ?>)((Function<Object, Object>)object -> "auto")), false);
        FIX_SMOOTH_LIGHTING_OFFSET = Indigo.asBoolean((String)properties.computeIfAbsent("fix-smooth-lighting-offset", (Function<? super Object, ?>)((Function<Object, Object>)object -> "auto")), true);
        boolean fixMeanLightCalculation = Indigo.asBoolean((String)properties.computeIfAbsent("fix-mean-light-calculation", (Function<? super Object, ?>)((Function<Object, Object>)object -> "auto")), true);
        FIX_EXTERIOR_VERTEX_LIGHTING = Indigo.asBoolean((String)properties.computeIfAbsent("fix-exterior-vertex-lighting", (Function<? super Object, ?>)((Function<Object, Object>)object -> "auto")), true);
        FIX_LUMINOUS_AO_SHADE = Indigo.asBoolean((String)properties.computeIfAbsent("fix-luminous-block-ambient-occlusion", (Function<? super Object, ?>)((Function<Object, Object>)object -> "auto")), false);
        if (fixMeanLightCalculation && !FIX_SMOOTH_LIGHTING_OFFSET) {
            fixMeanLightCalculation = false;
            LOGGER.warn("[Indigo] Config enabled 'fix-mean-light-calculation' but disabled 'fix-smooth-lighting-offset'; this is not supported! 'fix-mean-light-calculation' will be considered disabled.");
        }
        FIX_MEAN_LIGHT_CALCULATION = fixMeanLightCalculation;
        if (configExists || GENERATE_CONFIG_FILE) {
            if (!Files.exists(configDir, new LinkOption[0])) {
                try {
                    Files.createDirectories(configDir, new FileAttribute[0]);
                }
                catch (IOException e) {
                    LOGGER.warn("[Indigo] Could not create configuration directory: {}", (Object)configDir.toAbsolutePath(), (Object)e);
                }
            }
            try (OutputStream stream = Files.newOutputStream(configFile, new OpenOption[0]);){
                properties.store(stream, "Fabric API Indigo properties file");
            }
            catch (IOException e) {
                LOGGER.warn("[Indigo] Could not store property file '{}'", (Object)configFile.toAbsolutePath(), (Object)e);
            }
        }
    }
}


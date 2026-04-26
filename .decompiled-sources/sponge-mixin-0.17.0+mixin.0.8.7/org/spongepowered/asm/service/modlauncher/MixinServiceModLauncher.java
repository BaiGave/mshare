/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.asm.service.modlauncher;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.TypesafeMap;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Optional;
import org.spongepowered.asm.launch.IClassProcessor;
import org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncher;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformerFactory;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.IClassProvider;
import org.spongepowered.asm.service.IClassTracker;
import org.spongepowered.asm.service.IMixinAuditTrail;
import org.spongepowered.asm.service.IMixinInternal;
import org.spongepowered.asm.service.ITransformerProvider;
import org.spongepowered.asm.service.MixinServiceAbstract;
import org.spongepowered.asm.service.modlauncher.LoggerAdapterLog4j2;
import org.spongepowered.asm.service.modlauncher.MixinTransformationHandler;
import org.spongepowered.asm.service.modlauncher.ModLauncherAuditTrail;
import org.spongepowered.asm.service.modlauncher.ModLauncherClassProvider;
import org.spongepowered.asm.service.modlauncher.ModLauncherClassTracker;
import org.spongepowered.asm.util.IConsumer;
import org.spongepowered.asm.util.VersionNumber;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

public class MixinServiceModLauncher
extends MixinServiceAbstract {
    private static final VersionNumber MODLAUNCHER_4_SPECIFICATION_VERSION = VersionNumber.parse("4.0");
    private static final VersionNumber MODLAUNCHER_9_SPECIFICATION_VERSION = VersionNumber.parse("8.0");
    private IClassProvider classProvider;
    private IClassBytecodeProvider bytecodeProvider;
    private MixinTransformationHandler transformationHandler;
    private ModLauncherClassTracker classTracker;
    private ModLauncherAuditTrail auditTrail;
    private IConsumer<MixinEnvironment.Phase> phaseConsumer;
    private volatile boolean initialised;
    private ContainerHandleModLauncher rootContainer;
    private MixinEnvironment.CompatibilityLevel minCompatibilityLevel = MixinEnvironment.CompatibilityLevel.JAVA_8;

    public MixinServiceModLauncher() {
        VersionNumber apiVersion = MixinServiceModLauncher.getModLauncherApiVersion();
        if (apiVersion.compareTo(MODLAUNCHER_9_SPECIFICATION_VERSION) >= 0) {
            this.createRootContainer("org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncherEx");
            this.minCompatibilityLevel = MixinEnvironment.CompatibilityLevel.JAVA_16;
        } else {
            this.createRootContainer("org.spongepowered.asm.launch.platform.container.ContainerHandleModLauncher");
        }
    }

    public void onInit(IClassBytecodeProvider bytecodeProvider) {
        if (this.initialised) {
            throw new IllegalStateException("Already initialised");
        }
        this.initialised = true;
        this.bytecodeProvider = bytecodeProvider;
    }

    private void createRootContainer(String rootContainerClassName) {
        try {
            Class<?> clRootContainer = this.getClassProvider().findClass(rootContainerClassName);
            Constructor<?> ctor = clRootContainer.getDeclaredConstructor(String.class);
            this.rootContainer = (ContainerHandleModLauncher)ctor.newInstance(this.getName());
        }
        catch (ReflectiveOperationException ex) {
            ex.printStackTrace();
        }
    }

    public void onStartup() {
        this.phaseConsumer.accept(MixinEnvironment.Phase.DEFAULT);
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformerFactory) {
            this.getTransformationHandler().offer((IMixinTransformerFactory)internal);
        }
        super.offer(internal);
    }

    @Override
    public void wire(MixinEnvironment.Phase phase, IConsumer<MixinEnvironment.Phase> phaseConsumer) {
        super.wire(phase, phaseConsumer);
        this.phaseConsumer = phaseConsumer;
    }

    @Override
    public String getName() {
        return "ModLauncher";
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return this.minCompatibilityLevel;
    }

    @Override
    protected ILogger createLogger(String name) {
        return new LoggerAdapterLog4j2(name);
    }

    @Override
    public boolean isValid() {
        try {
            VersionNumber apiVersion = MixinServiceModLauncher.getModLauncherApiVersion();
            if (apiVersion.compareTo(MODLAUNCHER_4_SPECIFICATION_VERSION) < 0) {
                return false;
            }
        }
        catch (Throwable th) {
            return false;
        }
        return true;
    }

    @Override
    public IClassProvider getClassProvider() {
        if (this.classProvider == null) {
            this.classProvider = new ModLauncherClassProvider();
        }
        return this.classProvider;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        if (this.bytecodeProvider == null) {
            throw new IllegalStateException("Service initialisation incomplete");
        }
        return this.bytecodeProvider;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return null;
    }

    @Override
    public IClassTracker getClassTracker() {
        if (this.classTracker == null) {
            this.classTracker = new ModLauncherClassTracker();
        }
        return this.classTracker;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        if (this.auditTrail == null) {
            this.auditTrail = new ModLauncherAuditTrail();
        }
        return this.auditTrail;
    }

    private MixinTransformationHandler getTransformationHandler() {
        if (this.transformationHandler == null) {
            this.transformationHandler = new MixinTransformationHandler();
        }
        return this.transformationHandler;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return ImmutableList.of("org.spongepowered.asm.launch.platform.MixinPlatformAgentMinecraftForge");
    }

    @Override
    public ContainerHandleModLauncher getPrimaryContainer() {
        return this.rootContainer;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    public Collection<IClassProcessor> getProcessors() {
        return ImmutableList.of(this.getTransformationHandler(), (IClassProcessor)((Object)this.getClassTracker()));
    }

    private static VersionNumber getModLauncherApiVersion() {
        TypesafeMap.Key versionProperty = (TypesafeMap.Key)IEnvironment.Keys.MLSPEC_VERSION.get();
        Optional<String> version = Launcher.INSTANCE.environment().getProperty(versionProperty);
        if (!version.isPresent()) {
            version = Optional.ofNullable(ITransformationService.class.getPackage().getSpecificationVersion());
        }
        return version.map(VersionNumber::parse).orElse(VersionNumber.NONE);
    }
}


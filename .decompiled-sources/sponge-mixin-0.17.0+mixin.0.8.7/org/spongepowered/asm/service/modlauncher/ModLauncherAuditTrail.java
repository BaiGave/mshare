/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.asm.service.modlauncher;

import java.util.function.Consumer;
import org.spongepowered.asm.service.IMixinAuditTrail;

public class ModLauncherAuditTrail
implements IMixinAuditTrail {
    private String currentClass;
    private Consumer<String[]> consumer;

    public void setConsumer(String className, Consumer<String[]> consumer) {
        this.currentClass = className;
        this.consumer = consumer;
    }

    @Override
    public void onApply(String className, String mixinName) {
        this.writeActivity(className, "APP", mixinName);
    }

    @Override
    public void onPostProcess(String className) {
        this.writeActivity(className, "DEC");
    }

    @Override
    public void onGenerate(String className, String generatorName) {
        this.writeActivity(className, "GEN");
    }

    private void writeActivity(String className, String ... activity) {
        if (this.consumer != null && className.equals(this.currentClass)) {
            this.consumer.accept(activity);
        }
    }
}


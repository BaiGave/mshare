/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection;

import java.util.List;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.common.data.CommonData;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.soft.annotation.injection.CommonInjectionAnnotationVisitor;
import org.objectweb.asm.AnnotationVisitor;

public class WrapWithConditionAnnotationVisitor
extends CommonInjectionAnnotationVisitor {
    public WrapWithConditionAnnotationVisitor(CommonData data, AnnotationVisitor delegate, List<String> targets) {
        super(data, delegate, targets);
    }
}


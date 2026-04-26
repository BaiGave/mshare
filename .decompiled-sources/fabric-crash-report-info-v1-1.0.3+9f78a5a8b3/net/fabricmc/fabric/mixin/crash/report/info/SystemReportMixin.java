/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.crash.report.info;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SystemReport.class})
public abstract class SystemReportMixin {
    @Shadow
    public abstract void setDetail(String var1, Supplier<String> var2);

    @Inject(at={@At(value="RETURN")}, method={"<init>"})
    private void fillSystemDetails(CallbackInfo info) {
        this.setDetail("Fabric Mods", () -> {
            ArrayList<ModContainer> topLevelMods = new ArrayList<ModContainer>();
            for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
                if (!container.getContainingMod().isEmpty()) continue;
                topLevelMods.add(container);
            }
            StringBuilder modString = new StringBuilder();
            SystemReportMixin.appendMods(modString, 2, topLevelMods);
            return modString.toString();
        });
    }

    @Unique
    private static void appendMods(StringBuilder modString, int depth, ArrayList<ModContainer> mods) {
        mods.sort(Comparator.comparing(mod -> mod.getMetadata().getId()));
        for (ModContainer mod2 : mods) {
            modString.append('\n');
            modString.append("\t".repeat(depth));
            modString.append(mod2.getMetadata().getId());
            modString.append(": ");
            modString.append(mod2.getMetadata().getName());
            modString.append(' ');
            modString.append(mod2.getMetadata().getVersion().getFriendlyString());
            if (mod2.getContainedMods().isEmpty()) continue;
            ArrayList<ModContainer> childMods = new ArrayList<ModContainer>(mod2.getContainedMods());
            SystemReportMixin.appendMods(modString, depth + 1, childMods);
        }
    }
}


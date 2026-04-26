/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.service.MixinService;

public final class VanillaTooltipProviderOrder {
    private static final List<DataComponentType<?>> VANILLA_ORDER = VanillaTooltipProviderOrder.scrapeVanillaOrder();

    private VanillaTooltipProviderOrder() {
    }

    public static void load() {
    }

    private static List<DataComponentType<?>> scrapeVanillaOrder() {
        try {
            ClassNode itemStackNode = MixinService.getService().getBytecodeProvider().getClassNode(Type.getInternalName(ItemStack.class));
            String methodName = "addDetailsToTooltip";
            String methodDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Item.TooltipContext.class), Type.getType(TooltipDisplay.class), Type.getType(Player.class), Type.getType(TooltipFlag.class), Type.getType(Consumer.class));
            String appendAttributeModifiersTooltipName = "addAttributeTooltips";
            String appendAttributeModifiersTooltipDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Consumer.class), Type.getType(TooltipDisplay.class), Type.getType(Player.class));
            MethodNode appendTooltipMethod = itemStackNode.methods.stream().filter(method -> method.name.equals(methodName) && method.desc.equals(methodDesc)).findAny().orElseThrow(() -> new IllegalStateException("No appendTooltip method in ItemStack"));
            ArrayList<DataComponentType<ItemAttributeModifiers>> componentTypes = new ArrayList<DataComponentType<ItemAttributeModifiers>>();
            HashSet<String> alreadyAddedComponents = new HashSet<String>();
            String owner = Type.getInternalName(DataComponents.class);
            String desc = Type.getDescriptor(DataComponentType.class);
            for (AbstractInsnNode insn : appendTooltipMethod.instructions) {
                FieldInsnNode fieldInsn;
                if (insn instanceof FieldInsnNode && (fieldInsn = (FieldInsnNode)insn).getOpcode() == 178 && fieldInsn.owner.equals(owner) && fieldInsn.desc.equals(desc)) {
                    String fieldName = fieldInsn.name;
                    if (!alreadyAddedComponents.add(fieldName)) continue;
                    componentTypes.add((DataComponentType)DataComponents.class.getField(fieldName).get(null));
                    continue;
                }
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode methodInsn = (MethodInsnNode)insn;
                if (!methodInsn.name.equals(appendAttributeModifiersTooltipName) || !methodInsn.desc.equals(appendAttributeModifiersTooltipDesc) || !methodInsn.owner.equals(Type.getInternalName(ItemStack.class))) continue;
                componentTypes.add(DataComponents.ATTRIBUTE_MODIFIERS);
            }
            if (componentTypes.isEmpty()) {
                throw new IllegalStateException("Found no component types in appendTooltip method");
            }
            return Collections.unmodifiableList(componentTypes);
        }
        catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<DataComponentType<?>> getVanillaOrder() {
        return VANILLA_ORDER;
    }
}


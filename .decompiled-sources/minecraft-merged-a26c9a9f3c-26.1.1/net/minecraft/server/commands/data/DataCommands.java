/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.commands.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.PrimitiveTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.commands.data.StorageDataAccessor;
import net.minecraft.util.Mth;

public class DataCommands {
    private static final SimpleCommandExceptionType ERROR_MERGE_UNCHANGED = new SimpleCommandExceptionType(Component.translatable("commands.data.merge.failed"));
    private static final DynamicCommandExceptionType ERROR_GET_NOT_NUMBER = new DynamicCommandExceptionType(path -> Component.translatableEscape("commands.data.get.invalid", path));
    private static final DynamicCommandExceptionType ERROR_GET_NON_EXISTENT = new DynamicCommandExceptionType(path -> Component.translatableEscape("commands.data.get.unknown", path));
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(Component.translatable("commands.data.get.multiple"));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_OBJECT = new DynamicCommandExceptionType(node -> Component.translatableEscape("commands.data.modify.expected_object", node));
    private static final DynamicCommandExceptionType ERROR_EXPECTED_VALUE = new DynamicCommandExceptionType(node -> Component.translatableEscape("commands.data.modify.expected_value", node));
    private static final Dynamic2CommandExceptionType ERROR_INVALID_SUBSTRING = new Dynamic2CommandExceptionType((start, end) -> Component.translatableEscape("commands.data.modify.invalid_substring", start, end));
    public static final List<Function<String, DataProvider>> ALL_PROVIDERS = ImmutableList.of(EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER);
    public static final List<DataProvider> TARGET_PROVIDERS = ALL_PROVIDERS.stream().map(f -> (DataProvider)f.apply("target")).collect(ImmutableList.toImmutableList());
    public static final List<DataProvider> SOURCE_PROVIDERS = ALL_PROVIDERS.stream().map(f -> (DataProvider)f.apply("source")).collect(ImmutableList.toImmutableList());

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder root = (LiteralArgumentBuilder)Commands.literal("data").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (DataProvider targetProvider : TARGET_PROVIDERS) {
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)root.then(targetProvider.wrap(Commands.literal("merge"), p -> p.then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes(c -> DataCommands.mergeData((CommandSourceStack)c.getSource(), targetProvider.access(c), CompoundTagArgument.getCompoundTag(c, "nbt"))))))).then(targetProvider.wrap(Commands.literal("get"), p -> ((ArgumentBuilder)p.executes(c -> DataCommands.getData((CommandSourceStack)c.getSource(), targetProvider.access(c)))).then(((RequiredArgumentBuilder)Commands.argument("path", NbtPathArgument.nbtPath()).executes(c -> DataCommands.getData((CommandSourceStack)c.getSource(), targetProvider.access(c), NbtPathArgument.getPath(c, "path")))).then(Commands.argument("scale", DoubleArgumentType.doubleArg()).executes(c -> DataCommands.getNumeric((CommandSourceStack)c.getSource(), targetProvider.access(c), NbtPathArgument.getPath(c, "path"), DoubleArgumentType.getDouble(c, "scale")))))))).then(targetProvider.wrap(Commands.literal("remove"), p -> p.then(Commands.argument("path", NbtPathArgument.nbtPath()).executes(c -> DataCommands.removeData((CommandSourceStack)c.getSource(), targetProvider.access(c), NbtPathArgument.getPath(c, "path"))))))).then(DataCommands.decorateModification((parent, rest) -> ((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)((ArgumentBuilder)parent.then(Commands.literal("insert").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("index", IntegerArgumentType.integer()).then(rest.create((context, target, targetPath, source) -> targetPath.insert(IntegerArgumentType.getInteger(context, "index"), target, source)))))).then(Commands.literal("prepend").then(rest.create((context, target, targetPath, source) -> targetPath.insert(0, target, source))))).then(Commands.literal("append").then(rest.create((context, target, targetPath, source) -> targetPath.insert(-1, target, source))))).then(Commands.literal("set").then(rest.create((context, target, targetPath, source) -> targetPath.set(target, (Tag)Iterables.getLast(source)))))).then(Commands.literal("merge").then(rest.create((context, target, targetPath, source) -> {
                CompoundTag combinedSources = new CompoundTag();
                for (Tag sourceTag : source) {
                    if (NbtPathArgument.NbtPath.isTooDeep(sourceTag, 0)) {
                        throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
                    }
                    if (sourceTag instanceof CompoundTag) {
                        CompoundTag tag = (CompoundTag)sourceTag;
                        combinedSources.merge(tag);
                        continue;
                    }
                    throw ERROR_EXPECTED_OBJECT.create(sourceTag);
                }
                List<Tag> targets = targetPath.getOrCreate(target, CompoundTag::new);
                int changedCount = 0;
                for (Tag targetTag : targets) {
                    if (!(targetTag instanceof CompoundTag)) {
                        throw ERROR_EXPECTED_OBJECT.create(targetTag);
                    }
                    CompoundTag targetObject = (CompoundTag)targetTag;
                    CompoundTag originalTarget = targetObject.copy();
                    targetObject.merge(combinedSources);
                    changedCount += originalTarget.equals(targetObject) ? 0 : 1;
                }
                return changedCount;
            })))));
        }
        dispatcher.register(root);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String getAsText(Tag tag) throws CommandSyntaxException {
        Tag tag2 = tag;
        Objects.requireNonNull(tag2);
        Tag tag3 = tag2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{StringTag.class, PrimitiveTag.class}, (Tag)tag3, n)) {
            case 0: {
                String string;
                StringTag stringTag = (StringTag)tag3;
                try {
                    String string2;
                    String value;
                    string = value = (string2 = stringTag.value());
                    return string;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: {
                PrimitiveTag primitiveTag = (PrimitiveTag)tag3;
                String string = primitiveTag.toString();
                return string;
            }
        }
        throw ERROR_EXPECTED_VALUE.create(tag);
    }

    private static List<Tag> stringifyTagList(List<Tag> source, StringProcessor stringProcessor) throws CommandSyntaxException {
        ArrayList<Tag> result = new ArrayList<Tag>(source.size());
        for (Tag tag : source) {
            String text = DataCommands.getAsText(tag);
            result.add(StringTag.valueOf(stringProcessor.process(text)));
        }
        return result;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> decorateModification(BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, DataManipulatorDecorator> nodeSupplier) {
        LiteralArgumentBuilder<CommandSourceStack> modify = Commands.literal("modify");
        for (DataProvider targetProvider : TARGET_PROVIDERS) {
            targetProvider.wrap(modify, t -> {
                RequiredArgumentBuilder<CommandSourceStack, NbtPathArgument.NbtPath> targetPathNode = Commands.argument("targetPath", NbtPathArgument.nbtPath());
                for (DataProvider sourceProvider : SOURCE_PROVIDERS) {
                    nodeSupplier.accept(targetPathNode, manipulator -> sourceProvider.wrap(Commands.literal("from"), s -> ((ArgumentBuilder)s.executes(c -> DataCommands.manipulateData(c, targetProvider, manipulator, DataCommands.getSingletonSource(c, sourceProvider)))).then(Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes(c -> DataCommands.manipulateData(c, targetProvider, manipulator, DataCommands.resolveSourcePath(c, sourceProvider))))));
                    nodeSupplier.accept(targetPathNode, manipulator -> sourceProvider.wrap(Commands.literal("string"), s -> ((ArgumentBuilder)s.executes(c -> DataCommands.manipulateData(c, targetProvider, manipulator, DataCommands.stringifyTagList(DataCommands.getSingletonSource(c, sourceProvider), str -> str)))).then(((RequiredArgumentBuilder)Commands.argument("sourcePath", NbtPathArgument.nbtPath()).executes(c -> DataCommands.manipulateData(c, targetProvider, manipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath(c, sourceProvider), str -> str)))).then(((RequiredArgumentBuilder)Commands.argument("start", IntegerArgumentType.integer()).executes(c -> DataCommands.manipulateData(c, targetProvider, manipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath(c, sourceProvider), str -> DataCommands.substring(str, IntegerArgumentType.getInteger(c, "start")))))).then(Commands.argument("end", IntegerArgumentType.integer()).executes(c -> DataCommands.manipulateData(c, targetProvider, manipulator, DataCommands.stringifyTagList(DataCommands.resolveSourcePath(c, sourceProvider), str -> DataCommands.substring(str, IntegerArgumentType.getInteger(c, "start"), IntegerArgumentType.getInteger(c, "end"))))))))));
                }
                nodeSupplier.accept(targetPathNode, manipulator -> Commands.literal("value").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("value", NbtTagArgument.nbtTag()).executes(c -> {
                    List<Tag> source = Collections.singletonList(NbtTagArgument.getNbtTag(c, "value"));
                    return DataCommands.manipulateData(c, targetProvider, manipulator, source);
                })));
                return t.then(targetPathNode);
            });
        }
        return modify;
    }

    private static String validatedSubstring(String input, int start, int end) throws CommandSyntaxException {
        if (start < 0 || end > input.length() || start > end) {
            throw ERROR_INVALID_SUBSTRING.create(start, end);
        }
        return input.substring(start, end);
    }

    private static String substring(String input, int start, int end) throws CommandSyntaxException {
        int length = input.length();
        int absoluteStart = DataCommands.getOffset(start, length);
        int absoluteEnd = DataCommands.getOffset(end, length);
        return DataCommands.validatedSubstring(input, absoluteStart, absoluteEnd);
    }

    private static String substring(String input, int start) throws CommandSyntaxException {
        int length = input.length();
        return DataCommands.validatedSubstring(input, DataCommands.getOffset(start, length), length);
    }

    private static int getOffset(int index, int length) {
        return index >= 0 ? index : length + index;
    }

    private static List<Tag> getSingletonSource(CommandContext<CommandSourceStack> context, DataProvider sourceProvider) throws CommandSyntaxException {
        DataAccessor source = sourceProvider.access(context);
        return Collections.singletonList(source.getData());
    }

    private static List<Tag> resolveSourcePath(CommandContext<CommandSourceStack> context, DataProvider sourceProvider) throws CommandSyntaxException {
        DataAccessor source = sourceProvider.access(context);
        NbtPathArgument.NbtPath sourcePath = NbtPathArgument.getPath(context, "sourcePath");
        return sourcePath.get(source.getData());
    }

    private static int manipulateData(CommandContext<CommandSourceStack> context, DataProvider targetProvider, DataManipulator manipulator, List<Tag> source) throws CommandSyntaxException {
        DataAccessor target = targetProvider.access(context);
        NbtPathArgument.NbtPath targetPath = NbtPathArgument.getPath(context, "targetPath");
        CompoundTag targetData = target.getData();
        int result = manipulator.modify(context, targetData, targetPath, source);
        if (result == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        target.setData(targetData);
        context.getSource().sendSuccess(() -> target.getModifiedSuccess(), true);
        return result;
    }

    private static int removeData(CommandSourceStack source, DataAccessor accessor, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        CompoundTag result = accessor.getData();
        int count = path.remove(result);
        if (count == 0) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        accessor.setData(result);
        source.sendSuccess(() -> accessor.getModifiedSuccess(), true);
        return count;
    }

    public static Tag getSingleTag(NbtPathArgument.NbtPath path, DataAccessor accessor) throws CommandSyntaxException {
        List<Tag> tags = path.get(accessor.getData());
        Iterator iterator = tags.iterator();
        Tag result = (Tag)iterator.next();
        if (iterator.hasNext()) {
            throw ERROR_MULTIPLE_TAGS.create();
        }
        return result;
    }

    /*
     * Loose catch block
     */
    private static int getData(CommandSourceStack source, DataAccessor accessor, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        Tag tag;
        Tag tag2 = tag = DataCommands.getSingleTag(path, accessor);
        Objects.requireNonNull(tag2);
        Tag tag3 = tag2;
        int n = 0;
        int result = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{NumericTag.class, CollectionTag.class, CompoundTag.class, StringTag.class, EndTag.class}, (Tag)tag3, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                NumericTag numericTag = (NumericTag)tag3;
                yield Mth.floor(numericTag.doubleValue());
            }
            case 1 -> {
                CollectionTag collectionTag = (CollectionTag)tag3;
                yield collectionTag.size();
            }
            case 2 -> {
                CompoundTag compoundTag = (CompoundTag)tag3;
                yield compoundTag.size();
            }
            case 3 -> {
                String var12_11;
                StringTag var10_10 = (StringTag)tag3;
                String value = var12_11 = var10_10.value();
                yield value.length();
            }
            case 4 -> {
                EndTag ignored = (EndTag)tag3;
                throw ERROR_GET_NON_EXISTENT.create(path.toString());
            }
        };
        source.sendSuccess(() -> accessor.getPrintSuccess(tag), false);
        return result;
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    private static int getNumeric(CommandSourceStack source, DataAccessor accessor, NbtPathArgument.NbtPath path, double scale) throws CommandSyntaxException {
        Tag tag = DataCommands.getSingleTag(path, accessor);
        if (!(tag instanceof NumericTag)) {
            throw ERROR_GET_NOT_NUMBER.create(path.toString());
        }
        int result = Mth.floor(((NumericTag)tag).doubleValue() * scale);
        source.sendSuccess(() -> accessor.getPrintSuccess(path, scale, result), false);
        return result;
    }

    private static int getData(CommandSourceStack source, DataAccessor accessor) throws CommandSyntaxException {
        CompoundTag data = accessor.getData();
        source.sendSuccess(() -> accessor.getPrintSuccess(data), false);
        return 1;
    }

    private static int mergeData(CommandSourceStack source, DataAccessor accessor, CompoundTag nbt) throws CommandSyntaxException {
        CompoundTag old = accessor.getData();
        if (NbtPathArgument.NbtPath.isTooDeep(nbt, 0)) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.create();
        }
        CompoundTag result = old.copy().merge(nbt);
        if (old.equals(result)) {
            throw ERROR_MERGE_UNCHANGED.create();
        }
        accessor.setData(result);
        source.sendSuccess(() -> accessor.getModifiedSuccess(), true);
        return 1;
    }

    public static interface DataProvider {
        public DataAccessor access(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;

        public ArgumentBuilder<CommandSourceStack, ?> wrap(ArgumentBuilder<CommandSourceStack, ?> var1, Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> var2);
    }

    @FunctionalInterface
    private static interface StringProcessor {
        public String process(String var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private static interface DataManipulator {
        public int modify(CommandContext<CommandSourceStack> var1, CompoundTag var2, NbtPathArgument.NbtPath var3, List<Tag> var4) throws CommandSyntaxException;
    }

    @FunctionalInterface
    private static interface DataManipulatorDecorator {
        public ArgumentBuilder<CommandSourceStack, ?> create(DataManipulator var1);
    }
}


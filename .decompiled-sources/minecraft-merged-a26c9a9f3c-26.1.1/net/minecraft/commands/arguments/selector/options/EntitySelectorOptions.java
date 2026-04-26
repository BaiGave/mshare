/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.arguments.selector.options;

import com.google.common.collect.Maps;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import org.slf4j.Logger;

public class EntitySelectorOptions {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Option> OPTIONS = Maps.newHashMap();
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_OPTION = new DynamicCommandExceptionType(name -> Component.translatableEscape("argument.entity.options.unknown", name));
    public static final DynamicCommandExceptionType ERROR_INAPPLICABLE_OPTION = new DynamicCommandExceptionType(name -> Component.translatableEscape("argument.entity.options.inapplicable", name));
    public static final SimpleCommandExceptionType ERROR_RANGE_NEGATIVE = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.distance.negative"));
    public static final SimpleCommandExceptionType ERROR_LEVEL_NEGATIVE = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.level.negative"));
    public static final SimpleCommandExceptionType ERROR_LIMIT_TOO_SMALL = new SimpleCommandExceptionType(Component.translatable("argument.entity.options.limit.toosmall"));
    public static final DynamicCommandExceptionType ERROR_SORT_UNKNOWN = new DynamicCommandExceptionType(name -> Component.translatableEscape("argument.entity.options.sort.irreversible", name));
    public static final DynamicCommandExceptionType ERROR_GAME_MODE_INVALID = new DynamicCommandExceptionType(name -> Component.translatableEscape("argument.entity.options.mode.invalid", name));
    public static final DynamicCommandExceptionType ERROR_ENTITY_TYPE_INVALID = new DynamicCommandExceptionType(type -> Component.translatableEscape("argument.entity.options.type.invalid", type));

    private static void register(String name, Modifier modifier, Predicate<EntitySelectorParser> predicate, Component description) {
        OPTIONS.put(name, new Option(modifier, predicate, description));
    }

    public static void bootStrap() {
        if (!OPTIONS.isEmpty()) {
            return;
        }
        EntitySelectorOptions.register("name", parser -> {
            int start = parser.getReader().getCursor();
            boolean not = parser.shouldInvertValue();
            String name = parser.getReader().readString();
            if (parser.hasNameNotEquals() && !not) {
                parser.getReader().setCursor(start);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext(parser.getReader(), "name");
            }
            if (not) {
                parser.setHasNameNotEquals(true);
            } else {
                parser.setHasNameEquals(true);
            }
            parser.addPredicate(e -> e.getPlainTextName().equals(name) != not);
        }, s -> !s.hasNameEquals(), Component.translatable("argument.entity.options.name.description"));
        EntitySelectorOptions.register("distance", parser -> {
            int start = parser.getReader().getCursor();
            MinMaxBounds.Doubles value = MinMaxBounds.Doubles.fromReader(parser.getReader());
            if (value.min().isPresent() && (Double)value.min().get() < 0.0 || value.max().isPresent() && (Double)value.max().get() < 0.0) {
                parser.getReader().setCursor(start);
                throw ERROR_RANGE_NEGATIVE.createWithContext(parser.getReader());
            }
            parser.setDistance(value);
            parser.setWorldLimited();
        }, s -> s.getDistance() == null, Component.translatable("argument.entity.options.distance.description"));
        EntitySelectorOptions.register("level", parser -> {
            int start = parser.getReader().getCursor();
            MinMaxBounds.Ints value = MinMaxBounds.Ints.fromReader(parser.getReader());
            if (value.min().isPresent() && (Integer)value.min().get() < 0 || value.max().isPresent() && (Integer)value.max().get() < 0) {
                parser.getReader().setCursor(start);
                throw ERROR_LEVEL_NEGATIVE.createWithContext(parser.getReader());
            }
            parser.setLevel(value);
            parser.setIncludesEntities(false);
        }, s -> s.getLevel() == null, Component.translatable("argument.entity.options.level.description"));
        EntitySelectorOptions.register("x", parser -> {
            parser.setWorldLimited();
            parser.setX(parser.getReader().readDouble());
        }, s -> s.getX() == null, Component.translatable("argument.entity.options.x.description"));
        EntitySelectorOptions.register("y", parser -> {
            parser.setWorldLimited();
            parser.setY(parser.getReader().readDouble());
        }, s -> s.getY() == null, Component.translatable("argument.entity.options.y.description"));
        EntitySelectorOptions.register("z", parser -> {
            parser.setWorldLimited();
            parser.setZ(parser.getReader().readDouble());
        }, s -> s.getZ() == null, Component.translatable("argument.entity.options.z.description"));
        EntitySelectorOptions.register("dx", parser -> {
            parser.setWorldLimited();
            parser.setDeltaX(parser.getReader().readDouble());
        }, s -> s.getDeltaX() == null, Component.translatable("argument.entity.options.dx.description"));
        EntitySelectorOptions.register("dy", parser -> {
            parser.setWorldLimited();
            parser.setDeltaY(parser.getReader().readDouble());
        }, s -> s.getDeltaY() == null, Component.translatable("argument.entity.options.dy.description"));
        EntitySelectorOptions.register("dz", parser -> {
            parser.setWorldLimited();
            parser.setDeltaZ(parser.getReader().readDouble());
        }, s -> s.getDeltaZ() == null, Component.translatable("argument.entity.options.dz.description"));
        EntitySelectorOptions.register("x_rotation", parser -> parser.setRotX(MinMaxBounds.FloatDegrees.fromReader(parser.getReader())), s -> s.getRotX() == null, Component.translatable("argument.entity.options.x_rotation.description"));
        EntitySelectorOptions.register("y_rotation", parser -> parser.setRotY(MinMaxBounds.FloatDegrees.fromReader(parser.getReader())), s -> s.getRotY() == null, Component.translatable("argument.entity.options.y_rotation.description"));
        EntitySelectorOptions.register("limit", parser -> {
            int start = parser.getReader().getCursor();
            int count = parser.getReader().readInt();
            if (count < 1) {
                parser.getReader().setCursor(start);
                throw ERROR_LIMIT_TOO_SMALL.createWithContext(parser.getReader());
            }
            parser.setMaxResults(count);
            parser.setLimited(true);
        }, s -> !s.isCurrentEntity() && !s.isLimited(), Component.translatable("argument.entity.options.limit.description"));
        EntitySelectorOptions.register("sort", parser -> {
            int start = parser.getReader().getCursor();
            String name = parser.getReader().readUnquotedString();
            parser.setSuggestions((b, n) -> SharedSuggestionProvider.suggest(Arrays.asList("nearest", "furthest", "random", "arbitrary"), b));
            parser.setOrder(switch (name) {
                case "nearest" -> EntitySelectorParser.ORDER_NEAREST;
                case "furthest" -> EntitySelectorParser.ORDER_FURTHEST;
                case "random" -> EntitySelectorParser.ORDER_RANDOM;
                case "arbitrary" -> EntitySelector.ORDER_ARBITRARY;
                default -> {
                    parser.getReader().setCursor(start);
                    throw ERROR_SORT_UNKNOWN.createWithContext(parser.getReader(), name);
                }
            });
            parser.setSorted(true);
        }, s -> !s.isCurrentEntity() && !s.isSorted(), Component.translatable("argument.entity.options.sort.description"));
        EntitySelectorOptions.register("gamemode", parser -> {
            parser.setSuggestions((b, m) -> {
                String prefix = b.getRemaining().toLowerCase(Locale.ROOT);
                boolean addNormal = !parser.hasGamemodeNotEquals();
                boolean addInverted = true;
                if (!prefix.isEmpty()) {
                    if (prefix.charAt(0) == '!') {
                        addNormal = false;
                        prefix = prefix.substring(1);
                    } else {
                        addInverted = false;
                    }
                }
                for (GameType type : GameType.values()) {
                    if (!type.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) continue;
                    if (addInverted) {
                        b.suggest("!" + type.getName());
                    }
                    if (!addNormal) continue;
                    b.suggest(type.getName());
                }
                return b.buildFuture();
            });
            int start = parser.getReader().getCursor();
            boolean inverted = parser.shouldInvertValue();
            if (parser.hasGamemodeNotEquals() && !inverted) {
                parser.getReader().setCursor(start);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext(parser.getReader(), "gamemode");
            }
            String name = parser.getReader().readUnquotedString();
            GameType expected = GameType.byName(name, null);
            if (expected == null) {
                parser.getReader().setCursor(start);
                throw ERROR_GAME_MODE_INVALID.createWithContext(parser.getReader(), name);
            }
            parser.setIncludesEntities(false);
            parser.addPredicate(e -> {
                if (e instanceof ServerPlayer) {
                    ServerPlayer player = (ServerPlayer)e;
                    GameType current = player.gameMode();
                    return current == expected ^ inverted;
                }
                return false;
            });
            if (inverted) {
                parser.setHasGamemodeNotEquals(true);
            } else {
                parser.setHasGamemodeEquals(true);
            }
        }, s -> !s.hasGamemodeEquals(), Component.translatable("argument.entity.options.gamemode.description"));
        EntitySelectorOptions.register("team", parser -> {
            boolean inverted = parser.shouldInvertValue();
            String expected = parser.getReader().readUnquotedString();
            parser.addPredicate(e -> {
                PlayerTeam current = e.getTeam();
                String currentName = current == null ? "" : ((Team)current).getName();
                return currentName.equals(expected) != inverted;
            });
            if (inverted) {
                parser.setHasTeamNotEquals(true);
            } else {
                parser.setHasTeamEquals(true);
            }
        }, s -> !s.hasTeamEquals(), Component.translatable("argument.entity.options.team.description"));
        EntitySelectorOptions.register("type", parser -> {
            parser.setSuggestions((b, m) -> {
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), b, String.valueOf('!'));
                SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTags().map(tag -> tag.key().location()), b, "!#");
                if (!parser.isTypeLimitedInversely()) {
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.keySet(), b);
                    SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.getTags().map(tag -> tag.key().location()), b, String.valueOf('#'));
                }
                return b.buildFuture();
            });
            int start = parser.getReader().getCursor();
            boolean inverted = parser.shouldInvertValue();
            if (parser.isTypeLimitedInversely() && !inverted) {
                parser.getReader().setCursor(start);
                throw ERROR_INAPPLICABLE_OPTION.createWithContext(parser.getReader(), "type");
            }
            if (inverted) {
                parser.setTypeLimitedInversely();
            }
            if (parser.isTag()) {
                TagKey<EntityType<?>> id = TagKey.create(Registries.ENTITY_TYPE, Identifier.read(parser.getReader()));
                parser.addPredicate(e -> e.is(id) != inverted);
            } else {
                Identifier id = Identifier.read(parser.getReader());
                EntityType type = (EntityType)BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElseThrow(() -> {
                    parser.getReader().setCursor(start);
                    return ERROR_ENTITY_TYPE_INVALID.createWithContext(parser.getReader(), id.toString());
                });
                if (Objects.equals(EntityType.PLAYER, type) && !inverted) {
                    parser.setIncludesEntities(false);
                }
                parser.addPredicate(e -> Objects.equals(type, e.getType()) != inverted);
                if (!inverted) {
                    parser.limitToType(type);
                }
            }
        }, s -> !s.isTypeLimited(), Component.translatable("argument.entity.options.type.description"));
        EntitySelectorOptions.register("tag", parser -> {
            boolean inverted = parser.shouldInvertValue();
            String tag = parser.getReader().readUnquotedString();
            parser.addPredicate(e -> {
                if ("".equals(tag)) {
                    return e.entityTags().isEmpty() != inverted;
                }
                return e.entityTags().contains(tag) != inverted;
            });
        }, s -> true, Component.translatable("argument.entity.options.tag.description"));
        EntitySelectorOptions.register("nbt", parser -> {
            boolean inverted = parser.shouldInvertValue();
            CompoundTag tag = TagParser.parseCompoundAsArgument(parser.getReader());
            parser.addPredicate(e -> {
                try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(e.problemPath(), LOGGER);){
                    ServerPlayer player2;
                    ItemStack selected;
                    TagValueOutput output = TagValueOutput.createWithContext(reporter, e.registryAccess());
                    e.saveWithoutId(output);
                    if (e instanceof ServerPlayer && !(selected = (player2 = (ServerPlayer)e).getInventory().getSelectedItem()).isEmpty()) {
                        output.store("SelectedItem", ItemStack.CODEC, selected);
                    }
                    boolean player2 = NbtUtils.compareNbt(tag, output.buildResult(), true) != inverted;
                    return player2;
                }
            });
        }, s -> true, Component.translatable("argument.entity.options.nbt.description"));
        EntitySelectorOptions.register("scores", parser -> {
            StringReader reader = parser.getReader();
            HashMap<String, MinMaxBounds.Ints> expected = Maps.newHashMap();
            reader.expect('{');
            reader.skipWhitespace();
            while (reader.canRead() && reader.peek() != '}') {
                reader.skipWhitespace();
                String name = reader.readUnquotedString();
                reader.skipWhitespace();
                reader.expect('=');
                reader.skipWhitespace();
                MinMaxBounds.Ints value = MinMaxBounds.Ints.fromReader(reader);
                expected.put(name, value);
                reader.skipWhitespace();
                if (!reader.canRead() || reader.peek() != ',') continue;
                reader.skip();
            }
            reader.expect('}');
            if (!expected.isEmpty()) {
                parser.addPredicate(entity -> {
                    ServerScoreboard scoreboard = entity.level().getServer().getScoreboard();
                    for (Map.Entry entry : expected.entrySet()) {
                        Objective objective = scoreboard.getObjective((String)entry.getKey());
                        if (objective == null) {
                            return false;
                        }
                        ReadOnlyScoreInfo scoreInfo = scoreboard.getPlayerScoreInfo((ScoreHolder)entity, objective);
                        if (scoreInfo == null) {
                            return false;
                        }
                        if (((MinMaxBounds.Ints)entry.getValue()).matches(scoreInfo.value())) continue;
                        return false;
                    }
                    return true;
                });
            }
            parser.setHasScores(true);
        }, s -> !s.hasScores(), Component.translatable("argument.entity.options.scores.description"));
        EntitySelectorOptions.register("advancements", parser -> {
            StringReader reader = parser.getReader();
            HashMap<Identifier, Predicate<AdvancementProgress>> expected = Maps.newHashMap();
            reader.expect('{');
            reader.skipWhitespace();
            while (reader.canRead() && reader.peek() != '}') {
                reader.skipWhitespace();
                Identifier name = Identifier.read(reader);
                reader.skipWhitespace();
                reader.expect('=');
                reader.skipWhitespace();
                if (reader.canRead() && reader.peek() == '{') {
                    HashMap<String, Predicate<CriterionProgress>> progress = Maps.newHashMap();
                    reader.skipWhitespace();
                    reader.expect('{');
                    reader.skipWhitespace();
                    while (reader.canRead() && reader.peek() != '}') {
                        reader.skipWhitespace();
                        String criterion = reader.readUnquotedString();
                        reader.skipWhitespace();
                        reader.expect('=');
                        reader.skipWhitespace();
                        boolean value = reader.readBoolean();
                        progress.put(criterion, p -> p.isDone() == value);
                        reader.skipWhitespace();
                        if (!reader.canRead() || reader.peek() != ',') continue;
                        reader.skip();
                    }
                    reader.skipWhitespace();
                    reader.expect('}');
                    reader.skipWhitespace();
                    expected.put(name, p -> {
                        for (Map.Entry entry : progress.entrySet()) {
                            CriterionProgress criterion = p.getCriterion((String)entry.getKey());
                            if (criterion != null && ((Predicate)entry.getValue()).test(criterion)) continue;
                            return false;
                        }
                        return true;
                    });
                } else {
                    boolean value = reader.readBoolean();
                    expected.put(name, p -> p.isDone() == value);
                }
                reader.skipWhitespace();
                if (!reader.canRead() || reader.peek() != ',') continue;
                reader.skip();
            }
            reader.expect('}');
            if (!expected.isEmpty()) {
                parser.addPredicate(e -> {
                    if (!(e instanceof ServerPlayer)) {
                        return false;
                    }
                    ServerPlayer player = (ServerPlayer)e;
                    PlayerAdvancements advancements = player.getAdvancements();
                    ServerAdvancementManager serverAdvancements = player.level().getServer().getAdvancements();
                    for (Map.Entry entry : expected.entrySet()) {
                        AdvancementHolder advancement = serverAdvancements.get((Identifier)entry.getKey());
                        if (advancement != null && ((Predicate)entry.getValue()).test(advancements.getOrStartProgress(advancement))) continue;
                        return false;
                    }
                    return true;
                });
                parser.setIncludesEntities(false);
            }
            parser.setHasAdvancements(true);
        }, s -> !s.hasAdvancements(), Component.translatable("argument.entity.options.advancements.description"));
        EntitySelectorOptions.register("predicate", parser -> {
            boolean inverted = parser.shouldInvertValue();
            ResourceKey<LootItemCondition> id = ResourceKey.create(Registries.PREDICATE, Identifier.read(parser.getReader()));
            parser.addPredicate(entity -> {
                Level patt0$temp = entity.level();
                if (!(patt0$temp instanceof ServerLevel)) {
                    return false;
                }
                ServerLevel level = (ServerLevel)patt0$temp;
                Optional<LootItemCondition> condition = level.getServer().reloadableRegistries().lookup().get(id).map(Holder::value);
                if (condition.isEmpty()) {
                    return false;
                }
                LootParams lootParams = new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, entity.position()).create(LootContextParamSets.SELECTOR);
                LootContext context = new LootContext.Builder(lootParams).create(Optional.empty());
                context.pushVisitedElement(LootContext.createVisitedEntry(condition.get()));
                return inverted ^ condition.get().test(context);
            });
        }, s -> true, Component.translatable("argument.entity.options.predicate.description"));
    }

    public static Modifier get(EntitySelectorParser parser, String key, int start) throws CommandSyntaxException {
        Option option = OPTIONS.get(key);
        if (option != null) {
            if (option.canUse.test(parser)) {
                return option.modifier;
            }
            throw ERROR_INAPPLICABLE_OPTION.createWithContext(parser.getReader(), key);
        }
        parser.getReader().setCursor(start);
        throw ERROR_UNKNOWN_OPTION.createWithContext(parser.getReader(), key);
    }

    public static void suggestNames(EntitySelectorParser parser, SuggestionsBuilder builder) {
        String lowerPrefix = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, Option> entry : OPTIONS.entrySet()) {
            if (!entry.getValue().canUse.test(parser) || !entry.getKey().toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) continue;
            builder.suggest(entry.getKey() + "=", (Message)entry.getValue().description);
        }
    }

    private record Option(Modifier modifier, Predicate<EntitySelectorParser> canUse, Component description) {
    }

    @FunctionalInterface
    public static interface Modifier {
        public void handle(EntitySelectorParser var1) throws CommandSyntaxException;
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerUpper;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataFixerBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFixerBuilder.class);
    private final int dataVersion;
    private final Int2ObjectSortedMap<Schema> schemas = new Int2ObjectAVLTreeMap<Schema>();
    private final List<DataFix> globalList = new ArrayList<DataFix>();
    private final IntSortedSet fixerVersions = new IntAVLTreeSet();

    public DataFixerBuilder(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Schema addSchema(int version, BiFunction<Integer, Schema, Schema> factory) {
        return this.addSchema(version, 0, factory);
    }

    public Schema addSchema(int version, int subVersion, BiFunction<Integer, Schema, Schema> factory) {
        int key = DataFixUtils.makeKey(version, subVersion);
        Schema parent = this.schemas.isEmpty() ? null : (Schema)this.schemas.get(DataFixerUpper.getLowestSchemaSameVersion(this.schemas, key - 1));
        Schema schema = factory.apply(DataFixUtils.makeKey(version, subVersion), parent);
        this.addSchema(schema);
        return schema;
    }

    public void addSchema(Schema schema) {
        this.schemas.put(schema.getVersionKey(), schema);
    }

    public void addFixer(DataFix fix) {
        int version = DataFixUtils.getVersion(fix.getVersionKey());
        if (version > this.dataVersion) {
            LOGGER.warn("Ignored fix registered for version: {} as the DataVersion of the game is: {}", (Object)version, (Object)this.dataVersion);
            return;
        }
        this.globalList.add(fix);
        this.fixerVersions.add(fix.getVersionKey());
    }

    public Result build() {
        DataFixerUpper fixer = new DataFixerUpper(new Int2ObjectAVLTreeMap<Schema>(this.schemas), new ArrayList<DataFix>(this.globalList), new IntAVLTreeSet(this.fixerVersions));
        return new Result(fixer);
    }

    public class Result {
        private final DataFixerUpper fixerUpper;

        public Result(DataFixerUpper fixerUpper) {
            this.fixerUpper = fixerUpper;
        }

        public DataFixer fixer() {
            return this.fixerUpper;
        }

        public CompletableFuture<?> optimize(Set<DSL.TypeReference> requiredTypes, Executor executor) {
            Instant started = Instant.now();
            ArrayList<CompletableFuture<Void>> doneFutures = new ArrayList<CompletableFuture<Void>>();
            ArrayList failFutures = new ArrayList();
            Set requiredTypeNames = requiredTypes.stream().map(DSL.TypeReference::typeName).collect(Collectors.toSet());
            IntBidirectionalIterator iterator = this.fixerUpper.fixerVersions().iterator();
            while (iterator.hasNext()) {
                int versionKey = iterator.nextInt();
                Schema schema = (Schema)DataFixerBuilder.this.schemas.get(versionKey);
                for (String typeName : schema.types()) {
                    if (!requiredTypeNames.contains(typeName)) continue;
                    CompletableFuture<Void> doneFuture = CompletableFuture.runAsync(() -> {
                        Type<?> dataType = schema.getType(() -> typeName);
                        TypeRewriteRule rule = this.fixerUpper.getRule(DataFixUtils.getVersion(versionKey), DataFixerBuilder.this.dataVersion);
                        dataType.rewrite(rule, DataFixerUpper.OPTIMIZATION_RULE);
                    }, executor);
                    doneFutures.add(doneFuture);
                    CompletableFuture failFuture = new CompletableFuture();
                    doneFuture.exceptionally(e -> {
                        failFuture.completeExceptionally((Throwable)e);
                        return null;
                    });
                    failFutures.add(failFuture);
                }
            }
            CompletionStage doneFuture = CompletableFuture.allOf((CompletableFuture[])doneFutures.toArray(CompletableFuture[]::new)).thenAccept(ignored -> LOGGER.info("{} Datafixer optimizations took {} milliseconds", (Object)doneFutures.size(), (Object)Duration.between(started, Instant.now()).toMillis()));
            CompletableFuture<Object> failFuture = CompletableFuture.anyOf((CompletableFuture[])failFutures.toArray(CompletableFuture[]::new));
            return CompletableFuture.anyOf(new CompletableFuture[]{doneFuture, failFuture});
        }
    }
}


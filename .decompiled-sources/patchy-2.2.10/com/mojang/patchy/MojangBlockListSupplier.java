/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.patchy;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.mojang.blocklist.BlockListSupplier;
import com.mojang.patchy.BlockedServers;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Predicate;
import javax.annotation.Nullable;

@AutoService(value={BlockListSupplier.class})
public class MojangBlockListSupplier
implements BlockListSupplier {
    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    @Nullable
    public Predicate<String> createBlockList() {
        try {
            URLConnection urlConnection = new URL("https://sessionserver.mojang.com/blockedservers").openConnection();
            try (InputStream is = urlConnection.getInputStream();){
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, BlockedServers.HASH_CHARSET));
                BlockedServers blockedServers = new BlockedServers(reader.lines().collect(ImmutableSet.toImmutableSet()));
                return blockedServers;
            }
        }
        catch (IOException iOException) {
            return null;
        }
    }
}


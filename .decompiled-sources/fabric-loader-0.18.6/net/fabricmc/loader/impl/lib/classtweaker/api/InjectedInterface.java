/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.classtweaker.api;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface InjectedInterface {
    public String getInterfaceName();

    public String getInterfaceSignature();

    public boolean hasGenerics();
}


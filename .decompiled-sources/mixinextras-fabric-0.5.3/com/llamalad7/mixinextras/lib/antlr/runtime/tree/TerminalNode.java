/*
 * Decompiled with CFR 0.152.
 */
package com.llamalad7.mixinextras.lib.antlr.runtime.tree;

import com.llamalad7.mixinextras.lib.antlr.runtime.Token;
import com.llamalad7.mixinextras.lib.antlr.runtime.tree.ParseTree;

public interface TerminalNode
extends ParseTree {
    public Token getSymbol();
}


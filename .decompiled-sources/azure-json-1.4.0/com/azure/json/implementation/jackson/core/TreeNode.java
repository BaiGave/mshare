/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

public interface TreeNode {
    public int size();

    public boolean isArray();

    public boolean isObject();

    public TreeNode get(String var1);

    public TreeNode get(int var1);
}


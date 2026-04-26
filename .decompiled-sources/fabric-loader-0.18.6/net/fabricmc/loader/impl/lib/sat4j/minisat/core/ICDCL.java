/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import net.fabricmc.loader.impl.lib.sat4j.minisat.core.ActivityListener;
import net.fabricmc.loader.impl.lib.sat4j.minisat.core.Learner;
import net.fabricmc.loader.impl.lib.sat4j.specs.ISolver;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public interface ICDCL
extends ActivityListener,
Learner,
ISolver,
UnitPropagationListener {
}


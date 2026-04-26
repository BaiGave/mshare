/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.discovery;

import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.impl.discovery.ModCandidateImpl;

class Explanation
implements Comparable<Explanation> {
    private static int nextCmpId;
    final ErrorKind error;
    final ModCandidateImpl mod;
    final ModDependency dep;
    final String data;
    private final int cmpId;

    Explanation(ErrorKind error, ModCandidateImpl mod) {
        this(error, mod, null, null);
    }

    Explanation(ErrorKind error, ModCandidateImpl mod, ModDependency dep) {
        this(error, mod, dep, null);
    }

    Explanation(ErrorKind error, String data) {
        this(error, null, data);
    }

    Explanation(ErrorKind error, ModCandidateImpl mod, String data) {
        this(error, mod, null, data);
    }

    private Explanation(ErrorKind error, ModCandidateImpl mod, ModDependency dep, String data) {
        this.error = error;
        this.mod = mod;
        this.dep = dep;
        this.data = data;
        this.cmpId = nextCmpId++;
    }

    @Override
    public int compareTo(Explanation o) {
        return Integer.compare(this.cmpId, o.cmpId);
    }

    public String toString() {
        if (this.mod == null) {
            return String.format("%s %s", new Object[]{this.error, this.data});
        }
        if (this.dep == null) {
            return String.format("%s %s", new Object[]{this.error, this.mod});
        }
        return String.format("%s %s %s", new Object[]{this.error, this.mod, this.dep});
    }

    static enum ErrorKind {
        PRESELECT_HARD_DEP(true),
        PRESELECT_SOFT_DEP(true),
        PRESELECT_NEG_HARD_DEP(true),
        PRESELECT_FORCELOAD(false),
        HARD_DEP_INCOMPATIBLE_PRESELECTED(true),
        HARD_DEP_NO_CANDIDATE(true),
        HARD_DEP(true),
        SOFT_DEP(true),
        NEG_HARD_DEP(true),
        NESTED_FORCELOAD(false),
        NESTED_REQ_PARENT(false),
        ROOT_FORCELOAD_SINGLE(false),
        ROOT_FORCELOAD(false),
        UNIQUE_ID(false);

        final boolean isDependencyError;

        private ErrorKind(boolean isDependencyError) {
            this.isDependencyError = isDependencyError;
        }
    }
}


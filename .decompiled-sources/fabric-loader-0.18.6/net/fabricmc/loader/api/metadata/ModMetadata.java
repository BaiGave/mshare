/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api.metadata;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.Person;

public interface ModMetadata {
    public String getType();

    public String getId();

    public Collection<String> getProvides();

    public Version getVersion();

    public ModEnvironment getEnvironment();

    public Collection<ModDependency> getDependencies();

    @Deprecated
    default public Collection<ModDependency> getDepends() {
        return this.getDependencies().stream().filter(d -> d.getKind() == ModDependency.Kind.DEPENDS).collect(Collectors.toList());
    }

    @Deprecated
    default public Collection<ModDependency> getRecommends() {
        return this.getDependencies().stream().filter(d -> d.getKind() == ModDependency.Kind.RECOMMENDS).collect(Collectors.toList());
    }

    @Deprecated
    default public Collection<ModDependency> getSuggests() {
        return this.getDependencies().stream().filter(d -> d.getKind() == ModDependency.Kind.SUGGESTS).collect(Collectors.toList());
    }

    @Deprecated
    default public Collection<ModDependency> getConflicts() {
        return this.getDependencies().stream().filter(d -> d.getKind() == ModDependency.Kind.CONFLICTS).collect(Collectors.toList());
    }

    @Deprecated
    default public Collection<ModDependency> getBreaks() {
        return this.getDependencies().stream().filter(d -> d.getKind() == ModDependency.Kind.BREAKS).collect(Collectors.toList());
    }

    public String getName();

    public String getDescription();

    public Collection<Person> getAuthors();

    public Collection<Person> getContributors();

    public ContactInformation getContact();

    public Collection<String> getLicense();

    public Optional<String> getIconPath(int var1);

    public boolean containsCustomValue(String var1);

    public CustomValue getCustomValue(String var1);

    public Map<String, CustomValue> getCustomValues();

    @Deprecated
    public boolean containsCustomElement(String var1);
}


/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync.packet;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.registry.sync.RegistryAttributeImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record RegistrySyncPayload(Map<Identifier, Object2IntMap<Identifier>> registryMap, Map<Identifier, EnumSet<RegistryAttribute>> registryAttributes) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<RegistrySyncPayload> ID = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("fabric", "registry/sync"));
    public static final StreamCodec<FriendlyByteBuf, RegistrySyncPayload> CODEC = CustomPacketPayload.codec(RegistrySyncPayload::write, RegistrySyncPayload::read);

    public RegistrySyncPayload(Map<Identifier, Object2IntMap<Identifier>> registryMap) {
        this(registryMap, RegistrySyncPayload.getRegistryAttributeMap(registryMap));
    }

    private static Map<Identifier, EnumSet<RegistryAttribute>> getRegistryAttributeMap(Map<Identifier, Object2IntMap<Identifier>> registryMap) {
        LinkedHashMap<Identifier, EnumSet<RegistryAttribute>> registryAttributes = new LinkedHashMap<Identifier, EnumSet<RegistryAttribute>>();
        registryMap.forEach((regId, idMap) -> {
            ResourceKey registryKey = ResourceKey.createRegistryKey(regId);
            RegistryAttributeImpl holder = (RegistryAttributeImpl)RegistryAttributeHolder.get(registryKey);
            registryAttributes.put((Identifier)regId, holder.getAttributes());
        });
        return registryAttributes;
    }

    private static RegistrySyncPayload read(FriendlyByteBuf combinedBuf) {
        LinkedHashMap<Identifier, Object2IntMap<Identifier>> syncedRegistryMap = new LinkedHashMap<Identifier, Object2IntMap<Identifier>>();
        LinkedHashMap<Identifier, EnumSet<RegistryAttribute>> syncedRegistryAttributes = new LinkedHashMap<Identifier, EnumSet<RegistryAttribute>>();
        int regNamespaceGroupAmount = combinedBuf.readVarInt();
        for (int i = 0; i < regNamespaceGroupAmount; ++i) {
            String regNamespace = RegistrySyncPayload.unoptimizeNamespace(combinedBuf.readUtf());
            int regNamespaceGroupLength = combinedBuf.readVarInt();
            for (int j = 0; j < regNamespaceGroupLength; ++j) {
                String regPath = combinedBuf.readUtf();
                EnumSet<RegistryAttribute> attributes = RegistrySyncPayload.decodeRegistryAttributes(combinedBuf.readByte());
                Object2IntLinkedOpenHashMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<Identifier>();
                int idNamespaceGroupAmount = combinedBuf.readVarInt();
                int lastBulkLastRawId = 0;
                for (int k = 0; k < idNamespaceGroupAmount; ++k) {
                    String idNamespace = RegistrySyncPayload.unoptimizeNamespace(combinedBuf.readUtf());
                    int rawIdBulkAmount = combinedBuf.readVarInt();
                    for (int l = 0; l < rawIdBulkAmount; ++l) {
                        int bulkRawIdStartDiff = combinedBuf.readVarInt();
                        int bulkSize = combinedBuf.readVarInt();
                        int currentRawId = lastBulkLastRawId + bulkRawIdStartDiff - 1;
                        for (int m = 0; m < bulkSize; ++m) {
                            String idPath = combinedBuf.readUtf();
                            idMap.put(Identifier.fromNamespaceAndPath(idNamespace, idPath), ++currentRawId);
                        }
                        lastBulkLastRawId = currentRawId;
                    }
                }
                Identifier registryId = Identifier.fromNamespaceAndPath(regNamespace, regPath);
                syncedRegistryMap.put(registryId, idMap);
                syncedRegistryAttributes.put(registryId, attributes);
            }
        }
        return new RegistrySyncPayload(syncedRegistryMap, syncedRegistryAttributes);
    }

    private void write(FriendlyByteBuf buf) {
        Map<String, List<Identifier>> regNamespaceGroups = this.registryMap.keySet().stream().collect(Collectors.groupingBy(Identifier::getNamespace));
        buf.writeVarInt(regNamespaceGroups.size());
        regNamespaceGroups.forEach((regNamespace, regIds) -> {
            buf.writeUtf(RegistrySyncPayload.optimizeNamespace(regNamespace));
            buf.writeVarInt(regIds.size());
            for (Identifier regId : regIds) {
                buf.writeUtf(regId.getPath());
                buf.writeByte(RegistrySyncPayload.encodeRegistryAttributes(this.registryAttributes.getOrDefault(regId, EnumSet.noneOf(RegistryAttribute.class))));
                Object2IntMap<Identifier> idMap = this.registryMap.get(regId);
                Map idNamespaceGroups = idMap.object2IntEntrySet().stream().collect(Collectors.groupingBy(e -> ((Identifier)e.getKey()).getNamespace(), LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));
                buf.writeVarInt(idNamespaceGroups.size());
                int lastBulkLastRawId = 0;
                for (Map.Entry idNamespaceEntry : idNamespaceGroups.entrySet()) {
                    List idPairs = (List)idNamespaceEntry.getValue();
                    idPairs.sort(Comparator.comparingInt(Object2IntMap.Entry::getIntValue));
                    ArrayList bulks = new ArrayList();
                    Iterator idPairIter = idPairs.iterator();
                    ArrayList<Object2IntMap.Entry> currentBulk = new ArrayList<Object2IntMap.Entry>();
                    Object2IntMap.Entry currentPair = (Object2IntMap.Entry)idPairIter.next();
                    currentBulk.add(currentPair);
                    while (idPairIter.hasNext()) {
                        currentPair = (Object2IntMap.Entry)idPairIter.next();
                        if (((Object2IntMap.Entry)currentBulk.get(currentBulk.size() - 1)).getIntValue() + 1 != currentPair.getIntValue()) {
                            bulks.add(currentBulk);
                            currentBulk = new ArrayList();
                        }
                        currentBulk.add(currentPair);
                    }
                    bulks.add(currentBulk);
                    buf.writeUtf(RegistrySyncPayload.optimizeNamespace((String)idNamespaceEntry.getKey()));
                    buf.writeVarInt(bulks.size());
                    for (List list : bulks) {
                        int firstRawId = ((Object2IntMap.Entry)list.get(0)).getIntValue();
                        int bulkRawIdStartDiff = firstRawId - lastBulkLastRawId;
                        buf.writeVarInt(bulkRawIdStartDiff);
                        buf.writeVarInt(list.size());
                        for (Object2IntMap.Entry idPair : list) {
                            buf.writeUtf(((Identifier)idPair.getKey()).getPath());
                            lastBulkLastRawId = idPair.getIntValue();
                        }
                    }
                }
            }
        });
    }

    private static byte encodeRegistryAttributes(EnumSet<RegistryAttribute> attributes) {
        byte encoded = 0;
        if (attributes.contains((Object)RegistryAttribute.OPTIONAL)) {
            encoded = (byte)(encoded | 1);
        }
        return encoded;
    }

    private static EnumSet<RegistryAttribute> decodeRegistryAttributes(byte encoded) {
        EnumSet<RegistryAttribute> attributes = EnumSet.noneOf(RegistryAttribute.class);
        if ((encoded & 1) != 0) {
            attributes.add(RegistryAttribute.OPTIONAL);
        }
        return attributes;
    }

    private static String optimizeNamespace(String namespace) {
        return namespace.equals("minecraft") ? "" : namespace;
    }

    private static String unoptimizeNamespace(String namespace) {
        return namespace.isEmpty() ? "minecraft" : namespace;
    }

    public CustomPacketPayload.Type<RegistrySyncPayload> type() {
        return ID;
    }
}


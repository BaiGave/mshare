/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.item.properties.select;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LocalTime
implements SelectItemModelProperty<String> {
    public static final String ROOT_LOCALE = "";
    private static final long UPDATE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1L);
    public static final Codec<String> VALUE_CODEC = Codec.STRING;
    private static final Codec<TimeZone> TIME_ZONE_CODEC = VALUE_CODEC.comapFlatMap(s -> {
        TimeZone tz = TimeZone.getTimeZone(s);
        if (tz.equals(TimeZone.UNKNOWN_ZONE)) {
            return DataResult.error(() -> "Unknown timezone: " + s);
        }
        return DataResult.success(tz);
    }, TimeZone::getID);
    private static final MapCodec<Data> DATA_MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.STRING.fieldOf("pattern")).forGetter(o -> o.format), Codec.STRING.optionalFieldOf("locale", ROOT_LOCALE).forGetter(o -> o.localeId), TIME_ZONE_CODEC.optionalFieldOf("time_zone").forGetter(o -> o.timeZone)).apply((Applicative<Data, ?>)i, Data::new));
    public static final SelectItemModelProperty.Type<LocalTime, String> TYPE = SelectItemModelProperty.Type.create(DATA_MAP_CODEC.flatXmap(LocalTime::create, d -> DataResult.success(d.data)), VALUE_CODEC);
    private final Data data;
    private final DateFormat parsedFormat;
    private long nextUpdateTimeMs;
    private String lastResult = "";

    private LocalTime(Data data, DateFormat parsedFormat) {
        this.data = data;
        this.parsedFormat = parsedFormat;
    }

    public static LocalTime create(String format, String localeId, Optional<TimeZone> timeZone) {
        return LocalTime.create(new Data(format, localeId, timeZone)).getOrThrow(msg -> new IllegalStateException("Failed to validate format: " + msg));
    }

    private static DataResult<LocalTime> create(Data data) {
        ULocale locale = new ULocale(data.localeId);
        Calendar calendar = data.timeZone.map(tz -> Calendar.getInstance(tz, locale)).orElseGet(() -> Calendar.getInstance(locale));
        SimpleDateFormat parsedFormat = new SimpleDateFormat(data.format, locale);
        parsedFormat.setCalendar(calendar);
        try {
            parsedFormat.format(new Date());
        }
        catch (Exception e) {
            return DataResult.error(() -> "Invalid time format '" + String.valueOf(parsedFormat) + "': " + e.getMessage());
        }
        return DataResult.success(new LocalTime(data, parsedFormat));
    }

    @Override
    public @Nullable String get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        long currentTimeMs = Util.getMillis();
        if (currentTimeMs > this.nextUpdateTimeMs) {
            this.lastResult = this.update();
            this.nextUpdateTimeMs = currentTimeMs + UPDATE_INTERVAL_MS;
        }
        return this.lastResult;
    }

    private String update() {
        return this.parsedFormat.format(new Date());
    }

    @Override
    public SelectItemModelProperty.Type<LocalTime, String> type() {
        return TYPE;
    }

    @Override
    public Codec<String> valueCodec() {
        return VALUE_CODEC;
    }

    @Environment(value=EnvType.CLIENT)
    private record Data(String format, String localeId, Optional<TimeZone> timeZone) {
    }
}


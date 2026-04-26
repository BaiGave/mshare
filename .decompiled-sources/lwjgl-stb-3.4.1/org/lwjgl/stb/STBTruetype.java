/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.stb;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.stb.LibSTB;
import org.lwjgl.stb.STBRPRect;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTBitmap;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTKerningentry;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTTVertex;
import org.lwjgl.system.Checks;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Struct;

public class STBTruetype {
    public static final byte STBTT_vmove = 1;
    public static final byte STBTT_vline = 2;
    public static final byte STBTT_vcurve = 3;
    public static final byte STBTT_vcubic = 4;
    public static final int STBTT_MACSTYLE_DONTCARE = 0;
    public static final int STBTT_MACSTYLE_BOLD = 1;
    public static final int STBTT_MACSTYLE_ITALIC = 2;
    public static final int STBTT_MACSTYLE_UNDERSCORE = 4;
    public static final int STBTT_MACSTYLE_NONE = 8;
    public static final int STBTT_PLATFORM_ID_UNICODE = 0;
    public static final int STBTT_PLATFORM_ID_MAC = 1;
    public static final int STBTT_PLATFORM_ID_ISO = 2;
    public static final int STBTT_PLATFORM_ID_MICROSOFT = 3;
    public static final int STBTT_UNICODE_EID_UNICODE_1_0 = 0;
    public static final int STBTT_UNICODE_EID_UNICODE_1_1 = 1;
    public static final int STBTT_UNICODE_EID_ISO_10646 = 2;
    public static final int STBTT_UNICODE_EID_UNICODE_2_0_BMP = 3;
    public static final int STBTT_UNICODE_EID_UNICODE_2_0_FULL = 4;
    public static final int STBTT_MS_EID_SYMBOL = 0;
    public static final int STBTT_MS_EID_UNICODE_BMP = 1;
    public static final int STBTT_MS_EID_SHIFTJIS = 2;
    public static final int STBTT_MS_EID_UNICODE_FULL = 10;
    public static final int STBTT_MAC_EID_ROMAN = 0;
    public static final int STBTT_MAC_EID_JAPANESE = 1;
    public static final int STBTT_MAC_EID_CHINESE_TRAD = 2;
    public static final int STBTT_MAC_EID_KOREAN = 3;
    public static final int STBTT_MAC_EID_ARABIC = 4;
    public static final int STBTT_MAC_EID_HEBREW = 5;
    public static final int STBTT_MAC_EID_GREEK = 6;
    public static final int STBTT_MAC_EID_RUSSIAN = 7;
    public static final int STBTT_MS_LANG_ENGLISH = 1033;
    public static final int STBTT_MS_LANG_CHINESE = 2052;
    public static final int STBTT_MS_LANG_DUTCH = 1043;
    public static final int STBTT_MS_LANG_FRENCH = 1036;
    public static final int STBTT_MS_LANG_GERMAN = 1031;
    public static final int STBTT_MS_LANG_HEBREW = 1037;
    public static final int STBTT_MS_LANG_ITALIAN = 1040;
    public static final int STBTT_MS_LANG_JAPANESE = 1041;
    public static final int STBTT_MS_LANG_KOREAN = 1042;
    public static final int STBTT_MS_LANG_RUSSIAN = 1049;
    public static final int STBTT_MS_LANG_SPANISH = 1033;
    public static final int STBTT_MS_LANG_SWEDISH = 1053;
    public static final int STBTT_MAC_LANG_ENGLISH = 0;
    public static final int STBTT_MAC_LANG_ARABIC = 12;
    public static final int STBTT_MAC_LANG_DUTCH = 4;
    public static final int STBTT_MAC_LANG_FRENCH = 1;
    public static final int STBTT_MAC_LANG_GERMAN = 2;
    public static final int STBTT_MAC_LANG_HEBREW = 10;
    public static final int STBTT_MAC_LANG_ITALIAN = 3;
    public static final int STBTT_MAC_LANG_JAPANESE = 11;
    public static final int STBTT_MAC_LANG_KOREAN = 23;
    public static final int STBTT_MAC_LANG_RUSSIAN = 32;
    public static final int STBTT_MAC_LANG_SPANISH = 6;
    public static final int STBTT_MAC_LANG_SWEDISH = 5;
    public static final int STBTT_MAC_LANG_CHINESE_SIMPLIFIED = 33;
    public static final int STBTT_MAC_LANG_CHINESE_TRAD = 19;

    protected STBTruetype() {
        throw new UnsupportedOperationException();
    }

    public static native int nstbtt_BakeFontBitmap(long var0, int var2, float var3, long var4, int var6, int var7, int var8, int var9, long var10);

    public static int stbtt_BakeFontBitmap(@NativeType(value="unsigned char const *") ByteBuffer data, float pixel_height, @NativeType(value="unsigned char *") ByteBuffer pixels, int pw, int ph, int first_char, @NativeType(value="stbtt_bakedchar *") STBTTBakedChar.Buffer chardata) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)pixels, pw * ph);
        }
        return STBTruetype.nstbtt_BakeFontBitmap(MemoryUtil.memAddress(data), 0, pixel_height, MemoryUtil.memAddress(pixels), pw, ph, first_char, chardata.remaining(), chardata.address());
    }

    public static native void nstbtt_GetBakedQuad(long var0, int var2, int var3, int var4, long var5, long var7, long var9, int var11);

    public static void stbtt_GetBakedQuad(@NativeType(value="stbtt_bakedchar const *") STBTTBakedChar.Buffer chardata, int pw, int ph, int char_index, @NativeType(value="float *") FloatBuffer xpos, @NativeType(value="float *") FloatBuffer ypos, @NativeType(value="stbtt_aligned_quad *") STBTTAlignedQuad q, @NativeType(value="int") boolean opengl_fillrule) {
        if (Checks.CHECKS) {
            Checks.check(chardata, char_index + 1);
            Checks.check((Buffer)xpos, 1);
            Checks.check((Buffer)ypos, 1);
        }
        STBTruetype.nstbtt_GetBakedQuad(chardata.address(), pw, ph, char_index, MemoryUtil.memAddress(xpos), MemoryUtil.memAddress(ypos), q.address(), opengl_fillrule ? 1 : 0);
    }

    public static native void nstbtt_GetScaledFontVMetrics(long var0, int var2, float var3, long var4, long var6, long var8);

    public static void stbtt_GetScaledFontVMetrics(@NativeType(value="unsigned char const *") ByteBuffer fontdata, int index, float size, @NativeType(value="float *") FloatBuffer ascent, @NativeType(value="float *") FloatBuffer descent, @NativeType(value="float *") FloatBuffer lineGap) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)ascent, 1);
            Checks.check((Buffer)descent, 1);
            Checks.check((Buffer)lineGap, 1);
        }
        STBTruetype.nstbtt_GetScaledFontVMetrics(MemoryUtil.memAddress(fontdata), index, size, MemoryUtil.memAddress(ascent), MemoryUtil.memAddress(descent), MemoryUtil.memAddress(lineGap));
    }

    public static native int nstbtt_PackBegin(long var0, long var2, int var4, int var5, int var6, int var7, long var8);

    @NativeType(value="int")
    public static boolean stbtt_PackBegin(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="unsigned char *") @Nullable ByteBuffer pixels, int width, int height, int stride_in_bytes, int padding, @NativeType(value="void *") long alloc_context) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)pixels, (stride_in_bytes != 0 ? stride_in_bytes : width) * height);
        }
        return STBTruetype.nstbtt_PackBegin(spc.address(), MemoryUtil.memAddressSafe(pixels), width, height, stride_in_bytes, padding, alloc_context) != 0;
    }

    @NativeType(value="int")
    public static boolean stbtt_PackBegin(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="unsigned char *") @Nullable ByteBuffer pixels, int width, int height, int stride_in_bytes, int padding) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)pixels, (stride_in_bytes != 0 ? stride_in_bytes : width) * height);
        }
        return STBTruetype.nstbtt_PackBegin(spc.address(), MemoryUtil.memAddressSafe(pixels), width, height, stride_in_bytes, padding, 0L) != 0;
    }

    public static native void nstbtt_PackEnd(long var0);

    public static void stbtt_PackEnd(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc) {
        STBTruetype.nstbtt_PackEnd(spc.address());
    }

    public static int STBTT_POINT_SIZE(int font_size) {
        return -font_size;
    }

    public static native int nstbtt_PackFontRange(long var0, long var2, int var4, float var5, int var6, int var7, long var8);

    @NativeType(value="int")
    public static boolean stbtt_PackFontRange(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="unsigned char const *") ByteBuffer fontdata, int font_index, float font_size, int first_unicode_char_in_range, @NativeType(value="stbtt_packedchar *") STBTTPackedchar.Buffer chardata_for_range) {
        return STBTruetype.nstbtt_PackFontRange(spc.address(), MemoryUtil.memAddress(fontdata), font_index, font_size, first_unicode_char_in_range, chardata_for_range.remaining(), chardata_for_range.address()) != 0;
    }

    public static native int nstbtt_PackFontRanges(long var0, long var2, int var4, long var5, int var7);

    @NativeType(value="int")
    public static boolean stbtt_PackFontRanges(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="unsigned char const *") ByteBuffer fontdata, int font_index, @NativeType(value="stbtt_pack_range *") STBTTPackRange.Buffer ranges) {
        if (Checks.CHECKS) {
            Struct.validate(ranges.address(), ranges.remaining(), STBTTPackRange.SIZEOF, STBTTPackRange::validate);
        }
        return STBTruetype.nstbtt_PackFontRanges(spc.address(), MemoryUtil.memAddress(fontdata), font_index, ranges.address(), ranges.remaining()) != 0;
    }

    public static native void nstbtt_PackSetOversampling(long var0, int var2, int var3);

    public static void stbtt_PackSetOversampling(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="unsigned int") int h_oversample, @NativeType(value="unsigned int") int v_oversample) {
        STBTruetype.nstbtt_PackSetOversampling(spc.address(), h_oversample, v_oversample);
    }

    public static native void nstbtt_PackSetSkipMissingCodepoints(long var0, int var2);

    public static void stbtt_PackSetSkipMissingCodepoints(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="int") boolean skip) {
        STBTruetype.nstbtt_PackSetSkipMissingCodepoints(spc.address(), skip ? 1 : 0);
    }

    public static native void nstbtt_GetPackedQuad(long var0, int var2, int var3, int var4, long var5, long var7, long var9, int var11);

    public static void stbtt_GetPackedQuad(@NativeType(value="stbtt_packedchar const *") STBTTPackedchar.Buffer chardata, int pw, int ph, int char_index, @NativeType(value="float *") FloatBuffer xpos, @NativeType(value="float *") FloatBuffer ypos, @NativeType(value="stbtt_aligned_quad *") STBTTAlignedQuad q, @NativeType(value="int") boolean align_to_integer) {
        if (Checks.CHECKS) {
            Checks.check(chardata, char_index + 1);
            Checks.check((Buffer)xpos, 1);
            Checks.check((Buffer)ypos, 1);
        }
        STBTruetype.nstbtt_GetPackedQuad(chardata.address(), pw, ph, char_index, MemoryUtil.memAddress(xpos), MemoryUtil.memAddress(ypos), q.address(), align_to_integer ? 1 : 0);
    }

    public static native int nstbtt_PackFontRangesGatherRects(long var0, long var2, long var4, int var6, long var7);

    public static int stbtt_PackFontRangesGatherRects(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="stbtt_fontinfo *") STBTTFontinfo info, @NativeType(value="stbtt_pack_range *") STBTTPackRange.Buffer ranges, @NativeType(value="stbrp_rect *") STBRPRect.Buffer rects) {
        if (Checks.CHECKS) {
            Struct.validate(ranges.address(), ranges.remaining(), STBTTPackRange.SIZEOF, STBTTPackRange::validate);
        }
        return STBTruetype.nstbtt_PackFontRangesGatherRects(spc.address(), info.address(), ranges.address(), ranges.remaining(), rects.address());
    }

    public static native void nstbtt_PackFontRangesPackRects(long var0, long var2, int var4);

    public static void stbtt_PackFontRangesPackRects(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="stbrp_rect *") STBRPRect.Buffer rects) {
        STBTruetype.nstbtt_PackFontRangesPackRects(spc.address(), rects.address(), rects.remaining());
    }

    public static native int nstbtt_PackFontRangesRenderIntoRects(long var0, long var2, long var4, int var6, long var7);

    @NativeType(value="int")
    public static boolean stbtt_PackFontRangesRenderIntoRects(@NativeType(value="stbtt_pack_context *") STBTTPackContext spc, @NativeType(value="stbtt_fontinfo *") STBTTFontinfo info, @NativeType(value="stbtt_pack_range *") STBTTPackRange.Buffer ranges, @NativeType(value="stbrp_rect *") STBRPRect.Buffer rects) {
        if (Checks.CHECKS) {
            Struct.validate(ranges.address(), ranges.remaining(), STBTTPackRange.SIZEOF, STBTTPackRange::validate);
        }
        return STBTruetype.nstbtt_PackFontRangesRenderIntoRects(spc.address(), info.address(), ranges.address(), ranges.remaining(), rects.address()) != 0;
    }

    public static native int nstbtt_GetNumberOfFonts(long var0);

    public static int stbtt_GetNumberOfFonts(@NativeType(value="unsigned char const *") ByteBuffer data) {
        return STBTruetype.nstbtt_GetNumberOfFonts(MemoryUtil.memAddress(data));
    }

    public static native int nstbtt_GetFontOffsetForIndex(long var0, int var2);

    public static int stbtt_GetFontOffsetForIndex(@NativeType(value="unsigned char const *") ByteBuffer data, int index) {
        return STBTruetype.nstbtt_GetFontOffsetForIndex(MemoryUtil.memAddress(data), index);
    }

    public static native int nstbtt_InitFont(long var0, long var2, int var4);

    @NativeType(value="int")
    public static boolean stbtt_InitFont(@NativeType(value="stbtt_fontinfo *") STBTTFontinfo info, @NativeType(value="unsigned char const *") ByteBuffer data, int offset) {
        return STBTruetype.nstbtt_InitFont(info.address(), MemoryUtil.memAddress(data), offset) != 0;
    }

    @NativeType(value="int")
    public static boolean stbtt_InitFont(@NativeType(value="stbtt_fontinfo *") STBTTFontinfo info, @NativeType(value="unsigned char const *") ByteBuffer data) {
        return STBTruetype.nstbtt_InitFont(info.address(), MemoryUtil.memAddress(data), 0) != 0;
    }

    public static native int nstbtt_FindGlyphIndex(long var0, int var2);

    public static int stbtt_FindGlyphIndex(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int unicode_codepoint) {
        return STBTruetype.nstbtt_FindGlyphIndex(info.address(), unicode_codepoint);
    }

    public static native float nstbtt_ScaleForPixelHeight(long var0, float var2);

    public static float stbtt_ScaleForPixelHeight(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float pixels) {
        return STBTruetype.nstbtt_ScaleForPixelHeight(info.address(), pixels);
    }

    public static native float nstbtt_ScaleForMappingEmToPixels(long var0, float var2);

    public static float stbtt_ScaleForMappingEmToPixels(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float pixels) {
        return STBTruetype.nstbtt_ScaleForMappingEmToPixels(info.address(), pixels);
    }

    public static native void nstbtt_GetFontVMetrics(long var0, long var2, long var4, long var6);

    public static void stbtt_GetFontVMetrics(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="int *") @Nullable IntBuffer ascent, @NativeType(value="int *") @Nullable IntBuffer descent, @NativeType(value="int *") @Nullable IntBuffer lineGap) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)ascent, 1);
            Checks.checkSafe((Buffer)descent, 1);
            Checks.checkSafe((Buffer)lineGap, 1);
        }
        STBTruetype.nstbtt_GetFontVMetrics(info.address(), MemoryUtil.memAddressSafe(ascent), MemoryUtil.memAddressSafe(descent), MemoryUtil.memAddressSafe(lineGap));
    }

    public static native int nstbtt_GetFontVMetricsOS2(long var0, long var2, long var4, long var6);

    @NativeType(value="int")
    public static boolean stbtt_GetFontVMetricsOS2(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="int *") @Nullable IntBuffer typoAscent, @NativeType(value="int *") @Nullable IntBuffer typoDescent, @NativeType(value="int *") @Nullable IntBuffer typoLineGap) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)typoAscent, 1);
            Checks.checkSafe((Buffer)typoDescent, 1);
            Checks.checkSafe((Buffer)typoLineGap, 1);
        }
        return STBTruetype.nstbtt_GetFontVMetricsOS2(info.address(), MemoryUtil.memAddressSafe(typoAscent), MemoryUtil.memAddressSafe(typoDescent), MemoryUtil.memAddressSafe(typoLineGap)) != 0;
    }

    public static native void nstbtt_GetFontBoundingBox(long var0, long var2, long var4, long var6, long var8);

    public static void stbtt_GetFontBoundingBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="int *") IntBuffer x0, @NativeType(value="int *") IntBuffer y0, @NativeType(value="int *") IntBuffer x1, @NativeType(value="int *") IntBuffer y1) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)x0, 1);
            Checks.check((Buffer)y0, 1);
            Checks.check((Buffer)x1, 1);
            Checks.check((Buffer)y1, 1);
        }
        STBTruetype.nstbtt_GetFontBoundingBox(info.address(), MemoryUtil.memAddress(x0), MemoryUtil.memAddress(y0), MemoryUtil.memAddress(x1), MemoryUtil.memAddress(y1));
    }

    public static native void nstbtt_GetCodepointHMetrics(long var0, int var2, long var3, long var5);

    public static void stbtt_GetCodepointHMetrics(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int codepoint, @NativeType(value="int *") @Nullable IntBuffer advanceWidth, @NativeType(value="int *") @Nullable IntBuffer leftSideBearing) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)advanceWidth, 1);
            Checks.checkSafe((Buffer)leftSideBearing, 1);
        }
        STBTruetype.nstbtt_GetCodepointHMetrics(info.address(), codepoint, MemoryUtil.memAddressSafe(advanceWidth), MemoryUtil.memAddressSafe(leftSideBearing));
    }

    public static native int nstbtt_GetCodepointKernAdvance(long var0, int var2, int var3);

    public static int stbtt_GetCodepointKernAdvance(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int ch1, int ch2) {
        return STBTruetype.nstbtt_GetCodepointKernAdvance(info.address(), ch1, ch2);
    }

    public static native int nstbtt_GetCodepointBox(long var0, int var2, long var3, long var5, long var7, long var9);

    @NativeType(value="int")
    public static boolean stbtt_GetCodepointBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int codepoint, @NativeType(value="int *") @Nullable IntBuffer x0, @NativeType(value="int *") @Nullable IntBuffer y0, @NativeType(value="int *") @Nullable IntBuffer x1, @NativeType(value="int *") @Nullable IntBuffer y1) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)x0, 1);
            Checks.checkSafe((Buffer)y0, 1);
            Checks.checkSafe((Buffer)x1, 1);
            Checks.checkSafe((Buffer)y1, 1);
        }
        return STBTruetype.nstbtt_GetCodepointBox(info.address(), codepoint, MemoryUtil.memAddressSafe(x0), MemoryUtil.memAddressSafe(y0), MemoryUtil.memAddressSafe(x1), MemoryUtil.memAddressSafe(y1)) != 0;
    }

    public static native void nstbtt_GetGlyphHMetrics(long var0, int var2, long var3, long var5);

    public static void stbtt_GetGlyphHMetrics(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int glyph_index, @NativeType(value="int *") @Nullable IntBuffer advanceWidth, @NativeType(value="int *") @Nullable IntBuffer leftSideBearing) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)advanceWidth, 1);
            Checks.checkSafe((Buffer)leftSideBearing, 1);
        }
        STBTruetype.nstbtt_GetGlyphHMetrics(info.address(), glyph_index, MemoryUtil.memAddressSafe(advanceWidth), MemoryUtil.memAddressSafe(leftSideBearing));
    }

    public static native int nstbtt_GetGlyphKernAdvance(long var0, int var2, int var3);

    public static int stbtt_GetGlyphKernAdvance(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int glyph1, int glyph2) {
        return STBTruetype.nstbtt_GetGlyphKernAdvance(info.address(), glyph1, glyph2);
    }

    public static native int nstbtt_GetGlyphBox(long var0, int var2, long var3, long var5, long var7, long var9);

    @NativeType(value="int")
    public static boolean stbtt_GetGlyphBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int glyph_index, @NativeType(value="int *") @Nullable IntBuffer x0, @NativeType(value="int *") @Nullable IntBuffer y0, @NativeType(value="int *") @Nullable IntBuffer x1, @NativeType(value="int *") @Nullable IntBuffer y1) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)x0, 1);
            Checks.checkSafe((Buffer)y0, 1);
            Checks.checkSafe((Buffer)x1, 1);
            Checks.checkSafe((Buffer)y1, 1);
        }
        return STBTruetype.nstbtt_GetGlyphBox(info.address(), glyph_index, MemoryUtil.memAddressSafe(x0), MemoryUtil.memAddressSafe(y0), MemoryUtil.memAddressSafe(x1), MemoryUtil.memAddressSafe(y1)) != 0;
    }

    public static native int nstbtt_GetKerningTableLength(long var0);

    public static int stbtt_GetKerningTableLength(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info) {
        return STBTruetype.nstbtt_GetKerningTableLength(info.address());
    }

    public static native int nstbtt_GetKerningTable(long var0, long var2, int var4);

    public static int stbtt_GetKerningTable(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="stbtt_kerningentry *") STBTTKerningentry.Buffer table) {
        return STBTruetype.nstbtt_GetKerningTable(info.address(), table.address(), table.remaining());
    }

    public static native int nstbtt_IsGlyphEmpty(long var0, int var2);

    @NativeType(value="int")
    public static boolean stbtt_IsGlyphEmpty(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int glyph_index) {
        return STBTruetype.nstbtt_IsGlyphEmpty(info.address(), glyph_index) != 0;
    }

    public static native int nstbtt_GetCodepointShape(long var0, int var2, long var3);

    public static int stbtt_GetCodepointShape(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int unicode_codepoint, @NativeType(value="stbtt_vertex **") PointerBuffer vertices) {
        if (Checks.CHECKS) {
            Checks.check(vertices, 1);
        }
        return STBTruetype.nstbtt_GetCodepointShape(info.address(), unicode_codepoint, MemoryUtil.memAddress(vertices));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="int")
    public static @Nullable STBTTVertex.Buffer stbtt_GetCodepointShape(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int unicode_codepoint) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            PointerBuffer vertices = stack.pointers(0L);
            int __result = STBTruetype.nstbtt_GetCodepointShape(info.address(), unicode_codepoint, MemoryUtil.memAddress(vertices));
            STBTTVertex.Buffer buffer = STBTTVertex.createSafe(vertices.get(0), __result);
            return buffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstbtt_GetGlyphShape(long var0, int var2, long var3);

    public static int stbtt_GetGlyphShape(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int glyph_index, @NativeType(value="stbtt_vertex **") PointerBuffer vertices) {
        if (Checks.CHECKS) {
            Checks.check(vertices, 1);
        }
        return STBTruetype.nstbtt_GetGlyphShape(info.address(), glyph_index, MemoryUtil.memAddress(vertices));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="int")
    public static @Nullable STBTTVertex.Buffer stbtt_GetGlyphShape(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int glyph_index) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            PointerBuffer vertices = stack.pointers(0L);
            int __result = STBTruetype.nstbtt_GetGlyphShape(info.address(), glyph_index, MemoryUtil.memAddress(vertices));
            STBTTVertex.Buffer buffer = STBTTVertex.createSafe(vertices.get(0), __result);
            return buffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native void nstbtt_FreeShape(long var0, long var2);

    public static void stbtt_FreeShape(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="stbtt_vertex *") STBTTVertex.Buffer vertices) {
        if (Checks.CHECKS) {
            Checks.check(vertices, 1);
        }
        STBTruetype.nstbtt_FreeShape(info.address(), vertices.address());
    }

    public static native long nstbtt_FindSVGDoc(long var0, int var2);

    @NativeType(value="unsigned char *")
    public static long stbtt_FindSVGDoc(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int gl) {
        return STBTruetype.nstbtt_FindSVGDoc(info.address(), gl);
    }

    public static native int nstbtt_GetCodepointSVG(long var0, int var2, long var3);

    public static int stbtt_GetCodepointSVG(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int unicode_codepoint, @NativeType(value="char const **") PointerBuffer svg) {
        if (Checks.CHECKS) {
            Checks.check(svg, 1);
        }
        return STBTruetype.nstbtt_GetCodepointSVG(info.address(), unicode_codepoint, MemoryUtil.memAddress(svg));
    }

    public static native int nstbtt_GetGlyphSVG(long var0, int var2, long var3);

    public static int stbtt_GetGlyphSVG(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int gl, @NativeType(value="char const **") PointerBuffer svg) {
        if (Checks.CHECKS) {
            Checks.check(svg, 1);
        }
        return STBTruetype.nstbtt_GetGlyphSVG(info.address(), gl, MemoryUtil.memAddress(svg));
    }

    public static native void nstbtt_FreeBitmap(long var0, long var2);

    public static void stbtt_FreeBitmap(@NativeType(value="unsigned char *") ByteBuffer bitmap, @NativeType(value="void *") long userdata) {
        STBTruetype.nstbtt_FreeBitmap(MemoryUtil.memAddress(bitmap), userdata);
    }

    public static void stbtt_FreeBitmap(@NativeType(value="unsigned char *") ByteBuffer bitmap) {
        STBTruetype.nstbtt_FreeBitmap(MemoryUtil.memAddress(bitmap), 0L);
    }

    public static native long nstbtt_GetCodepointBitmap(long var0, float var2, float var3, int var4, long var5, long var7, long var9, long var11);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetCodepointBitmap(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float scale_x, float scale_y, int codepoint, @NativeType(value="int *") IntBuffer width, @NativeType(value="int *") IntBuffer height, @NativeType(value="int *") @Nullable IntBuffer xoff, @NativeType(value="int *") @Nullable IntBuffer yoff) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)width, 1);
            Checks.check((Buffer)height, 1);
            Checks.checkSafe((Buffer)xoff, 1);
            Checks.checkSafe((Buffer)yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetCodepointBitmap(info.address(), scale_x, scale_y, codepoint, MemoryUtil.memAddress(width), MemoryUtil.memAddress(height), MemoryUtil.memAddressSafe(xoff), MemoryUtil.memAddressSafe(yoff));
        return MemoryUtil.memByteBufferSafe(__result, width.get(width.position()) * height.get(height.position()));
    }

    public static native long nstbtt_GetCodepointBitmapSubpixel(long var0, float var2, float var3, float var4, float var5, int var6, long var7, long var9, long var11, long var13);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetCodepointBitmapSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float scale_x, float scale_y, float shift_x, float shift_y, int codepoint, @NativeType(value="int *") IntBuffer width, @NativeType(value="int *") IntBuffer height, @NativeType(value="int *") @Nullable IntBuffer xoff, @NativeType(value="int *") @Nullable IntBuffer yoff) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)width, 1);
            Checks.check((Buffer)height, 1);
            Checks.checkSafe((Buffer)xoff, 1);
            Checks.checkSafe((Buffer)yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetCodepointBitmapSubpixel(info.address(), scale_x, scale_y, shift_x, shift_y, codepoint, MemoryUtil.memAddress(width), MemoryUtil.memAddress(height), MemoryUtil.memAddressSafe(xoff), MemoryUtil.memAddressSafe(yoff));
        return MemoryUtil.memByteBufferSafe(__result, width.get(width.position()) * height.get(height.position()));
    }

    public static native void nstbtt_MakeCodepointBitmap(long var0, long var2, int var4, int var5, int var6, float var7, float var8, int var9);

    public static void stbtt_MakeCodepointBitmap(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="unsigned char *") ByteBuffer output, int out_w, int out_h, int out_stride, float scale_x, float scale_y, int codepoint) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)output, (out_stride != 0 ? out_stride : out_w) * out_h);
        }
        STBTruetype.nstbtt_MakeCodepointBitmap(info.address(), MemoryUtil.memAddress(output), out_w, out_h, out_stride, scale_x, scale_y, codepoint);
    }

    public static native void nstbtt_MakeCodepointBitmapSubpixel(long var0, long var2, int var4, int var5, int var6, float var7, float var8, float var9, float var10, int var11);

    public static void stbtt_MakeCodepointBitmapSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="unsigned char *") ByteBuffer output, int out_w, int out_h, int out_stride, float scale_x, float scale_y, float shift_x, float shift_y, int codepoint) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)output, (out_stride != 0 ? out_stride : out_w) * out_h);
        }
        STBTruetype.nstbtt_MakeCodepointBitmapSubpixel(info.address(), MemoryUtil.memAddress(output), out_w, out_h, out_stride, scale_x, scale_y, shift_x, shift_y, codepoint);
    }

    public static native void nstbtt_MakeCodepointBitmapSubpixelPrefilter(long var0, long var2, int var4, int var5, int var6, float var7, float var8, float var9, float var10, int var11, int var12, long var13, long var15, int var17);

    public static void stbtt_MakeCodepointBitmapSubpixelPrefilter(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="unsigned char *") ByteBuffer output, int out_w, int out_h, int out_stride, float scale_x, float scale_y, float shift_x, float shift_y, int oversample_x, int oversample_y, @NativeType(value="float *") FloatBuffer sub_x, @NativeType(value="float *") FloatBuffer sub_y, int codepoint) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)output, (out_stride != 0 ? out_stride : out_w) * out_h);
            Checks.check((Buffer)sub_x, 1);
            Checks.check((Buffer)sub_y, 1);
        }
        STBTruetype.nstbtt_MakeCodepointBitmapSubpixelPrefilter(info.address(), MemoryUtil.memAddress(output), out_w, out_h, out_stride, scale_x, scale_y, shift_x, shift_y, oversample_x, oversample_y, MemoryUtil.memAddress(sub_x), MemoryUtil.memAddress(sub_y), codepoint);
    }

    public static native void nstbtt_GetCodepointBitmapBox(long var0, int var2, float var3, float var4, long var5, long var7, long var9, long var11);

    public static void stbtt_GetCodepointBitmapBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int codepoint, float scale_x, float scale_y, @NativeType(value="int *") @Nullable IntBuffer ix0, @NativeType(value="int *") @Nullable IntBuffer iy0, @NativeType(value="int *") @Nullable IntBuffer ix1, @NativeType(value="int *") @Nullable IntBuffer iy1) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)ix0, 1);
            Checks.checkSafe((Buffer)iy0, 1);
            Checks.checkSafe((Buffer)ix1, 1);
            Checks.checkSafe((Buffer)iy1, 1);
        }
        STBTruetype.nstbtt_GetCodepointBitmapBox(font.address(), codepoint, scale_x, scale_y, MemoryUtil.memAddressSafe(ix0), MemoryUtil.memAddressSafe(iy0), MemoryUtil.memAddressSafe(ix1), MemoryUtil.memAddressSafe(iy1));
    }

    public static native void nstbtt_GetCodepointBitmapBoxSubpixel(long var0, int var2, float var3, float var4, float var5, float var6, long var7, long var9, long var11, long var13);

    public static void stbtt_GetCodepointBitmapBoxSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int codepoint, float scale_x, float scale_y, float shift_x, float shift_y, @NativeType(value="int *") @Nullable IntBuffer ix0, @NativeType(value="int *") @Nullable IntBuffer iy0, @NativeType(value="int *") @Nullable IntBuffer ix1, @NativeType(value="int *") @Nullable IntBuffer iy1) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)ix0, 1);
            Checks.checkSafe((Buffer)iy0, 1);
            Checks.checkSafe((Buffer)ix1, 1);
            Checks.checkSafe((Buffer)iy1, 1);
        }
        STBTruetype.nstbtt_GetCodepointBitmapBoxSubpixel(font.address(), codepoint, scale_x, scale_y, shift_x, shift_y, MemoryUtil.memAddressSafe(ix0), MemoryUtil.memAddressSafe(iy0), MemoryUtil.memAddressSafe(ix1), MemoryUtil.memAddressSafe(iy1));
    }

    public static native long nstbtt_GetGlyphBitmap(long var0, float var2, float var3, int var4, long var5, long var7, long var9, long var11);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetGlyphBitmap(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float scale_x, float scale_y, int glyph, @NativeType(value="int *") IntBuffer width, @NativeType(value="int *") IntBuffer height, @NativeType(value="int *") @Nullable IntBuffer xoff, @NativeType(value="int *") @Nullable IntBuffer yoff) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)width, 1);
            Checks.check((Buffer)height, 1);
            Checks.checkSafe((Buffer)xoff, 1);
            Checks.checkSafe((Buffer)yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetGlyphBitmap(info.address(), scale_x, scale_y, glyph, MemoryUtil.memAddress(width), MemoryUtil.memAddress(height), MemoryUtil.memAddressSafe(xoff), MemoryUtil.memAddressSafe(yoff));
        return MemoryUtil.memByteBufferSafe(__result, width.get(width.position()) * height.get(height.position()));
    }

    public static native long nstbtt_GetGlyphBitmapSubpixel(long var0, float var2, float var3, float var4, float var5, int var6, long var7, long var9, long var11, long var13);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetGlyphBitmapSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float scale_x, float scale_y, float shift_x, float shift_y, int glyph, @NativeType(value="int *") IntBuffer width, @NativeType(value="int *") IntBuffer height, @NativeType(value="int *") @Nullable IntBuffer xoff, @NativeType(value="int *") @Nullable IntBuffer yoff) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)width, 1);
            Checks.check((Buffer)height, 1);
            Checks.checkSafe((Buffer)xoff, 1);
            Checks.checkSafe((Buffer)yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetGlyphBitmapSubpixel(info.address(), scale_x, scale_y, shift_x, shift_y, glyph, MemoryUtil.memAddress(width), MemoryUtil.memAddress(height), MemoryUtil.memAddressSafe(xoff), MemoryUtil.memAddressSafe(yoff));
        return MemoryUtil.memByteBufferSafe(__result, width.get(width.position()) * height.get(height.position()));
    }

    public static native void nstbtt_MakeGlyphBitmap(long var0, long var2, int var4, int var5, int var6, float var7, float var8, int var9);

    public static void stbtt_MakeGlyphBitmap(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="unsigned char *") ByteBuffer output, int out_w, int out_h, int out_stride, float scale_x, float scale_y, int glyph) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)output, (out_stride != 0 ? out_stride : out_w) * out_h);
        }
        STBTruetype.nstbtt_MakeGlyphBitmap(info.address(), MemoryUtil.memAddress(output), out_w, out_h, out_stride, scale_x, scale_y, glyph);
    }

    public static native void nstbtt_MakeGlyphBitmapSubpixel(long var0, long var2, int var4, int var5, int var6, float var7, float var8, float var9, float var10, int var11);

    public static void stbtt_MakeGlyphBitmapSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="unsigned char *") ByteBuffer output, int out_w, int out_h, int out_stride, float scale_x, float scale_y, float shift_x, float shift_y, int glyph) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)output, (out_stride != 0 ? out_stride : out_w) * out_h);
        }
        STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(info.address(), MemoryUtil.memAddress(output), out_w, out_h, out_stride, scale_x, scale_y, shift_x, shift_y, glyph);
    }

    public static native void nstbtt_MakeGlyphBitmapSubpixelPrefilter(long var0, long var2, int var4, int var5, int var6, float var7, float var8, float var9, float var10, int var11, int var12, long var13, long var15, int var17);

    public static void stbtt_MakeGlyphBitmapSubpixelPrefilter(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="unsigned char *") ByteBuffer output, int out_w, int out_h, int out_stride, float scale_x, float scale_y, float shift_x, float shift_y, int oversample_x, int oversample_y, @NativeType(value="float *") FloatBuffer sub_x, @NativeType(value="float *") FloatBuffer sub_y, int glyph) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)output, (out_stride != 0 ? out_stride : out_w) * out_h);
            Checks.check((Buffer)sub_x, 1);
            Checks.check((Buffer)sub_y, 1);
        }
        STBTruetype.nstbtt_MakeGlyphBitmapSubpixelPrefilter(info.address(), MemoryUtil.memAddress(output), out_w, out_h, out_stride, scale_x, scale_y, shift_x, shift_y, oversample_x, oversample_y, MemoryUtil.memAddress(sub_x), MemoryUtil.memAddress(sub_y), glyph);
    }

    public static native void nstbtt_GetGlyphBitmapBox(long var0, int var2, float var3, float var4, long var5, long var7, long var9, long var11);

    public static void stbtt_GetGlyphBitmapBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int glyph, float scale_x, float scale_y, @NativeType(value="int *") @Nullable IntBuffer ix0, @NativeType(value="int *") @Nullable IntBuffer iy0, @NativeType(value="int *") @Nullable IntBuffer ix1, @NativeType(value="int *") @Nullable IntBuffer iy1) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)ix0, 1);
            Checks.checkSafe((Buffer)iy0, 1);
            Checks.checkSafe((Buffer)ix1, 1);
            Checks.checkSafe((Buffer)iy1, 1);
        }
        STBTruetype.nstbtt_GetGlyphBitmapBox(font.address(), glyph, scale_x, scale_y, MemoryUtil.memAddressSafe(ix0), MemoryUtil.memAddressSafe(iy0), MemoryUtil.memAddressSafe(ix1), MemoryUtil.memAddressSafe(iy1));
    }

    public static native void nstbtt_GetGlyphBitmapBoxSubpixel(long var0, int var2, float var3, float var4, float var5, float var6, long var7, long var9, long var11, long var13);

    public static void stbtt_GetGlyphBitmapBoxSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int glyph, float scale_x, float scale_y, float shift_x, float shift_y, @NativeType(value="int *") @Nullable IntBuffer ix0, @NativeType(value="int *") @Nullable IntBuffer iy0, @NativeType(value="int *") @Nullable IntBuffer ix1, @NativeType(value="int *") @Nullable IntBuffer iy1) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)ix0, 1);
            Checks.checkSafe((Buffer)iy0, 1);
            Checks.checkSafe((Buffer)ix1, 1);
            Checks.checkSafe((Buffer)iy1, 1);
        }
        STBTruetype.nstbtt_GetGlyphBitmapBoxSubpixel(font.address(), glyph, scale_x, scale_y, shift_x, shift_y, MemoryUtil.memAddressSafe(ix0), MemoryUtil.memAddressSafe(iy0), MemoryUtil.memAddressSafe(ix1), MemoryUtil.memAddressSafe(iy1));
    }

    public static native void nstbtt_Rasterize(long var0, float var2, long var3, int var5, float var6, float var7, float var8, float var9, int var10, int var11, int var12, long var13);

    public static void stbtt_Rasterize(@NativeType(value="stbtt__bitmap *") STBTTBitmap result, float flatness_in_pixels, @NativeType(value="stbtt_vertex *") STBTTVertex.Buffer vertices, float scale_x, float scale_y, float shift_x, float shift_y, int x_off, int y_off, @NativeType(value="int") boolean invert) {
        STBTruetype.nstbtt_Rasterize(result.address(), flatness_in_pixels, vertices.address(), vertices.remaining(), scale_x, scale_y, shift_x, shift_y, x_off, y_off, invert ? 1 : 0, 0L);
    }

    public static native void nstbtt_FreeSDF(long var0, long var2);

    public static void stbtt_FreeSDF(@NativeType(value="unsigned char *") ByteBuffer bitmap, @NativeType(value="void *") long userdata) {
        STBTruetype.nstbtt_FreeSDF(MemoryUtil.memAddress(bitmap), userdata);
    }

    public static void stbtt_FreeSDF(@NativeType(value="unsigned char *") ByteBuffer bitmap) {
        STBTruetype.nstbtt_FreeSDF(MemoryUtil.memAddress(bitmap), 0L);
    }

    public static native long nstbtt_GetGlyphSDF(long var0, float var2, int var3, int var4, byte var5, float var6, long var7, long var9, long var11, long var13);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetGlyphSDF(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, float scale, int glyph, int padding, @NativeType(value="unsigned char") byte onedge_value, float pixel_dist_scale, @NativeType(value="int *") IntBuffer width, @NativeType(value="int *") IntBuffer height, @NativeType(value="int *") IntBuffer xoff, @NativeType(value="int *") IntBuffer yoff) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)width, 1);
            Checks.check((Buffer)height, 1);
            Checks.check((Buffer)xoff, 1);
            Checks.check((Buffer)yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetGlyphSDF(font.address(), scale, glyph, padding, onedge_value, pixel_dist_scale, MemoryUtil.memAddress(width), MemoryUtil.memAddress(height), MemoryUtil.memAddress(xoff), MemoryUtil.memAddress(yoff));
        return MemoryUtil.memByteBufferSafe(__result, width.get(width.position()) * height.get(height.position()));
    }

    public static native long nstbtt_GetCodepointSDF(long var0, float var2, int var3, int var4, byte var5, float var6, long var7, long var9, long var11, long var13);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetCodepointSDF(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, float scale, int codepoint, int padding, @NativeType(value="unsigned char") byte onedge_value, float pixel_dist_scale, @NativeType(value="int *") IntBuffer width, @NativeType(value="int *") IntBuffer height, @NativeType(value="int *") IntBuffer xoff, @NativeType(value="int *") IntBuffer yoff) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)width, 1);
            Checks.check((Buffer)height, 1);
            Checks.check((Buffer)xoff, 1);
            Checks.check((Buffer)yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetCodepointSDF(font.address(), scale, codepoint, padding, onedge_value, pixel_dist_scale, MemoryUtil.memAddress(width), MemoryUtil.memAddress(height), MemoryUtil.memAddress(xoff), MemoryUtil.memAddress(yoff));
        return MemoryUtil.memByteBufferSafe(__result, width.get(width.position()) * height.get(height.position()));
    }

    public static native int nstbtt_FindMatchingFont(long var0, long var2, int var4);

    public static int stbtt_FindMatchingFont(@NativeType(value="unsigned char const *") ByteBuffer fontdata, @NativeType(value="char const *") ByteBuffer name, int flags) {
        if (Checks.CHECKS) {
            Checks.checkNT1(name);
        }
        return STBTruetype.nstbtt_FindMatchingFont(MemoryUtil.memAddress(fontdata), MemoryUtil.memAddress(name), flags);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int stbtt_FindMatchingFont(@NativeType(value="unsigned char const *") ByteBuffer fontdata, @NativeType(value="char const *") CharSequence name, int flags) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(name, true);
            long nameEncoded = stack.getPointerAddress();
            int n = STBTruetype.nstbtt_FindMatchingFont(MemoryUtil.memAddress(fontdata), nameEncoded, flags);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native int nstbtt_CompareUTF8toUTF16_bigendian(long var0, int var2, long var3, int var5);

    @NativeType(value="int")
    public static boolean stbtt_CompareUTF8toUTF16_bigendian(@NativeType(value="char const *") ByteBuffer s1, @NativeType(value="char const *") ByteBuffer s2) {
        return STBTruetype.nstbtt_CompareUTF8toUTF16_bigendian(MemoryUtil.memAddress(s1), s1.remaining(), MemoryUtil.memAddress(s2), s2.remaining()) != 0;
    }

    public static native long nstbtt_GetFontNameString(long var0, long var2, int var4, int var5, int var6, int var7);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="char const *")
    public static @Nullable ByteBuffer stbtt_GetFontNameString(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int platformID, int encodingID, int languageID, int nameID) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        IntBuffer length = stack.callocInt(1);
        try {
            long __result = STBTruetype.nstbtt_GetFontNameString(font.address(), MemoryUtil.memAddress(length), platformID, encodingID, languageID, nameID);
            ByteBuffer byteBuffer = MemoryUtil.memByteBufferSafe(__result, length.get(0));
            return byteBuffer;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static native void nstbtt_GetBakedQuad(long var0, int var2, int var3, int var4, float[] var5, float[] var6, long var7, int var9);

    public static void stbtt_GetBakedQuad(@NativeType(value="stbtt_bakedchar const *") STBTTBakedChar.Buffer chardata, int pw, int ph, int char_index, @NativeType(value="float *") float[] xpos, @NativeType(value="float *") float[] ypos, @NativeType(value="stbtt_aligned_quad *") STBTTAlignedQuad q, @NativeType(value="int") boolean opengl_fillrule) {
        if (Checks.CHECKS) {
            Checks.check(chardata, char_index + 1);
            Checks.check(xpos, 1);
            Checks.check(ypos, 1);
        }
        STBTruetype.nstbtt_GetBakedQuad(chardata.address(), pw, ph, char_index, xpos, ypos, q.address(), opengl_fillrule ? 1 : 0);
    }

    public static native void nstbtt_GetScaledFontVMetrics(long var0, int var2, float var3, float[] var4, float[] var5, float[] var6);

    public static void stbtt_GetScaledFontVMetrics(@NativeType(value="unsigned char const *") ByteBuffer fontdata, int index, float size, @NativeType(value="float *") float[] ascent, @NativeType(value="float *") float[] descent, @NativeType(value="float *") float[] lineGap) {
        if (Checks.CHECKS) {
            Checks.check(ascent, 1);
            Checks.check(descent, 1);
            Checks.check(lineGap, 1);
        }
        STBTruetype.nstbtt_GetScaledFontVMetrics(MemoryUtil.memAddress(fontdata), index, size, ascent, descent, lineGap);
    }

    public static native void nstbtt_GetPackedQuad(long var0, int var2, int var3, int var4, float[] var5, float[] var6, long var7, int var9);

    public static void stbtt_GetPackedQuad(@NativeType(value="stbtt_packedchar const *") STBTTPackedchar.Buffer chardata, int pw, int ph, int char_index, @NativeType(value="float *") float[] xpos, @NativeType(value="float *") float[] ypos, @NativeType(value="stbtt_aligned_quad *") STBTTAlignedQuad q, @NativeType(value="int") boolean align_to_integer) {
        if (Checks.CHECKS) {
            Checks.check(chardata, char_index + 1);
            Checks.check(xpos, 1);
            Checks.check(ypos, 1);
        }
        STBTruetype.nstbtt_GetPackedQuad(chardata.address(), pw, ph, char_index, xpos, ypos, q.address(), align_to_integer ? 1 : 0);
    }

    public static native void nstbtt_GetFontVMetrics(long var0, int[] var2, int[] var3, int[] var4);

    public static void stbtt_GetFontVMetrics(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="int *") int @Nullable [] ascent, @NativeType(value="int *") int @Nullable [] descent, @NativeType(value="int *") int @Nullable [] lineGap) {
        if (Checks.CHECKS) {
            Checks.checkSafe(ascent, 1);
            Checks.checkSafe(descent, 1);
            Checks.checkSafe(lineGap, 1);
        }
        STBTruetype.nstbtt_GetFontVMetrics(info.address(), ascent, descent, lineGap);
    }

    public static native int nstbtt_GetFontVMetricsOS2(long var0, int[] var2, int[] var3, int[] var4);

    @NativeType(value="int")
    public static boolean stbtt_GetFontVMetricsOS2(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="int *") int @Nullable [] typoAscent, @NativeType(value="int *") int @Nullable [] typoDescent, @NativeType(value="int *") int @Nullable [] typoLineGap) {
        if (Checks.CHECKS) {
            Checks.checkSafe(typoAscent, 1);
            Checks.checkSafe(typoDescent, 1);
            Checks.checkSafe(typoLineGap, 1);
        }
        return STBTruetype.nstbtt_GetFontVMetricsOS2(info.address(), typoAscent, typoDescent, typoLineGap) != 0;
    }

    public static native void nstbtt_GetFontBoundingBox(long var0, int[] var2, int[] var3, int[] var4, int[] var5);

    public static void stbtt_GetFontBoundingBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="int *") int[] x0, @NativeType(value="int *") int[] y0, @NativeType(value="int *") int[] x1, @NativeType(value="int *") int[] y1) {
        if (Checks.CHECKS) {
            Checks.check(x0, 1);
            Checks.check(y0, 1);
            Checks.check(x1, 1);
            Checks.check(y1, 1);
        }
        STBTruetype.nstbtt_GetFontBoundingBox(info.address(), x0, y0, x1, y1);
    }

    public static native void nstbtt_GetCodepointHMetrics(long var0, int var2, int[] var3, int[] var4);

    public static void stbtt_GetCodepointHMetrics(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int codepoint, @NativeType(value="int *") int @Nullable [] advanceWidth, @NativeType(value="int *") int @Nullable [] leftSideBearing) {
        if (Checks.CHECKS) {
            Checks.checkSafe(advanceWidth, 1);
            Checks.checkSafe(leftSideBearing, 1);
        }
        STBTruetype.nstbtt_GetCodepointHMetrics(info.address(), codepoint, advanceWidth, leftSideBearing);
    }

    public static native int nstbtt_GetCodepointBox(long var0, int var2, int[] var3, int[] var4, int[] var5, int[] var6);

    @NativeType(value="int")
    public static boolean stbtt_GetCodepointBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int codepoint, @NativeType(value="int *") int @Nullable [] x0, @NativeType(value="int *") int @Nullable [] y0, @NativeType(value="int *") int @Nullable [] x1, @NativeType(value="int *") int @Nullable [] y1) {
        if (Checks.CHECKS) {
            Checks.checkSafe(x0, 1);
            Checks.checkSafe(y0, 1);
            Checks.checkSafe(x1, 1);
            Checks.checkSafe(y1, 1);
        }
        return STBTruetype.nstbtt_GetCodepointBox(info.address(), codepoint, x0, y0, x1, y1) != 0;
    }

    public static native void nstbtt_GetGlyphHMetrics(long var0, int var2, int[] var3, int[] var4);

    public static void stbtt_GetGlyphHMetrics(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int glyph_index, @NativeType(value="int *") int @Nullable [] advanceWidth, @NativeType(value="int *") int @Nullable [] leftSideBearing) {
        if (Checks.CHECKS) {
            Checks.checkSafe(advanceWidth, 1);
            Checks.checkSafe(leftSideBearing, 1);
        }
        STBTruetype.nstbtt_GetGlyphHMetrics(info.address(), glyph_index, advanceWidth, leftSideBearing);
    }

    public static native int nstbtt_GetGlyphBox(long var0, int var2, int[] var3, int[] var4, int[] var5, int[] var6);

    @NativeType(value="int")
    public static boolean stbtt_GetGlyphBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, int glyph_index, @NativeType(value="int *") int @Nullable [] x0, @NativeType(value="int *") int @Nullable [] y0, @NativeType(value="int *") int @Nullable [] x1, @NativeType(value="int *") int @Nullable [] y1) {
        if (Checks.CHECKS) {
            Checks.checkSafe(x0, 1);
            Checks.checkSafe(y0, 1);
            Checks.checkSafe(x1, 1);
            Checks.checkSafe(y1, 1);
        }
        return STBTruetype.nstbtt_GetGlyphBox(info.address(), glyph_index, x0, y0, x1, y1) != 0;
    }

    public static native long nstbtt_GetCodepointBitmap(long var0, float var2, float var3, int var4, int[] var5, int[] var6, int[] var7, int[] var8);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetCodepointBitmap(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float scale_x, float scale_y, int codepoint, @NativeType(value="int *") int[] width, @NativeType(value="int *") int[] height, @NativeType(value="int *") int @Nullable [] xoff, @NativeType(value="int *") int @Nullable [] yoff) {
        if (Checks.CHECKS) {
            Checks.check(width, 1);
            Checks.check(height, 1);
            Checks.checkSafe(xoff, 1);
            Checks.checkSafe(yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetCodepointBitmap(info.address(), scale_x, scale_y, codepoint, width, height, xoff, yoff);
        return MemoryUtil.memByteBufferSafe(__result, width[0] * height[0]);
    }

    public static native long nstbtt_GetCodepointBitmapSubpixel(long var0, float var2, float var3, float var4, float var5, int var6, int[] var7, int[] var8, int[] var9, int[] var10);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetCodepointBitmapSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float scale_x, float scale_y, float shift_x, float shift_y, int codepoint, @NativeType(value="int *") int[] width, @NativeType(value="int *") int[] height, @NativeType(value="int *") int @Nullable [] xoff, @NativeType(value="int *") int @Nullable [] yoff) {
        if (Checks.CHECKS) {
            Checks.check(width, 1);
            Checks.check(height, 1);
            Checks.checkSafe(xoff, 1);
            Checks.checkSafe(yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetCodepointBitmapSubpixel(info.address(), scale_x, scale_y, shift_x, shift_y, codepoint, width, height, xoff, yoff);
        return MemoryUtil.memByteBufferSafe(__result, width[0] * height[0]);
    }

    public static native void nstbtt_MakeCodepointBitmapSubpixelPrefilter(long var0, long var2, int var4, int var5, int var6, float var7, float var8, float var9, float var10, int var11, int var12, float[] var13, float[] var14, int var15);

    public static void stbtt_MakeCodepointBitmapSubpixelPrefilter(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="unsigned char *") ByteBuffer output, int out_w, int out_h, int out_stride, float scale_x, float scale_y, float shift_x, float shift_y, int oversample_x, int oversample_y, @NativeType(value="float *") float[] sub_x, @NativeType(value="float *") float[] sub_y, int codepoint) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)output, (out_stride != 0 ? out_stride : out_w) * out_h);
            Checks.check(sub_x, 1);
            Checks.check(sub_y, 1);
        }
        STBTruetype.nstbtt_MakeCodepointBitmapSubpixelPrefilter(info.address(), MemoryUtil.memAddress(output), out_w, out_h, out_stride, scale_x, scale_y, shift_x, shift_y, oversample_x, oversample_y, sub_x, sub_y, codepoint);
    }

    public static native void nstbtt_GetCodepointBitmapBox(long var0, int var2, float var3, float var4, int[] var5, int[] var6, int[] var7, int[] var8);

    public static void stbtt_GetCodepointBitmapBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int codepoint, float scale_x, float scale_y, @NativeType(value="int *") int @Nullable [] ix0, @NativeType(value="int *") int @Nullable [] iy0, @NativeType(value="int *") int @Nullable [] ix1, @NativeType(value="int *") int @Nullable [] iy1) {
        if (Checks.CHECKS) {
            Checks.checkSafe(ix0, 1);
            Checks.checkSafe(iy0, 1);
            Checks.checkSafe(ix1, 1);
            Checks.checkSafe(iy1, 1);
        }
        STBTruetype.nstbtt_GetCodepointBitmapBox(font.address(), codepoint, scale_x, scale_y, ix0, iy0, ix1, iy1);
    }

    public static native void nstbtt_GetCodepointBitmapBoxSubpixel(long var0, int var2, float var3, float var4, float var5, float var6, int[] var7, int[] var8, int[] var9, int[] var10);

    public static void stbtt_GetCodepointBitmapBoxSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int codepoint, float scale_x, float scale_y, float shift_x, float shift_y, @NativeType(value="int *") int @Nullable [] ix0, @NativeType(value="int *") int @Nullable [] iy0, @NativeType(value="int *") int @Nullable [] ix1, @NativeType(value="int *") int @Nullable [] iy1) {
        if (Checks.CHECKS) {
            Checks.checkSafe(ix0, 1);
            Checks.checkSafe(iy0, 1);
            Checks.checkSafe(ix1, 1);
            Checks.checkSafe(iy1, 1);
        }
        STBTruetype.nstbtt_GetCodepointBitmapBoxSubpixel(font.address(), codepoint, scale_x, scale_y, shift_x, shift_y, ix0, iy0, ix1, iy1);
    }

    public static native long nstbtt_GetGlyphBitmap(long var0, float var2, float var3, int var4, int[] var5, int[] var6, int[] var7, int[] var8);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetGlyphBitmap(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float scale_x, float scale_y, int glyph, @NativeType(value="int *") int[] width, @NativeType(value="int *") int[] height, @NativeType(value="int *") int @Nullable [] xoff, @NativeType(value="int *") int @Nullable [] yoff) {
        if (Checks.CHECKS) {
            Checks.check(width, 1);
            Checks.check(height, 1);
            Checks.checkSafe(xoff, 1);
            Checks.checkSafe(yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetGlyphBitmap(info.address(), scale_x, scale_y, glyph, width, height, xoff, yoff);
        return MemoryUtil.memByteBufferSafe(__result, width[0] * height[0]);
    }

    public static native long nstbtt_GetGlyphBitmapSubpixel(long var0, float var2, float var3, float var4, float var5, int var6, int[] var7, int[] var8, int[] var9, int[] var10);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetGlyphBitmapSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, float scale_x, float scale_y, float shift_x, float shift_y, int glyph, @NativeType(value="int *") int[] width, @NativeType(value="int *") int[] height, @NativeType(value="int *") int @Nullable [] xoff, @NativeType(value="int *") int @Nullable [] yoff) {
        if (Checks.CHECKS) {
            Checks.check(width, 1);
            Checks.check(height, 1);
            Checks.checkSafe(xoff, 1);
            Checks.checkSafe(yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetGlyphBitmapSubpixel(info.address(), scale_x, scale_y, shift_x, shift_y, glyph, width, height, xoff, yoff);
        return MemoryUtil.memByteBufferSafe(__result, width[0] * height[0]);
    }

    public static native void nstbtt_MakeGlyphBitmapSubpixelPrefilter(long var0, long var2, int var4, int var5, int var6, float var7, float var8, float var9, float var10, int var11, int var12, float[] var13, float[] var14, int var15);

    public static void stbtt_MakeGlyphBitmapSubpixelPrefilter(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo info, @NativeType(value="unsigned char *") ByteBuffer output, int out_w, int out_h, int out_stride, float scale_x, float scale_y, float shift_x, float shift_y, int oversample_x, int oversample_y, @NativeType(value="float *") float[] sub_x, @NativeType(value="float *") float[] sub_y, int glyph) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)output, (out_stride != 0 ? out_stride : out_w) * out_h);
            Checks.check(sub_x, 1);
            Checks.check(sub_y, 1);
        }
        STBTruetype.nstbtt_MakeGlyphBitmapSubpixelPrefilter(info.address(), MemoryUtil.memAddress(output), out_w, out_h, out_stride, scale_x, scale_y, shift_x, shift_y, oversample_x, oversample_y, sub_x, sub_y, glyph);
    }

    public static native void nstbtt_GetGlyphBitmapBox(long var0, int var2, float var3, float var4, int[] var5, int[] var6, int[] var7, int[] var8);

    public static void stbtt_GetGlyphBitmapBox(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int glyph, float scale_x, float scale_y, @NativeType(value="int *") int @Nullable [] ix0, @NativeType(value="int *") int @Nullable [] iy0, @NativeType(value="int *") int @Nullable [] ix1, @NativeType(value="int *") int @Nullable [] iy1) {
        if (Checks.CHECKS) {
            Checks.checkSafe(ix0, 1);
            Checks.checkSafe(iy0, 1);
            Checks.checkSafe(ix1, 1);
            Checks.checkSafe(iy1, 1);
        }
        STBTruetype.nstbtt_GetGlyphBitmapBox(font.address(), glyph, scale_x, scale_y, ix0, iy0, ix1, iy1);
    }

    public static native void nstbtt_GetGlyphBitmapBoxSubpixel(long var0, int var2, float var3, float var4, float var5, float var6, int[] var7, int[] var8, int[] var9, int[] var10);

    public static void stbtt_GetGlyphBitmapBoxSubpixel(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, int glyph, float scale_x, float scale_y, float shift_x, float shift_y, @NativeType(value="int *") int @Nullable [] ix0, @NativeType(value="int *") int @Nullable [] iy0, @NativeType(value="int *") int @Nullable [] ix1, @NativeType(value="int *") int @Nullable [] iy1) {
        if (Checks.CHECKS) {
            Checks.checkSafe(ix0, 1);
            Checks.checkSafe(iy0, 1);
            Checks.checkSafe(ix1, 1);
            Checks.checkSafe(iy1, 1);
        }
        STBTruetype.nstbtt_GetGlyphBitmapBoxSubpixel(font.address(), glyph, scale_x, scale_y, shift_x, shift_y, ix0, iy0, ix1, iy1);
    }

    public static native long nstbtt_GetGlyphSDF(long var0, float var2, int var3, int var4, byte var5, float var6, int[] var7, int[] var8, int[] var9, int[] var10);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetGlyphSDF(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, float scale, int glyph, int padding, @NativeType(value="unsigned char") byte onedge_value, float pixel_dist_scale, @NativeType(value="int *") int[] width, @NativeType(value="int *") int[] height, @NativeType(value="int *") int[] xoff, @NativeType(value="int *") int[] yoff) {
        if (Checks.CHECKS) {
            Checks.check(width, 1);
            Checks.check(height, 1);
            Checks.check(xoff, 1);
            Checks.check(yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetGlyphSDF(font.address(), scale, glyph, padding, onedge_value, pixel_dist_scale, width, height, xoff, yoff);
        return MemoryUtil.memByteBufferSafe(__result, width[0] * height[0]);
    }

    public static native long nstbtt_GetCodepointSDF(long var0, float var2, int var3, int var4, byte var5, float var6, int[] var7, int[] var8, int[] var9, int[] var10);

    @NativeType(value="unsigned char *")
    public static @Nullable ByteBuffer stbtt_GetCodepointSDF(@NativeType(value="stbtt_fontinfo const *") STBTTFontinfo font, float scale, int codepoint, int padding, @NativeType(value="unsigned char") byte onedge_value, float pixel_dist_scale, @NativeType(value="int *") int[] width, @NativeType(value="int *") int[] height, @NativeType(value="int *") int[] xoff, @NativeType(value="int *") int[] yoff) {
        if (Checks.CHECKS) {
            Checks.check(width, 1);
            Checks.check(height, 1);
            Checks.check(xoff, 1);
            Checks.check(yoff, 1);
        }
        long __result = STBTruetype.nstbtt_GetCodepointSDF(font.address(), scale, codepoint, padding, onedge_value, pixel_dist_scale, width, height, xoff, yoff);
        return MemoryUtil.memByteBufferSafe(__result, width[0] * height[0]);
    }

    static {
        LibSTB.initialize();
    }
}


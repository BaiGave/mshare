/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.util.freetype;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.jspecify.annotations.Nullable;
import org.lwjgl.CLongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Checks;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.JNI;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.Platform;
import org.lwjgl.system.SharedLibrary;
import org.lwjgl.system.libffi.FFICIF;
import org.lwjgl.system.libffi.LibFFI;
import org.lwjgl.util.freetype.BDF_Property;
import org.lwjgl.util.freetype.FTC_Face_RequesterI;
import org.lwjgl.util.freetype.FTC_ImageType;
import org.lwjgl.util.freetype.FTC_Scaler;
import org.lwjgl.util.freetype.FT_BBox;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_COLR_Paint;
import org.lwjgl.util.freetype.FT_CharMap;
import org.lwjgl.util.freetype.FT_ClipBox;
import org.lwjgl.util.freetype.FT_Color;
import org.lwjgl.util.freetype.FT_ColorStop;
import org.lwjgl.util.freetype.FT_ColorStopIterator;
import org.lwjgl.util.freetype.FT_DebugHook_FuncI;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Glyph;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_LayerIterator;
import org.lwjgl.util.freetype.FT_List;
import org.lwjgl.util.freetype.FT_ListNode;
import org.lwjgl.util.freetype.FT_List_DestructorI;
import org.lwjgl.util.freetype.FT_List_IteratorI;
import org.lwjgl.util.freetype.FT_MM_Var;
import org.lwjgl.util.freetype.FT_Matrix;
import org.lwjgl.util.freetype.FT_Memory;
import org.lwjgl.util.freetype.FT_Module_Class;
import org.lwjgl.util.freetype.FT_Multi_Master;
import org.lwjgl.util.freetype.FT_OpaquePaint;
import org.lwjgl.util.freetype.FT_Open_Args;
import org.lwjgl.util.freetype.FT_Outline;
import org.lwjgl.util.freetype.FT_Outline_Funcs;
import org.lwjgl.util.freetype.FT_Palette_Data;
import org.lwjgl.util.freetype.FT_Parameter;
import org.lwjgl.util.freetype.FT_Raster_Params;
import org.lwjgl.util.freetype.FT_SfntLangTag;
import org.lwjgl.util.freetype.FT_SfntName;
import org.lwjgl.util.freetype.FT_Size;
import org.lwjgl.util.freetype.FT_Size_Request;
import org.lwjgl.util.freetype.FT_Stream;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.PS_FontInfo;

public class FreeType {
    private static final SharedLibrary FREETYPE = Library.loadNative(FreeType.class, "org.lwjgl.freetype", Configuration.FREETYPE_LIBRARY_NAME.get(Platform.mapLibraryNameBundled("freetype")), true);
    public static final int FT_ENCODING_NONE = FreeType.FT_ENC_TAG(0, 0, 0, 0);
    public static final int FT_ENCODING_MS_SYMBOL = FreeType.FT_ENC_TAG(115, 121, 109, 98);
    public static final int FT_ENCODING_UNICODE = FreeType.FT_ENC_TAG(117, 110, 105, 99);
    public static final int FT_ENCODING_SJIS = FreeType.FT_ENC_TAG(115, 106, 105, 115);
    public static final int FT_ENCODING_PRC = FreeType.FT_ENC_TAG(103, 98, 32, 32);
    public static final int FT_ENCODING_BIG5 = FreeType.FT_ENC_TAG(98, 105, 103, 53);
    public static final int FT_ENCODING_WANSUNG = FreeType.FT_ENC_TAG(119, 97, 110, 115);
    public static final int FT_ENCODING_JOHAB = FreeType.FT_ENC_TAG(106, 111, 104, 97);
    public static final int FT_ENCODING_GB2312 = FT_ENCODING_PRC;
    public static final int FT_ENCODING_MS_SJIS = FT_ENCODING_SJIS;
    public static final int FT_ENCODING_MS_GB2312 = FT_ENCODING_PRC;
    public static final int FT_ENCODING_MS_BIG5 = FT_ENCODING_BIG5;
    public static final int FT_ENCODING_MS_WANSUNG = FT_ENCODING_WANSUNG;
    public static final int FT_ENCODING_MS_JOHAB = FT_ENCODING_JOHAB;
    public static final int FT_ENCODING_ADOBE_STANDARD = FreeType.FT_ENC_TAG(65, 68, 79, 66);
    public static final int FT_ENCODING_ADOBE_EXPERT = FreeType.FT_ENC_TAG(65, 68, 66, 69);
    public static final int FT_ENCODING_ADOBE_CUSTOM = FreeType.FT_ENC_TAG(65, 68, 66, 67);
    public static final int FT_ENCODING_ADOBE_LATIN_1 = FreeType.FT_ENC_TAG(108, 97, 116, 49);
    public static final int FT_ENCODING_OLD_LATIN_2 = FreeType.FT_ENC_TAG(108, 97, 116, 50);
    public static final int FT_ENCODING_APPLE_ROMAN = FreeType.FT_ENC_TAG(97, 114, 109, 110);
    public static final int FT_FACE_FLAG_SCALABLE = 1;
    public static final int FT_FACE_FLAG_FIXED_SIZES = 2;
    public static final int FT_FACE_FLAG_FIXED_WIDTH = 4;
    public static final int FT_FACE_FLAG_SFNT = 8;
    public static final int FT_FACE_FLAG_HORIZONTAL = 16;
    public static final int FT_FACE_FLAG_VERTICAL = 32;
    public static final int FT_FACE_FLAG_KERNING = 64;
    public static final int FT_FACE_FLAG_FAST_GLYPHS = 128;
    public static final int FT_FACE_FLAG_MULTIPLE_MASTERS = 256;
    public static final int FT_FACE_FLAG_GLYPH_NAMES = 512;
    public static final int FT_FACE_FLAG_EXTERNAL_STREAM = 1024;
    public static final int FT_FACE_FLAG_HINTER = 2048;
    public static final int FT_FACE_FLAG_CID_KEYED = 4096;
    public static final int FT_FACE_FLAG_TRICKY = 8192;
    public static final int FT_FACE_FLAG_COLOR = 16384;
    public static final int FT_FACE_FLAG_VARIATION = 32768;
    public static final int FT_FACE_FLAG_SVG = 65536;
    public static final int FT_FACE_FLAG_SBIX = 131072;
    public static final int FT_FACE_FLAG_SBIX_OVERLAY = 262144;
    public static final int FT_STYLE_FLAG_ITALIC = 1;
    public static final int FT_STYLE_FLAG_BOLD = 2;
    public static final int FT_OPEN_MEMORY = 1;
    public static final int FT_OPEN_STREAM = 2;
    public static final int FT_OPEN_PATHNAME = 4;
    public static final int FT_OPEN_DRIVER = 8;
    public static final int FT_OPEN_PARAMS = 16;
    public static final int FT_SIZE_REQUEST_TYPE_NOMINAL = 0;
    public static final int FT_SIZE_REQUEST_TYPE_REAL_DIM = 1;
    public static final int FT_SIZE_REQUEST_TYPE_BBOX = 2;
    public static final int FT_SIZE_REQUEST_TYPE_CELL = 3;
    public static final int FT_SIZE_REQUEST_TYPE_SCALES = 4;
    public static final int FT_SIZE_REQUEST_TYPE_MAX = 5;
    public static final int FT_LOAD_DEFAULT = 0;
    public static final int FT_LOAD_NO_SCALE = 1;
    public static final int FT_LOAD_NO_HINTING = 2;
    public static final int FT_LOAD_RENDER = 4;
    public static final int FT_LOAD_NO_BITMAP = 8;
    public static final int FT_LOAD_VERTICAL_LAYOUT = 16;
    public static final int FT_LOAD_FORCE_AUTOHINT = 32;
    public static final int FT_LOAD_CROP_BITMAP = 64;
    public static final int FT_LOAD_PEDANTIC = 128;
    public static final int FT_LOAD_IGNORE_GLOBAL_ADVANCE_WIDTH = 512;
    public static final int FT_LOAD_NO_RECURSE = 1024;
    public static final int FT_LOAD_IGNORE_TRANSFORM = 2048;
    public static final int FT_LOAD_MONOCHROME = 4096;
    public static final int FT_LOAD_LINEAR_DESIGN = 8192;
    public static final int FT_LOAD_SBITS_ONLY = 16384;
    public static final int FT_LOAD_NO_AUTOHINT = 32768;
    public static final int FT_LOAD_COLOR = 0x100000;
    public static final int FT_LOAD_COMPUTE_METRICS = 0x200000;
    public static final int FT_LOAD_BITMAP_METRICS_ONLY = 0x400000;
    public static final int FT_LOAD_NO_SVG = 0x1000000;
    public static final int FT_LOAD_ADVANCE_ONLY = 256;
    public static final int FT_LOAD_SVG_ONLY = 0x800000;
    public static final int FT_RENDER_MODE_NORMAL = 0;
    public static final int FT_RENDER_MODE_LIGHT = 1;
    public static final int FT_RENDER_MODE_MONO = 2;
    public static final int FT_RENDER_MODE_LCD = 3;
    public static final int FT_RENDER_MODE_LCD_V = 4;
    public static final int FT_RENDER_MODE_SDF = 5;
    public static final int FT_RENDER_MODE_MAX = 6;
    public static final int FT_FT_LOAD_TARGET_NORMAL = FreeType.FT_LOAD_TARGET_(0);
    public static final int FT_FT_LOAD_TARGET_LIGHT = FreeType.FT_LOAD_TARGET_(1);
    public static final int FT_FT_LOAD_TARGET_MONO = FreeType.FT_LOAD_TARGET_(2);
    public static final int FT_FT_LOAD_TARGET_LCD = FreeType.FT_LOAD_TARGET_(3);
    public static final int FT_FT_LOAD_TARGET_LCD_V = FreeType.FT_LOAD_TARGET_(4);
    public static final int FT_KERNING_DEFAULT = 0;
    public static final int FT_KERNING_UNFITTED = 1;
    public static final int FT_KERNING_UNSCALED = 2;
    public static final int FT_SUBGLYPH_FLAG_ARGS_ARE_WORDS = 1;
    public static final int FT_SUBGLYPH_FLAG_ARGS_ARE_XY_VALUES = 2;
    public static final int FT_SUBGLYPH_FLAG_ROUND_XY_TO_GRID = 4;
    public static final int FT_SUBGLYPH_FLAG_SCALE = 8;
    public static final int FT_SUBGLYPH_FLAG_XY_SCALE = 64;
    public static final int FT_SUBGLYPH_FLAG_2X2 = 128;
    public static final int FT_SUBGLYPH_FLAG_USE_MY_METRICS = 512;
    public static final int FT_FSTYPE_INSTALLABLE_EMBEDDING = 0;
    public static final int FT_FSTYPE_RESTRICTED_LICENSE_EMBEDDING = 2;
    public static final int FT_FSTYPE_PREVIEW_AND_PRINT_EMBEDDING = 4;
    public static final int FT_FSTYPE_EDITABLE_EMBEDDING = 8;
    public static final int FT_FSTYPE_NO_SUBSETTING = 256;
    public static final int FT_FSTYPE_BITMAP_EMBEDDING_ONLY = 512;
    public static final int FREETYPE_MAJOR = 2;
    public static final int FREETYPE_MINOR = 14;
    public static final int FREETYPE_PATCH = 1;
    public static final int FT_ADVANCE_FLAG_FAST_ONLY = 0x20000000;
    public static final int BDF_PROPERTY_TYPE_NONE = 0;
    public static final int BDF_PROPERTY_TYPE_ATOM = 1;
    public static final int BDF_PROPERTY_TYPE_INTEGER = 2;
    public static final int BDF_PROPERTY_TYPE_CARDINAL = 3;
    public static final int FT_PALETTE_FOR_LIGHT_BACKGROUND = 1;
    public static final int FT_PALETTE_FOR_DARK_BACKGROUND = 2;
    public static final int FT_COLR_PAINTFORMAT_COLR_LAYERS = 1;
    public static final int FT_COLR_PAINTFORMAT_SOLID = 2;
    public static final int FT_COLR_PAINTFORMAT_LINEAR_GRADIENT = 4;
    public static final int FT_COLR_PAINTFORMAT_RADIAL_GRADIENT = 6;
    public static final int FT_COLR_PAINTFORMAT_SWEEP_GRADIENT = 8;
    public static final int FT_COLR_PAINTFORMAT_GLYPH = 10;
    public static final int FT_COLR_PAINTFORMAT_COLR_GLYPH = 11;
    public static final int FT_COLR_PAINTFORMAT_TRANSFORM = 12;
    public static final int FT_COLR_PAINTFORMAT_TRANSLATE = 14;
    public static final int FT_COLR_PAINTFORMAT_SCALE = 16;
    public static final int FT_COLR_PAINTFORMAT_ROTATE = 24;
    public static final int FT_COLR_PAINTFORMAT_SKEW = 28;
    public static final int FT_COLR_PAINTFORMAT_COMPOSITE = 32;
    public static final int FT_COLR_PAINT_FORMAT_MAX = 33;
    public static final int FT_COLR_PAINTFORMAT_UNSUPPORTED = 255;
    public static final int FT_COLR_PAINT_EXTEND_PAD = 0;
    public static final int FT_COLR_PAINT_EXTEND_REPEAT = 1;
    public static final int FT_COLR_PAINT_EXTEND_REFLECT = 2;
    public static final int FT_COLR_COMPOSITE_CLEAR = 0;
    public static final int FT_COLR_COMPOSITE_SRC = 1;
    public static final int FT_COLR_COMPOSITE_DEST = 2;
    public static final int FT_COLR_COMPOSITE_SRC_OVER = 3;
    public static final int FT_COLR_COMPOSITE_DEST_OVER = 4;
    public static final int FT_COLR_COMPOSITE_SRC_IN = 5;
    public static final int FT_COLR_COMPOSITE_DEST_IN = 6;
    public static final int FT_COLR_COMPOSITE_SRC_OUT = 7;
    public static final int FT_COLR_COMPOSITE_DEST_OUT = 8;
    public static final int FT_COLR_COMPOSITE_SRC_ATOP = 9;
    public static final int FT_COLR_COMPOSITE_DEST_ATOP = 10;
    public static final int FT_COLR_COMPOSITE_XOR = 11;
    public static final int FT_COLR_COMPOSITE_PLUS = 12;
    public static final int FT_COLR_COMPOSITE_SCREEN = 13;
    public static final int FT_COLR_COMPOSITE_OVERLAY = 14;
    public static final int FT_COLR_COMPOSITE_DARKEN = 15;
    public static final int FT_COLR_COMPOSITE_LIGHTEN = 16;
    public static final int FT_COLR_COMPOSITE_COLOR_DODGE = 17;
    public static final int FT_COLR_COMPOSITE_COLOR_BURN = 18;
    public static final int FT_COLR_COMPOSITE_HARD_LIGHT = 19;
    public static final int FT_COLR_COMPOSITE_SOFT_LIGHT = 20;
    public static final int FT_COLR_COMPOSITE_DIFFERENCE = 21;
    public static final int FT_COLR_COMPOSITE_EXCLUSION = 22;
    public static final int FT_COLR_COMPOSITE_MULTIPLY = 23;
    public static final int FT_COLR_COMPOSITE_HSL_HUE = 24;
    public static final int FT_COLR_COMPOSITE_HSL_SATURATION = 25;
    public static final int FT_COLR_COMPOSITE_HSL_COLOR = 26;
    public static final int FT_COLR_COMPOSITE_HSL_LUMINOSITY = 27;
    public static final int FT_COLR_COMPOSITE_MAX = 28;
    public static final int FT_COLOR_INCLUDE_ROOT_TRANSFORM = 0;
    public static final int FT_COLOR_NO_ROOT_TRANSFORM = 1;
    public static final int FT_COLOR_ROOT_TRANSFORM_MAX = 2;
    public static final int FT_HINTING_FREETYPE = 0;
    public static final int FT_HINTING_ADOBE = 1;
    public static final int TT_INTERPRETER_VERSION_35 = 35;
    public static final int TT_INTERPRETER_VERSION_38 = 38;
    public static final int TT_INTERPRETER_VERSION_40 = 40;
    public static final int FT_AUTOHINTER_SCRIPT_NONE = 0;
    public static final int FT_AUTOHINTER_SCRIPT_LATIN = 1;
    public static final int FT_AUTOHINTER_SCRIPT_CJK = 2;
    public static final int FT_AUTOHINTER_SCRIPT_INDIC = 3;
    public static final int FT_Err_Ok = 0;
    public static final int FT_Err_Cannot_Open_Resource = 1;
    public static final int FT_Err_Unknown_File_Format = 2;
    public static final int FT_Err_Invalid_File_Format = 3;
    public static final int FT_Err_Invalid_Version = 4;
    public static final int FT_Err_Lower_Module_Version = 5;
    public static final int FT_Err_Invalid_Argument = 6;
    public static final int FT_Err_Unimplemented_Feature = 7;
    public static final int FT_Err_Invalid_Table = 8;
    public static final int FT_Err_Invalid_Offset = 9;
    public static final int FT_Err_Array_Too_Large = 10;
    public static final int FT_Err_Missing_Module = 11;
    public static final int FT_Err_Missing_Property = 12;
    public static final int FT_Err_Invalid_Glyph_Index = 16;
    public static final int FT_Err_Invalid_Character_Code = 17;
    public static final int FT_Err_Invalid_Glyph_Format = 18;
    public static final int FT_Err_Cannot_Render_Glyph = 19;
    public static final int FT_Err_Invalid_Outline = 20;
    public static final int FT_Err_Invalid_Composite = 21;
    public static final int FT_Err_Too_Many_Hints = 22;
    public static final int FT_Err_Invalid_Pixel_Size = 23;
    public static final int FT_Err_Invalid_SVG_Document = 24;
    public static final int FT_Err_Invalid_Handle = 32;
    public static final int FT_Err_Invalid_Library_Handle = 33;
    public static final int FT_Err_Invalid_Driver_Handle = 34;
    public static final int FT_Err_Invalid_Face_Handle = 35;
    public static final int FT_Err_Invalid_Size_Handle = 36;
    public static final int FT_Err_Invalid_Slot_Handle = 37;
    public static final int FT_Err_Invalid_CharMap_Handle = 38;
    public static final int FT_Err_Invalid_Cache_Handle = 39;
    public static final int FT_Err_Invalid_Stream_Handle = 40;
    public static final int FT_Err_Too_Many_Drivers = 48;
    public static final int FT_Err_Too_Many_Extensions = 49;
    public static final int FT_Err_Out_Of_Memory = 64;
    public static final int FT_Err_Unlisted_Object = 65;
    public static final int FT_Err_Cannot_Open_Stream = 81;
    public static final int FT_Err_Invalid_Stream_Seek = 82;
    public static final int FT_Err_Invalid_Stream_Skip = 83;
    public static final int FT_Err_Invalid_Stream_Read = 84;
    public static final int FT_Err_Invalid_Stream_Operation = 85;
    public static final int FT_Err_Invalid_Frame_Operation = 86;
    public static final int FT_Err_Nested_Frame_Access = 87;
    public static final int FT_Err_Invalid_Frame_Read = 88;
    public static final int FT_Err_Raster_Uninitialized = 96;
    public static final int FT_Err_Raster_Corrupted = 97;
    public static final int FT_Err_Raster_Overflow = 98;
    public static final int FT_Err_Raster_Negative_Height = 99;
    public static final int FT_Err_Too_Many_Caches = 112;
    public static final int FT_Err_Invalid_Opcode = 128;
    public static final int FT_Err_Too_Few_Arguments = 129;
    public static final int FT_Err_Stack_Overflow = 130;
    public static final int FT_Err_Code_Overflow = 131;
    public static final int FT_Err_Bad_Argument = 132;
    public static final int FT_Err_Divide_By_Zero = 133;
    public static final int FT_Err_Invalid_Reference = 134;
    public static final int FT_Err_Debug_OpCode = 135;
    public static final int FT_Err_ENDF_In_Exec_Stream = 136;
    public static final int FT_Err_Nested_DEFS = 137;
    public static final int FT_Err_Invalid_CodeRange = 138;
    public static final int FT_Err_Execution_Too_Long = 139;
    public static final int FT_Err_Too_Many_Function_Defs = 140;
    public static final int FT_Err_Too_Many_Instruction_Defs = 141;
    public static final int FT_Err_Table_Missing = 142;
    public static final int FT_Err_Horiz_Header_Missing = 143;
    public static final int FT_Err_Locations_Missing = 144;
    public static final int FT_Err_Name_Table_Missing = 145;
    public static final int FT_Err_CMap_Table_Missing = 146;
    public static final int FT_Err_Hmtx_Table_Missing = 147;
    public static final int FT_Err_Post_Table_Missing = 148;
    public static final int FT_Err_Invalid_Horiz_Metrics = 149;
    public static final int FT_Err_Invalid_CharMap_Format = 150;
    public static final int FT_Err_Invalid_PPem = 151;
    public static final int FT_Err_Invalid_Vert_Metrics = 152;
    public static final int FT_Err_Could_Not_Find_Context = 153;
    public static final int FT_Err_Invalid_Post_Table_Format = 154;
    public static final int FT_Err_Invalid_Post_Table = 155;
    public static final int FT_Err_DEF_In_Glyf_Bytecode = 156;
    public static final int FT_Err_Missing_Bitmap = 157;
    public static final int FT_Err_Missing_SVG_Hooks = 158;
    public static final int FT_Err_Syntax_Error = 160;
    public static final int FT_Err_Stack_Underflow = 161;
    public static final int FT_Err_Ignore = 162;
    public static final int FT_Err_No_Unicode_Glyph_Name = 163;
    public static final int FT_Err_Glyph_Too_Big = 164;
    public static final int FT_Err_Missing_Startfont_Field = 176;
    public static final int FT_Err_Missing_Font_Field = 177;
    public static final int FT_Err_Missing_Size_Field = 178;
    public static final int FT_Err_Missing_Fontboundingbox_Field = 179;
    public static final int FT_Err_Missing_Chars_Field = 180;
    public static final int FT_Err_Missing_Startchar_Field = 181;
    public static final int FT_Err_Missing_Encoding_Field = 182;
    public static final int FT_Err_Missing_Bbx_Field = 183;
    public static final int FT_Err_Bbx_Too_Big = 184;
    public static final int FT_Err_Corrupted_Font_Header = 185;
    public static final int FT_Err_Corrupted_Font_Glyphs = 186;
    public static final int FT_Err_Max = 187;
    public static final int FT_GASP_NO_TABLE = -1;
    public static final int FT_GASP_DO_GRIDFIT = 1;
    public static final int FT_GASP_DO_GRAY = 2;
    public static final int FT_GASP_SYMMETRIC_GRIDFIT = 4;
    public static final int FT_GASP_SYMMETRIC_SMOOTHING = 8;
    public static final int FT_GLYPH_BBOX_UNSCALED = 0;
    public static final int FT_GLYPH_BBOX_SUBPIXELS = 0;
    public static final int FT_GLYPH_BBOX_GRIDFIT = 1;
    public static final int FT_GLYPH_BBOX_TRUNCATE = 2;
    public static final int FT_GLYPH_BBOX_PIXELS = 3;
    public static final int FT_VALIDATE_feat_INDEX = 0;
    public static final int FT_VALIDATE_mort_INDEX = 1;
    public static final int FT_VALIDATE_morx_INDEX = 2;
    public static final int FT_VALIDATE_bsln_INDEX = 3;
    public static final int FT_VALIDATE_just_INDEX = 4;
    public static final int FT_VALIDATE_kern_INDEX = 5;
    public static final int FT_VALIDATE_opbd_INDEX = 6;
    public static final int FT_VALIDATE_trak_INDEX = 7;
    public static final int FT_VALIDATE_prop_INDEX = 8;
    public static final int FT_VALIDATE_lcar_INDEX = 9;
    public static final int FT_VALIDATE_GX_LAST_INDEX = 9;
    public static final int FT_VALIDATE_GX_LENGTH = 10;
    public static final int FT_VALIDATE_GX_START = 16384;
    public static final int FT_VALIDATE_feat = 16384;
    public static final int FT_VALIDATE_mort = 32768;
    public static final int FT_VALIDATE_morx = 65536;
    public static final int FT_VALIDATE_bsln = 131072;
    public static final int FT_VALIDATE_just = 262144;
    public static final int FT_VALIDATE_kern = 524288;
    public static final int FT_VALIDATE_opbd = 0x100000;
    public static final int FT_VALIDATE_trak = 0x200000;
    public static final int FT_VALIDATE_prop = 0x400000;
    public static final int FT_VALIDATE_lcar = 0x800000;
    public static final int FT_FT_VALIDATE_GX = 0xFFC000;
    public static final int FT_VALIDATE_MS = 16384;
    public static final int FT_VALIDATE_APPLE = 32768;
    public static final int FT_VALIDATE_CKERN = 49152;
    public static final int FT_PIXEL_MODE_NONE = 0;
    public static final int FT_PIXEL_MODE_MONO = 1;
    public static final int FT_PIXEL_MODE_GRAY = 2;
    public static final int FT_PIXEL_MODE_GRAY2 = 3;
    public static final int FT_PIXEL_MODE_GRAY4 = 4;
    public static final int FT_PIXEL_MODE_LCD = 5;
    public static final int FT_PIXEL_MODE_LCD_V = 6;
    public static final int FT_PIXEL_MODE_BGRA = 7;
    public static final int FT_PIXEL_MODE_MAX = 8;
    public static final int FT_OUTLINE_CONTOURS_MAX = 65535;
    public static final int FT_OUTLINE_POINTS_MAX = 65535;
    public static final int FT_OUTLINE_NONE = 0;
    public static final int FT_OUTLINE_OWNER = 1;
    public static final int FT_OUTLINE_EVEN_ODD_FILL = 2;
    public static final int FT_OUTLINE_REVERSE_FILL = 4;
    public static final int FT_OUTLINE_IGNORE_DROPOUTS = 8;
    public static final int FT_OUTLINE_SMART_DROPOUTS = 16;
    public static final int FT_OUTLINE_INCLUDE_STUBS = 32;
    public static final int FT_OUTLINE_OVERLAP = 64;
    public static final int FT_OUTLINE_HIGH_PRECISION = 256;
    public static final int FT_OUTLINE_SINGLE_PASS = 512;
    public static final int FT_CURVE_TAG_ON = 1;
    public static final int FT_CURVE_TAG_CONIC = 0;
    public static final int FT_CURVE_TAG_CUBIC = 2;
    public static final int FT_CURVE_TAG_HAS_SCANMODE = 4;
    public static final int FT_CURVE_TAG_TOUCH_X = 8;
    public static final int FT_CURVE_TAG_TOUCH_Y = 16;
    public static final int FT_CURVE_TAG_TOUCH_BOTH = 24;
    public static final int FT_GLYPH_FORMAT_NONE = FreeType.FT_IMAGE_TAG(0, 0, 0, 0);
    public static final int FT_GLYPH_FORMAT_COMPOSITE = FreeType.FT_IMAGE_TAG(99, 111, 109, 112);
    public static final int FT_GLYPH_FORMAT_BITMAP = FreeType.FT_IMAGE_TAG(98, 105, 116, 115);
    public static final int FT_GLYPH_FORMAT_OUTLINE = FreeType.FT_IMAGE_TAG(111, 117, 116, 108);
    public static final int FT_GLYPH_FORMAT_PLOTTER = FreeType.FT_IMAGE_TAG(112, 108, 111, 116);
    public static final int FT_GLYPH_FORMAT_SVG = FreeType.FT_IMAGE_TAG(83, 86, 71, 32);
    public static final int FT_RASTER_FLAG_DEFAULT = 0;
    public static final int FT_RASTER_FLAG_AA = 1;
    public static final int FT_RASTER_FLAG_DIRECT = 2;
    public static final int FT_RASTER_FLAG_CLIP = 4;
    public static final int FT_RASTER_FLAG_SDF = 8;
    public static final int FT_LCD_FILTER_NONE = 0;
    public static final int FT_LCD_FILTER_DEFAULT = 1;
    public static final int FT_LCD_FILTER_LIGHT = 2;
    public static final int FT_LCD_FILTER_LEGACY1 = 3;
    public static final int FT_LCD_FILTER_LEGACY = 16;
    public static final int FT_LCD_FILTER_MAX = 17;
    public static final int FT_LCD_FILTER_FIVE_TAPS = 5;
    public static final int T1_MAX_MM_AXIS = 4;
    public static final int T1_MAX_MM_DESIGNS = 16;
    public static final int T1_MAX_MM_MAP_POINTS = 20;
    public static final int FT_VAR_AXIS_FLAG_HIDDEN = 1;
    public static final int FT_MODULE_FONT_DRIVER = 1;
    public static final int FT_MODULE_RENDERER = 2;
    public static final int FT_MODULE_HINTER = 4;
    public static final int FT_MODULE_STYLER = 8;
    public static final int FT_MODULE_DRIVER_SCALABLE = 256;
    public static final int FT_MODULE_DRIVER_NO_OUTLINES = 512;
    public static final int FT_MODULE_DRIVER_HAS_HINTER = 1024;
    public static final int FT_MODULE_DRIVER_HINTS_LIGHTLY = 2048;
    public static final int FT_DEBUG_HOOK_TRUETYPE = 0;
    public static final int FT_TRUETYPE_ENGINE_TYPE_NONE = 0;
    public static final int FT_TRUETYPE_ENGINE_TYPE_UNPATENTED = 1;
    public static final int FT_TRUETYPE_ENGINE_TYPE_PATENTED = 2;
    public static final int FT_Mod_Err_Base = 0;
    public static final int FT_Mod_Err_Autofit = 256;
    public static final int FT_Mod_Err_BDF = 512;
    public static final int FT_Mod_Err_Bzip2 = 768;
    public static final int FT_Mod_Err_Cache = 1024;
    public static final int FT_Mod_Err_CFF = 1280;
    public static final int FT_Mod_Err_CID = 1536;
    public static final int FT_Mod_Err_Gzip = 1792;
    public static final int FT_Mod_Err_LZW = 2048;
    public static final int FT_Mod_Err_OTvalid = 2304;
    public static final int FT_Mod_Err_PCF = 2560;
    public static final int FT_Mod_Err_PFR = 2816;
    public static final int FT_Mod_Err_PSaux = 3072;
    public static final int FT_Mod_Err_PShinter = 3328;
    public static final int FT_Mod_Err_PSnames = 3584;
    public static final int FT_Mod_Err_Raster = 3840;
    public static final int FT_Mod_Err_SFNT = 4096;
    public static final int FT_Mod_Err_Smooth = 4352;
    public static final int FT_Mod_Err_TrueType = 4608;
    public static final int FT_Mod_Err_Type1 = 4864;
    public static final int FT_Mod_Err_Type42 = 5120;
    public static final int FT_Mod_Err_Winfonts = 5376;
    public static final int FT_Mod_Err_GXvalid = 5632;
    public static final int FT_Mod_Err_Sdf = 5888;
    public static final int FT_Mod_Err_Max = 5889;
    public static final int FT_VALIDATE_BASE = 256;
    public static final int FT_VALIDATE_GDEF = 512;
    public static final int FT_VALIDATE_GPOS = 1024;
    public static final int FT_VALIDATE_GSUB = 2048;
    public static final int FT_VALIDATE_JSTF = 4096;
    public static final int FT_VALIDATE_MATH = 8192;
    public static final int FT_VALIDATE_OT = 16128;
    public static final int FT_ORIENTATION_TRUETYPE = 0;
    public static final int FT_ORIENTATION_POSTSCRIPT = 1;
    public static final int FT_ORIENTATION_FILL_RIGHT = 0;
    public static final int FT_ORIENTATION_FILL_LEFT = 1;
    public static final int FT_ORIENTATION_NONE = 2;
    public static final int FT_PARAM_TAG_IGNORE_TYPOGRAPHIC_FAMILY = FreeType.FT_MAKE_TAG(105, 103, 112, 102);
    public static final int FT_PARAM_TAG_IGNORE_TYPOGRAPHIC_SUBFAMILY = FreeType.FT_MAKE_TAG(105, 103, 112, 115);
    public static final int FT_PARAM_TAG_INCREMENTAL = FreeType.FT_MAKE_TAG(105, 110, 99, 114);
    public static final int FT_PARAM_TAG_IGNORE_SBIX = FreeType.FT_MAKE_TAG(105, 115, 98, 120);
    public static final int FT_PARAM_TAG_LCD_FILTER_WEIGHTS = FreeType.FT_MAKE_TAG(108, 99, 100, 102);
    public static final int FT_PARAM_TAG_RANDOM_SEED = FreeType.FT_MAKE_TAG(115, 101, 101, 100);
    public static final int FT_PARAM_TAG_STEM_DARKENING = FreeType.FT_MAKE_TAG(100, 97, 114, 107);
    public static final int FT_PARAM_TAG_UNPATENTED_HINTING = FreeType.FT_MAKE_TAG(117, 110, 112, 97);
    public static final int FT_STROKER_LINEJOIN_ROUND = 0;
    public static final int FT_STROKER_LINEJOIN_BEVEL = 1;
    public static final int FT_STROKER_LINEJOIN_MITER_VARIABLE = 2;
    public static final int FT_STROKER_LINEJOIN_MITER = 2;
    public static final int FT_STROKER_LINEJOIN_MITER_FIXED = 3;
    public static final int FT_STROKER_LINECAP_BUTT = 0;
    public static final int FT_STROKER_LINECAP_ROUND = 1;
    public static final int FT_STROKER_LINECAP_SQUARE = 2;
    public static final int FT_STROKER_BORDER_LEFT = 0;
    public static final int FT_STROKER_BORDER_RIGHT = 1;
    public static final int FT_ANGLE_PI = 0xB40000;
    public static final int FT_ANGLE_2PI = 23592960;
    public static final int FT_ANGLE_PI2 = 0x5A0000;
    public static final int FT_ANGLE_PI4 = 0x2D0000;
    public static final int T1_BLEND_UNDERLINE_POSITION = 0;
    public static final int T1_BLEND_UNDERLINE_THICKNESS = 1;
    public static final int T1_BLEND_ITALIC_ANGLE = 2;
    public static final int T1_BLEND_BLUE_VALUES = 3;
    public static final int T1_BLEND_OTHER_BLUES = 4;
    public static final int T1_BLEND_STANDARD_WIDTH = 5;
    public static final int T1_BLEND_STANDARD_HEIGHT = 6;
    public static final int T1_BLEND_STEM_SNAP_WIDTHS = 7;
    public static final int T1_BLEND_STEM_SNAP_HEIGHTS = 8;
    public static final int T1_BLEND_BLUE_SCALE = 9;
    public static final int T1_BLEND_BLUE_SHIFT = 10;
    public static final int T1_BLEND_FAMILY_BLUES = 11;
    public static final int T1_BLEND_FAMILY_OTHER_BLUES = 12;
    public static final int T1_BLEND_FORCE_BOLD = 13;
    public static final int T1_BLEND_MAX = 14;
    public static final int T1_ENCODING_TYPE_NONE = 0;
    public static final int T1_ENCODING_TYPE_ARRAY = 1;
    public static final int T1_ENCODING_TYPE_STANDARD = 2;
    public static final int T1_ENCODING_TYPE_ISOLATIN1 = 3;
    public static final int T1_ENCODING_TYPE_EXPERT = 4;
    public static final int PS_DICT_FONT_TYPE = 0;
    public static final int PS_DICT_FONT_MATRIX = 1;
    public static final int PS_DICT_FONT_BBOX = 2;
    public static final int PS_DICT_PAINT_TYPE = 3;
    public static final int PS_DICT_FONT_NAME = 4;
    public static final int PS_DICT_UNIQUE_ID = 5;
    public static final int PS_DICT_NUM_CHAR_STRINGS = 6;
    public static final int PS_DICT_CHAR_STRING_KEY = 7;
    public static final int PS_DICT_CHAR_STRING = 8;
    public static final int PS_DICT_ENCODING_TYPE = 9;
    public static final int PS_DICT_ENCODING_ENTRY = 10;
    public static final int PS_DICT_NUM_SUBRS = 11;
    public static final int PS_DICT_SUBR = 12;
    public static final int PS_DICT_STD_HW = 13;
    public static final int PS_DICT_STD_VW = 14;
    public static final int PS_DICT_NUM_BLUE_VALUES = 15;
    public static final int PS_DICT_BLUE_VALUE = 16;
    public static final int PS_DICT_BLUE_FUZZ = 17;
    public static final int PS_DICT_NUM_OTHER_BLUES = 18;
    public static final int PS_DICT_OTHER_BLUE = 19;
    public static final int PS_DICT_NUM_FAMILY_BLUES = 20;
    public static final int PS_DICT_FAMILY_BLUE = 21;
    public static final int PS_DICT_NUM_FAMILY_OTHER_BLUES = 22;
    public static final int PS_DICT_FAMILY_OTHER_BLUE = 23;
    public static final int PS_DICT_BLUE_SCALE = 24;
    public static final int PS_DICT_BLUE_SHIFT = 25;
    public static final int PS_DICT_NUM_STEM_SNAP_H = 26;
    public static final int PS_DICT_STEM_SNAP_H = 27;
    public static final int PS_DICT_NUM_STEM_SNAP_V = 28;
    public static final int PS_DICT_STEM_SNAP_V = 29;
    public static final int PS_DICT_FORCE_BOLD = 30;
    public static final int PS_DICT_RND_STEM_UP = 31;
    public static final int PS_DICT_MIN_FEATURE = 32;
    public static final int PS_DICT_LEN_IV = 33;
    public static final int PS_DICT_PASSWORD = 34;
    public static final int PS_DICT_LANGUAGE_GROUP = 35;
    public static final int PS_DICT_VERSION = 36;
    public static final int PS_DICT_NOTICE = 37;
    public static final int PS_DICT_FULL_NAME = 38;
    public static final int PS_DICT_FAMILY_NAME = 39;
    public static final int PS_DICT_WEIGHT = 40;
    public static final int PS_DICT_IS_FIXED_PITCH = 41;
    public static final int PS_DICT_UNDERLINE_POSITION = 42;
    public static final int PS_DICT_UNDERLINE_THICKNESS = 43;
    public static final int PS_DICT_FS_TYPE = 44;
    public static final int PS_DICT_ITALIC_ANGLE = 45;
    public static final int PS_DICT_MAX = 45;
    public static final int TT_PLATFORM_APPLE_UNICODE = 0;
    public static final int TT_PLATFORM_MACINTOSH = 1;
    public static final int TT_PLATFORM_ISO = 2;
    public static final int TT_PLATFORM_MICROSOFT = 3;
    public static final int TT_PLATFORM_CUSTOM = 4;
    public static final int TT_PLATFORM_ADOBE = 7;
    public static final int TT_APPLE_ID_DEFAULT = 0;
    public static final int TT_APPLE_ID_UNICODE_1_1 = 1;
    public static final int TT_APPLE_ID_ISO_10646 = 2;
    public static final int TT_APPLE_ID_UNICODE_2_0 = 3;
    public static final int TT_APPLE_ID_UNICODE_32 = 4;
    public static final int TT_APPLE_ID_VARIANT_SELECTOR = 5;
    public static final int TT_APPLE_ID_FULL_UNICODE = 6;
    public static final int TT_MAC_ID_ROMAN = 0;
    public static final int TT_MAC_ID_JAPANESE = 1;
    public static final int TT_MAC_ID_TRADITIONAL_CHINESE = 2;
    public static final int TT_MAC_ID_KOREAN = 3;
    public static final int TT_MAC_ID_ARABIC = 4;
    public static final int TT_MAC_ID_HEBREW = 5;
    public static final int TT_MAC_ID_GREEK = 6;
    public static final int TT_MAC_ID_RUSSIAN = 7;
    public static final int TT_MAC_ID_RSYMBOL = 8;
    public static final int TT_MAC_ID_DEVANAGARI = 9;
    public static final int TT_MAC_ID_GURMUKHI = 10;
    public static final int TT_MAC_ID_GUJARATI = 11;
    public static final int TT_MAC_ID_ORIYA = 12;
    public static final int TT_MAC_ID_BENGALI = 13;
    public static final int TT_MAC_ID_TAMIL = 14;
    public static final int TT_MAC_ID_TELUGU = 15;
    public static final int TT_MAC_ID_KANNADA = 16;
    public static final int TT_MAC_ID_MALAYALAM = 17;
    public static final int TT_MAC_ID_SINHALESE = 18;
    public static final int TT_MAC_ID_BURMESE = 19;
    public static final int TT_MAC_ID_KHMER = 20;
    public static final int TT_MAC_ID_THAI = 21;
    public static final int TT_MAC_ID_LAOTIAN = 22;
    public static final int TT_MAC_ID_GEORGIAN = 23;
    public static final int TT_MAC_ID_ARMENIAN = 24;
    public static final int TT_MAC_ID_MALDIVIAN = 25;
    public static final int TT_MAC_ID_SIMPLIFIED_CHINESE = 26;
    public static final int TT_MAC_ID_TIBETAN = 27;
    public static final int TT_MAC_ID_MONGOLIAN = 28;
    public static final int TT_MAC_ID_GEEZ = 29;
    public static final int TT_MAC_ID_SLAVIC = 30;
    public static final int TT_MAC_ID_VIETNAMESE = 31;
    public static final int TT_MAC_ID_SINDHI = 32;
    public static final int TT_MAC_ID_UNINTERP = 33;
    public static final int TT_ISO_ID_7BIT_ASCII = 0;
    public static final int TT_ISO_ID_10646 = 1;
    public static final int TT_ISO_ID_8859_1 = 2;
    public static final int TT_MS_ID_SYMBOL_CS = 0;
    public static final int TT_MS_ID_UNICODE_CS = 1;
    public static final int TT_MS_ID_SJIS = 2;
    public static final int TT_MS_ID_PRC = 3;
    public static final int TT_MS_ID_BIG_5 = 4;
    public static final int TT_MS_ID_WANSUNG = 5;
    public static final int TT_MS_ID_JOHAB = 6;
    public static final int TT_MS_ID_UCS_4 = 10;
    public static final int TT_ADOBE_ID_STANDARD = 0;
    public static final int TT_ADOBE_ID_EXPERT = 1;
    public static final int TT_ADOBE_ID_CUSTOM = 2;
    public static final int TT_ADOBE_ID_LATIN_1 = 3;
    public static final int TT_MAC_LANGID_ENGLISH = 0;
    public static final int TT_MAC_LANGID_FRENCH = 1;
    public static final int TT_MAC_LANGID_GERMAN = 2;
    public static final int TT_MAC_LANGID_ITALIAN = 3;
    public static final int TT_MAC_LANGID_DUTCH = 4;
    public static final int TT_MAC_LANGID_SWEDISH = 5;
    public static final int TT_MAC_LANGID_SPANISH = 6;
    public static final int TT_MAC_LANGID_DANISH = 7;
    public static final int TT_MAC_LANGID_PORTUGUESE = 8;
    public static final int TT_MAC_LANGID_NORWEGIAN = 9;
    public static final int TT_MAC_LANGID_HEBREW = 10;
    public static final int TT_MAC_LANGID_JAPANESE = 11;
    public static final int TT_MAC_LANGID_ARABIC = 12;
    public static final int TT_MAC_LANGID_FINNISH = 13;
    public static final int TT_MAC_LANGID_GREEK = 14;
    public static final int TT_MAC_LANGID_ICELANDIC = 15;
    public static final int TT_MAC_LANGID_MALTESE = 16;
    public static final int TT_MAC_LANGID_TURKISH = 17;
    public static final int TT_MAC_LANGID_CROATIAN = 18;
    public static final int TT_MAC_LANGID_CHINESE_TRADITIONAL = 19;
    public static final int TT_MAC_LANGID_URDU = 20;
    public static final int TT_MAC_LANGID_HINDI = 21;
    public static final int TT_MAC_LANGID_THAI = 22;
    public static final int TT_MAC_LANGID_KOREAN = 23;
    public static final int TT_MAC_LANGID_LITHUANIAN = 24;
    public static final int TT_MAC_LANGID_POLISH = 25;
    public static final int TT_MAC_LANGID_HUNGARIAN = 26;
    public static final int TT_MAC_LANGID_ESTONIAN = 27;
    public static final int TT_MAC_LANGID_LETTISH = 28;
    public static final int TT_MAC_LANGID_SAAMISK = 29;
    public static final int TT_MAC_LANGID_FAEROESE = 30;
    public static final int TT_MAC_LANGID_FARSI = 31;
    public static final int TT_MAC_LANGID_RUSSIAN = 32;
    public static final int TT_MAC_LANGID_CHINESE_SIMPLIFIED = 33;
    public static final int TT_MAC_LANGID_FLEMISH = 34;
    public static final int TT_MAC_LANGID_IRISH = 35;
    public static final int TT_MAC_LANGID_ALBANIAN = 36;
    public static final int TT_MAC_LANGID_ROMANIAN = 37;
    public static final int TT_MAC_LANGID_CZECH = 38;
    public static final int TT_MAC_LANGID_SLOVAK = 39;
    public static final int TT_MAC_LANGID_SLOVENIAN = 40;
    public static final int TT_MAC_LANGID_YIDDISH = 41;
    public static final int TT_MAC_LANGID_SERBIAN = 42;
    public static final int TT_MAC_LANGID_MACEDONIAN = 43;
    public static final int TT_MAC_LANGID_BULGARIAN = 44;
    public static final int TT_MAC_LANGID_UKRAINIAN = 45;
    public static final int TT_MAC_LANGID_BYELORUSSIAN = 46;
    public static final int TT_MAC_LANGID_UZBEK = 47;
    public static final int TT_MAC_LANGID_KAZAKH = 48;
    public static final int TT_MAC_LANGID_AZERBAIJANI = 49;
    public static final int TT_MAC_LANGID_AZERBAIJANI_CYRILLIC_SCRIPT = 50;
    public static final int TT_MAC_LANGID_AZERBAIJANI_ARABIC_SCRIPT = 51;
    public static final int TT_MAC_LANGID_ARMENIAN = 52;
    public static final int TT_MAC_LANGID_GEORGIAN = 53;
    public static final int TT_MAC_LANGID_MOLDAVIAN = 54;
    public static final int TT_MAC_LANGID_KIRGHIZ = 55;
    public static final int TT_MAC_LANGID_TAJIKI = 56;
    public static final int TT_MAC_LANGID_TURKMEN = 57;
    public static final int TT_MAC_LANGID_MONGOLIAN = 58;
    public static final int TT_MAC_LANGID_MONGOLIAN_MONGOLIAN_SCRIPT = 59;
    public static final int TT_MAC_LANGID_MONGOLIAN_CYRILLIC_SCRIPT = 60;
    public static final int TT_MAC_LANGID_PASHTO = 61;
    public static final int TT_MAC_LANGID_KURDISH = 62;
    public static final int TT_MAC_LANGID_KASHMIRI = 63;
    public static final int TT_MAC_LANGID_SINDHI = 64;
    public static final int TT_MAC_LANGID_TIBETAN = 65;
    public static final int TT_MAC_LANGID_NEPALI = 66;
    public static final int TT_MAC_LANGID_SANSKRIT = 67;
    public static final int TT_MAC_LANGID_MARATHI = 68;
    public static final int TT_MAC_LANGID_BENGALI = 69;
    public static final int TT_MAC_LANGID_ASSAMESE = 70;
    public static final int TT_MAC_LANGID_GUJARATI = 71;
    public static final int TT_MAC_LANGID_PUNJABI = 72;
    public static final int TT_MAC_LANGID_ORIYA = 73;
    public static final int TT_MAC_LANGID_MALAYALAM = 74;
    public static final int TT_MAC_LANGID_KANNADA = 75;
    public static final int TT_MAC_LANGID_TAMIL = 76;
    public static final int TT_MAC_LANGID_TELUGU = 77;
    public static final int TT_MAC_LANGID_SINHALESE = 78;
    public static final int TT_MAC_LANGID_BURMESE = 79;
    public static final int TT_MAC_LANGID_KHMER = 80;
    public static final int TT_MAC_LANGID_LAO = 81;
    public static final int TT_MAC_LANGID_VIETNAMESE = 82;
    public static final int TT_MAC_LANGID_INDONESIAN = 83;
    public static final int TT_MAC_LANGID_TAGALOG = 84;
    public static final int TT_MAC_LANGID_MALAY_ROMAN_SCRIPT = 85;
    public static final int TT_MAC_LANGID_MALAY_ARABIC_SCRIPT = 86;
    public static final int TT_MAC_LANGID_AMHARIC = 87;
    public static final int TT_MAC_LANGID_TIGRINYA = 88;
    public static final int TT_MAC_LANGID_GALLA = 89;
    public static final int TT_MAC_LANGID_SOMALI = 90;
    public static final int TT_MAC_LANGID_SWAHILI = 91;
    public static final int TT_MAC_LANGID_RUANDA = 92;
    public static final int TT_MAC_LANGID_RUNDI = 93;
    public static final int TT_MAC_LANGID_CHEWA = 94;
    public static final int TT_MAC_LANGID_MALAGASY = 95;
    public static final int TT_MAC_LANGID_ESPERANTO = 96;
    public static final int TT_MAC_LANGID_WELSH = 128;
    public static final int TT_MAC_LANGID_BASQUE = 129;
    public static final int TT_MAC_LANGID_CATALAN = 130;
    public static final int TT_MAC_LANGID_LATIN = 131;
    public static final int TT_MAC_LANGID_QUECHUA = 132;
    public static final int TT_MAC_LANGID_GUARANI = 133;
    public static final int TT_MAC_LANGID_AYMARA = 134;
    public static final int TT_MAC_LANGID_TATAR = 135;
    public static final int TT_MAC_LANGID_UIGHUR = 136;
    public static final int TT_MAC_LANGID_DZONGKHA = 137;
    public static final int TT_MAC_LANGID_JAVANESE = 138;
    public static final int TT_MAC_LANGID_SUNDANESE = 139;
    public static final int TT_MAC_LANGID_GALICIAN = 140;
    public static final int TT_MAC_LANGID_AFRIKAANS = 141;
    public static final int TT_MAC_LANGID_BRETON = 142;
    public static final int TT_MAC_LANGID_INUKTITUT = 143;
    public static final int TT_MAC_LANGID_SCOTTISH_GAELIC = 144;
    public static final int TT_MAC_LANGID_MANX_GAELIC = 145;
    public static final int TT_MAC_LANGID_IRISH_GAELIC = 146;
    public static final int TT_MAC_LANGID_TONGAN = 147;
    public static final int TT_MAC_LANGID_GREEK_POLYTONIC = 148;
    public static final int TT_MAC_LANGID_GREELANDIC = 149;
    public static final int TT_MAC_LANGID_AZERBAIJANI_ROMAN_SCRIPT = 150;
    public static final int TT_MS_LANGID_ARABIC_SAUDI_ARABIA = 1025;
    public static final int TT_MS_LANGID_ARABIC_IRAQ = 2049;
    public static final int TT_MS_LANGID_ARABIC_EGYPT = 3073;
    public static final int TT_MS_LANGID_ARABIC_LIBYA = 4097;
    public static final int TT_MS_LANGID_ARABIC_ALGERIA = 5121;
    public static final int TT_MS_LANGID_ARABIC_MOROCCO = 6145;
    public static final int TT_MS_LANGID_ARABIC_TUNISIA = 7169;
    public static final int TT_MS_LANGID_ARABIC_OMAN = 8193;
    public static final int TT_MS_LANGID_ARABIC_YEMEN = 9217;
    public static final int TT_MS_LANGID_ARABIC_SYRIA = 10241;
    public static final int TT_MS_LANGID_ARABIC_JORDAN = 11265;
    public static final int TT_MS_LANGID_ARABIC_LEBANON = 12289;
    public static final int TT_MS_LANGID_ARABIC_KUWAIT = 13313;
    public static final int TT_MS_LANGID_ARABIC_UAE = 14337;
    public static final int TT_MS_LANGID_ARABIC_BAHRAIN = 15361;
    public static final int TT_MS_LANGID_ARABIC_QATAR = 16385;
    public static final int TT_MS_LANGID_BULGARIAN_BULGARIA = 1026;
    public static final int TT_MS_LANGID_CATALAN_CATALAN = 1027;
    public static final int TT_MS_LANGID_CHINESE_TAIWAN = 1028;
    public static final int TT_MS_LANGID_CHINESE_PRC = 2052;
    public static final int TT_MS_LANGID_CHINESE_HONG_KONG = 3076;
    public static final int TT_MS_LANGID_CHINESE_SINGAPORE = 4100;
    public static final int TT_MS_LANGID_CHINESE_MACAO = 5124;
    public static final int TT_MS_LANGID_CZECH_CZECH_REPUBLIC = 1029;
    public static final int TT_MS_LANGID_DANISH_DENMARK = 1030;
    public static final int TT_MS_LANGID_GERMAN_GERMANY = 1031;
    public static final int TT_MS_LANGID_GERMAN_SWITZERLAND = 2055;
    public static final int TT_MS_LANGID_GERMAN_AUSTRIA = 3079;
    public static final int TT_MS_LANGID_GERMAN_LUXEMBOURG = 4103;
    public static final int TT_MS_LANGID_GERMAN_LIECHTENSTEIN = 5127;
    public static final int TT_MS_LANGID_GREEK_GREECE = 1032;
    public static final int TT_MS_LANGID_ENGLISH_UNITED_STATES = 1033;
    public static final int TT_MS_LANGID_ENGLISH_UNITED_KINGDOM = 2057;
    public static final int TT_MS_LANGID_ENGLISH_AUSTRALIA = 3081;
    public static final int TT_MS_LANGID_ENGLISH_CANADA = 4105;
    public static final int TT_MS_LANGID_ENGLISH_NEW_ZEALAND = 5129;
    public static final int TT_MS_LANGID_ENGLISH_IRELAND = 6153;
    public static final int TT_MS_LANGID_ENGLISH_SOUTH_AFRICA = 7177;
    public static final int TT_MS_LANGID_ENGLISH_JAMAICA = 8201;
    public static final int TT_MS_LANGID_ENGLISH_CARIBBEAN = 9225;
    public static final int TT_MS_LANGID_ENGLISH_BELIZE = 10249;
    public static final int TT_MS_LANGID_ENGLISH_TRINIDAD = 11273;
    public static final int TT_MS_LANGID_ENGLISH_ZIMBABWE = 12297;
    public static final int TT_MS_LANGID_ENGLISH_PHILIPPINES = 13321;
    public static final int TT_MS_LANGID_ENGLISH_INDIA = 16393;
    public static final int TT_MS_LANGID_ENGLISH_MALAYSIA = 17417;
    public static final int TT_MS_LANGID_ENGLISH_SINGAPORE = 18441;
    public static final int TT_MS_LANGID_SPANISH_SPAIN_TRADITIONAL_SORT = 1034;
    public static final int TT_MS_LANGID_SPANISH_MEXICO = 2058;
    public static final int TT_MS_LANGID_SPANISH_SPAIN_MODERN_SORT = 3082;
    public static final int TT_MS_LANGID_SPANISH_GUATEMALA = 4106;
    public static final int TT_MS_LANGID_SPANISH_COSTA_RICA = 5130;
    public static final int TT_MS_LANGID_SPANISH_PANAMA = 6154;
    public static final int TT_MS_LANGID_SPANISH_DOMINICAN_REPUBLIC = 7178;
    public static final int TT_MS_LANGID_SPANISH_VENEZUELA = 8202;
    public static final int TT_MS_LANGID_SPANISH_COLOMBIA = 9226;
    public static final int TT_MS_LANGID_SPANISH_PERU = 10250;
    public static final int TT_MS_LANGID_SPANISH_ARGENTINA = 11274;
    public static final int TT_MS_LANGID_SPANISH_ECUADOR = 12298;
    public static final int TT_MS_LANGID_SPANISH_CHILE = 13322;
    public static final int TT_MS_LANGID_SPANISH_URUGUAY = 14346;
    public static final int TT_MS_LANGID_SPANISH_PARAGUAY = 15370;
    public static final int TT_MS_LANGID_SPANISH_BOLIVIA = 16394;
    public static final int TT_MS_LANGID_SPANISH_EL_SALVADOR = 17418;
    public static final int TT_MS_LANGID_SPANISH_HONDURAS = 18442;
    public static final int TT_MS_LANGID_SPANISH_NICARAGUA = 19466;
    public static final int TT_MS_LANGID_SPANISH_PUERTO_RICO = 20490;
    public static final int TT_MS_LANGID_SPANISH_UNITED_STATES = 21514;
    public static final int TT_MS_LANGID_FINNISH_FINLAND = 1035;
    public static final int TT_MS_LANGID_FRENCH_FRANCE = 1036;
    public static final int TT_MS_LANGID_FRENCH_BELGIUM = 2060;
    public static final int TT_MS_LANGID_FRENCH_CANADA = 3084;
    public static final int TT_MS_LANGID_FRENCH_SWITZERLAND = 4108;
    public static final int TT_MS_LANGID_FRENCH_LUXEMBOURG = 5132;
    public static final int TT_MS_LANGID_FRENCH_MONACO = 6156;
    public static final int TT_MS_LANGID_HEBREW_ISRAEL = 1037;
    public static final int TT_MS_LANGID_HUNGARIAN_HUNGARY = 1038;
    public static final int TT_MS_LANGID_ICELANDIC_ICELAND = 1039;
    public static final int TT_MS_LANGID_ITALIAN_ITALY = 1040;
    public static final int TT_MS_LANGID_ITALIAN_SWITZERLAND = 2064;
    public static final int TT_MS_LANGID_JAPANESE_JAPAN = 1041;
    public static final int TT_MS_LANGID_KOREAN_KOREA = 1042;
    public static final int TT_MS_LANGID_DUTCH_NETHERLANDS = 1043;
    public static final int TT_MS_LANGID_DUTCH_BELGIUM = 2067;
    public static final int TT_MS_LANGID_NORWEGIAN_NORWAY_BOKMAL = 1044;
    public static final int TT_MS_LANGID_NORWEGIAN_NORWAY_NYNORSK = 2068;
    public static final int TT_MS_LANGID_POLISH_POLAND = 1045;
    public static final int TT_MS_LANGID_PORTUGUESE_BRAZIL = 1046;
    public static final int TT_MS_LANGID_PORTUGUESE_PORTUGAL = 2070;
    public static final int TT_MS_LANGID_ROMANSH_SWITZERLAND = 1047;
    public static final int TT_MS_LANGID_ROMANIAN_ROMANIA = 1048;
    public static final int TT_MS_LANGID_RUSSIAN_RUSSIA = 1049;
    public static final int TT_MS_LANGID_CROATIAN_CROATIA = 1050;
    public static final int TT_MS_LANGID_SERBIAN_SERBIA_LATIN = 2074;
    public static final int TT_MS_LANGID_SERBIAN_SERBIA_CYRILLIC = 3098;
    public static final int TT_MS_LANGID_CROATIAN_BOSNIA_HERZEGOVINA = 4122;
    public static final int TT_MS_LANGID_BOSNIAN_BOSNIA_HERZEGOVINA = 5146;
    public static final int TT_MS_LANGID_SERBIAN_BOSNIA_HERZ_LATIN = 6170;
    public static final int TT_MS_LANGID_SERBIAN_BOSNIA_HERZ_CYRILLIC = 7194;
    public static final int TT_MS_LANGID_BOSNIAN_BOSNIA_HERZ_CYRILLIC = 8218;
    public static final int TT_MS_LANGID_SLOVAK_SLOVAKIA = 1051;
    public static final int TT_MS_LANGID_ALBANIAN_ALBANIA = 1052;
    public static final int TT_MS_LANGID_SWEDISH_SWEDEN = 1053;
    public static final int TT_MS_LANGID_SWEDISH_FINLAND = 2077;
    public static final int TT_MS_LANGID_THAI_THAILAND = 1054;
    public static final int TT_MS_LANGID_TURKISH_TURKEY = 1055;
    public static final int TT_MS_LANGID_URDU_PAKISTAN = 1056;
    public static final int TT_MS_LANGID_INDONESIAN_INDONESIA = 1057;
    public static final int TT_MS_LANGID_UKRAINIAN_UKRAINE = 1058;
    public static final int TT_MS_LANGID_BELARUSIAN_BELARUS = 1059;
    public static final int TT_MS_LANGID_SLOVENIAN_SLOVENIA = 1060;
    public static final int TT_MS_LANGID_ESTONIAN_ESTONIA = 1061;
    public static final int TT_MS_LANGID_LATVIAN_LATVIA = 1062;
    public static final int TT_MS_LANGID_LITHUANIAN_LITHUANIA = 1063;
    public static final int TT_MS_LANGID_TAJIK_TAJIKISTAN = 1064;
    public static final int TT_MS_LANGID_VIETNAMESE_VIET_NAM = 1066;
    public static final int TT_MS_LANGID_ARMENIAN_ARMENIA = 1067;
    public static final int TT_MS_LANGID_AZERI_AZERBAIJAN_LATIN = 1068;
    public static final int TT_MS_LANGID_AZERI_AZERBAIJAN_CYRILLIC = 2092;
    public static final int TT_MS_LANGID_BASQUE_BASQUE = 1069;
    public static final int TT_MS_LANGID_UPPER_SORBIAN_GERMANY = 1070;
    public static final int TT_MS_LANGID_LOWER_SORBIAN_GERMANY = 2094;
    public static final int TT_MS_LANGID_MACEDONIAN_MACEDONIA = 1071;
    public static final int TT_MS_LANGID_SETSWANA_SOUTH_AFRICA = 1074;
    public static final int TT_MS_LANGID_ISIXHOSA_SOUTH_AFRICA = 1076;
    public static final int TT_MS_LANGID_ISIZULU_SOUTH_AFRICA = 1077;
    public static final int TT_MS_LANGID_AFRIKAANS_SOUTH_AFRICA = 1078;
    public static final int TT_MS_LANGID_GEORGIAN_GEORGIA = 1079;
    public static final int TT_MS_LANGID_FAEROESE_FAEROE_ISLANDS = 1080;
    public static final int TT_MS_LANGID_HINDI_INDIA = 1081;
    public static final int TT_MS_LANGID_MALTESE_MALTA = 1082;
    public static final int TT_MS_LANGID_SAMI_NORTHERN_NORWAY = 1083;
    public static final int TT_MS_LANGID_SAMI_NORTHERN_SWEDEN = 2107;
    public static final int TT_MS_LANGID_SAMI_NORTHERN_FINLAND = 3131;
    public static final int TT_MS_LANGID_SAMI_LULE_NORWAY = 4155;
    public static final int TT_MS_LANGID_SAMI_LULE_SWEDEN = 5179;
    public static final int TT_MS_LANGID_SAMI_SOUTHERN_NORWAY = 6203;
    public static final int TT_MS_LANGID_SAMI_SOUTHERN_SWEDEN = 7227;
    public static final int TT_MS_LANGID_SAMI_SKOLT_FINLAND = 8251;
    public static final int TT_MS_LANGID_SAMI_INARI_FINLAND = 9275;
    public static final int TT_MS_LANGID_IRISH_IRELAND = 2108;
    public static final int TT_MS_LANGID_MALAY_MALAYSIA = 1086;
    public static final int TT_MS_LANGID_MALAY_BRUNEI_DARUSSALAM = 2110;
    public static final int TT_MS_LANGID_KAZAKH_KAZAKHSTAN = 1087;
    public static final int TT_MS_LANGID_KYRGYZ_KYRGYZSTAN = 1088;
    public static final int TT_MS_LANGID_KISWAHILI_KENYA = 1089;
    public static final int TT_MS_LANGID_TURKMEN_TURKMENISTAN = 1090;
    public static final int TT_MS_LANGID_UZBEK_UZBEKISTAN_LATIN = 1091;
    public static final int TT_MS_LANGID_UZBEK_UZBEKISTAN_CYRILLIC = 2115;
    public static final int TT_MS_LANGID_TATAR_RUSSIA = 1092;
    public static final int TT_MS_LANGID_BENGALI_INDIA = 1093;
    public static final int TT_MS_LANGID_BENGALI_BANGLADESH = 2117;
    public static final int TT_MS_LANGID_PUNJABI_INDIA = 1094;
    public static final int TT_MS_LANGID_GUJARATI_INDIA = 1095;
    public static final int TT_MS_LANGID_ODIA_INDIA = 1096;
    public static final int TT_MS_LANGID_TAMIL_INDIA = 1097;
    public static final int TT_MS_LANGID_TELUGU_INDIA = 1098;
    public static final int TT_MS_LANGID_KANNADA_INDIA = 1099;
    public static final int TT_MS_LANGID_MALAYALAM_INDIA = 1100;
    public static final int TT_MS_LANGID_ASSAMESE_INDIA = 1101;
    public static final int TT_MS_LANGID_MARATHI_INDIA = 1102;
    public static final int TT_MS_LANGID_SANSKRIT_INDIA = 1103;
    public static final int TT_MS_LANGID_MONGOLIAN_MONGOLIA = 1104;
    public static final int TT_MS_LANGID_MONGOLIAN_PRC = 2128;
    public static final int TT_MS_LANGID_TIBETAN_PRC = 1105;
    public static final int TT_MS_LANGID_WELSH_UNITED_KINGDOM = 1106;
    public static final int TT_MS_LANGID_KHMER_CAMBODIA = 1107;
    public static final int TT_MS_LANGID_LAO_LAOS = 1108;
    public static final int TT_MS_LANGID_GALICIAN_GALICIAN = 1110;
    public static final int TT_MS_LANGID_KONKANI_INDIA = 1111;
    public static final int TT_MS_LANGID_SYRIAC_SYRIA = 1114;
    public static final int TT_MS_LANGID_SINHALA_SRI_LANKA = 1115;
    public static final int TT_MS_LANGID_INUKTITUT_CANADA = 1117;
    public static final int TT_MS_LANGID_INUKTITUT_CANADA_LATIN = 2141;
    public static final int TT_MS_LANGID_AMHARIC_ETHIOPIA = 1118;
    public static final int TT_MS_LANGID_TAMAZIGHT_ALGERIA = 2143;
    public static final int TT_MS_LANGID_NEPALI_NEPAL = 1121;
    public static final int TT_MS_LANGID_FRISIAN_NETHERLANDS = 1122;
    public static final int TT_MS_LANGID_PASHTO_AFGHANISTAN = 1123;
    public static final int TT_MS_LANGID_FILIPINO_PHILIPPINES = 1124;
    public static final int TT_MS_LANGID_DHIVEHI_MALDIVES = 1125;
    public static final int TT_MS_LANGID_HAUSA_NIGERIA = 1128;
    public static final int TT_MS_LANGID_YORUBA_NIGERIA = 1130;
    public static final int TT_MS_LANGID_QUECHUA_BOLIVIA = 1131;
    public static final int TT_MS_LANGID_QUECHUA_ECUADOR = 2155;
    public static final int TT_MS_LANGID_QUECHUA_PERU = 3179;
    public static final int TT_MS_LANGID_SESOTHO_SA_LEBOA_SOUTH_AFRICA = 1132;
    public static final int TT_MS_LANGID_BASHKIR_RUSSIA = 1133;
    public static final int TT_MS_LANGID_LUXEMBOURGISH_LUXEMBOURG = 1134;
    public static final int TT_MS_LANGID_GREENLANDIC_GREENLAND = 1135;
    public static final int TT_MS_LANGID_IGBO_NIGERIA = 1136;
    public static final int TT_MS_LANGID_YI_PRC = 1144;
    public static final int TT_MS_LANGID_MAPUDUNGUN_CHILE = 1146;
    public static final int TT_MS_LANGID_MOHAWK_MOHAWK = 1148;
    public static final int TT_MS_LANGID_BRETON_FRANCE = 1150;
    public static final int TT_MS_LANGID_UIGHUR_PRC = 1152;
    public static final int TT_MS_LANGID_MAORI_NEW_ZEALAND = 1153;
    public static final int TT_MS_LANGID_OCCITAN_FRANCE = 1154;
    public static final int TT_MS_LANGID_CORSICAN_FRANCE = 1155;
    public static final int TT_MS_LANGID_ALSATIAN_FRANCE = 1156;
    public static final int TT_MS_LANGID_YAKUT_RUSSIA = 1157;
    public static final int TT_MS_LANGID_KICHE_GUATEMALA = 1158;
    public static final int TT_MS_LANGID_KINYARWANDA_RWANDA = 1159;
    public static final int TT_MS_LANGID_WOLOF_SENEGAL = 1160;
    public static final int TT_MS_LANGID_DARI_AFGHANISTAN = 1164;
    public static final int TT_NAME_ID_COPYRIGHT = 0;
    public static final int TT_NAME_ID_FONT_FAMILY = 1;
    public static final int TT_NAME_ID_FONT_SUBFAMILY = 2;
    public static final int TT_NAME_ID_UNIQUE_ID = 3;
    public static final int TT_NAME_ID_FULL_NAME = 4;
    public static final int TT_NAME_ID_VERSION_STRING = 5;
    public static final int TT_NAME_ID_PS_NAME = 6;
    public static final int TT_NAME_ID_TRADEMARK = 7;
    public static final int TT_NAME_ID_MANUFACTURER = 8;
    public static final int TT_NAME_ID_DESIGNER = 9;
    public static final int TT_NAME_ID_DESCRIPTION = 10;
    public static final int TT_NAME_ID_VENDOR_URL = 11;
    public static final int TT_NAME_ID_DESIGNER_URL = 12;
    public static final int TT_NAME_ID_LICENSE = 13;
    public static final int TT_NAME_ID_LICENSE_URL = 14;
    public static final int TT_NAME_ID_TYPOGRAPHIC_FAMILY = 16;
    public static final int TT_NAME_ID_TYPOGRAPHIC_SUBFAMILY = 17;
    public static final int TT_NAME_ID_MAC_FULL_NAME = 18;
    public static final int TT_NAME_ID_SAMPLE_TEXT = 19;
    public static final int TT_NAME_ID_CID_FINDFONT_NAME = 20;
    public static final int TT_NAME_ID_WWS_FAMILY = 21;
    public static final int TT_NAME_ID_WWS_SUBFAMILY = 22;
    public static final int TT_NAME_ID_LIGHT_BACKGROUND = 23;
    public static final int TT_NAME_ID_DARK_BACKGROUND = 24;
    public static final int TT_NAME_ID_VARIATIONS_PREFIX = 25;
    public static final int TT_UCR_BASIC_LATIN = 1;
    public static final int TT_UCR_LATIN1_SUPPLEMENT = 2;
    public static final int TT_UCR_LATIN_EXTENDED_A = 4;
    public static final int TT_UCR_LATIN_EXTENDED_B = 8;
    public static final int TT_UCR_IPA_EXTENSIONS = 16;
    public static final int TT_UCR_SPACING_MODIFIER = 32;
    public static final int TT_UCR_COMBINING_DIACRITICAL_MARKS = 64;
    public static final int TT_UCR_GREEK = 128;
    public static final int TT_UCR_COPTIC = 256;
    public static final int TT_UCR_CYRILLIC = 512;
    public static final int TT_UCR_ARMENIAN = 1024;
    public static final int TT_UCR_HEBREW = 2048;
    public static final int TT_UCR_VAI = 4096;
    public static final int TT_UCR_ARABIC = 8192;
    public static final int TT_UCR_NKO = 16384;
    public static final int TT_UCR_DEVANAGARI = 32768;
    public static final int TT_UCR_BENGALI = 65536;
    public static final int TT_UCR_GURMUKHI = 131072;
    public static final int TT_UCR_GUJARATI = 262144;
    public static final int TT_UCR_ORIYA = 524288;
    public static final int TT_UCR_TAMIL = 0x100000;
    public static final int TT_UCR_TELUGU = 0x200000;
    public static final int TT_UCR_KANNADA = 0x400000;
    public static final int TT_UCR_MALAYALAM = 0x800000;
    public static final int TT_UCR_THAI = 0x1000000;
    public static final int TT_UCR_LAO = 0x2000000;
    public static final int TT_UCR_GEORGIAN = 0x4000000;
    public static final int TT_UCR_BALINESE = 0x8000000;
    public static final int TT_UCR_HANGUL_JAMO = 0x10000000;
    public static final int TT_UCR_LATIN_EXTENDED_ADDITIONAL = 0x20000000;
    public static final int TT_UCR_GREEK_EXTENDED = 0x40000000;
    public static final int TT_UCR_GENERAL_PUNCTUATION = Integer.MIN_VALUE;
    public static final int TT_UCR_SUPERSCRIPTS_SUBSCRIPTS = 1;
    public static final int TT_UCR_CURRENCY_SYMBOLS = 2;
    public static final int TT_UCR_COMBINING_DIACRITICAL_MARKS_SYMB = 4;
    public static final int TT_UCR_LETTERLIKE_SYMBOLS = 8;
    public static final int TT_UCR_NUMBER_FORMS = 16;
    public static final int TT_UCR_ARROWS = 32;
    public static final int TT_UCR_MATHEMATICAL_OPERATORS = 64;
    public static final int TT_UCR_MISCELLANEOUS_TECHNICAL = 128;
    public static final int TT_UCR_CONTROL_PICTURES = 256;
    public static final int TT_UCR_OCR = 512;
    public static final int TT_UCR_ENCLOSED_ALPHANUMERICS = 1024;
    public static final int TT_UCR_BOX_DRAWING = 2048;
    public static final int TT_UCR_BLOCK_ELEMENTS = 4096;
    public static final int TT_UCR_GEOMETRIC_SHAPES = 8192;
    public static final int TT_UCR_MISCELLANEOUS_SYMBOLS = 16384;
    public static final int TT_UCR_DINGBATS = 32768;
    public static final int TT_UCR_CJK_SYMBOLS = 65536;
    public static final int TT_UCR_HIRAGANA = 131072;
    public static final int TT_UCR_KATAKANA = 262144;
    public static final int TT_UCR_BOPOMOFO = 524288;
    public static final int TT_UCR_HANGUL_COMPATIBILITY_JAMO = 0x100000;
    public static final int TT_UCR_CJK_MISC = 0x200000;
    public static final int TT_UCR_ENCLOSED_CJK_LETTERS_MONTHS = 0x400000;
    public static final int TT_UCR_CJK_COMPATIBILITY = 0x800000;
    public static final int TT_UCR_HANGUL = 0x1000000;
    public static final int TT_UCR_SURROGATES = 0x2000000;
    public static final int TT_UCR_PHOENICIAN = 0x4000000;
    public static final int TT_UCR_CJK_UNIFIED_IDEOGRAPHS = 0x8000000;
    public static final int TT_UCR_PRIVATE_USE = 0x10000000;
    public static final int TT_UCR_CJK_COMPATIBILITY_IDEOGRAPHS = 0x20000000;
    public static final int TT_UCR_ALPHABETIC_PRESENTATION_FORMS = 0x40000000;
    public static final int TT_UCR_ARABIC_PRESENTATION_FORMS_A = Integer.MIN_VALUE;
    public static final int TT_UCR_COMBINING_HALF_MARKS = 1;
    public static final int TT_UCR_CJK_COMPATIBILITY_FORMS = 2;
    public static final int TT_UCR_SMALL_FORM_VARIANTS = 4;
    public static final int TT_UCR_ARABIC_PRESENTATION_FORMS_B = 8;
    public static final int TT_UCR_HALFWIDTH_FULLWIDTH_FORMS = 16;
    public static final int TT_UCR_SPECIALS = 32;
    public static final int TT_UCR_TIBETAN = 64;
    public static final int TT_UCR_SYRIAC = 128;
    public static final int TT_UCR_THAANA = 256;
    public static final int TT_UCR_SINHALA = 512;
    public static final int TT_UCR_MYANMAR = 1024;
    public static final int TT_UCR_ETHIOPIC = 2048;
    public static final int TT_UCR_CHEROKEE = 4096;
    public static final int TT_UCR_CANADIAN_ABORIGINAL_SYLLABICS = 8192;
    public static final int TT_UCR_OGHAM = 16384;
    public static final int TT_UCR_RUNIC = 32768;
    public static final int TT_UCR_KHMER = 65536;
    public static final int TT_UCR_MONGOLIAN = 131072;
    public static final int TT_UCR_BRAILLE = 262144;
    public static final int TT_UCR_YI = 524288;
    public static final int TT_UCR_PHILIPPINE = 0x100000;
    public static final int TT_UCR_OLD_ITALIC = 0x200000;
    public static final int TT_UCR_GOTHIC = 0x400000;
    public static final int TT_UCR_DESERET = 0x800000;
    public static final int TT_UCR_MUSICAL_SYMBOLS = 0x1000000;
    public static final int TT_UCR_MATH_ALPHANUMERIC_SYMBOLS = 0x2000000;
    public static final int TT_UCR_PRIVATE_USE_SUPPLEMENTARY = 0x4000000;
    public static final int TT_UCR_VARIATION_SELECTORS = 0x8000000;
    public static final int TT_UCR_TAGS = 0x10000000;
    public static final int TT_UCR_LIMBU = 0x20000000;
    public static final int TT_UCR_TAI_LE = 0x40000000;
    public static final int TT_UCR_NEW_TAI_LUE = Integer.MIN_VALUE;
    public static final int TT_UCR_BUGINESE = 1;
    public static final int TT_UCR_GLAGOLITIC = 2;
    public static final int TT_UCR_TIFINAGH = 4;
    public static final int TT_UCR_YIJING = 8;
    public static final int TT_UCR_SYLOTI_NAGRI = 16;
    public static final int TT_UCR_LINEAR_B = 32;
    public static final int TT_UCR_ANCIENT_GREEK_NUMBERS = 64;
    public static final int TT_UCR_UGARITIC = 128;
    public static final int TT_UCR_OLD_PERSIAN = 256;
    public static final int TT_UCR_SHAVIAN = 512;
    public static final int TT_UCR_OSMANYA = 1024;
    public static final int TT_UCR_CYPRIOT_SYLLABARY = 2048;
    public static final int TT_UCR_KHAROSHTHI = 4096;
    public static final int TT_UCR_TAI_XUAN_JING = 8192;
    public static final int TT_UCR_CUNEIFORM = 16384;
    public static final int TT_UCR_COUNTING_ROD_NUMERALS = 32768;
    public static final int TT_UCR_SUNDANESE = 65536;
    public static final int TT_UCR_LEPCHA = 131072;
    public static final int TT_UCR_OL_CHIKI = 262144;
    public static final int TT_UCR_SAURASHTRA = 524288;
    public static final int TT_UCR_KAYAH_LI = 0x100000;
    public static final int TT_UCR_REJANG = 0x200000;
    public static final int TT_UCR_CHAM = 0x400000;
    public static final int TT_UCR_ANCIENT_SYMBOLS = 0x800000;
    public static final int TT_UCR_PHAISTOS_DISC = 0x1000000;
    public static final int TT_UCR_OLD_ANATOLIAN = 0x2000000;
    public static final int TT_UCR_GAME_TILES = 0x4000000;
    public static final int FT_SFNT_HEAD = 0;
    public static final int FT_SFNT_MAXP = 1;
    public static final int FT_SFNT_OS2 = 2;
    public static final int FT_SFNT_HHEA = 3;
    public static final int FT_SFNT_VHEA = 4;
    public static final int FT_SFNT_POST = 5;
    public static final int FT_SFNT_PCLT = 6;
    public static final int FT_SFNT_MAX = 7;
    public static final int TTAG_avar = FreeType.FT_MAKE_TAG(97, 118, 97, 114);
    public static final int TTAG_BASE = FreeType.FT_MAKE_TAG(66, 65, 83, 69);
    public static final int TTAG_bdat = FreeType.FT_MAKE_TAG(98, 100, 97, 116);
    public static final int TTAG_BDF = FreeType.FT_MAKE_TAG(66, 68, 70, 32);
    public static final int TTAG_bhed = FreeType.FT_MAKE_TAG(98, 104, 101, 100);
    public static final int TTAG_bloc = FreeType.FT_MAKE_TAG(98, 108, 111, 99);
    public static final int TTAG_bsln = FreeType.FT_MAKE_TAG(98, 115, 108, 110);
    public static final int TTAG_CBDT = FreeType.FT_MAKE_TAG(67, 66, 68, 84);
    public static final int TTAG_CBLC = FreeType.FT_MAKE_TAG(67, 66, 76, 67);
    public static final int TTAG_CFF = FreeType.FT_MAKE_TAG(67, 70, 70, 32);
    public static final int TTAG_CFF2 = FreeType.FT_MAKE_TAG(67, 70, 70, 50);
    public static final int TTAG_CID = FreeType.FT_MAKE_TAG(67, 73, 68, 32);
    public static final int TTAG_cmap = FreeType.FT_MAKE_TAG(99, 109, 97, 112);
    public static final int TTAG_COLR = FreeType.FT_MAKE_TAG(67, 79, 76, 82);
    public static final int TTAG_CPAL = FreeType.FT_MAKE_TAG(67, 80, 65, 76);
    public static final int TTAG_cvar = FreeType.FT_MAKE_TAG(99, 118, 97, 114);
    public static final int TTAG_cvt = FreeType.FT_MAKE_TAG(99, 118, 116, 32);
    public static final int TTAG_DSIG = FreeType.FT_MAKE_TAG(68, 83, 73, 71);
    public static final int TTAG_EBDT = FreeType.FT_MAKE_TAG(69, 66, 68, 84);
    public static final int TTAG_EBLC = FreeType.FT_MAKE_TAG(69, 66, 76, 67);
    public static final int TTAG_EBSC = FreeType.FT_MAKE_TAG(69, 66, 83, 67);
    public static final int TTAG_feat = FreeType.FT_MAKE_TAG(102, 101, 97, 116);
    public static final int TTAG_FOND = FreeType.FT_MAKE_TAG(70, 79, 78, 68);
    public static final int TTAG_fpgm = FreeType.FT_MAKE_TAG(102, 112, 103, 109);
    public static final int TTAG_fvar = FreeType.FT_MAKE_TAG(102, 118, 97, 114);
    public static final int TTAG_gasp = FreeType.FT_MAKE_TAG(103, 97, 115, 112);
    public static final int TTAG_GDEF = FreeType.FT_MAKE_TAG(71, 68, 69, 70);
    public static final int TTAG_glyf = FreeType.FT_MAKE_TAG(103, 108, 121, 102);
    public static final int TTAG_GPOS = FreeType.FT_MAKE_TAG(71, 80, 79, 83);
    public static final int TTAG_GSUB = FreeType.FT_MAKE_TAG(71, 83, 85, 66);
    public static final int TTAG_gvar = FreeType.FT_MAKE_TAG(103, 118, 97, 114);
    public static final int TTAG_HVAR = FreeType.FT_MAKE_TAG(72, 86, 65, 82);
    public static final int TTAG_hdmx = FreeType.FT_MAKE_TAG(104, 100, 109, 120);
    public static final int TTAG_head = FreeType.FT_MAKE_TAG(104, 101, 97, 100);
    public static final int TTAG_hhea = FreeType.FT_MAKE_TAG(104, 104, 101, 97);
    public static final int TTAG_hmtx = FreeType.FT_MAKE_TAG(104, 109, 116, 120);
    public static final int TTAG_JSTF = FreeType.FT_MAKE_TAG(74, 83, 84, 70);
    public static final int TTAG_just = FreeType.FT_MAKE_TAG(106, 117, 115, 116);
    public static final int TTAG_kern = FreeType.FT_MAKE_TAG(107, 101, 114, 110);
    public static final int TTAG_lcar = FreeType.FT_MAKE_TAG(108, 99, 97, 114);
    public static final int TTAG_loca = FreeType.FT_MAKE_TAG(108, 111, 99, 97);
    public static final int TTAG_LTSH = FreeType.FT_MAKE_TAG(76, 84, 83, 72);
    public static final int TTAG_LWFN = FreeType.FT_MAKE_TAG(76, 87, 70, 78);
    public static final int TTAG_MATH = FreeType.FT_MAKE_TAG(77, 65, 84, 72);
    public static final int TTAG_maxp = FreeType.FT_MAKE_TAG(109, 97, 120, 112);
    public static final int TTAG_META = FreeType.FT_MAKE_TAG(77, 69, 84, 65);
    public static final int TTAG_MMFX = FreeType.FT_MAKE_TAG(77, 77, 70, 88);
    public static final int TTAG_MMSD = FreeType.FT_MAKE_TAG(77, 77, 83, 68);
    public static final int TTAG_mort = FreeType.FT_MAKE_TAG(109, 111, 114, 116);
    public static final int TTAG_morx = FreeType.FT_MAKE_TAG(109, 111, 114, 120);
    public static final int TTAG_MVAR = FreeType.FT_MAKE_TAG(77, 86, 65, 82);
    public static final int TTAG_name = FreeType.FT_MAKE_TAG(110, 97, 109, 101);
    public static final int TTAG_opbd = FreeType.FT_MAKE_TAG(111, 112, 98, 100);
    public static final int TTAG_OS2 = FreeType.FT_MAKE_TAG(79, 83, 47, 50);
    public static final int TTAG_OTTO = FreeType.FT_MAKE_TAG(79, 84, 84, 79);
    public static final int TTAG_PCLT = FreeType.FT_MAKE_TAG(80, 67, 76, 84);
    public static final int TTAG_POST = FreeType.FT_MAKE_TAG(80, 79, 83, 84);
    public static final int TTAG_post = FreeType.FT_MAKE_TAG(112, 111, 115, 116);
    public static final int TTAG_prep = FreeType.FT_MAKE_TAG(112, 114, 101, 112);
    public static final int TTAG_prop = FreeType.FT_MAKE_TAG(112, 114, 111, 112);
    public static final int TTAG_sbix = FreeType.FT_MAKE_TAG(115, 98, 105, 120);
    public static final int TTAG_sfnt = FreeType.FT_MAKE_TAG(115, 102, 110, 116);
    public static final int TTAG_SING = FreeType.FT_MAKE_TAG(83, 73, 78, 71);
    public static final int TTAG_SVG = FreeType.FT_MAKE_TAG(83, 86, 71, 32);
    public static final int TTAG_trak = FreeType.FT_MAKE_TAG(116, 114, 97, 107);
    public static final int TTAG_true = FreeType.FT_MAKE_TAG(116, 114, 117, 101);
    public static final int TTAG_ttc = FreeType.FT_MAKE_TAG(116, 116, 99, 32);
    public static final int TTAG_ttcf = FreeType.FT_MAKE_TAG(116, 116, 99, 102);
    public static final int TTAG_TYP1 = FreeType.FT_MAKE_TAG(84, 89, 80, 49);
    public static final int TTAG_typ1 = FreeType.FT_MAKE_TAG(116, 121, 112, 49);
    public static final int TTAG_VDMX = FreeType.FT_MAKE_TAG(86, 68, 77, 88);
    public static final int TTAG_vhea = FreeType.FT_MAKE_TAG(118, 104, 101, 97);
    public static final int TTAG_vmtx = FreeType.FT_MAKE_TAG(118, 109, 116, 120);
    public static final int TTAG_VVAR = FreeType.FT_MAKE_TAG(86, 86, 65, 82);
    public static final int TTAG_wOFF = FreeType.FT_MAKE_TAG(119, 79, 70, 70);
    public static final int TTAG_wOF2 = FreeType.FT_MAKE_TAG(119, 79, 70, 50);
    public static final int TTAG_0xA5kbd = FreeType.FT_MAKE_TAG(165, 107, 98, 100);
    public static final int TTAG_0xA5lst = FreeType.FT_MAKE_TAG(165, 108, 115, 116);
    private static final FFICIF FT_Bitmap_BlendCIF = APIUtil.apiCreateCIF(LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, APIUtil.apiCreateStruct(LibFFI.ffi_type_slong, LibFFI.ffi_type_slong), LibFFI.ffi_type_pointer, LibFFI.ffi_type_pointer, APIUtil.apiCreateStruct(LibFFI.ffi_type_uint8, LibFFI.ffi_type_uint8, LibFFI.ffi_type_uint8, LibFFI.ffi_type_uint8));
    private static final FFICIF FT_Palette_Set_Foreground_ColorCIF = APIUtil.apiCreateCIF(LibFFI.ffi_type_sint32, LibFFI.ffi_type_pointer, APIUtil.apiCreateStruct(LibFFI.ffi_type_uint8, LibFFI.ffi_type_uint8, LibFFI.ffi_type_uint8, LibFFI.ffi_type_uint8));
    private static final FFICIF FT_Get_PaintCIF = APIUtil.apiCreateCIF(LibFFI.ffi_type_uint8, LibFFI.ffi_type_pointer, APIUtil.apiCreateStruct(LibFFI.ffi_type_pointer, LibFFI.ffi_type_uint8), LibFFI.ffi_type_pointer);

    public static SharedLibrary getLibrary() {
        return FREETYPE;
    }

    protected FreeType() {
        throw new UnsupportedOperationException();
    }

    public static int nFT_Init_FreeType(long alibrary) {
        long __functionAddress = Functions.Init_FreeType;
        return JNI.invokePI(alibrary, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Init_FreeType(@NativeType(value="FT_Library *") PointerBuffer alibrary) {
        if (Checks.CHECKS) {
            Checks.check(alibrary, 1);
        }
        return FreeType.nFT_Init_FreeType(MemoryUtil.memAddress(alibrary));
    }

    @NativeType(value="FT_Error")
    public static int FT_Done_FreeType(@NativeType(value="FT_Library") long library) {
        long __functionAddress = Functions.Done_FreeType;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePI(library, __functionAddress);
    }

    public static int nFT_New_Face(long library, long filepathname, long face_index, long aface) {
        long __functionAddress = Functions.New_Face;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPNPI(library, filepathname, face_index, aface, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_New_Face(@NativeType(value="FT_Library") long library, @NativeType(value="char const *") ByteBuffer filepathname, @NativeType(value="FT_Long") long face_index, @NativeType(value="FT_Face *") PointerBuffer aface) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filepathname);
            Checks.check(aface, 1);
        }
        return FreeType.nFT_New_Face(library, MemoryUtil.memAddress(filepathname), face_index, MemoryUtil.memAddress(aface));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="FT_Error")
    public static int FT_New_Face(@NativeType(value="FT_Library") long library, @NativeType(value="char const *") CharSequence filepathname, @NativeType(value="FT_Long") long face_index, @NativeType(value="FT_Face *") PointerBuffer aface) {
        if (Checks.CHECKS) {
            Checks.check(aface, 1);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filepathname, true);
            long filepathnameEncoded = stack.getPointerAddress();
            int n = FreeType.nFT_New_Face(library, filepathnameEncoded, face_index, MemoryUtil.memAddress(aface));
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static int nFT_New_Memory_Face(long library, long file_base, long file_size, long face_index, long aface) {
        long __functionAddress = Functions.New_Memory_Face;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPNNPI(library, file_base, file_size, face_index, aface, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_New_Memory_Face(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Byte const *") ByteBuffer file_base, @NativeType(value="FT_Long") long face_index, @NativeType(value="FT_Face *") PointerBuffer aface) {
        if (Checks.CHECKS) {
            Checks.check(aface, 1);
        }
        return FreeType.nFT_New_Memory_Face(library, MemoryUtil.memAddress(file_base), file_base.remaining(), face_index, MemoryUtil.memAddress(aface));
    }

    public static int nFT_Open_Face(long library, long args, long face_index, long aface) {
        long __functionAddress = Functions.Open_Face;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPNPI(library, args, face_index, aface, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Open_Face(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Open_Args const *") FT_Open_Args args, @NativeType(value="FT_Long") long face_index, @NativeType(value="FT_Face *") PointerBuffer aface) {
        if (Checks.CHECKS) {
            Checks.check(aface, 1);
        }
        return FreeType.nFT_Open_Face(library, args.address(), face_index, MemoryUtil.memAddress(aface));
    }

    public static int nFT_Attach_File(long face, long filepathname) {
        long __functionAddress = Functions.Attach_File;
        return JNI.invokePPI(face, filepathname, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Attach_File(FT_Face face, @NativeType(value="char const *") ByteBuffer filepathname) {
        if (Checks.CHECKS) {
            Checks.checkNT1(filepathname);
        }
        return FreeType.nFT_Attach_File(face.address(), MemoryUtil.memAddress(filepathname));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="FT_Error")
    public static int FT_Attach_File(FT_Face face, @NativeType(value="char const *") CharSequence filepathname) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(filepathname, true);
            long filepathnameEncoded = stack.getPointerAddress();
            int n = FreeType.nFT_Attach_File(face.address(), filepathnameEncoded);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static int nFT_Attach_Stream(long face, long parameters) {
        long __functionAddress = Functions.Attach_Stream;
        return JNI.invokePPI(face, parameters, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Attach_Stream(FT_Face face, @NativeType(value="FT_Open_Args const *") FT_Open_Args parameters) {
        return FreeType.nFT_Attach_Stream(face.address(), parameters.address());
    }

    public static int nFT_Reference_Face(long face) {
        long __functionAddress = Functions.Reference_Face;
        return JNI.invokePI(face, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Reference_Face(FT_Face face) {
        return FreeType.nFT_Reference_Face(face.address());
    }

    public static int nFT_Done_Face(long face) {
        long __functionAddress = Functions.Done_Face;
        return JNI.invokePI(face, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Done_Face(FT_Face face) {
        return FreeType.nFT_Done_Face(face.address());
    }

    public static int nFT_Select_Size(long face, int strike_index) {
        long __functionAddress = Functions.Select_Size;
        return JNI.invokePI(face, strike_index, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Select_Size(FT_Face face, @NativeType(value="FT_Int") int strike_index) {
        return FreeType.nFT_Select_Size(face.address(), strike_index);
    }

    public static int nFT_Request_Size(long face, long req) {
        long __functionAddress = Functions.Request_Size;
        return JNI.invokePPI(face, req, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Request_Size(FT_Face face, FT_Size_Request req) {
        return FreeType.nFT_Request_Size(face.address(), req.address());
    }

    public static int nFT_Set_Char_Size(long face, long char_width, long char_height, int horz_resolution, int vert_resolution) {
        long __functionAddress = Functions.Set_Char_Size;
        return JNI.invokePNNI(face, char_width, char_height, horz_resolution, vert_resolution, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_Char_Size(FT_Face face, @NativeType(value="FT_F26Dot6") long char_width, @NativeType(value="FT_F26Dot6") long char_height, @NativeType(value="FT_UInt") int horz_resolution, @NativeType(value="FT_UInt") int vert_resolution) {
        return FreeType.nFT_Set_Char_Size(face.address(), char_width, char_height, horz_resolution, vert_resolution);
    }

    public static int nFT_Set_Pixel_Sizes(long face, int pixel_width, int pixel_height) {
        long __functionAddress = Functions.Set_Pixel_Sizes;
        return JNI.invokePI(face, pixel_width, pixel_height, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_Pixel_Sizes(FT_Face face, @NativeType(value="FT_UInt") int pixel_width, @NativeType(value="FT_UInt") int pixel_height) {
        return FreeType.nFT_Set_Pixel_Sizes(face.address(), pixel_width, pixel_height);
    }

    public static int nFT_Load_Glyph(long face, int glyph_index, int load_flags) {
        long __functionAddress = Functions.Load_Glyph;
        return JNI.invokePI(face, glyph_index, load_flags, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Load_Glyph(FT_Face face, @NativeType(value="FT_UInt") int glyph_index, @NativeType(value="FT_Int32") int load_flags) {
        return FreeType.nFT_Load_Glyph(face.address(), glyph_index, load_flags);
    }

    public static int nFT_Load_Char(long face, long char_code, int load_flags) {
        long __functionAddress = Functions.Load_Char;
        return JNI.invokePNI(face, char_code, load_flags, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Load_Char(FT_Face face, @NativeType(value="FT_ULong") long char_code, @NativeType(value="FT_Int32") int load_flags) {
        return FreeType.nFT_Load_Char(face.address(), char_code, load_flags);
    }

    public static void nFT_Set_Transform(long face, long matrix, long delta) {
        long __functionAddress = Functions.Set_Transform;
        JNI.invokePPPV(face, matrix, delta, __functionAddress);
    }

    public static void FT_Set_Transform(FT_Face face, @NativeType(value="FT_Matrix *") @Nullable FT_Matrix matrix, @NativeType(value="FT_Vector *") @Nullable FT_Vector delta) {
        FreeType.nFT_Set_Transform(face.address(), MemoryUtil.memAddressSafe(matrix), MemoryUtil.memAddressSafe(delta));
    }

    public static void nFT_Get_Transform(long face, long matrix, long delta) {
        long __functionAddress = Functions.Get_Transform;
        JNI.invokePPPV(face, matrix, delta, __functionAddress);
    }

    public static void FT_Get_Transform(FT_Face face, @NativeType(value="FT_Matrix *") @Nullable FT_Matrix matrix, @NativeType(value="FT_Vector *") @Nullable FT_Vector delta) {
        FreeType.nFT_Get_Transform(face.address(), MemoryUtil.memAddressSafe(matrix), MemoryUtil.memAddressSafe(delta));
    }

    public static int nFT_Render_Glyph(long slot, int render_mode) {
        long __functionAddress = Functions.Render_Glyph;
        return JNI.invokePI(slot, render_mode, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Render_Glyph(FT_GlyphSlot slot, @NativeType(value="FT_Render_Mode") int render_mode) {
        return FreeType.nFT_Render_Glyph(slot.address(), render_mode);
    }

    public static int nFT_Get_Kerning(long face, int left_glyph, int right_glyph, int kern_mode, long akerning) {
        long __functionAddress = Functions.Get_Kerning;
        return JNI.invokePPI(face, left_glyph, right_glyph, kern_mode, akerning, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Kerning(FT_Face face, @NativeType(value="FT_UInt") int left_glyph, @NativeType(value="FT_UInt") int right_glyph, @NativeType(value="FT_UInt") int kern_mode, @NativeType(value="FT_Vector *") FT_Vector akerning) {
        return FreeType.nFT_Get_Kerning(face.address(), left_glyph, right_glyph, kern_mode, akerning.address());
    }

    public static int nFT_Get_Track_Kerning(long face, long point_size, int degree, long akerning) {
        long __functionAddress = Functions.Get_Track_Kerning;
        return JNI.invokePNPI(face, point_size, degree, akerning, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Track_Kerning(FT_Face face, @NativeType(value="FT_Fixed") long point_size, @NativeType(value="FT_Int") int degree, @NativeType(value="FT_Fixed *") CLongBuffer akerning) {
        if (Checks.CHECKS) {
            Checks.check(akerning, 1);
        }
        return FreeType.nFT_Get_Track_Kerning(face.address(), point_size, degree, MemoryUtil.memAddress(akerning));
    }

    public static int nFT_Select_Charmap(long face, int encoding) {
        long __functionAddress = Functions.Select_Charmap;
        return JNI.invokePI(face, encoding, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Select_Charmap(FT_Face face, @NativeType(value="FT_Encoding") int encoding) {
        return FreeType.nFT_Select_Charmap(face.address(), encoding);
    }

    public static int nFT_Set_Charmap(long face, long charmap) {
        long __functionAddress = Functions.Set_Charmap;
        return JNI.invokePPI(face, charmap, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_Charmap(FT_Face face, FT_CharMap charmap) {
        return FreeType.nFT_Set_Charmap(face.address(), charmap.address());
    }

    public static int nFT_Get_Charmap_Index(long charmap) {
        long __functionAddress = Functions.Get_Charmap_Index;
        return JNI.invokePI(charmap, __functionAddress);
    }

    @NativeType(value="FT_Int")
    public static int FT_Get_Charmap_Index(FT_CharMap charmap) {
        return FreeType.nFT_Get_Charmap_Index(charmap.address());
    }

    public static int nFT_Get_Char_Index(long face, long charcode) {
        long __functionAddress = Functions.Get_Char_Index;
        return JNI.invokePNI(face, charcode, __functionAddress);
    }

    @NativeType(value="FT_UInt")
    public static int FT_Get_Char_Index(FT_Face face, @NativeType(value="FT_ULong") long charcode) {
        return FreeType.nFT_Get_Char_Index(face.address(), charcode);
    }

    public static long nFT_Get_First_Char(long face, long agindex) {
        long __functionAddress = Functions.Get_First_Char;
        return JNI.invokePPN(face, agindex, __functionAddress);
    }

    @NativeType(value="FT_ULong")
    public static long FT_Get_First_Char(FT_Face face, @NativeType(value="FT_UInt *") IntBuffer agindex) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)agindex, 1);
        }
        return FreeType.nFT_Get_First_Char(face.address(), MemoryUtil.memAddress(agindex));
    }

    public static long nFT_Get_Next_Char(long face, long char_code, long agindex) {
        long __functionAddress = Functions.Get_Next_Char;
        return JNI.invokePNPN(face, char_code, agindex, __functionAddress);
    }

    @NativeType(value="FT_ULong")
    public static long FT_Get_Next_Char(FT_Face face, @NativeType(value="FT_ULong") long char_code, @NativeType(value="FT_UInt *") IntBuffer agindex) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)agindex, 1);
        }
        return FreeType.nFT_Get_Next_Char(face.address(), char_code, MemoryUtil.memAddress(agindex));
    }

    public static int nFT_Face_Properties(long face, int num_properties, long properties) {
        long __functionAddress = Functions.Face_Properties;
        return JNI.invokePPI(face, num_properties, properties, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Face_Properties(FT_Face face, @NativeType(value="FT_Parameter *") FT_Parameter.Buffer properties) {
        return FreeType.nFT_Face_Properties(face.address(), properties.remaining(), properties.address());
    }

    public static int nFT_Get_Name_Index(long face, long glyph_name) {
        long __functionAddress = Functions.Get_Name_Index;
        return JNI.invokePPI(face, glyph_name, __functionAddress);
    }

    @NativeType(value="FT_UInt")
    public static int FT_Get_Name_Index(FT_Face face, @NativeType(value="FT_String const *") ByteBuffer glyph_name) {
        if (Checks.CHECKS) {
            Checks.checkNT1(glyph_name);
        }
        return FreeType.nFT_Get_Name_Index(face.address(), MemoryUtil.memAddress(glyph_name));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="FT_UInt")
    public static int FT_Get_Name_Index(FT_Face face, @NativeType(value="FT_String const *") CharSequence glyph_name) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(glyph_name, true);
            long glyph_nameEncoded = stack.getPointerAddress();
            int n = FreeType.nFT_Get_Name_Index(face.address(), glyph_nameEncoded);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static int nFT_Get_Glyph_Name(long face, int glyph_index, long buffer, int buffer_max) {
        long __functionAddress = Functions.Get_Glyph_Name;
        return JNI.invokePPI(face, glyph_index, buffer, buffer_max, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Glyph_Name(FT_Face face, @NativeType(value="FT_UInt") int glyph_index, @NativeType(value="FT_Pointer") ByteBuffer buffer) {
        return FreeType.nFT_Get_Glyph_Name(face.address(), glyph_index, MemoryUtil.memAddress(buffer), buffer.remaining());
    }

    public static long nFT_Get_Postscript_Name(long face) {
        long __functionAddress = Functions.Get_Postscript_Name;
        return JNI.invokePP(face, __functionAddress);
    }

    @NativeType(value="char const *")
    public static @Nullable String FT_Get_Postscript_Name(FT_Face face) {
        long __result = FreeType.nFT_Get_Postscript_Name(face.address());
        return MemoryUtil.memASCIISafe(__result);
    }

    public static int nFT_Get_SubGlyph_Info(long glyph, int sub_index, long p_index, long p_flags, long p_arg1, long p_arg2, long p_transform) {
        long __functionAddress = Functions.Get_SubGlyph_Info;
        return JNI.invokePPPPPPI(glyph, sub_index, p_index, p_flags, p_arg1, p_arg2, p_transform, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_SubGlyph_Info(FT_GlyphSlot glyph, @NativeType(value="FT_UInt") int sub_index, @NativeType(value="FT_Int *") IntBuffer p_index, @NativeType(value="FT_UInt *") IntBuffer p_flags, @NativeType(value="FT_Int *") IntBuffer p_arg1, @NativeType(value="FT_Int *") IntBuffer p_arg2, @NativeType(value="FT_Matrix *") FT_Matrix p_transform) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)p_index, 1);
            Checks.check((Buffer)p_flags, 1);
            Checks.check((Buffer)p_arg1, 1);
            Checks.check((Buffer)p_arg2, 1);
        }
        return FreeType.nFT_Get_SubGlyph_Info(glyph.address(), sub_index, MemoryUtil.memAddress(p_index), MemoryUtil.memAddress(p_flags), MemoryUtil.memAddress(p_arg1), MemoryUtil.memAddress(p_arg2), p_transform.address());
    }

    public static short nFT_Get_FSType_Flags(long face) {
        long __functionAddress = Functions.Get_FSType_Flags;
        return JNI.invokePC(face, __functionAddress);
    }

    @NativeType(value="FT_UShort")
    public static short FT_Get_FSType_Flags(FT_Face face) {
        return FreeType.nFT_Get_FSType_Flags(face.address());
    }

    public static int nFT_Face_GetCharVariantIndex(long face, long charcode, long variantSelector) {
        long __functionAddress = Functions.Face_GetCharVariantIndex;
        return JNI.invokePNNI(face, charcode, variantSelector, __functionAddress);
    }

    @NativeType(value="FT_UInt")
    public static int FT_Face_GetCharVariantIndex(FT_Face face, @NativeType(value="FT_ULong") long charcode, @NativeType(value="FT_ULong") long variantSelector) {
        return FreeType.nFT_Face_GetCharVariantIndex(face.address(), charcode, variantSelector);
    }

    public static int nFT_Face_GetCharVariantIsDefault(long face, long charcode, long variantSelector) {
        long __functionAddress = Functions.Face_GetCharVariantIsDefault;
        return JNI.invokePNNI(face, charcode, variantSelector, __functionAddress);
    }

    @NativeType(value="FT_Int")
    public static int FT_Face_GetCharVariantIsDefault(FT_Face face, @NativeType(value="FT_ULong") long charcode, @NativeType(value="FT_ULong") long variantSelector) {
        return FreeType.nFT_Face_GetCharVariantIsDefault(face.address(), charcode, variantSelector);
    }

    public static long nFT_Face_GetVariantSelectors(long face) {
        long __functionAddress = Functions.Face_GetVariantSelectors;
        return JNI.invokePP(face, __functionAddress);
    }

    @NativeType(value="FT_UInt32 *")
    public static long FT_Face_GetVariantSelectors(FT_Face face) {
        return FreeType.nFT_Face_GetVariantSelectors(face.address());
    }

    public static long nFT_Face_GetVariantsOfChar(long face, long charcode) {
        long __functionAddress = Functions.Face_GetVariantsOfChar;
        return JNI.invokePNP(face, charcode, __functionAddress);
    }

    @NativeType(value="FT_UInt32 *")
    public static long FT_Face_GetVariantsOfChar(FT_Face face, @NativeType(value="FT_ULong") long charcode) {
        return FreeType.nFT_Face_GetVariantsOfChar(face.address(), charcode);
    }

    public static long nFT_Face_GetCharsOfVariant(long face, long variantSelector) {
        long __functionAddress = Functions.Face_GetCharsOfVariant;
        return JNI.invokePNP(face, variantSelector, __functionAddress);
    }

    @NativeType(value="FT_UInt32 *")
    public static long FT_Face_GetCharsOfVariant(FT_Face face, @NativeType(value="FT_ULong") long variantSelector) {
        return FreeType.nFT_Face_GetCharsOfVariant(face.address(), variantSelector);
    }

    @NativeType(value="FT_Long")
    public static long FT_MulDiv(@NativeType(value="FT_Long") long a, @NativeType(value="FT_Long") long b, @NativeType(value="FT_Long") long c) {
        long __functionAddress = Functions.MulDiv;
        return JNI.invokeNNNN(a, b, c, __functionAddress);
    }

    @NativeType(value="FT_Long")
    public static long FT_MulFix(@NativeType(value="FT_Long") long a, @NativeType(value="FT_Long") long b) {
        long __functionAddress = Functions.MulFix;
        return JNI.invokeNNN(a, b, __functionAddress);
    }

    @NativeType(value="FT_Long")
    public static long FT_DivFix(@NativeType(value="FT_Long") long a, @NativeType(value="FT_Long") long b) {
        long __functionAddress = Functions.DivFix;
        return JNI.invokeNNN(a, b, __functionAddress);
    }

    @NativeType(value="FT_Fixed")
    public static long FT_RoundFix(@NativeType(value="FT_Fixed") long a) {
        long __functionAddress = Functions.RoundFix;
        return JNI.invokeNN(a, __functionAddress);
    }

    @NativeType(value="FT_Fixed")
    public static long FT_CeilFix(@NativeType(value="FT_Fixed") long a) {
        long __functionAddress = Functions.CeilFix;
        return JNI.invokeNN(a, __functionAddress);
    }

    @NativeType(value="FT_Fixed")
    public static long FT_FloorFix(@NativeType(value="FT_Fixed") long a) {
        long __functionAddress = Functions.FloorFix;
        return JNI.invokeNN(a, __functionAddress);
    }

    public static void nFT_Vector_Transform(long vector, long matrix) {
        long __functionAddress = Functions.Vector_Transform;
        JNI.invokePPV(vector, matrix, __functionAddress);
    }

    public static void FT_Vector_Transform(@NativeType(value="FT_Vector *") FT_Vector vector, @NativeType(value="FT_Matrix const *") FT_Matrix matrix) {
        FreeType.nFT_Vector_Transform(vector.address(), matrix.address());
    }

    public static void nFT_Library_Version(long library, long amajor, long aminor, long apatch) {
        long __functionAddress = Functions.Library_Version;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        JNI.invokePPPPV(library, amajor, aminor, apatch, __functionAddress);
    }

    public static void FT_Library_Version(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Int *") IntBuffer amajor, @NativeType(value="FT_Int *") IntBuffer aminor, @NativeType(value="FT_Int *") IntBuffer apatch) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)amajor, 1);
            Checks.check((Buffer)aminor, 1);
            Checks.check((Buffer)apatch, 1);
        }
        FreeType.nFT_Library_Version(library, MemoryUtil.memAddress(amajor), MemoryUtil.memAddress(aminor), MemoryUtil.memAddress(apatch));
    }

    public static boolean nFT_Face_CheckTrueTypePatents(long face) {
        long __functionAddress = Functions.Face_CheckTrueTypePatents;
        return JNI.invokePZ(face, __functionAddress);
    }

    @NativeType(value="FT_Bool")
    public static boolean FT_Face_CheckTrueTypePatents(FT_Face face) {
        return FreeType.nFT_Face_CheckTrueTypePatents(face.address());
    }

    public static boolean nFT_Face_SetUnpatentedHinting(long face, boolean value) {
        long __functionAddress = Functions.Face_SetUnpatentedHinting;
        return JNI.invokePZ(face, value, __functionAddress);
    }

    @NativeType(value="FT_Bool")
    public static boolean FT_Face_SetUnpatentedHinting(FT_Face face, @NativeType(value="FT_Bool") boolean value) {
        return FreeType.nFT_Face_SetUnpatentedHinting(face.address(), value);
    }

    public static int nFT_Get_Advance(long face, int gindex, int load_flags, long padvance) {
        long __functionAddress = Functions.Get_Advance;
        return JNI.invokePPI(face, gindex, load_flags, padvance, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Advance(FT_Face face, @NativeType(value="FT_UInt") int gindex, @NativeType(value="FT_Int32") int load_flags, @NativeType(value="FT_Fixed *") CLongBuffer padvance) {
        if (Checks.CHECKS) {
            Checks.check(padvance, 1);
        }
        return FreeType.nFT_Get_Advance(face.address(), gindex, load_flags, MemoryUtil.memAddress(padvance));
    }

    public static int nFT_Get_Advances(long face, int start, int count, int load_flags, long padvances) {
        long __functionAddress = Functions.Get_Advances;
        return JNI.invokePPI(face, start, count, load_flags, padvances, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Advances(FT_Face face, @NativeType(value="FT_UInt") int start, @NativeType(value="FT_Int32") int load_flags, @NativeType(value="FT_Fixed *") CLongBuffer padvances) {
        return FreeType.nFT_Get_Advances(face.address(), start, padvances.remaining(), load_flags, MemoryUtil.memAddress(padvances));
    }

    public static int nFT_Outline_Get_BBox(long outline, long abbox) {
        long __functionAddress = Functions.Outline_Get_BBox;
        return JNI.invokePPI(outline, abbox, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_Get_BBox(@NativeType(value="FT_Outline *") FT_Outline outline, @NativeType(value="FT_BBox *") FT_BBox abbox) {
        return FreeType.nFT_Outline_Get_BBox(outline.address(), abbox.address());
    }

    public static int nFT_Get_BDF_Charset_ID(long face, long acharset_encoding, long acharset_registry) {
        long __functionAddress = Functions.Get_BDF_Charset_ID;
        return JNI.invokePPPI(face, acharset_encoding, acharset_registry, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_BDF_Charset_ID(FT_Face face, @NativeType(value="char const **") PointerBuffer acharset_encoding, @NativeType(value="char const **") PointerBuffer acharset_registry) {
        if (Checks.CHECKS) {
            Checks.check(acharset_encoding, 1);
            Checks.check(acharset_registry, 1);
        }
        return FreeType.nFT_Get_BDF_Charset_ID(face.address(), MemoryUtil.memAddress(acharset_encoding), MemoryUtil.memAddress(acharset_registry));
    }

    public static int nFT_Get_BDF_Property(long face, long prop_name, long aproperty) {
        long __functionAddress = Functions.Get_BDF_Property;
        return JNI.invokePPPI(face, prop_name, aproperty, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_BDF_Property(FT_Face face, @NativeType(value="char const *") ByteBuffer prop_name, @NativeType(value="BDF_PropertyRec *") BDF_Property aproperty) {
        if (Checks.CHECKS) {
            Checks.checkNT1(prop_name);
        }
        return FreeType.nFT_Get_BDF_Property(face.address(), MemoryUtil.memAddress(prop_name), aproperty.address());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="FT_Error")
    public static int FT_Get_BDF_Property(FT_Face face, @NativeType(value="char const *") CharSequence prop_name, @NativeType(value="BDF_PropertyRec *") BDF_Property aproperty) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(prop_name, true);
            long prop_nameEncoded = stack.getPointerAddress();
            int n = FreeType.nFT_Get_BDF_Property(face.address(), prop_nameEncoded, aproperty.address());
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static void nFT_Bitmap_Init(long abitmap) {
        long __functionAddress = Functions.Bitmap_Init;
        JNI.invokePV(abitmap, __functionAddress);
    }

    public static void FT_Bitmap_Init(@NativeType(value="FT_Bitmap *") FT_Bitmap abitmap) {
        FreeType.nFT_Bitmap_Init(abitmap.address());
    }

    public static int nFT_Bitmap_Copy(long library, long source, long target) {
        long __functionAddress = Functions.Bitmap_Copy;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPPI(library, source, target, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Bitmap_Copy(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Bitmap const *") FT_Bitmap source, @NativeType(value="FT_Bitmap *") FT_Bitmap target) {
        return FreeType.nFT_Bitmap_Copy(library, source.address(), target.address());
    }

    public static int nFT_Bitmap_Embolden(long library, long bitmap, long xStrength, long yStrength) {
        long __functionAddress = Functions.Bitmap_Embolden;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPNNI(library, bitmap, xStrength, yStrength, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Bitmap_Embolden(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Bitmap *") FT_Bitmap bitmap, @NativeType(value="FT_Pos") long xStrength, @NativeType(value="FT_Pos") long yStrength) {
        return FreeType.nFT_Bitmap_Embolden(library, bitmap.address(), xStrength, yStrength);
    }

    public static int nFT_Bitmap_Convert(long library, long source, long target, int alignment) {
        long __functionAddress = Functions.Bitmap_Convert;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPPI(library, source, target, alignment, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Bitmap_Convert(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Bitmap const *") FT_Bitmap source, @NativeType(value="FT_Bitmap *") FT_Bitmap target, @NativeType(value="FT_Int") int alignment) {
        return FreeType.nFT_Bitmap_Convert(library, source.address(), target.address(), alignment);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int nFT_Bitmap_Blend(long library, long source, long source_offset, long target, long atarget_offset, long color) {
        long __functionAddress = Functions.Bitmap_Blend;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            long __result = stack.nint(0);
            long arguments = stack.nmalloc(MemoryStack.POINTER_SIZE, MemoryStack.POINTER_SIZE * 6);
            MemoryUtil.memPutAddress(arguments, stack.npointer(library));
            MemoryUtil.memPutAddress(arguments + (long)MemoryStack.POINTER_SIZE, stack.npointer(source));
            MemoryUtil.memPutAddress(arguments + (long)(2 * MemoryStack.POINTER_SIZE), source_offset);
            MemoryUtil.memPutAddress(arguments + (long)(3 * MemoryStack.POINTER_SIZE), stack.npointer(target));
            MemoryUtil.memPutAddress(arguments + (long)(4 * MemoryStack.POINTER_SIZE), stack.npointer(atarget_offset));
            MemoryUtil.memPutAddress(arguments + (long)(5 * MemoryStack.POINTER_SIZE), color);
            LibFFI.nffi_call(FT_Bitmap_BlendCIF.address(), __functionAddress, __result, arguments);
            int n = MemoryUtil.memGetInt(__result);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    @NativeType(value="FT_Error")
    public static int FT_Bitmap_Blend(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Bitmap const *") FT_Bitmap source, @NativeType(value="FT_Vector const") FT_Vector source_offset, @NativeType(value="FT_Bitmap *") FT_Bitmap target, @NativeType(value="FT_Vector *") FT_Vector atarget_offset, FT_Color color) {
        return FreeType.nFT_Bitmap_Blend(library, source.address(), source_offset.address(), target.address(), atarget_offset.address(), color.address());
    }

    public static int nFT_GlyphSlot_Own_Bitmap(long slot) {
        long __functionAddress = Functions.GlyphSlot_Own_Bitmap;
        return JNI.invokePI(slot, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_GlyphSlot_Own_Bitmap(FT_GlyphSlot slot) {
        return FreeType.nFT_GlyphSlot_Own_Bitmap(slot.address());
    }

    public static int nFT_Bitmap_Done(long library, long bitmap) {
        long __functionAddress = Functions.Bitmap_Done;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPI(library, bitmap, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Bitmap_Done(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Bitmap *") FT_Bitmap bitmap) {
        return FreeType.nFT_Bitmap_Done(library, bitmap.address());
    }

    public static int nFT_Stream_OpenBzip2(long stream, long source) {
        long __functionAddress = Functions.Stream_OpenBzip2;
        if (Checks.CHECKS) {
            Checks.check(__functionAddress);
        }
        return JNI.invokePPI(stream, source, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stream_OpenBzip2(FT_Stream stream, FT_Stream source) {
        return FreeType.nFT_Stream_OpenBzip2(stream.address(), source.address());
    }

    public static int nFTC_Manager_New(long library, int max_faces, int max_sizes, long max_bytes, long requester, long req_data, long amanager) {
        long __functionAddress = Functions.FTC_Manager_New;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePNPPPI(library, max_faces, max_sizes, max_bytes, requester, req_data, amanager, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_Manager_New(@NativeType(value="FT_Library") long library, @NativeType(value="FT_UInt") int max_faces, @NativeType(value="FT_UInt") int max_sizes, @NativeType(value="FT_ULong") long max_bytes, @NativeType(value="FTC_Face_Requester") FTC_Face_RequesterI requester, @NativeType(value="FT_Pointer") @Nullable ByteBuffer req_data, @NativeType(value="FTC_Manager *") PointerBuffer amanager) {
        if (Checks.CHECKS) {
            Checks.check(amanager, 1);
        }
        return FreeType.nFTC_Manager_New(library, max_faces, max_sizes, max_bytes, requester.address(), MemoryUtil.memAddressSafe(req_data), MemoryUtil.memAddress(amanager));
    }

    public static void FTC_Manager_Reset(@NativeType(value="FTC_Manager") long manager) {
        long __functionAddress = Functions.FTC_Manager_Reset;
        if (Checks.CHECKS) {
            Checks.check(manager);
        }
        JNI.invokePV(manager, __functionAddress);
    }

    public static void FTC_Manager_Done(@NativeType(value="FTC_Manager") long manager) {
        long __functionAddress = Functions.FTC_Manager_Done;
        if (Checks.CHECKS) {
            Checks.check(manager);
        }
        JNI.invokePV(manager, __functionAddress);
    }

    public static int nFTC_Manager_LookupFace(long manager, long face_id, long aface) {
        long __functionAddress = Functions.FTC_Manager_LookupFace;
        if (Checks.CHECKS) {
            Checks.check(manager);
            Checks.check(face_id);
        }
        return JNI.invokePPPI(manager, face_id, aface, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_Manager_LookupFace(@NativeType(value="FTC_Manager") long manager, @NativeType(value="FTC_FaceID") long face_id, @NativeType(value="FT_Face *") PointerBuffer aface) {
        if (Checks.CHECKS) {
            Checks.check(aface, 1);
        }
        return FreeType.nFTC_Manager_LookupFace(manager, face_id, MemoryUtil.memAddress(aface));
    }

    public static int nFTC_Manager_LookupSize(long manager, long scaler, long asize) {
        long __functionAddress = Functions.FTC_Manager_LookupSize;
        if (Checks.CHECKS) {
            Checks.check(manager);
        }
        return JNI.invokePPPI(manager, scaler, asize, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_Manager_LookupSize(@NativeType(value="FTC_Manager") long manager, FTC_Scaler scaler, @NativeType(value="FT_Size *") PointerBuffer asize) {
        if (Checks.CHECKS) {
            Checks.check(asize, 1);
        }
        return FreeType.nFTC_Manager_LookupSize(manager, scaler.address(), MemoryUtil.memAddress(asize));
    }

    public static void FTC_Node_Unref(@NativeType(value="FTC_Node") long node, @NativeType(value="FTC_Manager") long manager) {
        long __functionAddress = Functions.FTC_Node_Unref;
        if (Checks.CHECKS) {
            Checks.check(node);
            Checks.check(manager);
        }
        JNI.invokePPV(node, manager, __functionAddress);
    }

    public static void FTC_Manager_RemoveFaceID(@NativeType(value="FTC_Manager") long manager, @NativeType(value="FTC_FaceID") long face_id) {
        long __functionAddress = Functions.FTC_Manager_RemoveFaceID;
        if (Checks.CHECKS) {
            Checks.check(manager);
            Checks.check(face_id);
        }
        JNI.invokePPV(manager, face_id, __functionAddress);
    }

    public static int nFTC_CMapCache_New(long manager, long acache) {
        long __functionAddress = Functions.FTC_CMapCache_New;
        if (Checks.CHECKS) {
            Checks.check(manager);
        }
        return JNI.invokePPI(manager, acache, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_CMapCache_New(@NativeType(value="FTC_Manager") long manager, @NativeType(value="FTC_CMapCache *") PointerBuffer acache) {
        if (Checks.CHECKS) {
            Checks.check(acache, 1);
        }
        return FreeType.nFTC_CMapCache_New(manager, MemoryUtil.memAddress(acache));
    }

    @NativeType(value="FT_UInt")
    public static int FTC_CMapCache_Lookup(@NativeType(value="FTC_CMapCache") long cache, @NativeType(value="FTC_FaceID") long face_id, @NativeType(value="FT_Int") int cmap_index, @NativeType(value="FT_UInt32") int char_code) {
        long __functionAddress = Functions.FTC_CMapCache_Lookup;
        if (Checks.CHECKS) {
            Checks.check(cache);
            Checks.check(face_id);
        }
        return JNI.invokePPI(cache, face_id, cmap_index, char_code, __functionAddress);
    }

    public static int nFTC_ImageCache_New(long manager, long acache) {
        long __functionAddress = Functions.FTC_ImageCache_New;
        if (Checks.CHECKS) {
            Checks.check(manager);
        }
        return JNI.invokePPI(manager, acache, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_ImageCache_New(@NativeType(value="FTC_Manager") long manager, @NativeType(value="FTC_ImageCache *") PointerBuffer acache) {
        if (Checks.CHECKS) {
            Checks.check(acache, 1);
        }
        return FreeType.nFTC_ImageCache_New(manager, MemoryUtil.memAddress(acache));
    }

    public static int nFTC_ImageCache_Lookup(long cache, long type, int gindex, long aglyph, long anode) {
        long __functionAddress = Functions.FTC_ImageCache_Lookup;
        if (Checks.CHECKS) {
            Checks.check(cache);
        }
        return JNI.invokePPPPI(cache, type, gindex, aglyph, anode, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_ImageCache_Lookup(@NativeType(value="FTC_ImageCache") long cache, FTC_ImageType type, @NativeType(value="FT_UInt") int gindex, @NativeType(value="FT_Glyph *") PointerBuffer aglyph, @NativeType(value="FTC_Node *") @Nullable PointerBuffer anode) {
        if (Checks.CHECKS) {
            Checks.check(aglyph, 1);
            Checks.checkSafe(anode, 1);
        }
        return FreeType.nFTC_ImageCache_Lookup(cache, type.address(), gindex, MemoryUtil.memAddress(aglyph), MemoryUtil.memAddressSafe(anode));
    }

    public static int nFTC_ImageCache_LookupScaler(long cache, long scaler, long load_flags, int gindex, long aglyph, long anode) {
        long __functionAddress = Functions.FTC_ImageCache_LookupScaler;
        if (Checks.CHECKS) {
            Checks.check(cache);
        }
        return JNI.invokePPNPPI(cache, scaler, load_flags, gindex, aglyph, anode, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_ImageCache_LookupScaler(@NativeType(value="FTC_ImageCache") long cache, FTC_Scaler scaler, @NativeType(value="FT_ULong") long load_flags, @NativeType(value="FT_UInt") int gindex, @NativeType(value="FT_Glyph *") PointerBuffer aglyph, @NativeType(value="FTC_Node *") @Nullable PointerBuffer anode) {
        if (Checks.CHECKS) {
            Checks.check(aglyph, 1);
            Checks.checkSafe(anode, 1);
        }
        return FreeType.nFTC_ImageCache_LookupScaler(cache, scaler.address(), load_flags, gindex, MemoryUtil.memAddress(aglyph), MemoryUtil.memAddressSafe(anode));
    }

    public static int nFTC_SBitCache_New(long manager, long acache) {
        long __functionAddress = Functions.FTC_SBitCache_New;
        if (Checks.CHECKS) {
            Checks.check(manager);
        }
        return JNI.invokePPI(manager, acache, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_SBitCache_New(@NativeType(value="FTC_Manager") long manager, @NativeType(value="FTC_SBitCache *") PointerBuffer acache) {
        if (Checks.CHECKS) {
            Checks.check(acache, 1);
        }
        return FreeType.nFTC_SBitCache_New(manager, MemoryUtil.memAddress(acache));
    }

    public static int nFTC_SBitCache_Lookup(long cache, long type, int gindex, long sbit, long anode) {
        long __functionAddress = Functions.FTC_SBitCache_Lookup;
        if (Checks.CHECKS) {
            Checks.check(cache);
        }
        return JNI.invokePPPPI(cache, type, gindex, sbit, anode, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_SBitCache_Lookup(@NativeType(value="FTC_SBitCache") long cache, FTC_ImageType type, @NativeType(value="FT_UInt") int gindex, @NativeType(value="FTC_SBit *") PointerBuffer sbit, @NativeType(value="FTC_Node *") @Nullable PointerBuffer anode) {
        if (Checks.CHECKS) {
            Checks.check(sbit, 1);
            Checks.checkSafe(anode, 1);
        }
        return FreeType.nFTC_SBitCache_Lookup(cache, type.address(), gindex, MemoryUtil.memAddress(sbit), MemoryUtil.memAddressSafe(anode));
    }

    public static int nFTC_SBitCache_LookupScaler(long cache, long scaler, long load_flags, int gindex, long sbit, long anode) {
        long __functionAddress = Functions.FTC_SBitCache_LookupScaler;
        if (Checks.CHECKS) {
            Checks.check(cache);
        }
        return JNI.invokePPNPPI(cache, scaler, load_flags, gindex, sbit, anode, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FTC_SBitCache_LookupScaler(@NativeType(value="FTC_SBitCache") long cache, FTC_Scaler scaler, @NativeType(value="FT_ULong") long load_flags, @NativeType(value="FT_UInt") int gindex, @NativeType(value="FTC_SBit *") PointerBuffer sbit, @NativeType(value="FTC_Node *") @Nullable PointerBuffer anode) {
        if (Checks.CHECKS) {
            Checks.check(sbit, 1);
            Checks.checkSafe(anode, 1);
        }
        return FreeType.nFTC_SBitCache_LookupScaler(cache, scaler.address(), load_flags, gindex, MemoryUtil.memAddress(sbit), MemoryUtil.memAddressSafe(anode));
    }

    public static int nFT_Get_CID_Registry_Ordering_Supplement(long face, long registry, long ordering, long supplement) {
        long __functionAddress = Functions.Get_CID_Registry_Ordering_Supplement;
        return JNI.invokePPPPI(face, registry, ordering, supplement, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_CID_Registry_Ordering_Supplement(FT_Face face, @NativeType(value="char const **") PointerBuffer registry, @NativeType(value="char const **") PointerBuffer ordering, @NativeType(value="FT_Int *") IntBuffer supplement) {
        if (Checks.CHECKS) {
            Checks.check(registry, 1);
            Checks.check(ordering, 1);
            Checks.check((Buffer)supplement, 1);
        }
        return FreeType.nFT_Get_CID_Registry_Ordering_Supplement(face.address(), MemoryUtil.memAddress(registry), MemoryUtil.memAddress(ordering), MemoryUtil.memAddress(supplement));
    }

    public static int nFT_Get_CID_Is_Internally_CID_Keyed(long face, long is_cid) {
        long __functionAddress = Functions.Get_CID_Is_Internally_CID_Keyed;
        return JNI.invokePPI(face, is_cid, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_CID_Is_Internally_CID_Keyed(FT_Face face, @NativeType(value="FT_Bool *") ByteBuffer is_cid) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)is_cid, 1);
        }
        return FreeType.nFT_Get_CID_Is_Internally_CID_Keyed(face.address(), MemoryUtil.memAddress(is_cid));
    }

    public static int nFT_Get_CID_From_Glyph_Index(long face, int glyph_index, long cid) {
        long __functionAddress = Functions.Get_CID_From_Glyph_Index;
        return JNI.invokePPI(face, glyph_index, cid, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_CID_From_Glyph_Index(FT_Face face, @NativeType(value="FT_UInt") int glyph_index, @NativeType(value="FT_UInt *") IntBuffer cid) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)cid, 1);
        }
        return FreeType.nFT_Get_CID_From_Glyph_Index(face.address(), glyph_index, MemoryUtil.memAddress(cid));
    }

    public static int nFT_Palette_Data_Get(long face, long apalette) {
        long __functionAddress = Functions.Palette_Data_Get;
        return JNI.invokePPI(face, apalette, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Palette_Data_Get(FT_Face face, @NativeType(value="FT_Palette_Data *") FT_Palette_Data apalette) {
        return FreeType.nFT_Palette_Data_Get(face.address(), apalette.address());
    }

    public static int nFT_Palette_Select(long face, short palette_index, long apalette) {
        long __functionAddress = Functions.Palette_Select;
        return JNI.invokePCPI(face, palette_index, apalette, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Palette_Select(FT_Face face, @NativeType(value="FT_UShort") short palette_index, @NativeType(value="FT_Color **") @Nullable PointerBuffer apalette) {
        if (Checks.CHECKS) {
            Checks.checkSafe(apalette, 1);
        }
        return FreeType.nFT_Palette_Select(face.address(), palette_index, MemoryUtil.memAddressSafe(apalette));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int nFT_Palette_Set_Foreground_Color(long face, long foreground_color) {
        long __functionAddress = Functions.Palette_Set_Foreground_Color;
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            long __result = stack.nint(0);
            long arguments = stack.nmalloc(MemoryStack.POINTER_SIZE, MemoryStack.POINTER_SIZE * 2);
            MemoryUtil.memPutAddress(arguments, stack.npointer(face));
            MemoryUtil.memPutAddress(arguments + (long)MemoryStack.POINTER_SIZE, foreground_color);
            LibFFI.nffi_call(FT_Palette_Set_Foreground_ColorCIF.address(), __functionAddress, __result, arguments);
            int n = MemoryUtil.memGetInt(__result);
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    @NativeType(value="FT_Error")
    public static int FT_Palette_Set_Foreground_Color(FT_Face face, FT_Color foreground_color) {
        return FreeType.nFT_Palette_Set_Foreground_Color(face.address(), foreground_color.address());
    }

    public static boolean nFT_Get_Color_Glyph_Layer(long face, int base_glyph, long aglyph_index, long acolor_index, long iterator) {
        long __functionAddress = Functions.Get_Color_Glyph_Layer;
        return JNI.invokePPPPZ(face, base_glyph, aglyph_index, acolor_index, iterator, __functionAddress);
    }

    @NativeType(value="FT_Bool")
    public static boolean FT_Get_Color_Glyph_Layer(FT_Face face, @NativeType(value="FT_UInt") int base_glyph, @NativeType(value="FT_UInt *") IntBuffer aglyph_index, @NativeType(value="FT_UInt *") IntBuffer acolor_index, @NativeType(value="FT_LayerIterator *") FT_LayerIterator iterator) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)aglyph_index, 1);
            Checks.check((Buffer)acolor_index, 1);
        }
        return FreeType.nFT_Get_Color_Glyph_Layer(face.address(), base_glyph, MemoryUtil.memAddress(aglyph_index), MemoryUtil.memAddress(acolor_index), iterator.address());
    }

    public static boolean nFT_Get_Color_Glyph_Paint(long face, int base_glyph, int root_transform, long paint) {
        long __functionAddress = Functions.Get_Color_Glyph_Paint;
        return JNI.invokePPZ(face, base_glyph, root_transform, paint, __functionAddress);
    }

    @NativeType(value="FT_Bool")
    public static boolean FT_Get_Color_Glyph_Paint(FT_Face face, @NativeType(value="FT_UInt") int base_glyph, @NativeType(value="FT_Color_Root_Transform") int root_transform, @NativeType(value="FT_OpaquePaintRec *") FT_OpaquePaint paint) {
        return FreeType.nFT_Get_Color_Glyph_Paint(face.address(), base_glyph, root_transform, paint.address());
    }

    public static boolean nFT_Get_Color_Glyph_ClipBox(long face, int base_glyph, long clip_box) {
        long __functionAddress = Functions.Get_Color_Glyph_ClipBox;
        return JNI.invokePPZ(face, base_glyph, clip_box, __functionAddress);
    }

    @NativeType(value="FT_Bool")
    public static boolean FT_Get_Color_Glyph_ClipBox(FT_Face face, @NativeType(value="FT_UInt") int base_glyph, @NativeType(value="FT_ClipBox *") FT_ClipBox clip_box) {
        return FreeType.nFT_Get_Color_Glyph_ClipBox(face.address(), base_glyph, clip_box.address());
    }

    public static boolean nFT_Get_Paint_Layers(long face, long iterator, long paint) {
        long __functionAddress = Functions.Get_Paint_Layers;
        return JNI.invokePPPZ(face, iterator, paint, __functionAddress);
    }

    @NativeType(value="FT_Bool")
    public static boolean FT_Get_Paint_Layers(FT_Face face, @NativeType(value="FT_LayerIterator *") FT_LayerIterator iterator, @NativeType(value="FT_OpaquePaintRec *") FT_OpaquePaint paint) {
        return FreeType.nFT_Get_Paint_Layers(face.address(), iterator.address(), paint.address());
    }

    public static boolean nFT_Get_Colorline_Stops(long face, long color_stop, long iterator) {
        long __functionAddress = Functions.Get_Colorline_Stops;
        return JNI.invokePPPZ(face, color_stop, iterator, __functionAddress);
    }

    @NativeType(value="FT_Bool")
    public static boolean FT_Get_Colorline_Stops(FT_Face face, @NativeType(value="FT_ColorStop *") FT_ColorStop color_stop, @NativeType(value="FT_ColorStopIterator *") FT_ColorStopIterator iterator) {
        return FreeType.nFT_Get_Colorline_Stops(face.address(), color_stop.address(), iterator.address());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean nFT_Get_Paint(long face, long opaque_paint, long paint) {
        long __functionAddress = Functions.Get_Paint;
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            long __result = stack.nbyte((byte)0);
            long arguments = stack.nmalloc(MemoryStack.POINTER_SIZE, MemoryStack.POINTER_SIZE * 3);
            MemoryUtil.memPutAddress(arguments, stack.npointer(face));
            MemoryUtil.memPutAddress(arguments + (long)MemoryStack.POINTER_SIZE, opaque_paint);
            MemoryUtil.memPutAddress(arguments + (long)(2 * MemoryStack.POINTER_SIZE), stack.npointer(paint));
            LibFFI.nffi_call(FT_Get_PaintCIF.address(), __functionAddress, __result, arguments);
            boolean bl = MemoryUtil.memGetByte(__result) != 0;
            return bl;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    @NativeType(value="FT_Bool")
    public static boolean FT_Get_Paint(FT_Face face, @NativeType(value="FT_OpaquePaintRec") FT_OpaquePaint opaque_paint, @NativeType(value="FT_COLR_Paint *") FT_COLR_Paint paint) {
        return FreeType.nFT_Get_Paint(face.address(), opaque_paint.address(), paint.address());
    }

    public static long nFT_Error_String(int error_code) {
        long __functionAddress = Functions.Error_String;
        return JNI.invokeP(error_code, __functionAddress);
    }

    @NativeType(value="char const *")
    public static @Nullable String FT_Error_String(@NativeType(value="FT_Error") int error_code) {
        long __result = FreeType.nFT_Error_String(error_code);
        return MemoryUtil.memASCIISafe(__result);
    }

    public static long nFT_Get_Font_Format(long face) {
        long __functionAddress = Functions.Get_Font_Format;
        return JNI.invokePP(face, __functionAddress);
    }

    @NativeType(value="char const *")
    public static @Nullable String FT_Get_Font_Format(FT_Face face) {
        long __result = FreeType.nFT_Get_Font_Format(face.address());
        return MemoryUtil.memASCIISafe(__result);
    }

    public static int nFT_Get_Gasp(long face, int ppem) {
        long __functionAddress = Functions.Get_Gasp;
        return JNI.invokePI(face, ppem, __functionAddress);
    }

    @NativeType(value="FT_Int")
    public static int FT_Get_Gasp(FT_Face face, @NativeType(value="FT_UInt") int ppem) {
        return FreeType.nFT_Get_Gasp(face.address(), ppem);
    }

    public static int nFT_New_Glyph(long library, int format, long aglyph) {
        long __functionAddress = Functions.New_Glyph;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPI(library, format, aglyph, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_New_Glyph(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Glyph_Format") int format, @NativeType(value="FT_Glyph *") PointerBuffer aglyph) {
        if (Checks.CHECKS) {
            Checks.check(aglyph, 1);
        }
        return FreeType.nFT_New_Glyph(library, format, MemoryUtil.memAddress(aglyph));
    }

    public static int nFT_Get_Glyph(long slot, long aglyph) {
        long __functionAddress = Functions.Get_Glyph;
        return JNI.invokePPI(slot, aglyph, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Glyph(FT_GlyphSlot slot, @NativeType(value="FT_Glyph *") PointerBuffer aglyph) {
        if (Checks.CHECKS) {
            Checks.check(aglyph, 1);
        }
        return FreeType.nFT_Get_Glyph(slot.address(), MemoryUtil.memAddress(aglyph));
    }

    public static int nFT_Glyph_Copy(long source, long target) {
        long __functionAddress = Functions.Glyph_Copy;
        return JNI.invokePPI(source, target, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Glyph_Copy(FT_Glyph source, @NativeType(value="FT_Glyph *") PointerBuffer target) {
        if (Checks.CHECKS) {
            Checks.check(target, 1);
        }
        return FreeType.nFT_Glyph_Copy(source.address(), MemoryUtil.memAddress(target));
    }

    public static int nFT_Glyph_Transform(long glyph, long matrix, long delta) {
        long __functionAddress = Functions.Glyph_Transform;
        return JNI.invokePPPI(glyph, matrix, delta, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Glyph_Transform(FT_Glyph glyph, @NativeType(value="FT_Matrix const *") @Nullable FT_Matrix matrix, @NativeType(value="FT_Vector const *") @Nullable FT_Vector delta) {
        return FreeType.nFT_Glyph_Transform(glyph.address(), MemoryUtil.memAddressSafe(matrix), MemoryUtil.memAddressSafe(delta));
    }

    public static void nFT_Glyph_Get_CBox(long glyph, int bbox_mode, long acbox) {
        long __functionAddress = Functions.Glyph_Get_CBox;
        JNI.invokePPV(glyph, bbox_mode, acbox, __functionAddress);
    }

    public static void FT_Glyph_Get_CBox(FT_Glyph glyph, @NativeType(value="FT_UInt") int bbox_mode, @NativeType(value="FT_BBox *") FT_BBox acbox) {
        FreeType.nFT_Glyph_Get_CBox(glyph.address(), bbox_mode, acbox.address());
    }

    public static int nFT_Glyph_To_Bitmap(long the_glyph, int render_mode, long origin, boolean destroy) {
        long __functionAddress = Functions.Glyph_To_Bitmap;
        return JNI.invokePPI(the_glyph, render_mode, origin, destroy, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Glyph_To_Bitmap(@NativeType(value="FT_Glyph *") PointerBuffer the_glyph, @NativeType(value="FT_Render_Mode") int render_mode, @NativeType(value="FT_Vector const *") @Nullable FT_Vector origin, @NativeType(value="FT_Bool") boolean destroy) {
        if (Checks.CHECKS) {
            Checks.check(the_glyph, 1);
        }
        return FreeType.nFT_Glyph_To_Bitmap(MemoryUtil.memAddress(the_glyph), render_mode, MemoryUtil.memAddressSafe(origin), destroy);
    }

    public static void nFT_Done_Glyph(long glyph) {
        long __functionAddress = Functions.Done_Glyph;
        JNI.invokePV(glyph, __functionAddress);
    }

    public static void FT_Done_Glyph(@NativeType(value="FT_Glyph") @Nullable FT_Glyph glyph) {
        FreeType.nFT_Done_Glyph(MemoryUtil.memAddressSafe(glyph));
    }

    public static void nFT_Matrix_Multiply(long a, long b) {
        long __functionAddress = Functions.Matrix_Multiply;
        JNI.invokePPV(a, b, __functionAddress);
    }

    public static void FT_Matrix_Multiply(@NativeType(value="FT_Matrix const *") FT_Matrix a, @NativeType(value="FT_Matrix *") FT_Matrix b) {
        FreeType.nFT_Matrix_Multiply(a.address(), b.address());
    }

    public static int nFT_Matrix_Invert(long matrix) {
        long __functionAddress = Functions.Matrix_Invert;
        return JNI.invokePI(matrix, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Matrix_Invert(@NativeType(value="FT_Matrix *") FT_Matrix matrix) {
        return FreeType.nFT_Matrix_Invert(matrix.address());
    }

    public static int nFT_TrueTypeGX_Validate(long face, int validation_flags, long tables, int table_length) {
        long __functionAddress = Functions.TrueTypeGX_Validate;
        return JNI.invokePPI(face, validation_flags, tables, table_length, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_TrueTypeGX_Validate(FT_Face face, @NativeType(value="FT_UInt") int validation_flags, @NativeType(value="FT_Bytes *") PointerBuffer tables) {
        return FreeType.nFT_TrueTypeGX_Validate(face.address(), validation_flags, MemoryUtil.memAddress(tables), tables.remaining());
    }

    public static void nFT_TrueTypeGX_Free(long face, long table) {
        long __functionAddress = Functions.TrueTypeGX_Free;
        JNI.invokePPV(face, table, __functionAddress);
    }

    public static void FT_TrueTypeGX_Free(FT_Face face, @NativeType(value="FT_Bytes") ByteBuffer table) {
        FreeType.nFT_TrueTypeGX_Free(face.address(), MemoryUtil.memAddress(table));
    }

    public static int nFT_ClassicKern_Validate(long face, int validation_flags, long ckern_table) {
        long __functionAddress = Functions.ClassicKern_Validate;
        return JNI.invokePPI(face, validation_flags, ckern_table, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_ClassicKern_Validate(FT_Face face, @NativeType(value="FT_UInt") int validation_flags, @NativeType(value="FT_Bytes *") PointerBuffer ckern_table) {
        if (Checks.CHECKS) {
            Checks.check(ckern_table, 1);
        }
        return FreeType.nFT_ClassicKern_Validate(face.address(), validation_flags, MemoryUtil.memAddress(ckern_table));
    }

    public static void nFT_ClassicKern_Free(long face, long table) {
        long __functionAddress = Functions.ClassicKern_Free;
        JNI.invokePPV(face, table, __functionAddress);
    }

    public static void FT_ClassicKern_Free(FT_Face face, @NativeType(value="FT_Bytes") ByteBuffer table) {
        FreeType.nFT_ClassicKern_Free(face.address(), MemoryUtil.memAddress(table));
    }

    public static int nFT_Stream_OpenGzip(long stream, long source) {
        long __functionAddress = Functions.Stream_OpenGzip;
        return JNI.invokePPI(stream, source, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stream_OpenGzip(FT_Stream stream, FT_Stream source) {
        return FreeType.nFT_Stream_OpenGzip(stream.address(), source.address());
    }

    public static int nFT_Gzip_Uncompress(long memory, long output, long output_len, long input, long input_len) {
        long __functionAddress = Functions.Gzip_Uncompress;
        return JNI.invokePPPPNI(memory, output, output_len, input, input_len, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Gzip_Uncompress(FT_Memory memory, @NativeType(value="FT_Byte *") ByteBuffer output, @NativeType(value="FT_ULong *") CLongBuffer output_len, @NativeType(value="FT_Byte const *") ByteBuffer input) {
        if (Checks.CHECKS) {
            Checks.check(output_len, 1);
            Checks.check((Buffer)output, output_len.get(output_len.position()));
        }
        return FreeType.nFT_Gzip_Uncompress(memory.address(), MemoryUtil.memAddress(output), MemoryUtil.memAddress(output_len), MemoryUtil.memAddress(input), input.remaining());
    }

    @NativeType(value="FT_Error")
    public static int FT_Library_SetLcdFilter(@NativeType(value="FT_Library") long library, @NativeType(value="FT_LcdFilter") int filter) {
        long __functionAddress = Functions.Library_SetLcdFilter;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePI(library, filter, __functionAddress);
    }

    public static int nFT_Library_SetLcdFilterWeights(long library, long weights) {
        long __functionAddress = Functions.Library_SetLcdFilterWeights;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPI(library, weights, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Library_SetLcdFilterWeights(@NativeType(value="FT_Library") long library, @NativeType(value="unsigned char *") ByteBuffer weights) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)weights, 5);
        }
        return FreeType.nFT_Library_SetLcdFilterWeights(library, MemoryUtil.memAddress(weights));
    }

    public static int nFT_Library_SetLcdGeometry(long library, long sub) {
        long __functionAddress = Functions.Library_SetLcdGeometry;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPI(library, sub, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Library_SetLcdGeometry(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Vector *") FT_Vector.Buffer sub) {
        if (Checks.CHECKS) {
            Checks.check(sub, 3);
        }
        return FreeType.nFT_Library_SetLcdGeometry(library, sub.address());
    }

    public static long nFT_List_Find(long list, long data) {
        long __functionAddress = Functions.List_Find;
        if (Checks.CHECKS) {
            Checks.check(data);
        }
        return JNI.invokePPP(list, data, __functionAddress);
    }

    @NativeType(value="FT_ListNode")
    public static @Nullable FT_ListNode FT_List_Find(FT_List list, @NativeType(value="void *") long data) {
        long __result = FreeType.nFT_List_Find(list.address(), data);
        return FT_ListNode.createSafe(__result);
    }

    public static void nFT_List_Add(long list, long node) {
        long __functionAddress = Functions.List_Add;
        JNI.invokePPV(list, node, __functionAddress);
    }

    public static void FT_List_Add(FT_List list, FT_ListNode node) {
        FreeType.nFT_List_Add(list.address(), node.address());
    }

    public static void nFT_List_Insert(long list, long node) {
        long __functionAddress = Functions.List_Insert;
        JNI.invokePPV(list, node, __functionAddress);
    }

    public static void FT_List_Insert(FT_List list, FT_ListNode node) {
        FreeType.nFT_List_Insert(list.address(), node.address());
    }

    public static void nFT_List_Remove(long list, long node) {
        long __functionAddress = Functions.List_Remove;
        JNI.invokePPV(list, node, __functionAddress);
    }

    public static void FT_List_Remove(FT_List list, FT_ListNode node) {
        FreeType.nFT_List_Remove(list.address(), node.address());
    }

    public static void nFT_List_Up(long list, long node) {
        long __functionAddress = Functions.List_Up;
        JNI.invokePPV(list, node, __functionAddress);
    }

    public static void FT_List_Up(FT_List list, FT_ListNode node) {
        FreeType.nFT_List_Up(list.address(), node.address());
    }

    public static int nFT_List_Iterate(long list, long iterator, long user) {
        long __functionAddress = Functions.List_Iterate;
        return JNI.invokePPPI(list, iterator, user, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_List_Iterate(FT_List list, @NativeType(value="FT_List_Iterator") FT_List_IteratorI iterator, @NativeType(value="void *") long user) {
        return FreeType.nFT_List_Iterate(list.address(), iterator.address(), user);
    }

    public static void nFT_List_Finalize(long list, long destroy, long memory, long user) {
        long __functionAddress = Functions.List_Finalize;
        JNI.invokePPPPV(list, destroy, memory, user, __functionAddress);
    }

    public static void FT_List_Finalize(FT_List list, @NativeType(value="FT_List_Destructor") @Nullable FT_List_DestructorI destroy, FT_Memory memory, @NativeType(value="void *") long user) {
        FreeType.nFT_List_Finalize(list.address(), MemoryUtil.memAddressSafe(destroy), memory.address(), user);
    }

    public static int nFT_Stream_OpenLZW(long stream, long source) {
        long __functionAddress = Functions.Stream_OpenLZW;
        return JNI.invokePPI(stream, source, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stream_OpenLZW(FT_Stream stream, FT_Stream source) {
        return FreeType.nFT_Stream_OpenLZW(stream.address(), source.address());
    }

    public static int nFT_Get_Multi_Master(long face, long amaster) {
        long __functionAddress = Functions.Get_Multi_Master;
        return JNI.invokePPI(face, amaster, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Multi_Master(FT_Face face, @NativeType(value="FT_Multi_Master *") FT_Multi_Master amaster) {
        return FreeType.nFT_Get_Multi_Master(face.address(), amaster.address());
    }

    public static int nFT_Get_MM_Var(long face, long amaster) {
        long __functionAddress = Functions.Get_MM_Var;
        return JNI.invokePPI(face, amaster, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_MM_Var(FT_Face face, @NativeType(value="FT_MM_Var **") PointerBuffer amaster) {
        if (Checks.CHECKS) {
            Checks.check(amaster, 1);
        }
        return FreeType.nFT_Get_MM_Var(face.address(), MemoryUtil.memAddress(amaster));
    }

    public static int nFT_Done_MM_Var(long library, long amaster) {
        long __functionAddress = Functions.Done_MM_Var;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPI(library, amaster, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Done_MM_Var(@NativeType(value="FT_Library") long library, @NativeType(value="FT_MM_Var *") FT_MM_Var amaster) {
        return FreeType.nFT_Done_MM_Var(library, amaster.address());
    }

    public static int nFT_Set_MM_Design_Coordinates(long face, int num_coords, long coords) {
        long __functionAddress = Functions.Set_MM_Design_Coordinates;
        return JNI.invokePPI(face, num_coords, coords, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_MM_Design_Coordinates(FT_Face face, @NativeType(value="FT_Long *") CLongBuffer coords) {
        return FreeType.nFT_Set_MM_Design_Coordinates(face.address(), coords.remaining(), MemoryUtil.memAddress(coords));
    }

    public static int nFT_Set_Var_Design_Coordinates(long face, int num_coords, long coords) {
        long __functionAddress = Functions.Set_Var_Design_Coordinates;
        return JNI.invokePPI(face, num_coords, coords, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_Var_Design_Coordinates(FT_Face face, @NativeType(value="FT_Fixed *") CLongBuffer coords) {
        return FreeType.nFT_Set_Var_Design_Coordinates(face.address(), coords.remaining(), MemoryUtil.memAddress(coords));
    }

    public static int nFT_Get_Var_Design_Coordinates(long face, int num_coords, long coords) {
        long __functionAddress = Functions.Get_Var_Design_Coordinates;
        return JNI.invokePPI(face, num_coords, coords, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Var_Design_Coordinates(FT_Face face, @NativeType(value="FT_Fixed *") CLongBuffer coords) {
        return FreeType.nFT_Get_Var_Design_Coordinates(face.address(), coords.remaining(), MemoryUtil.memAddress(coords));
    }

    public static int nFT_Set_MM_Blend_Coordinates(long face, int num_coords, long coords) {
        long __functionAddress = Functions.Set_MM_Blend_Coordinates;
        return JNI.invokePPI(face, num_coords, coords, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_MM_Blend_Coordinates(FT_Face face, @NativeType(value="FT_Fixed *") CLongBuffer coords) {
        return FreeType.nFT_Set_MM_Blend_Coordinates(face.address(), coords.remaining(), MemoryUtil.memAddress(coords));
    }

    public static int nFT_Get_MM_Blend_Coordinates(long face, int num_coords, long coords) {
        long __functionAddress = Functions.Get_MM_Blend_Coordinates;
        return JNI.invokePPI(face, num_coords, coords, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_MM_Blend_Coordinates(FT_Face face, @NativeType(value="FT_Fixed *") CLongBuffer coords) {
        return FreeType.nFT_Get_MM_Blend_Coordinates(face.address(), coords.remaining(), MemoryUtil.memAddress(coords));
    }

    public static int nFT_Set_Var_Blend_Coordinates(long face, int num_coords, long coords) {
        long __functionAddress = Functions.Set_Var_Blend_Coordinates;
        return JNI.invokePPI(face, num_coords, coords, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_Var_Blend_Coordinates(FT_Face face, @NativeType(value="FT_Fixed *") CLongBuffer coords) {
        return FreeType.nFT_Set_Var_Blend_Coordinates(face.address(), coords.remaining(), MemoryUtil.memAddress(coords));
    }

    public static int nFT_Get_Var_Blend_Coordinates(long face, int num_coords, long coords) {
        long __functionAddress = Functions.Get_Var_Blend_Coordinates;
        return JNI.invokePPI(face, num_coords, coords, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Var_Blend_Coordinates(FT_Face face, @NativeType(value="FT_Fixed *") CLongBuffer coords) {
        return FreeType.nFT_Get_Var_Blend_Coordinates(face.address(), coords.remaining(), MemoryUtil.memAddress(coords));
    }

    public static int nFT_Set_MM_WeightVector(long face, int len, long weightvector) {
        long __functionAddress = Functions.Set_MM_WeightVector;
        return JNI.invokePPI(face, len, weightvector, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_MM_WeightVector(FT_Face face, @NativeType(value="FT_Fixed *") @Nullable CLongBuffer weightvector) {
        return FreeType.nFT_Set_MM_WeightVector(face.address(), Checks.remainingSafe(weightvector), MemoryUtil.memAddressSafe(weightvector));
    }

    public static int nFT_Get_MM_WeightVector(long face, long len, long weightvector) {
        long __functionAddress = Functions.Get_MM_WeightVector;
        return JNI.invokePPPI(face, len, weightvector, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_MM_WeightVector(FT_Face face, @NativeType(value="FT_UInt *") IntBuffer len, @NativeType(value="FT_Fixed *") CLongBuffer weightvector) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)len, 1);
            Checks.check(weightvector, len.get(len.position()));
        }
        return FreeType.nFT_Get_MM_WeightVector(face.address(), MemoryUtil.memAddress(len), MemoryUtil.memAddress(weightvector));
    }

    public static int nFT_Get_Var_Axis_Flags(long master, int axis_index, long flags) {
        long __functionAddress = Functions.Get_Var_Axis_Flags;
        return JNI.invokePPI(master, axis_index, flags, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Var_Axis_Flags(@NativeType(value="FT_MM_Var *") FT_MM_Var master, @NativeType(value="FT_UInt") int axis_index, @NativeType(value="FT_UInt *") IntBuffer flags) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)flags, 1);
        }
        return FreeType.nFT_Get_Var_Axis_Flags(master.address(), axis_index, MemoryUtil.memAddress(flags));
    }

    public static int nFT_Set_Named_Instance(long face, int instance_index) {
        long __functionAddress = Functions.Set_Named_Instance;
        return JNI.invokePI(face, instance_index, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_Named_Instance(FT_Face face, @NativeType(value="FT_UInt") int instance_index) {
        return FreeType.nFT_Set_Named_Instance(face.address(), instance_index);
    }

    public static int nFT_Get_Default_Named_Instance(long face, long instance_index) {
        long __functionAddress = Functions.Get_Default_Named_Instance;
        return JNI.invokePPI(face, instance_index, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Default_Named_Instance(FT_Face face, @NativeType(value="FT_UInt *") IntBuffer instance_index) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)instance_index, 1);
        }
        return FreeType.nFT_Get_Default_Named_Instance(face.address(), MemoryUtil.memAddress(instance_index));
    }

    public static int nFT_Add_Module(long library, long clazz) {
        long __functionAddress = Functions.Add_Module;
        if (Checks.CHECKS) {
            Checks.check(library);
            FT_Module_Class.validate(clazz);
        }
        return JNI.invokePPI(library, clazz, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Add_Module(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Module_Class const *") FT_Module_Class clazz) {
        return FreeType.nFT_Add_Module(library, clazz.address());
    }

    public static long nFT_Get_Module(long library, long module_name) {
        long __functionAddress = Functions.Get_Module;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPP(library, module_name, __functionAddress);
    }

    @NativeType(value="FT_Module")
    public static long FT_Get_Module(@NativeType(value="FT_Library") long library, @NativeType(value="char const *") ByteBuffer module_name) {
        if (Checks.CHECKS) {
            Checks.checkNT1(module_name);
        }
        return FreeType.nFT_Get_Module(library, MemoryUtil.memAddress(module_name));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="FT_Module")
    public static long FT_Get_Module(@NativeType(value="FT_Library") long library, @NativeType(value="char const *") CharSequence module_name) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nASCII(module_name, true);
            long module_nameEncoded = stack.getPointerAddress();
            long l = FreeType.nFT_Get_Module(library, module_nameEncoded);
            return l;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    @NativeType(value="FT_Error")
    public static int FT_Remove_Module(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Module") long module) {
        long __functionAddress = Functions.Remove_Module;
        if (Checks.CHECKS) {
            Checks.check(library);
            Checks.check(module);
        }
        return JNI.invokePPI(library, module, __functionAddress);
    }

    public static int nFT_Property_Set(long library, long module_name, long property_name, long value) {
        long __functionAddress = Functions.Property_Set;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPPPI(library, module_name, property_name, value, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Property_Set(@NativeType(value="FT_Library") long library, @NativeType(value="FT_String const *") ByteBuffer module_name, @NativeType(value="FT_String const *") ByteBuffer property_name, @NativeType(value="void const *") ByteBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkNT1(module_name);
            Checks.checkNT1(property_name);
        }
        return FreeType.nFT_Property_Set(library, MemoryUtil.memAddress(module_name), MemoryUtil.memAddress(property_name), MemoryUtil.memAddress(value));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="FT_Error")
    public static int FT_Property_Set(@NativeType(value="FT_Library") long library, @NativeType(value="FT_String const *") CharSequence module_name, @NativeType(value="FT_String const *") CharSequence property_name, @NativeType(value="void const *") ByteBuffer value) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(module_name, true);
            long module_nameEncoded = stack.getPointerAddress();
            stack.nUTF8(property_name, true);
            long property_nameEncoded = stack.getPointerAddress();
            int n = FreeType.nFT_Property_Set(library, module_nameEncoded, property_nameEncoded, MemoryUtil.memAddress(value));
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static int nFT_Property_Get(long library, long module_name, long property_name, long value) {
        long __functionAddress = Functions.Property_Get;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPPPI(library, module_name, property_name, value, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Property_Get(@NativeType(value="FT_Library") long library, @NativeType(value="FT_String const *") ByteBuffer module_name, @NativeType(value="FT_String const *") ByteBuffer property_name, @NativeType(value="void *") ByteBuffer value) {
        if (Checks.CHECKS) {
            Checks.checkNT1(module_name);
            Checks.checkNT1(property_name);
        }
        return FreeType.nFT_Property_Get(library, MemoryUtil.memAddress(module_name), MemoryUtil.memAddress(property_name), MemoryUtil.memAddress(value));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @NativeType(value="FT_Error")
    public static int FT_Property_Get(@NativeType(value="FT_Library") long library, @NativeType(value="FT_String const *") CharSequence module_name, @NativeType(value="FT_String const *") CharSequence property_name, @NativeType(value="void *") ByteBuffer value) {
        MemoryStack stack = MemoryStack.stackGet();
        int stackPointer = stack.getPointer();
        try {
            stack.nUTF8(module_name, true);
            long module_nameEncoded = stack.getPointerAddress();
            stack.nUTF8(property_name, true);
            long property_nameEncoded = stack.getPointerAddress();
            int n = FreeType.nFT_Property_Get(library, module_nameEncoded, property_nameEncoded, MemoryUtil.memAddress(value));
            return n;
        }
        finally {
            stack.setPointer(stackPointer);
        }
    }

    public static void FT_Set_Default_Properties(@NativeType(value="FT_Library") long library) {
        long __functionAddress = Functions.Set_Default_Properties;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        JNI.invokePV(library, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Reference_Library(@NativeType(value="FT_Library") long library) {
        long __functionAddress = Functions.Reference_Library;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePI(library, __functionAddress);
    }

    public static int nFT_New_Library(long memory, long alibrary) {
        long __functionAddress = Functions.New_Library;
        return JNI.invokePPI(memory, alibrary, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_New_Library(FT_Memory memory, @NativeType(value="FT_Library *") PointerBuffer alibrary) {
        if (Checks.CHECKS) {
            Checks.check(alibrary, 1);
        }
        return FreeType.nFT_New_Library(memory.address(), MemoryUtil.memAddress(alibrary));
    }

    @NativeType(value="FT_Error")
    public static int FT_Done_Library(@NativeType(value="FT_Library") long library) {
        long __functionAddress = Functions.Done_Library;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePI(library, __functionAddress);
    }

    public static void nFT_Set_Debug_Hook(long library, int hook_index, long debug_hook) {
        long __functionAddress = Functions.Set_Debug_Hook;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        JNI.invokePPV(library, hook_index, debug_hook, __functionAddress);
    }

    public static void FT_Set_Debug_Hook(@NativeType(value="FT_Library") long library, @NativeType(value="FT_UInt") int hook_index, @NativeType(value="FT_DebugHook_Func") @Nullable FT_DebugHook_FuncI debug_hook) {
        FreeType.nFT_Set_Debug_Hook(library, hook_index, MemoryUtil.memAddressSafe(debug_hook));
    }

    public static void FT_Add_Default_Modules(@NativeType(value="FT_Library") long library) {
        long __functionAddress = Functions.Add_Default_Modules;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        JNI.invokePV(library, __functionAddress);
    }

    @NativeType(value="FT_TrueTypeEngineType")
    public static int FT_Get_TrueType_Engine_Type(@NativeType(value="FT_Library") long library) {
        long __functionAddress = Functions.Get_TrueType_Engine_Type;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePI(library, __functionAddress);
    }

    public static int nFT_OpenType_Validate(long face, int validation_flags, long BASE_table, long GDEF_table, long GPOS_table, long GSUB_table, long JSTF_table) {
        long __functionAddress = Functions.OpenType_Validate;
        return JNI.invokePPPPPPI(face, validation_flags, BASE_table, GDEF_table, GPOS_table, GSUB_table, JSTF_table, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_OpenType_Validate(FT_Face face, @NativeType(value="FT_UInt") int validation_flags, @NativeType(value="FT_Bytes *") PointerBuffer BASE_table, @NativeType(value="FT_Bytes *") PointerBuffer GDEF_table, @NativeType(value="FT_Bytes *") PointerBuffer GPOS_table, @NativeType(value="FT_Bytes *") PointerBuffer GSUB_table, @NativeType(value="FT_Bytes *") PointerBuffer JSTF_table) {
        if (Checks.CHECKS) {
            Checks.check(BASE_table, 1);
            Checks.check(GDEF_table, 1);
            Checks.check(GPOS_table, 1);
            Checks.check(GSUB_table, 1);
            Checks.check(JSTF_table, 1);
        }
        return FreeType.nFT_OpenType_Validate(face.address(), validation_flags, MemoryUtil.memAddress(BASE_table), MemoryUtil.memAddress(GDEF_table), MemoryUtil.memAddress(GPOS_table), MemoryUtil.memAddress(GSUB_table), MemoryUtil.memAddress(JSTF_table));
    }

    public static void nFT_OpenType_Free(long face, long table) {
        long __functionAddress = Functions.OpenType_Free;
        JNI.invokePPV(face, table, __functionAddress);
    }

    public static void FT_OpenType_Free(FT_Face face, @NativeType(value="FT_Bytes") ByteBuffer table) {
        FreeType.nFT_OpenType_Free(face.address(), MemoryUtil.memAddress(table));
    }

    public static int nFT_Outline_Decompose(long outline, long func_interface, long user) {
        long __functionAddress = Functions.Outline_Decompose;
        return JNI.invokePPPI(outline, func_interface, user, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_Decompose(@NativeType(value="FT_Outline *") FT_Outline outline, @NativeType(value="FT_Outline_Funcs const *") FT_Outline_Funcs func_interface, @NativeType(value="void *") long user) {
        return FreeType.nFT_Outline_Decompose(outline.address(), func_interface.address(), user);
    }

    public static int nFT_Outline_New(long library, int numPoints, int numContours, long anoutline) {
        long __functionAddress = Functions.Outline_New;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPI(library, numPoints, numContours, anoutline, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_New(@NativeType(value="FT_Library") long library, @NativeType(value="FT_UInt") int numPoints, @NativeType(value="FT_Int") int numContours, @NativeType(value="FT_Outline *") FT_Outline anoutline) {
        return FreeType.nFT_Outline_New(library, numPoints, numContours, anoutline.address());
    }

    public static int nFT_Outline_Done(long library, long outline) {
        long __functionAddress = Functions.Outline_Done;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPI(library, outline, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_Done(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Outline *") FT_Outline outline) {
        return FreeType.nFT_Outline_Done(library, outline.address());
    }

    public static int nFT_Outline_Check(long outline) {
        long __functionAddress = Functions.Outline_Check;
        return JNI.invokePI(outline, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_Check(@NativeType(value="FT_Outline *") FT_Outline outline) {
        return FreeType.nFT_Outline_Check(outline.address());
    }

    public static void nFT_Outline_Get_CBox(long outline, long acbox) {
        long __functionAddress = Functions.Outline_Get_CBox;
        JNI.invokePPV(outline, acbox, __functionAddress);
    }

    public static void FT_Outline_Get_CBox(@NativeType(value="FT_Outline const *") FT_Outline outline, @NativeType(value="FT_BBox *") FT_BBox acbox) {
        FreeType.nFT_Outline_Get_CBox(outline.address(), acbox.address());
    }

    public static void nFT_Outline_Translate(long outline, long xOffset, long yOffset) {
        long __functionAddress = Functions.Outline_Translate;
        JNI.invokePNNV(outline, xOffset, yOffset, __functionAddress);
    }

    public static void FT_Outline_Translate(@NativeType(value="FT_Outline const *") FT_Outline outline, @NativeType(value="FT_Pos") long xOffset, @NativeType(value="FT_Pos") long yOffset) {
        FreeType.nFT_Outline_Translate(outline.address(), xOffset, yOffset);
    }

    public static int nFT_Outline_Copy(long source, long target) {
        long __functionAddress = Functions.Outline_Copy;
        return JNI.invokePPI(source, target, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_Copy(@NativeType(value="FT_Outline const *") FT_Outline source, @NativeType(value="FT_Outline *") FT_Outline target) {
        return FreeType.nFT_Outline_Copy(source.address(), target.address());
    }

    public static void nFT_Outline_Transform(long outline, long matrix) {
        long __functionAddress = Functions.Outline_Transform;
        JNI.invokePPV(outline, matrix, __functionAddress);
    }

    public static void FT_Outline_Transform(@NativeType(value="FT_Outline const *") FT_Outline outline, @NativeType(value="FT_Matrix const *") FT_Matrix matrix) {
        FreeType.nFT_Outline_Transform(outline.address(), matrix.address());
    }

    public static int nFT_Outline_Embolden(long outline, long strength) {
        long __functionAddress = Functions.Outline_Embolden;
        return JNI.invokePNI(outline, strength, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_Embolden(@NativeType(value="FT_Outline *") FT_Outline outline, @NativeType(value="FT_Pos") long strength) {
        return FreeType.nFT_Outline_Embolden(outline.address(), strength);
    }

    public static int nFT_Outline_EmboldenXY(long outline, long xstrength, long ystrength) {
        long __functionAddress = Functions.Outline_EmboldenXY;
        return JNI.invokePNNI(outline, xstrength, ystrength, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_EmboldenXY(@NativeType(value="FT_Outline *") FT_Outline outline, @NativeType(value="FT_Pos") long xstrength, @NativeType(value="FT_Pos") long ystrength) {
        return FreeType.nFT_Outline_EmboldenXY(outline.address(), xstrength, ystrength);
    }

    public static void nFT_Outline_Reverse(long outline) {
        long __functionAddress = Functions.Outline_Reverse;
        JNI.invokePV(outline, __functionAddress);
    }

    public static void FT_Outline_Reverse(@NativeType(value="FT_Outline *") FT_Outline outline) {
        FreeType.nFT_Outline_Reverse(outline.address());
    }

    public static int nFT_Outline_Get_Bitmap(long library, long outline, long abitmap) {
        long __functionAddress = Functions.Outline_Get_Bitmap;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPPI(library, outline, abitmap, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_Get_Bitmap(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Outline *") FT_Outline outline, @NativeType(value="FT_Bitmap const *") FT_Bitmap abitmap) {
        return FreeType.nFT_Outline_Get_Bitmap(library, outline.address(), abitmap.address());
    }

    public static int nFT_Outline_Render(long library, long outline, long params) {
        long __functionAddress = Functions.Outline_Render;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPPI(library, outline, params, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Outline_Render(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Outline *") FT_Outline outline, @NativeType(value="FT_Raster_Params *") FT_Raster_Params params) {
        return FreeType.nFT_Outline_Render(library, outline.address(), params.address());
    }

    public static int nFT_Outline_Get_Orientation(long outline) {
        long __functionAddress = Functions.Outline_Get_Orientation;
        return JNI.invokePI(outline, __functionAddress);
    }

    @NativeType(value="FT_Orientation")
    public static int FT_Outline_Get_Orientation(@NativeType(value="FT_Outline *") FT_Outline outline) {
        return FreeType.nFT_Outline_Get_Orientation(outline.address());
    }

    public static int nFT_Get_PFR_Metrics(long face, long aoutline_resolution, long ametrics_resolution, long ametrics_x_scale, long ametrics_y_scale) {
        long __functionAddress = Functions.Get_PFR_Metrics;
        return JNI.invokePPPPPI(face, aoutline_resolution, ametrics_resolution, ametrics_x_scale, ametrics_y_scale, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_PFR_Metrics(FT_Face face, @NativeType(value="FT_UInt *") @Nullable IntBuffer aoutline_resolution, @NativeType(value="FT_UInt *") @Nullable IntBuffer ametrics_resolution, @NativeType(value="FT_Fixed *") @Nullable CLongBuffer ametrics_x_scale, @NativeType(value="FT_Fixed *") @Nullable CLongBuffer ametrics_y_scale) {
        if (Checks.CHECKS) {
            Checks.checkSafe((Buffer)aoutline_resolution, 1);
            Checks.checkSafe((Buffer)ametrics_resolution, 1);
            Checks.checkSafe(ametrics_x_scale, 1);
            Checks.checkSafe(ametrics_y_scale, 1);
        }
        return FreeType.nFT_Get_PFR_Metrics(face.address(), MemoryUtil.memAddressSafe(aoutline_resolution), MemoryUtil.memAddressSafe(ametrics_resolution), MemoryUtil.memAddressSafe(ametrics_x_scale), MemoryUtil.memAddressSafe(ametrics_y_scale));
    }

    public static int nFT_Get_PFR_Kerning(long face, int left, int right, long avector) {
        long __functionAddress = Functions.Get_PFR_Kerning;
        return JNI.invokePPI(face, left, right, avector, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_PFR_Kerning(FT_Face face, @NativeType(value="FT_UInt") int left, @NativeType(value="FT_UInt") int right, @NativeType(value="FT_Vector *") FT_Vector avector) {
        return FreeType.nFT_Get_PFR_Kerning(face.address(), left, right, avector.address());
    }

    public static int nFT_Get_PFR_Advance(long face, int gindex, long aadvance) {
        long __functionAddress = Functions.Get_PFR_Advance;
        return JNI.invokePPI(face, gindex, aadvance, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_PFR_Advance(FT_Face face, @NativeType(value="FT_UInt") int gindex, @NativeType(value="FT_Pos *") CLongBuffer aadvance) {
        if (Checks.CHECKS) {
            Checks.check(aadvance, 1);
        }
        return FreeType.nFT_Get_PFR_Advance(face.address(), gindex, MemoryUtil.memAddress(aadvance));
    }

    @NativeType(value="FT_Renderer")
    public static long FT_Get_Renderer(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Glyph_Format") int format) {
        long __functionAddress = Functions.Get_Renderer;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePP(library, format, __functionAddress);
    }

    public static int nFT_Set_Renderer(long library, long renderer, int num_params, long parameters) {
        long __functionAddress = Functions.Set_Renderer;
        if (Checks.CHECKS) {
            Checks.check(library);
            Checks.check(renderer);
        }
        return JNI.invokePPPI(library, renderer, num_params, parameters, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Set_Renderer(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Renderer") long renderer, @NativeType(value="FT_Parameter *") FT_Parameter.Buffer parameters) {
        return FreeType.nFT_Set_Renderer(library, renderer, parameters.remaining(), parameters.address());
    }

    public static int nFT_New_Size(long face, long size) {
        long __functionAddress = Functions.New_Size;
        return JNI.invokePPI(face, size, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_New_Size(FT_Face face, @NativeType(value="FT_Size *") PointerBuffer size) {
        if (Checks.CHECKS) {
            Checks.check(size, 1);
        }
        return FreeType.nFT_New_Size(face.address(), MemoryUtil.memAddress(size));
    }

    public static int nFT_Done_Size(long size) {
        long __functionAddress = Functions.Done_Size;
        return JNI.invokePI(size, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Done_Size(FT_Size size) {
        return FreeType.nFT_Done_Size(size.address());
    }

    public static int nFT_Activate_Size(long size) {
        long __functionAddress = Functions.Activate_Size;
        return JNI.invokePI(size, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Activate_Size(FT_Size size) {
        return FreeType.nFT_Activate_Size(size.address());
    }

    public static int nFT_Get_Sfnt_Name_Count(long face) {
        long __functionAddress = Functions.Get_Sfnt_Name_Count;
        return JNI.invokePI(face, __functionAddress);
    }

    @NativeType(value="FT_UInt")
    public static int FT_Get_Sfnt_Name_Count(FT_Face face) {
        return FreeType.nFT_Get_Sfnt_Name_Count(face.address());
    }

    public static int nFT_Get_Sfnt_Name(long face, int idx, long aname) {
        long __functionAddress = Functions.Get_Sfnt_Name;
        return JNI.invokePPI(face, idx, aname, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Sfnt_Name(FT_Face face, @NativeType(value="FT_UInt") int idx, @NativeType(value="FT_SfntName *") FT_SfntName aname) {
        return FreeType.nFT_Get_Sfnt_Name(face.address(), idx, aname.address());
    }

    public static int nFT_Get_Sfnt_LangTag(long face, int langID, long alangTag) {
        long __functionAddress = Functions.Get_Sfnt_LangTag;
        return JNI.invokePPI(face, langID, alangTag, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_Sfnt_LangTag(FT_Face face, @NativeType(value="FT_UInt") int langID, @NativeType(value="FT_SfntLangTag *") FT_SfntLangTag alangTag) {
        return FreeType.nFT_Get_Sfnt_LangTag(face.address(), langID, alangTag.address());
    }

    public static int nFT_Outline_GetInsideBorder(long outline) {
        long __functionAddress = Functions.Outline_GetInsideBorder;
        return JNI.invokePI(outline, __functionAddress);
    }

    @NativeType(value="FT_StrokerBorder")
    public static int FT_Outline_GetInsideBorder(@NativeType(value="FT_Outline *") FT_Outline outline) {
        return FreeType.nFT_Outline_GetInsideBorder(outline.address());
    }

    public static int nFT_Outline_GetOutsideBorder(long outline) {
        long __functionAddress = Functions.Outline_GetOutsideBorder;
        return JNI.invokePI(outline, __functionAddress);
    }

    @NativeType(value="FT_StrokerBorder")
    public static int FT_Outline_GetOutsideBorder(@NativeType(value="FT_Outline *") FT_Outline outline) {
        return FreeType.nFT_Outline_GetOutsideBorder(outline.address());
    }

    public static int nFT_Stroker_New(long library, long astroker) {
        long __functionAddress = Functions.Stroker_New;
        if (Checks.CHECKS) {
            Checks.check(library);
        }
        return JNI.invokePPI(library, astroker, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_New(@NativeType(value="FT_Library") long library, @NativeType(value="FT_Stroker *") PointerBuffer astroker) {
        if (Checks.CHECKS) {
            Checks.check(astroker, 1);
        }
        return FreeType.nFT_Stroker_New(library, MemoryUtil.memAddress(astroker));
    }

    public static void FT_Stroker_Set(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Fixed") long radius, @NativeType(value="FT_Stroker_LineCap") int line_cap, @NativeType(value="FT_Stroker_LineJoin") int line_join, @NativeType(value="FT_Fixed") long miter_limit) {
        long __functionAddress = Functions.Stroker_Set;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        JNI.invokePNNV(stroker, radius, line_cap, line_join, miter_limit, __functionAddress);
    }

    public static void FT_Stroker_Rewind(@NativeType(value="FT_Stroker") long stroker) {
        long __functionAddress = Functions.Stroker_Rewind;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        JNI.invokePV(stroker, __functionAddress);
    }

    public static int nFT_Stroker_ParseOutline(long stroker, long outline, boolean opened) {
        long __functionAddress = Functions.Stroker_ParseOutline;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPI(stroker, outline, opened, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_ParseOutline(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Outline *") FT_Outline outline, @NativeType(value="FT_Bool") boolean opened) {
        return FreeType.nFT_Stroker_ParseOutline(stroker, outline.address(), opened);
    }

    public static int nFT_Stroker_BeginSubPath(long stroker, long to, boolean open) {
        long __functionAddress = Functions.Stroker_BeginSubPath;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPI(stroker, to, open, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_BeginSubPath(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Vector *") FT_Vector to, @NativeType(value="FT_Bool") boolean open) {
        return FreeType.nFT_Stroker_BeginSubPath(stroker, to.address(), open);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_EndSubPath(@NativeType(value="FT_Stroker") long stroker) {
        long __functionAddress = Functions.Stroker_EndSubPath;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePI(stroker, __functionAddress);
    }

    public static int nFT_Stroker_LineTo(long stroker, long to) {
        long __functionAddress = Functions.Stroker_LineTo;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPI(stroker, to, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_LineTo(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Vector *") FT_Vector to) {
        return FreeType.nFT_Stroker_LineTo(stroker, to.address());
    }

    public static int nFT_Stroker_ConicTo(long stroker, long control, long to) {
        long __functionAddress = Functions.Stroker_ConicTo;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPPI(stroker, control, to, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_ConicTo(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Vector *") FT_Vector control, @NativeType(value="FT_Vector *") FT_Vector to) {
        return FreeType.nFT_Stroker_ConicTo(stroker, control.address(), to.address());
    }

    public static int nFT_Stroker_CubicTo(long stroker, long control1, long control2, long to) {
        long __functionAddress = Functions.Stroker_CubicTo;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPPPI(stroker, control1, control2, to, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_CubicTo(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Vector *") FT_Vector control1, @NativeType(value="FT_Vector *") FT_Vector control2, @NativeType(value="FT_Vector *") FT_Vector to) {
        return FreeType.nFT_Stroker_CubicTo(stroker, control1.address(), control2.address(), to.address());
    }

    public static int nFT_Stroker_GetBorderCounts(long stroker, int border, long anum_points, long anum_contours) {
        long __functionAddress = Functions.Stroker_GetBorderCounts;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPPI(stroker, border, anum_points, anum_contours, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_GetBorderCounts(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_StrokerBorder") int border, @NativeType(value="FT_UInt *") IntBuffer anum_points, @NativeType(value="FT_UInt *") IntBuffer anum_contours) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)anum_points, 1);
            Checks.check((Buffer)anum_contours, 1);
        }
        return FreeType.nFT_Stroker_GetBorderCounts(stroker, border, MemoryUtil.memAddress(anum_points), MemoryUtil.memAddress(anum_contours));
    }

    public static void nFT_Stroker_ExportBorder(long stroker, int border, long outline) {
        long __functionAddress = Functions.Stroker_ExportBorder;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        JNI.invokePPV(stroker, border, outline, __functionAddress);
    }

    public static void FT_Stroker_ExportBorder(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_StrokerBorder") int border, @NativeType(value="FT_Outline *") FT_Outline outline) {
        FreeType.nFT_Stroker_ExportBorder(stroker, border, outline.address());
    }

    public static int nFT_Stroker_GetCounts(long stroker, long anum_points, long anum_contours) {
        long __functionAddress = Functions.Stroker_GetCounts;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPPI(stroker, anum_points, anum_contours, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Stroker_GetCounts(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_UInt *") IntBuffer anum_points, @NativeType(value="FT_UInt *") IntBuffer anum_contours) {
        if (Checks.CHECKS) {
            Checks.check((Buffer)anum_points, 1);
            Checks.check((Buffer)anum_contours, 1);
        }
        return FreeType.nFT_Stroker_GetCounts(stroker, MemoryUtil.memAddress(anum_points), MemoryUtil.memAddress(anum_contours));
    }

    public static void nFT_Stroker_Export(long stroker, long outline) {
        long __functionAddress = Functions.Stroker_Export;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        JNI.invokePPV(stroker, outline, __functionAddress);
    }

    public static void FT_Stroker_Export(@NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Outline *") FT_Outline outline) {
        FreeType.nFT_Stroker_Export(stroker, outline.address());
    }

    public static void FT_Stroker_Done(@NativeType(value="FT_Stroker") long stroker) {
        long __functionAddress = Functions.Stroker_Done;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        JNI.invokePV(stroker, __functionAddress);
    }

    public static int nFT_Glyph_Stroke(long pglyph, long stroker, boolean destroy) {
        long __functionAddress = Functions.Glyph_Stroke;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPI(pglyph, stroker, destroy, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Glyph_Stroke(@NativeType(value="FT_Glyph *") PointerBuffer pglyph, @NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Bool") boolean destroy) {
        if (Checks.CHECKS) {
            Checks.check(pglyph, 1);
        }
        return FreeType.nFT_Glyph_Stroke(MemoryUtil.memAddress(pglyph), stroker, destroy);
    }

    public static int nFT_Glyph_StrokeBorder(long pglyph, long stroker, boolean inside, boolean destroy) {
        long __functionAddress = Functions.Glyph_StrokeBorder;
        if (Checks.CHECKS) {
            Checks.check(stroker);
        }
        return JNI.invokePPI(pglyph, stroker, inside, destroy, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Glyph_StrokeBorder(@NativeType(value="FT_Glyph *") PointerBuffer pglyph, @NativeType(value="FT_Stroker") long stroker, @NativeType(value="FT_Bool") boolean inside, @NativeType(value="FT_Bool") boolean destroy) {
        if (Checks.CHECKS) {
            Checks.check(pglyph, 1);
        }
        return FreeType.nFT_Glyph_StrokeBorder(MemoryUtil.memAddress(pglyph), stroker, inside, destroy);
    }

    public static void nFT_GlyphSlot_Embolden(long slot) {
        long __functionAddress = Functions.GlyphSlot_Embolden;
        JNI.invokePV(slot, __functionAddress);
    }

    public static void FT_GlyphSlot_Embolden(FT_GlyphSlot slot) {
        FreeType.nFT_GlyphSlot_Embolden(slot.address());
    }

    public static void nFT_GlyphSlot_AdjustWeight(long slot, long xdelta, long ydelta) {
        long __functionAddress = Functions.GlyphSlot_AdjustWeight;
        JNI.invokePNNV(slot, xdelta, ydelta, __functionAddress);
    }

    public static void FT_GlyphSlot_AdjustWeight(FT_GlyphSlot slot, @NativeType(value="FT_Fixed") long xdelta, @NativeType(value="FT_Fixed") long ydelta) {
        FreeType.nFT_GlyphSlot_AdjustWeight(slot.address(), xdelta, ydelta);
    }

    public static void nFT_GlyphSlot_Oblique(long slot) {
        long __functionAddress = Functions.GlyphSlot_Oblique;
        JNI.invokePV(slot, __functionAddress);
    }

    public static void FT_GlyphSlot_Oblique(FT_GlyphSlot slot) {
        FreeType.nFT_GlyphSlot_Oblique(slot.address());
    }

    public static void nFT_GlyphSlot_Slant(long slot, long xslant, long yslant) {
        long __functionAddress = Functions.GlyphSlot_Slant;
        JNI.invokePNNV(slot, xslant, yslant, __functionAddress);
    }

    public static void FT_GlyphSlot_Slant(FT_GlyphSlot slot, @NativeType(value="FT_Fixed") long xslant, @NativeType(value="FT_Fixed") long yslant) {
        FreeType.nFT_GlyphSlot_Slant(slot.address(), xslant, yslant);
    }

    @NativeType(value="FT_Fixed")
    public static long FT_Sin(@NativeType(value="FT_Angle") long angle) {
        long __functionAddress = Functions.Sin;
        return JNI.invokeNN(angle, __functionAddress);
    }

    @NativeType(value="FT_Fixed")
    public static long FT_Cos(@NativeType(value="FT_Angle") long angle) {
        long __functionAddress = Functions.Cos;
        return JNI.invokeNN(angle, __functionAddress);
    }

    @NativeType(value="FT_Fixed")
    public static long FT_Tan(@NativeType(value="FT_Angle") long angle) {
        long __functionAddress = Functions.Tan;
        return JNI.invokeNN(angle, __functionAddress);
    }

    @NativeType(value="FT_Angle")
    public static long FT_Atan2(@NativeType(value="FT_Fixed") long x, @NativeType(value="FT_Fixed") long y) {
        long __functionAddress = Functions.Atan2;
        return JNI.invokeNNN(x, y, __functionAddress);
    }

    @NativeType(value="FT_Angle")
    public static long FT_Angle_Diff(@NativeType(value="FT_Angle") long angle1, @NativeType(value="FT_Angle") long angle2) {
        long __functionAddress = Functions.Angle_Diff;
        return JNI.invokeNNN(angle1, angle2, __functionAddress);
    }

    public static void nFT_Vector_Unit(long vec, long angle) {
        long __functionAddress = Functions.Vector_Unit;
        JNI.invokePNV(vec, angle, __functionAddress);
    }

    public static void FT_Vector_Unit(@NativeType(value="FT_Vector *") FT_Vector vec, @NativeType(value="FT_Angle") long angle) {
        FreeType.nFT_Vector_Unit(vec.address(), angle);
    }

    public static void nFT_Vector_Rotate(long vec, long angle) {
        long __functionAddress = Functions.Vector_Rotate;
        JNI.invokePNV(vec, angle, __functionAddress);
    }

    public static void FT_Vector_Rotate(@NativeType(value="FT_Vector *") FT_Vector vec, @NativeType(value="FT_Angle") long angle) {
        FreeType.nFT_Vector_Rotate(vec.address(), angle);
    }

    public static long nFT_Vector_Length(long vec) {
        long __functionAddress = Functions.Vector_Length;
        return JNI.invokePN(vec, __functionAddress);
    }

    @NativeType(value="FT_Fixed")
    public static long FT_Vector_Length(@NativeType(value="FT_Vector *") FT_Vector vec) {
        return FreeType.nFT_Vector_Length(vec.address());
    }

    public static void nFT_Vector_Polarize(long vec, long length, long angle) {
        long __functionAddress = Functions.Vector_Polarize;
        JNI.invokePPPV(vec, length, angle, __functionAddress);
    }

    public static void FT_Vector_Polarize(@NativeType(value="FT_Vector *") FT_Vector vec, @NativeType(value="FT_Fixed *") CLongBuffer length, @NativeType(value="FT_Angle *") CLongBuffer angle) {
        if (Checks.CHECKS) {
            Checks.check(length, 1);
            Checks.check(angle, 1);
        }
        FreeType.nFT_Vector_Polarize(vec.address(), MemoryUtil.memAddress(length), MemoryUtil.memAddress(angle));
    }

    public static void nFT_Vector_From_Polar(long vec, long length, long angle) {
        long __functionAddress = Functions.Vector_From_Polar;
        JNI.invokePNNV(vec, length, angle, __functionAddress);
    }

    public static void FT_Vector_From_Polar(@NativeType(value="FT_Vector *") FT_Vector vec, @NativeType(value="FT_Fixed") long length, @NativeType(value="FT_Angle") long angle) {
        FreeType.nFT_Vector_From_Polar(vec.address(), length, angle);
    }

    public static int nFT_Has_PS_Glyph_Names(long face) {
        long __functionAddress = Functions.Has_PS_Glyph_Names;
        return JNI.invokePI(face, __functionAddress);
    }

    @NativeType(value="FT_Int")
    public static int FT_Has_PS_Glyph_Names(FT_Face face) {
        return FreeType.nFT_Has_PS_Glyph_Names(face.address());
    }

    public static int nFT_Get_PS_Font_Info(long face, long afont_info) {
        long __functionAddress = Functions.Get_PS_Font_Info;
        return JNI.invokePPI(face, afont_info, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_PS_Font_Info(FT_Face face, PS_FontInfo afont_info) {
        return FreeType.nFT_Get_PS_Font_Info(face.address(), afont_info.address());
    }

    public static int nFT_Get_PS_Font_Private(long face, long afont_private) {
        long __functionAddress = Functions.Get_PS_Font_Private;
        if (Checks.CHECKS) {
            Checks.check(afont_private);
        }
        return JNI.invokePPI(face, afont_private, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Get_PS_Font_Private(FT_Face face, @NativeType(value="PS_Private") long afont_private) {
        return FreeType.nFT_Get_PS_Font_Private(face.address(), afont_private);
    }

    public static long nFT_Get_PS_Font_Value(long face, int key, int idx, long value, long value_len) {
        long __functionAddress = Functions.Get_PS_Font_Value;
        return JNI.invokePPNN(face, key, idx, value, value_len, __functionAddress);
    }

    @NativeType(value="FT_Long")
    public static long FT_Get_PS_Font_Value(FT_Face face, @NativeType(value="PS_Dict_Keys") int key, @NativeType(value="FT_UInt") int idx, @NativeType(value="void *") @Nullable ByteBuffer value) {
        return FreeType.nFT_Get_PS_Font_Value(face.address(), key, idx, MemoryUtil.memAddressSafe(value), Checks.remainingSafe(value));
    }

    public static long nFT_Get_Sfnt_Table(long face, int tag) {
        long __functionAddress = Functions.Get_Sfnt_Table;
        return JNI.invokePP(face, tag, __functionAddress);
    }

    @NativeType(value="void *")
    public static long FT_Get_Sfnt_Table(FT_Face face, @NativeType(value="FT_Sfnt_Tag") int tag) {
        return FreeType.nFT_Get_Sfnt_Table(face.address(), tag);
    }

    public static int nFT_Load_Sfnt_Table(long face, long tag, long offset, long buffer, long length) {
        long __functionAddress = Functions.Load_Sfnt_Table;
        return JNI.invokePNNPPI(face, tag, offset, buffer, length, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Load_Sfnt_Table(FT_Face face, @NativeType(value="FT_ULong") long tag, @NativeType(value="FT_Long") long offset, @NativeType(value="FT_Byte *") @Nullable ByteBuffer buffer, @NativeType(value="FT_ULong *") @Nullable CLongBuffer length) {
        if (Checks.CHECKS) {
            Checks.checkSafe(length, 1);
        }
        return FreeType.nFT_Load_Sfnt_Table(face.address(), tag, offset, MemoryUtil.memAddressSafe(buffer), MemoryUtil.memAddressSafe(length));
    }

    public static int nFT_Sfnt_Table_Info(long face, int table_index, long tag, long length) {
        long __functionAddress = Functions.Sfnt_Table_Info;
        return JNI.invokePPPI(face, table_index, tag, length, __functionAddress);
    }

    @NativeType(value="FT_Error")
    public static int FT_Sfnt_Table_Info(FT_Face face, @NativeType(value="FT_UInt") int table_index, @NativeType(value="FT_ULong *") @Nullable CLongBuffer tag, @NativeType(value="FT_ULong *") @Nullable CLongBuffer length) {
        if (Checks.CHECKS) {
            Checks.checkSafe(length, 1);
        }
        return FreeType.nFT_Sfnt_Table_Info(face.address(), table_index, MemoryUtil.memAddressSafe(tag), MemoryUtil.memAddressSafe(length));
    }

    public static long nFT_Get_CMap_Language_ID(long charmap) {
        long __functionAddress = Functions.Get_CMap_Language_ID;
        return JNI.invokePN(charmap, __functionAddress);
    }

    @NativeType(value="FT_ULong")
    public static long FT_Get_CMap_Language_ID(FT_CharMap charmap) {
        return FreeType.nFT_Get_CMap_Language_ID(charmap.address());
    }

    public static long nFT_Get_CMap_Format(long charmap) {
        long __functionAddress = Functions.Get_CMap_Format;
        return JNI.invokePN(charmap, __functionAddress);
    }

    @NativeType(value="FT_Long")
    public static long FT_Get_CMap_Format(FT_CharMap charmap) {
        return FreeType.nFT_Get_CMap_Format(charmap.address());
    }

    public static int FT_ENC_TAG(int a, int b, int c, int d) {
        return (a & 0xFF) << 24 | (b & 0xFF) << 16 | (c & 0xFF) << 8 | d & 0xFF;
    }

    public static boolean FT_HAS_HORIZONTAL(FT_Face face) {
        return (face.face_flags() & 0x10L) != 0L;
    }

    public static boolean FT_HAS_VERTICAL(FT_Face face) {
        return (face.face_flags() & 0x20L) != 0L;
    }

    public static boolean FT_HAS_KERNING(FT_Face face) {
        return (face.face_flags() & 0x40L) != 0L;
    }

    public static boolean FT_IS_SCALABLE(FT_Face face) {
        return (face.face_flags() & 1L) != 0L;
    }

    public static boolean FT_IS_SFNT(FT_Face face) {
        return (face.face_flags() & 8L) != 0L;
    }

    public static boolean FT_IS_FIXED_WIDTH(FT_Face face) {
        return (face.face_flags() & 4L) != 0L;
    }

    public static boolean FT_HAS_FIXED_SIZES(FT_Face face) {
        return (face.face_flags() & 2L) != 0L;
    }

    public static boolean FT_HAS_GLYPH_NAMES(FT_Face face) {
        return (face.face_flags() & 0x200L) != 0L;
    }

    public static boolean FT_HAS_MULTIPLE_MASTERS(FT_Face face) {
        return (face.face_flags() & 0x100L) != 0L;
    }

    public static boolean FT_IS_NAMED_INSTANCE(FT_Face face) {
        return (face.face_index() & 0x7FFF0000L) != 0L;
    }

    public static boolean FT_IS_VARIATION(FT_Face face) {
        return (face.face_flags() & 0x8000L) != 0L;
    }

    public static boolean FT_IS_CID_KEYED(FT_Face face) {
        return (face.face_flags() & 0x1000L) != 0L;
    }

    public static boolean FT_IS_TRICKY(FT_Face face) {
        return (face.face_flags() & 0x2000L) != 0L;
    }

    public static boolean FT_HAS_COLOR(FT_Face face) {
        return (face.face_flags() & 0x4000L) != 0L;
    }

    public static boolean FT_HAS_SVG(FT_Face face) {
        return (face.face_flags() & 0x10000L) != 0L;
    }

    public static boolean FT_HAS_SBIX(FT_Face face) {
        return (face.face_flags() & 0x20000L) != 0L;
    }

    public static boolean FT_HAS_SBIX_OVERLAY(FT_Face face) {
        return (face.face_flags() & 0x40000L) != 0L;
    }

    private static int FT_LOAD_TARGET_(int x) {
        return (x & 0xF) << 16;
    }

    public static int FT_LOAD_TARGET_MODE(int x) {
        return x >> 16 & 0xF;
    }

    public static boolean FTC_IMAGE_TYPE_COMPARE(FTC_ImageType d1, FTC_ImageType d2) {
        return d1.face_id() == d2.face_id() && d1.width() == d2.width() && d1.flags() == d2.flags();
    }

    public static int FT_CURVE_TAG(int flag) {
        return flag & 3;
    }

    public static int FT_IMAGE_TAG(int _x1, int _x2, int _x3, int _x4) {
        return (_x1 & 0xFF) << 24 | (_x2 & 0xFF) << 16 | (_x3 & 0xFF) << 8 | _x4 & 0xFF;
    }

    public static int FT_MAKE_TAG(int _x1, int _x2, int _x3, int _x4) {
        return (_x1 & 0xFF) << 24 | (_x2 & 0xFF) << 16 | (_x3 & 0xFF) << 8 | _x4 & 0xFF;
    }

    public static boolean FT_IS_EMPTY(FT_List list) {
        return list.head() == null;
    }

    static /* synthetic */ SharedLibrary access$000() {
        return FREETYPE;
    }

    public static final class Functions {
        public static final long Init_FreeType = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Init_FreeType");
        public static final long Done_FreeType = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Done_FreeType");
        public static final long New_Face = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_New_Face");
        public static final long New_Memory_Face = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_New_Memory_Face");
        public static final long Open_Face = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Open_Face");
        public static final long Attach_File = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Attach_File");
        public static final long Attach_Stream = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Attach_Stream");
        public static final long Reference_Face = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Reference_Face");
        public static final long Done_Face = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Done_Face");
        public static final long Select_Size = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Select_Size");
        public static final long Request_Size = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Request_Size");
        public static final long Set_Char_Size = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Char_Size");
        public static final long Set_Pixel_Sizes = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Pixel_Sizes");
        public static final long Load_Glyph = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Load_Glyph");
        public static final long Load_Char = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Load_Char");
        public static final long Set_Transform = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Transform");
        public static final long Get_Transform = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Transform");
        public static final long Render_Glyph = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Render_Glyph");
        public static final long Get_Kerning = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Kerning");
        public static final long Get_Track_Kerning = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Track_Kerning");
        public static final long Select_Charmap = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Select_Charmap");
        public static final long Set_Charmap = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Charmap");
        public static final long Get_Charmap_Index = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Charmap_Index");
        public static final long Get_Char_Index = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Char_Index");
        public static final long Get_First_Char = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_First_Char");
        public static final long Get_Next_Char = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Next_Char");
        public static final long Face_Properties = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Face_Properties");
        public static final long Get_Name_Index = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Name_Index");
        public static final long Get_Glyph_Name = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Glyph_Name");
        public static final long Get_Postscript_Name = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Postscript_Name");
        public static final long Get_SubGlyph_Info = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_SubGlyph_Info");
        public static final long Get_FSType_Flags = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_FSType_Flags");
        public static final long Face_GetCharVariantIndex = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Face_GetCharVariantIndex");
        public static final long Face_GetCharVariantIsDefault = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Face_GetCharVariantIsDefault");
        public static final long Face_GetVariantSelectors = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Face_GetVariantSelectors");
        public static final long Face_GetVariantsOfChar = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Face_GetVariantsOfChar");
        public static final long Face_GetCharsOfVariant = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Face_GetCharsOfVariant");
        public static final long MulDiv = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_MulDiv");
        public static final long MulFix = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_MulFix");
        public static final long DivFix = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_DivFix");
        public static final long RoundFix = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_RoundFix");
        public static final long CeilFix = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_CeilFix");
        public static final long FloorFix = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_FloorFix");
        public static final long Vector_Transform = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Vector_Transform");
        public static final long Library_Version = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Library_Version");
        public static final long Face_CheckTrueTypePatents = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Face_CheckTrueTypePatents");
        public static final long Face_SetUnpatentedHinting = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Face_SetUnpatentedHinting");
        public static final long Get_Advance = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Advance");
        public static final long Get_Advances = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Advances");
        public static final long Outline_Get_BBox = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Get_BBox");
        public static final long Get_BDF_Charset_ID = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_BDF_Charset_ID");
        public static final long Get_BDF_Property = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_BDF_Property");
        public static final long Bitmap_Init = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Bitmap_Init");
        public static final long Bitmap_Copy = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Bitmap_Copy");
        public static final long Bitmap_Embolden = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Bitmap_Embolden");
        public static final long Bitmap_Convert = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Bitmap_Convert");
        public static final long Bitmap_Blend = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Bitmap_Blend");
        public static final long GlyphSlot_Own_Bitmap = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_GlyphSlot_Own_Bitmap");
        public static final long Bitmap_Done = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Bitmap_Done");
        public static final long Stream_OpenBzip2 = APIUtil.apiGetFunctionAddressOptional(FreeType.access$000(), "FT_Stream_OpenBzip2");
        public static final long FTC_Manager_New = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_Manager_New");
        public static final long FTC_Manager_Reset = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_Manager_Reset");
        public static final long FTC_Manager_Done = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_Manager_Done");
        public static final long FTC_Manager_LookupFace = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_Manager_LookupFace");
        public static final long FTC_Manager_LookupSize = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_Manager_LookupSize");
        public static final long FTC_Node_Unref = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_Node_Unref");
        public static final long FTC_Manager_RemoveFaceID = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_Manager_RemoveFaceID");
        public static final long FTC_CMapCache_New = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_CMapCache_New");
        public static final long FTC_CMapCache_Lookup = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_CMapCache_Lookup");
        public static final long FTC_ImageCache_New = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_ImageCache_New");
        public static final long FTC_ImageCache_Lookup = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_ImageCache_Lookup");
        public static final long FTC_ImageCache_LookupScaler = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_ImageCache_LookupScaler");
        public static final long FTC_SBitCache_New = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_SBitCache_New");
        public static final long FTC_SBitCache_Lookup = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_SBitCache_Lookup");
        public static final long FTC_SBitCache_LookupScaler = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FTC_SBitCache_LookupScaler");
        public static final long Get_CID_Registry_Ordering_Supplement = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_CID_Registry_Ordering_Supplement");
        public static final long Get_CID_Is_Internally_CID_Keyed = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_CID_Is_Internally_CID_Keyed");
        public static final long Get_CID_From_Glyph_Index = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_CID_From_Glyph_Index");
        public static final long Palette_Data_Get = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Palette_Data_Get");
        public static final long Palette_Select = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Palette_Select");
        public static final long Palette_Set_Foreground_Color = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Palette_Set_Foreground_Color");
        public static final long Get_Color_Glyph_Layer = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Color_Glyph_Layer");
        public static final long Get_Color_Glyph_Paint = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Color_Glyph_Paint");
        public static final long Get_Color_Glyph_ClipBox = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Color_Glyph_ClipBox");
        public static final long Get_Paint_Layers = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Paint_Layers");
        public static final long Get_Colorline_Stops = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Colorline_Stops");
        public static final long Get_Paint = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Paint");
        public static final long Error_String = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Error_String");
        public static final long Get_Font_Format = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Font_Format");
        public static final long Get_Gasp = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Gasp");
        public static final long New_Glyph = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_New_Glyph");
        public static final long Get_Glyph = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Glyph");
        public static final long Glyph_Copy = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Glyph_Copy");
        public static final long Glyph_Transform = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Glyph_Transform");
        public static final long Glyph_Get_CBox = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Glyph_Get_CBox");
        public static final long Glyph_To_Bitmap = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Glyph_To_Bitmap");
        public static final long Done_Glyph = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Done_Glyph");
        public static final long Matrix_Multiply = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Matrix_Multiply");
        public static final long Matrix_Invert = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Matrix_Invert");
        public static final long TrueTypeGX_Validate = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_TrueTypeGX_Validate");
        public static final long TrueTypeGX_Free = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_TrueTypeGX_Free");
        public static final long ClassicKern_Validate = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_ClassicKern_Validate");
        public static final long ClassicKern_Free = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_ClassicKern_Free");
        public static final long Stream_OpenGzip = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stream_OpenGzip");
        public static final long Gzip_Uncompress = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Gzip_Uncompress");
        public static final long Library_SetLcdFilter = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Library_SetLcdFilter");
        public static final long Library_SetLcdFilterWeights = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Library_SetLcdFilterWeights");
        public static final long Library_SetLcdGeometry = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Library_SetLcdGeometry");
        public static final long List_Find = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_List_Find");
        public static final long List_Add = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_List_Add");
        public static final long List_Insert = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_List_Insert");
        public static final long List_Remove = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_List_Remove");
        public static final long List_Up = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_List_Up");
        public static final long List_Iterate = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_List_Iterate");
        public static final long List_Finalize = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_List_Finalize");
        public static final long Stream_OpenLZW = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stream_OpenLZW");
        public static final long Get_Multi_Master = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Multi_Master");
        public static final long Get_MM_Var = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_MM_Var");
        public static final long Done_MM_Var = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Done_MM_Var");
        public static final long Set_MM_Design_Coordinates = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_MM_Design_Coordinates");
        public static final long Set_Var_Design_Coordinates = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Var_Design_Coordinates");
        public static final long Get_Var_Design_Coordinates = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Var_Design_Coordinates");
        public static final long Set_MM_Blend_Coordinates = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_MM_Blend_Coordinates");
        public static final long Get_MM_Blend_Coordinates = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_MM_Blend_Coordinates");
        public static final long Set_Var_Blend_Coordinates = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Var_Blend_Coordinates");
        public static final long Get_Var_Blend_Coordinates = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Var_Blend_Coordinates");
        public static final long Set_MM_WeightVector = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_MM_WeightVector");
        public static final long Get_MM_WeightVector = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_MM_WeightVector");
        public static final long Get_Var_Axis_Flags = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Var_Axis_Flags");
        public static final long Set_Named_Instance = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Named_Instance");
        public static final long Get_Default_Named_Instance = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Default_Named_Instance");
        public static final long Add_Module = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Add_Module");
        public static final long Get_Module = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Module");
        public static final long Remove_Module = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Remove_Module");
        public static final long Property_Set = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Property_Set");
        public static final long Property_Get = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Property_Get");
        public static final long Set_Default_Properties = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Default_Properties");
        public static final long Reference_Library = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Reference_Library");
        public static final long New_Library = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_New_Library");
        public static final long Done_Library = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Done_Library");
        public static final long Set_Debug_Hook = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Debug_Hook");
        public static final long Add_Default_Modules = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Add_Default_Modules");
        public static final long Get_TrueType_Engine_Type = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_TrueType_Engine_Type");
        public static final long OpenType_Validate = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_OpenType_Validate");
        public static final long OpenType_Free = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_OpenType_Free");
        public static final long Outline_Decompose = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Decompose");
        public static final long Outline_New = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_New");
        public static final long Outline_Done = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Done");
        public static final long Outline_Check = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Check");
        public static final long Outline_Get_CBox = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Get_CBox");
        public static final long Outline_Translate = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Translate");
        public static final long Outline_Copy = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Copy");
        public static final long Outline_Transform = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Transform");
        public static final long Outline_Embolden = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Embolden");
        public static final long Outline_EmboldenXY = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_EmboldenXY");
        public static final long Outline_Reverse = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Reverse");
        public static final long Outline_Get_Bitmap = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Get_Bitmap");
        public static final long Outline_Render = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Render");
        public static final long Outline_Get_Orientation = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_Get_Orientation");
        public static final long Get_PFR_Metrics = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_PFR_Metrics");
        public static final long Get_PFR_Kerning = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_PFR_Kerning");
        public static final long Get_PFR_Advance = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_PFR_Advance");
        public static final long Get_Renderer = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Renderer");
        public static final long Set_Renderer = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Set_Renderer");
        public static final long New_Size = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_New_Size");
        public static final long Done_Size = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Done_Size");
        public static final long Activate_Size = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Activate_Size");
        public static final long Get_Sfnt_Name_Count = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Sfnt_Name_Count");
        public static final long Get_Sfnt_Name = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Sfnt_Name");
        public static final long Get_Sfnt_LangTag = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Sfnt_LangTag");
        public static final long Outline_GetInsideBorder = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_GetInsideBorder");
        public static final long Outline_GetOutsideBorder = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Outline_GetOutsideBorder");
        public static final long Stroker_New = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_New");
        public static final long Stroker_Set = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_Set");
        public static final long Stroker_Rewind = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_Rewind");
        public static final long Stroker_ParseOutline = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_ParseOutline");
        public static final long Stroker_BeginSubPath = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_BeginSubPath");
        public static final long Stroker_EndSubPath = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_EndSubPath");
        public static final long Stroker_LineTo = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_LineTo");
        public static final long Stroker_ConicTo = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_ConicTo");
        public static final long Stroker_CubicTo = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_CubicTo");
        public static final long Stroker_GetBorderCounts = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_GetBorderCounts");
        public static final long Stroker_ExportBorder = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_ExportBorder");
        public static final long Stroker_GetCounts = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_GetCounts");
        public static final long Stroker_Export = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_Export");
        public static final long Stroker_Done = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Stroker_Done");
        public static final long Glyph_Stroke = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Glyph_Stroke");
        public static final long Glyph_StrokeBorder = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Glyph_StrokeBorder");
        public static final long GlyphSlot_Embolden = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_GlyphSlot_Embolden");
        public static final long GlyphSlot_AdjustWeight = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_GlyphSlot_AdjustWeight");
        public static final long GlyphSlot_Oblique = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_GlyphSlot_Oblique");
        public static final long GlyphSlot_Slant = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_GlyphSlot_Slant");
        public static final long Sin = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Sin");
        public static final long Cos = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Cos");
        public static final long Tan = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Tan");
        public static final long Atan2 = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Atan2");
        public static final long Angle_Diff = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Angle_Diff");
        public static final long Vector_Unit = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Vector_Unit");
        public static final long Vector_Rotate = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Vector_Rotate");
        public static final long Vector_Length = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Vector_Length");
        public static final long Vector_Polarize = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Vector_Polarize");
        public static final long Vector_From_Polar = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Vector_From_Polar");
        public static final long Has_PS_Glyph_Names = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Has_PS_Glyph_Names");
        public static final long Get_PS_Font_Info = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_PS_Font_Info");
        public static final long Get_PS_Font_Private = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_PS_Font_Private");
        public static final long Get_PS_Font_Value = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_PS_Font_Value");
        public static final long Get_Sfnt_Table = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_Sfnt_Table");
        public static final long Load_Sfnt_Table = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Load_Sfnt_Table");
        public static final long Sfnt_Table_Info = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Sfnt_Table_Info");
        public static final long Get_CMap_Language_ID = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_CMap_Language_ID");
        public static final long Get_CMap_Format = APIUtil.apiGetFunctionAddress(FreeType.access$000(), "FT_Get_CMap_Format");

        private Functions() {
        }
    }
}


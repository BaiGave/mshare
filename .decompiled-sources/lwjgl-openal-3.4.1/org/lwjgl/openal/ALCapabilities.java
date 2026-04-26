/*
 * Decompiled with CFR 0.152.
 */
package org.lwjgl.openal;

import java.util.Set;
import java.util.function.IntFunction;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.Checks;
import org.lwjgl.system.FunctionProvider;
import org.lwjgl.system.ThreadLocalUtil;

public final class ALCapabilities {
    public final long alGetError;
    public final long alGetErrorDirect;
    public final long alEnable;
    public final long alEnableDirect;
    public final long alDisable;
    public final long alDisableDirect;
    public final long alIsEnabled;
    public final long alIsEnabledDirect;
    public final long alGetBoolean;
    public final long alGetBooleanDirect;
    public final long alGetInteger;
    public final long alGetIntegerDirect;
    public final long alGetFloat;
    public final long alGetFloatDirect;
    public final long alGetDouble;
    public final long alGetDoubleDirect;
    public final long alGetBooleanv;
    public final long alGetBooleanvDirect;
    public final long alGetIntegerv;
    public final long alGetIntegervDirect;
    public final long alGetFloatv;
    public final long alGetFloatvDirect;
    public final long alGetDoublev;
    public final long alGetDoublevDirect;
    public final long alGetString;
    public final long alGetStringDirect;
    public final long alDistanceModel;
    public final long alDistanceModelDirect;
    public final long alDopplerFactor;
    public final long alDopplerFactorDirect;
    public final long alDopplerVelocity;
    public final long alListenerf;
    public final long alListenerfDirect;
    public final long alListeneri;
    public final long alListeneriDirect;
    public final long alListener3f;
    public final long alListener3fDirect;
    public final long alListenerfv;
    public final long alListenerfvDirect;
    public final long alGetListenerf;
    public final long alGetListenerfDirect;
    public final long alGetListeneri;
    public final long alGetListeneriDirect;
    public final long alGetListener3f;
    public final long alGetListener3fDirect;
    public final long alGetListenerfv;
    public final long alGetListenerfvDirect;
    public final long alGenSources;
    public final long alGenSourcesDirect;
    public final long alDeleteSources;
    public final long alDeleteSourcesDirect;
    public final long alIsSource;
    public final long alIsSourceDirect;
    public final long alSourcef;
    public final long alSourcefDirect;
    public final long alSource3f;
    public final long alSource3fDirect;
    public final long alSourcefv;
    public final long alSourcefvDirect;
    public final long alSourcei;
    public final long alSourceiDirect;
    public final long alGetSourcef;
    public final long alGetSourcefDirect;
    public final long alGetSource3f;
    public final long alGetSource3fDirect;
    public final long alGetSourcefv;
    public final long alGetSourcefvDirect;
    public final long alGetSourcei;
    public final long alGetSourceiDirect;
    public final long alGetSourceiv;
    public final long alGetSourceivDirect;
    public final long alSourceQueueBuffers;
    public final long alSourceQueueBuffersDirect;
    public final long alSourceUnqueueBuffers;
    public final long alSourceUnqueueBuffersDirect;
    public final long alSourcePlay;
    public final long alSourcePlayDirect;
    public final long alSourcePause;
    public final long alSourcePauseDirect;
    public final long alSourceStop;
    public final long alSourceStopDirect;
    public final long alSourceRewind;
    public final long alSourceRewindDirect;
    public final long alSourcePlayv;
    public final long alSourcePlayvDirect;
    public final long alSourcePausev;
    public final long alSourcePausevDirect;
    public final long alSourceStopv;
    public final long alSourceStopvDirect;
    public final long alSourceRewindv;
    public final long alSourceRewindvDirect;
    public final long alGenBuffers;
    public final long alGenBuffersDirect;
    public final long alDeleteBuffers;
    public final long alDeleteBuffersDirect;
    public final long alIsBuffer;
    public final long alIsBufferDirect;
    public final long alGetBufferf;
    public final long alGetBufferfDirect;
    public final long alGetBufferi;
    public final long alGetBufferiDirect;
    public final long alBufferData;
    public final long alBufferDataDirect;
    public final long alGetEnumValue;
    public final long alGetEnumValueDirect;
    public final long alGetProcAddress;
    public final long alGetProcAddressDirect;
    public final long alIsExtensionPresent;
    public final long alIsExtensionPresentDirect;
    public final long alListener3i;
    public final long alListener3iDirect;
    public final long alGetListener3i;
    public final long alGetListener3iDirect;
    public final long alGetListeneriv;
    public final long alGetListenerivDirect;
    public final long alSource3i;
    public final long alSource3iDirect;
    public final long alGetSource3i;
    public final long alGetSource3iDirect;
    public final long alListeneriv;
    public final long alListenerivDirect;
    public final long alSourceiv;
    public final long alSourceivDirect;
    public final long alBufferf;
    public final long alBufferfDirect;
    public final long alBuffer3f;
    public final long alBuffer3fDirect;
    public final long alBufferfv;
    public final long alBufferfvDirect;
    public final long alBufferi;
    public final long alBufferiDirect;
    public final long alBuffer3i;
    public final long alBuffer3iDirect;
    public final long alBufferiv;
    public final long alBufferivDirect;
    public final long alGetBuffer3i;
    public final long alGetBuffer3iDirect;
    public final long alGetBufferiv;
    public final long alGetBufferivDirect;
    public final long alGetBuffer3f;
    public final long alGetBuffer3fDirect;
    public final long alGetBufferfv;
    public final long alGetBufferfvDirect;
    public final long alSpeedOfSound;
    public final long alSpeedOfSoundDirect;
    public final long alDebugMessageCallbackEXT;
    public final long alDebugMessageCallbackDirectEXT;
    public final long alDebugMessageInsertEXT;
    public final long alDebugMessageInsertDirectEXT;
    public final long alDebugMessageControlEXT;
    public final long alDebugMessageControlDirectEXT;
    public final long alPushDebugGroupEXT;
    public final long alPushDebugGroupDirectEXT;
    public final long alPopDebugGroupEXT;
    public final long alPopDebugGroupDirectEXT;
    public final long alGetDebugMessageLogEXT;
    public final long alGetDebugMessageLogDirectEXT;
    public final long alObjectLabelEXT;
    public final long alObjectLabelDirectEXT;
    public final long alGetObjectLabelEXT;
    public final long alGetObjectLabelDirectEXT;
    public final long alGetPointerEXT;
    public final long alGetPointerDirectEXT;
    public final long alGetPointervEXT;
    public final long alGetPointervDirectEXT;
    public final long alGenEffects;
    public final long alGenEffectsDirect;
    public final long alDeleteEffects;
    public final long alDeleteEffectsDirect;
    public final long alIsEffect;
    public final long alIsEffectDirect;
    public final long alEffecti;
    public final long alEffectiDirect;
    public final long alEffectiv;
    public final long alEffectivDirect;
    public final long alEffectf;
    public final long alEffectfDirect;
    public final long alEffectfv;
    public final long alEffectfvDirect;
    public final long alGetEffecti;
    public final long alGetEffectiDirect;
    public final long alGetEffectiv;
    public final long alGetEffectivDirect;
    public final long alGetEffectf;
    public final long alGetEffectfDirect;
    public final long alGetEffectfv;
    public final long alGetEffectfvDirect;
    public final long alGenFilters;
    public final long alGenFiltersDirect;
    public final long alDeleteFilters;
    public final long alDeleteFiltersDirect;
    public final long alIsFilter;
    public final long alIsFilterDirect;
    public final long alFilteri;
    public final long alFilteriDirect;
    public final long alFilteriv;
    public final long alFilterivDirect;
    public final long alFilterf;
    public final long alFilterfDirect;
    public final long alFilterfv;
    public final long alFilterfvDirect;
    public final long alGetFilteri;
    public final long alGetFilteriDirect;
    public final long alGetFilteriv;
    public final long alGetFilterivDirect;
    public final long alGetFilterf;
    public final long alGetFilterfDirect;
    public final long alGetFilterfv;
    public final long alGetFilterfvDirect;
    public final long alGenAuxiliaryEffectSlots;
    public final long alGenAuxiliaryEffectSlotsDirect;
    public final long alDeleteAuxiliaryEffectSlots;
    public final long alDeleteAuxiliaryEffectSlotsDirect;
    public final long alIsAuxiliaryEffectSlot;
    public final long alIsAuxiliaryEffectSlotDirect;
    public final long alAuxiliaryEffectSloti;
    public final long alAuxiliaryEffectSlotiDirect;
    public final long alAuxiliaryEffectSlotiv;
    public final long alAuxiliaryEffectSlotivDirect;
    public final long alAuxiliaryEffectSlotf;
    public final long alAuxiliaryEffectSlotfDirect;
    public final long alAuxiliaryEffectSlotfv;
    public final long alAuxiliaryEffectSlotfvDirect;
    public final long alGetAuxiliaryEffectSloti;
    public final long alGetAuxiliaryEffectSlotiDirect;
    public final long alGetAuxiliaryEffectSlotiv;
    public final long alGetAuxiliaryEffectSlotivDirect;
    public final long alGetAuxiliaryEffectSlotf;
    public final long alGetAuxiliaryEffectSlotfDirect;
    public final long alGetAuxiliaryEffectSlotfv;
    public final long alGetAuxiliaryEffectSlotfvDirect;
    public final long alBufferDataStatic;
    public final long alBufferDataStaticDirect;
    public final long alBufferSamplesSOFT;
    public final long alBufferSubSamplesSOFT;
    public final long alGetBufferSamplesSOFT;
    public final long alIsBufferFormatSupportedSOFT;
    public final long alBufferSubDataSOFT;
    public final long alBufferSubDataDirectSOFT;
    public final long alBufferCallbackSOFT;
    public final long alBufferCallbackDirectSOFT;
    public final long alGetBufferPtrSOFT;
    public final long alGetBufferPtrDirectSOFT;
    public final long alGetBuffer3PtrSOFT;
    public final long alGetBuffer3PtrDirectSOFT;
    public final long alGetBufferPtrvSOFT;
    public final long alGetBufferPtrvDirectSOFT;
    public final long alDeferUpdatesSOFT;
    public final long alDeferUpdatesDirectSOFT;
    public final long alProcessUpdatesSOFT;
    public final long alProcessUpdatesDirectSOFT;
    public final long alEventControlSOFT;
    public final long alEventControlDirectSOFT;
    public final long alEventCallbackSOFT;
    public final long alEventCallbackDirectSOFT;
    public final long alGetPointerSOFT;
    public final long alGetPointerDirectSOFT;
    public final long alGetPointervSOFT;
    public final long alGetPointervDirectSOFT;
    public final long alSourcedSOFT;
    public final long alSourcedDirectSOFT;
    public final long alSource3dSOFT;
    public final long alSource3dDirectSOFT;
    public final long alSourcedvSOFT;
    public final long alSourcedvDirectSOFT;
    public final long alGetSourcedSOFT;
    public final long alGetSourcedDirectSOFT;
    public final long alGetSource3dSOFT;
    public final long alGetSource3dDirectSOFT;
    public final long alGetSourcedvSOFT;
    public final long alGetSourcedvDirectSOFT;
    public final long alSourcei64SOFT;
    public final long alSourcei64DirectSOFT;
    public final long alSource3i64SOFT;
    public final long alSource3i64DirectSOFT;
    public final long alSourcei64vSOFT;
    public final long alSourcei64vDirectSOFT;
    public final long alGetSourcei64SOFT;
    public final long alGetSourcei64DirectSOFT;
    public final long alGetSource3i64SOFT;
    public final long alGetSource3i64DirectSOFT;
    public final long alGetSourcei64vSOFT;
    public final long alGetSourcei64vDirectSOFT;
    public final long alGetStringiSOFT;
    public final long alGetStringiDirectSOFT;
    public final long alSourcePlayAtTimeSOFT;
    public final long alSourcePlayAtTimeDirectSOFT;
    public final long alSourcePlayAtTimevSOFT;
    public final long alSourcePlayAtTimevDirectSOFT;
    public final boolean OpenAL10;
    public final boolean OpenAL11;
    public final boolean AL_EXT_ALAW;
    public final boolean AL_EXT_BFORMAT;
    public final boolean AL_EXT_debug;
    public final boolean AL_EXT_direct_context;
    public final boolean AL_EXT_DOUBLE;
    public final boolean ALC_EXT_EFX;
    public final boolean AL_EXT_EXPONENT_DISTANCE;
    public final boolean AL_EXT_FLOAT32;
    public final boolean AL_EXT_IMA4;
    public final boolean AL_EXT_LINEAR_DISTANCE;
    public final boolean AL_EXT_MCFORMATS;
    public final boolean AL_EXT_MULAW;
    public final boolean AL_EXT_MULAW_BFORMAT;
    public final boolean AL_EXT_MULAW_MCFORMATS;
    public final boolean AL_EXT_OFFSET;
    public final boolean AL_EXT_source_distance_model;
    public final boolean AL_EXT_SOURCE_RADIUS;
    public final boolean AL_EXT_STATIC_BUFFER;
    public final boolean AL_EXT_STEREO_ANGLES;
    public final boolean AL_EXT_vorbis;
    public final boolean AL_LOKI_IMA_ADPCM;
    public final boolean AL_LOKI_quadriphonic;
    public final boolean AL_LOKI_WAVE_format;
    public final boolean AL_SOFT_bformat_ex;
    public final boolean AL_SOFT_bformat_hoa;
    public final boolean AL_SOFT_block_alignment;
    public final boolean AL_SOFT_buffer_length_query;
    public final boolean AL_SOFT_buffer_samples;
    public final boolean AL_SOFT_buffer_sub_data;
    public final boolean AL_SOFT_callback_buffer;
    public final boolean AL_SOFT_deferred_updates;
    public final boolean AL_SOFT_direct_channels;
    public final boolean AL_SOFT_direct_channels_remix;
    public final boolean AL_SOFT_effect_target;
    public final boolean AL_SOFT_events;
    public final boolean AL_SOFT_gain_clamp_ex;
    public final boolean AL_SOFT_loop_points;
    public final boolean AL_SOFT_MSADPCM;
    public final boolean AL_SOFT_source_latency;
    public final boolean AL_SOFT_source_length;
    public final boolean AL_SOFT_source_resampler;
    public final boolean AL_SOFT_source_spatialize;
    public final boolean AL_SOFT_source_start_delay;
    public final boolean AL_SOFT_UHJ;
    public final boolean AL_SOFT_UHJ_ex;
    public final boolean AL_SOFTX_hold_on_disconnect;
    final PointerBuffer addresses;

    ALCapabilities(FunctionProvider provider, Set<String> ext, IntFunction<PointerBuffer> bufferFactory) {
        PointerBuffer caps = bufferFactory.apply(289);
        this.OpenAL10 = ALCapabilities.check_AL10(provider, caps, ext);
        this.OpenAL11 = ALCapabilities.check_AL11(provider, caps, ext);
        this.AL_EXT_ALAW = ext.contains("AL_EXT_ALAW");
        this.AL_EXT_BFORMAT = ext.contains("AL_EXT_BFORMAT");
        this.AL_EXT_debug = ALCapabilities.check_EXT_debug(provider, caps, ext);
        this.AL_EXT_direct_context = ext.contains("AL_EXT_direct_context");
        this.AL_EXT_DOUBLE = ext.contains("AL_EXT_DOUBLE");
        this.ALC_EXT_EFX = ALCapabilities.check_EXT_EFX(provider, caps, ext);
        this.AL_EXT_EXPONENT_DISTANCE = ext.contains("AL_EXT_EXPONENT_DISTANCE");
        this.AL_EXT_FLOAT32 = ext.contains("AL_EXT_FLOAT32");
        this.AL_EXT_IMA4 = ext.contains("AL_EXT_IMA4");
        this.AL_EXT_LINEAR_DISTANCE = ext.contains("AL_EXT_LINEAR_DISTANCE");
        this.AL_EXT_MCFORMATS = ext.contains("AL_EXT_MCFORMATS");
        this.AL_EXT_MULAW = ext.contains("AL_EXT_MULAW");
        this.AL_EXT_MULAW_BFORMAT = ext.contains("AL_EXT_MULAW_BFORMAT");
        this.AL_EXT_MULAW_MCFORMATS = ext.contains("AL_EXT_MULAW_MCFORMATS");
        this.AL_EXT_OFFSET = ext.contains("AL_EXT_OFFSET");
        this.AL_EXT_source_distance_model = ext.contains("AL_EXT_source_distance_model");
        this.AL_EXT_SOURCE_RADIUS = ext.contains("AL_EXT_SOURCE_RADIUS");
        this.AL_EXT_STATIC_BUFFER = ALCapabilities.check_EXT_STATIC_BUFFER(provider, caps, ext);
        this.AL_EXT_STEREO_ANGLES = ext.contains("AL_EXT_STEREO_ANGLES");
        this.AL_EXT_vorbis = ext.contains("AL_EXT_vorbis");
        this.AL_LOKI_IMA_ADPCM = ext.contains("AL_LOKI_IMA_ADPCM");
        this.AL_LOKI_quadriphonic = ext.contains("AL_LOKI_quadriphonic");
        this.AL_LOKI_WAVE_format = ext.contains("AL_LOKI_WAVE_format");
        this.AL_SOFT_bformat_ex = ext.contains("AL_SOFT_bformat_ex");
        this.AL_SOFT_bformat_hoa = ext.contains("AL_SOFT_bformat_hoa");
        this.AL_SOFT_block_alignment = ext.contains("AL_SOFT_block_alignment");
        this.AL_SOFT_buffer_length_query = ext.contains("AL_SOFT_buffer_length_query");
        this.AL_SOFT_buffer_samples = ALCapabilities.check_SOFT_buffer_samples(provider, caps, ext);
        this.AL_SOFT_buffer_sub_data = ALCapabilities.check_SOFT_buffer_sub_data(provider, caps, ext);
        this.AL_SOFT_callback_buffer = ALCapabilities.check_SOFT_callback_buffer(provider, caps, ext);
        this.AL_SOFT_deferred_updates = ALCapabilities.check_SOFT_deferred_updates(provider, caps, ext);
        this.AL_SOFT_direct_channels = ext.contains("AL_SOFT_direct_channels");
        this.AL_SOFT_direct_channels_remix = ext.contains("AL_SOFT_direct_channels_remix");
        this.AL_SOFT_effect_target = ext.contains("AL_SOFT_effect_target");
        this.AL_SOFT_events = ALCapabilities.check_SOFT_events(provider, caps, ext);
        this.AL_SOFT_gain_clamp_ex = ext.contains("AL_SOFT_gain_clamp_ex");
        this.AL_SOFT_loop_points = ext.contains("AL_SOFT_loop_points");
        this.AL_SOFT_MSADPCM = ext.contains("AL_SOFT_MSADPCM");
        this.AL_SOFT_source_latency = ALCapabilities.check_SOFT_source_latency(provider, caps, ext);
        this.AL_SOFT_source_length = ext.contains("AL_SOFT_source_length");
        this.AL_SOFT_source_resampler = ALCapabilities.check_SOFT_source_resampler(provider, caps, ext);
        this.AL_SOFT_source_spatialize = ext.contains("AL_SOFT_source_spatialize");
        this.AL_SOFT_source_start_delay = ALCapabilities.check_SOFT_source_start_delay(provider, caps, ext);
        this.AL_SOFT_UHJ = ext.contains("AL_SOFT_UHJ");
        this.AL_SOFT_UHJ_ex = ext.contains("AL_SOFT_UHJ_ex");
        this.AL_SOFTX_hold_on_disconnect = ext.contains("AL_SOFTX_hold_on_disconnect");
        this.alGetError = caps.get(0);
        this.alGetErrorDirect = caps.get(1);
        this.alEnable = caps.get(2);
        this.alEnableDirect = caps.get(3);
        this.alDisable = caps.get(4);
        this.alDisableDirect = caps.get(5);
        this.alIsEnabled = caps.get(6);
        this.alIsEnabledDirect = caps.get(7);
        this.alGetBoolean = caps.get(8);
        this.alGetBooleanDirect = caps.get(9);
        this.alGetInteger = caps.get(10);
        this.alGetIntegerDirect = caps.get(11);
        this.alGetFloat = caps.get(12);
        this.alGetFloatDirect = caps.get(13);
        this.alGetDouble = caps.get(14);
        this.alGetDoubleDirect = caps.get(15);
        this.alGetBooleanv = caps.get(16);
        this.alGetBooleanvDirect = caps.get(17);
        this.alGetIntegerv = caps.get(18);
        this.alGetIntegervDirect = caps.get(19);
        this.alGetFloatv = caps.get(20);
        this.alGetFloatvDirect = caps.get(21);
        this.alGetDoublev = caps.get(22);
        this.alGetDoublevDirect = caps.get(23);
        this.alGetString = caps.get(24);
        this.alGetStringDirect = caps.get(25);
        this.alDistanceModel = caps.get(26);
        this.alDistanceModelDirect = caps.get(27);
        this.alDopplerFactor = caps.get(28);
        this.alDopplerFactorDirect = caps.get(29);
        this.alDopplerVelocity = caps.get(30);
        this.alListenerf = caps.get(31);
        this.alListenerfDirect = caps.get(32);
        this.alListeneri = caps.get(33);
        this.alListeneriDirect = caps.get(34);
        this.alListener3f = caps.get(35);
        this.alListener3fDirect = caps.get(36);
        this.alListenerfv = caps.get(37);
        this.alListenerfvDirect = caps.get(38);
        this.alGetListenerf = caps.get(39);
        this.alGetListenerfDirect = caps.get(40);
        this.alGetListeneri = caps.get(41);
        this.alGetListeneriDirect = caps.get(42);
        this.alGetListener3f = caps.get(43);
        this.alGetListener3fDirect = caps.get(44);
        this.alGetListenerfv = caps.get(45);
        this.alGetListenerfvDirect = caps.get(46);
        this.alGenSources = caps.get(47);
        this.alGenSourcesDirect = caps.get(48);
        this.alDeleteSources = caps.get(49);
        this.alDeleteSourcesDirect = caps.get(50);
        this.alIsSource = caps.get(51);
        this.alIsSourceDirect = caps.get(52);
        this.alSourcef = caps.get(53);
        this.alSourcefDirect = caps.get(54);
        this.alSource3f = caps.get(55);
        this.alSource3fDirect = caps.get(56);
        this.alSourcefv = caps.get(57);
        this.alSourcefvDirect = caps.get(58);
        this.alSourcei = caps.get(59);
        this.alSourceiDirect = caps.get(60);
        this.alGetSourcef = caps.get(61);
        this.alGetSourcefDirect = caps.get(62);
        this.alGetSource3f = caps.get(63);
        this.alGetSource3fDirect = caps.get(64);
        this.alGetSourcefv = caps.get(65);
        this.alGetSourcefvDirect = caps.get(66);
        this.alGetSourcei = caps.get(67);
        this.alGetSourceiDirect = caps.get(68);
        this.alGetSourceiv = caps.get(69);
        this.alGetSourceivDirect = caps.get(70);
        this.alSourceQueueBuffers = caps.get(71);
        this.alSourceQueueBuffersDirect = caps.get(72);
        this.alSourceUnqueueBuffers = caps.get(73);
        this.alSourceUnqueueBuffersDirect = caps.get(74);
        this.alSourcePlay = caps.get(75);
        this.alSourcePlayDirect = caps.get(76);
        this.alSourcePause = caps.get(77);
        this.alSourcePauseDirect = caps.get(78);
        this.alSourceStop = caps.get(79);
        this.alSourceStopDirect = caps.get(80);
        this.alSourceRewind = caps.get(81);
        this.alSourceRewindDirect = caps.get(82);
        this.alSourcePlayv = caps.get(83);
        this.alSourcePlayvDirect = caps.get(84);
        this.alSourcePausev = caps.get(85);
        this.alSourcePausevDirect = caps.get(86);
        this.alSourceStopv = caps.get(87);
        this.alSourceStopvDirect = caps.get(88);
        this.alSourceRewindv = caps.get(89);
        this.alSourceRewindvDirect = caps.get(90);
        this.alGenBuffers = caps.get(91);
        this.alGenBuffersDirect = caps.get(92);
        this.alDeleteBuffers = caps.get(93);
        this.alDeleteBuffersDirect = caps.get(94);
        this.alIsBuffer = caps.get(95);
        this.alIsBufferDirect = caps.get(96);
        this.alGetBufferf = caps.get(97);
        this.alGetBufferfDirect = caps.get(98);
        this.alGetBufferi = caps.get(99);
        this.alGetBufferiDirect = caps.get(100);
        this.alBufferData = caps.get(101);
        this.alBufferDataDirect = caps.get(102);
        this.alGetEnumValue = caps.get(103);
        this.alGetEnumValueDirect = caps.get(104);
        this.alGetProcAddress = caps.get(105);
        this.alGetProcAddressDirect = caps.get(106);
        this.alIsExtensionPresent = caps.get(107);
        this.alIsExtensionPresentDirect = caps.get(108);
        this.alListener3i = caps.get(109);
        this.alListener3iDirect = caps.get(110);
        this.alGetListener3i = caps.get(111);
        this.alGetListener3iDirect = caps.get(112);
        this.alGetListeneriv = caps.get(113);
        this.alGetListenerivDirect = caps.get(114);
        this.alSource3i = caps.get(115);
        this.alSource3iDirect = caps.get(116);
        this.alGetSource3i = caps.get(117);
        this.alGetSource3iDirect = caps.get(118);
        this.alListeneriv = caps.get(119);
        this.alListenerivDirect = caps.get(120);
        this.alSourceiv = caps.get(121);
        this.alSourceivDirect = caps.get(122);
        this.alBufferf = caps.get(123);
        this.alBufferfDirect = caps.get(124);
        this.alBuffer3f = caps.get(125);
        this.alBuffer3fDirect = caps.get(126);
        this.alBufferfv = caps.get(127);
        this.alBufferfvDirect = caps.get(128);
        this.alBufferi = caps.get(129);
        this.alBufferiDirect = caps.get(130);
        this.alBuffer3i = caps.get(131);
        this.alBuffer3iDirect = caps.get(132);
        this.alBufferiv = caps.get(133);
        this.alBufferivDirect = caps.get(134);
        this.alGetBuffer3i = caps.get(135);
        this.alGetBuffer3iDirect = caps.get(136);
        this.alGetBufferiv = caps.get(137);
        this.alGetBufferivDirect = caps.get(138);
        this.alGetBuffer3f = caps.get(139);
        this.alGetBuffer3fDirect = caps.get(140);
        this.alGetBufferfv = caps.get(141);
        this.alGetBufferfvDirect = caps.get(142);
        this.alSpeedOfSound = caps.get(143);
        this.alSpeedOfSoundDirect = caps.get(144);
        this.alDebugMessageCallbackEXT = caps.get(145);
        this.alDebugMessageCallbackDirectEXT = caps.get(146);
        this.alDebugMessageInsertEXT = caps.get(147);
        this.alDebugMessageInsertDirectEXT = caps.get(148);
        this.alDebugMessageControlEXT = caps.get(149);
        this.alDebugMessageControlDirectEXT = caps.get(150);
        this.alPushDebugGroupEXT = caps.get(151);
        this.alPushDebugGroupDirectEXT = caps.get(152);
        this.alPopDebugGroupEXT = caps.get(153);
        this.alPopDebugGroupDirectEXT = caps.get(154);
        this.alGetDebugMessageLogEXT = caps.get(155);
        this.alGetDebugMessageLogDirectEXT = caps.get(156);
        this.alObjectLabelEXT = caps.get(157);
        this.alObjectLabelDirectEXT = caps.get(158);
        this.alGetObjectLabelEXT = caps.get(159);
        this.alGetObjectLabelDirectEXT = caps.get(160);
        this.alGetPointerEXT = caps.get(161);
        this.alGetPointerDirectEXT = caps.get(162);
        this.alGetPointervEXT = caps.get(163);
        this.alGetPointervDirectEXT = caps.get(164);
        this.alGenEffects = caps.get(165);
        this.alGenEffectsDirect = caps.get(166);
        this.alDeleteEffects = caps.get(167);
        this.alDeleteEffectsDirect = caps.get(168);
        this.alIsEffect = caps.get(169);
        this.alIsEffectDirect = caps.get(170);
        this.alEffecti = caps.get(171);
        this.alEffectiDirect = caps.get(172);
        this.alEffectiv = caps.get(173);
        this.alEffectivDirect = caps.get(174);
        this.alEffectf = caps.get(175);
        this.alEffectfDirect = caps.get(176);
        this.alEffectfv = caps.get(177);
        this.alEffectfvDirect = caps.get(178);
        this.alGetEffecti = caps.get(179);
        this.alGetEffectiDirect = caps.get(180);
        this.alGetEffectiv = caps.get(181);
        this.alGetEffectivDirect = caps.get(182);
        this.alGetEffectf = caps.get(183);
        this.alGetEffectfDirect = caps.get(184);
        this.alGetEffectfv = caps.get(185);
        this.alGetEffectfvDirect = caps.get(186);
        this.alGenFilters = caps.get(187);
        this.alGenFiltersDirect = caps.get(188);
        this.alDeleteFilters = caps.get(189);
        this.alDeleteFiltersDirect = caps.get(190);
        this.alIsFilter = caps.get(191);
        this.alIsFilterDirect = caps.get(192);
        this.alFilteri = caps.get(193);
        this.alFilteriDirect = caps.get(194);
        this.alFilteriv = caps.get(195);
        this.alFilterivDirect = caps.get(196);
        this.alFilterf = caps.get(197);
        this.alFilterfDirect = caps.get(198);
        this.alFilterfv = caps.get(199);
        this.alFilterfvDirect = caps.get(200);
        this.alGetFilteri = caps.get(201);
        this.alGetFilteriDirect = caps.get(202);
        this.alGetFilteriv = caps.get(203);
        this.alGetFilterivDirect = caps.get(204);
        this.alGetFilterf = caps.get(205);
        this.alGetFilterfDirect = caps.get(206);
        this.alGetFilterfv = caps.get(207);
        this.alGetFilterfvDirect = caps.get(208);
        this.alGenAuxiliaryEffectSlots = caps.get(209);
        this.alGenAuxiliaryEffectSlotsDirect = caps.get(210);
        this.alDeleteAuxiliaryEffectSlots = caps.get(211);
        this.alDeleteAuxiliaryEffectSlotsDirect = caps.get(212);
        this.alIsAuxiliaryEffectSlot = caps.get(213);
        this.alIsAuxiliaryEffectSlotDirect = caps.get(214);
        this.alAuxiliaryEffectSloti = caps.get(215);
        this.alAuxiliaryEffectSlotiDirect = caps.get(216);
        this.alAuxiliaryEffectSlotiv = caps.get(217);
        this.alAuxiliaryEffectSlotivDirect = caps.get(218);
        this.alAuxiliaryEffectSlotf = caps.get(219);
        this.alAuxiliaryEffectSlotfDirect = caps.get(220);
        this.alAuxiliaryEffectSlotfv = caps.get(221);
        this.alAuxiliaryEffectSlotfvDirect = caps.get(222);
        this.alGetAuxiliaryEffectSloti = caps.get(223);
        this.alGetAuxiliaryEffectSlotiDirect = caps.get(224);
        this.alGetAuxiliaryEffectSlotiv = caps.get(225);
        this.alGetAuxiliaryEffectSlotivDirect = caps.get(226);
        this.alGetAuxiliaryEffectSlotf = caps.get(227);
        this.alGetAuxiliaryEffectSlotfDirect = caps.get(228);
        this.alGetAuxiliaryEffectSlotfv = caps.get(229);
        this.alGetAuxiliaryEffectSlotfvDirect = caps.get(230);
        this.alBufferDataStatic = caps.get(231);
        this.alBufferDataStaticDirect = caps.get(232);
        this.alBufferSamplesSOFT = caps.get(233);
        this.alBufferSubSamplesSOFT = caps.get(234);
        this.alGetBufferSamplesSOFT = caps.get(235);
        this.alIsBufferFormatSupportedSOFT = caps.get(236);
        this.alBufferSubDataSOFT = caps.get(237);
        this.alBufferSubDataDirectSOFT = caps.get(238);
        this.alBufferCallbackSOFT = caps.get(239);
        this.alBufferCallbackDirectSOFT = caps.get(240);
        this.alGetBufferPtrSOFT = caps.get(241);
        this.alGetBufferPtrDirectSOFT = caps.get(242);
        this.alGetBuffer3PtrSOFT = caps.get(243);
        this.alGetBuffer3PtrDirectSOFT = caps.get(244);
        this.alGetBufferPtrvSOFT = caps.get(245);
        this.alGetBufferPtrvDirectSOFT = caps.get(246);
        this.alDeferUpdatesSOFT = caps.get(247);
        this.alDeferUpdatesDirectSOFT = caps.get(248);
        this.alProcessUpdatesSOFT = caps.get(249);
        this.alProcessUpdatesDirectSOFT = caps.get(250);
        this.alEventControlSOFT = caps.get(251);
        this.alEventControlDirectSOFT = caps.get(252);
        this.alEventCallbackSOFT = caps.get(253);
        this.alEventCallbackDirectSOFT = caps.get(254);
        this.alGetPointerSOFT = caps.get(255);
        this.alGetPointerDirectSOFT = caps.get(256);
        this.alGetPointervSOFT = caps.get(257);
        this.alGetPointervDirectSOFT = caps.get(258);
        this.alSourcedSOFT = caps.get(259);
        this.alSourcedDirectSOFT = caps.get(260);
        this.alSource3dSOFT = caps.get(261);
        this.alSource3dDirectSOFT = caps.get(262);
        this.alSourcedvSOFT = caps.get(263);
        this.alSourcedvDirectSOFT = caps.get(264);
        this.alGetSourcedSOFT = caps.get(265);
        this.alGetSourcedDirectSOFT = caps.get(266);
        this.alGetSource3dSOFT = caps.get(267);
        this.alGetSource3dDirectSOFT = caps.get(268);
        this.alGetSourcedvSOFT = caps.get(269);
        this.alGetSourcedvDirectSOFT = caps.get(270);
        this.alSourcei64SOFT = caps.get(271);
        this.alSourcei64DirectSOFT = caps.get(272);
        this.alSource3i64SOFT = caps.get(273);
        this.alSource3i64DirectSOFT = caps.get(274);
        this.alSourcei64vSOFT = caps.get(275);
        this.alSourcei64vDirectSOFT = caps.get(276);
        this.alGetSourcei64SOFT = caps.get(277);
        this.alGetSourcei64DirectSOFT = caps.get(278);
        this.alGetSource3i64SOFT = caps.get(279);
        this.alGetSource3i64DirectSOFT = caps.get(280);
        this.alGetSourcei64vSOFT = caps.get(281);
        this.alGetSourcei64vDirectSOFT = caps.get(282);
        this.alGetStringiSOFT = caps.get(283);
        this.alGetStringiDirectSOFT = caps.get(284);
        this.alSourcePlayAtTimeSOFT = caps.get(285);
        this.alSourcePlayAtTimeDirectSOFT = caps.get(286);
        this.alSourcePlayAtTimevSOFT = caps.get(287);
        this.alSourcePlayAtTimevDirectSOFT = caps.get(288);
        this.addresses = ThreadLocalUtil.setupAddressBuffer(caps);
    }

    public PointerBuffer getAddressBuffer() {
        return this.addresses;
    }

    private static boolean check_AL10(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenAL10")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108}, "alGetError", "alGetErrorDirect", "alEnable", "alEnableDirect", "alDisable", "alDisableDirect", "alIsEnabled", "alIsEnabledDirect", "alGetBoolean", "alGetBooleanDirect", "alGetInteger", "alGetIntegerDirect", "alGetFloat", "alGetFloatDirect", "alGetDouble", "alGetDoubleDirect", "alGetBooleanv", "alGetBooleanvDirect", "alGetIntegerv", "alGetIntegervDirect", "alGetFloatv", "alGetFloatvDirect", "alGetDoublev", "alGetDoublevDirect", "alGetString", "alGetStringDirect", "alDistanceModel", "alDistanceModelDirect", "alDopplerFactor", "alDopplerFactorDirect", "alDopplerVelocity", "alListenerf", "alListenerfDirect", "alListeneri", "alListeneriDirect", "alListener3f", "alListener3fDirect", "alListenerfv", "alListenerfvDirect", "alGetListenerf", "alGetListenerfDirect", "alGetListeneri", "alGetListeneriDirect", "alGetListener3f", "alGetListener3fDirect", "alGetListenerfv", "alGetListenerfvDirect", "alGenSources", "alGenSourcesDirect", "alDeleteSources", "alDeleteSourcesDirect", "alIsSource", "alIsSourceDirect", "alSourcef", "alSourcefDirect", "alSource3f", "alSource3fDirect", "alSourcefv", "alSourcefvDirect", "alSourcei", "alSourceiDirect", "alGetSourcef", "alGetSourcefDirect", "alGetSource3f", "alGetSource3fDirect", "alGetSourcefv", "alGetSourcefvDirect", "alGetSourcei", "alGetSourceiDirect", "alGetSourceiv", "alGetSourceivDirect", "alSourceQueueBuffers", "alSourceQueueBuffersDirect", "alSourceUnqueueBuffers", "alSourceUnqueueBuffersDirect", "alSourcePlay", "alSourcePlayDirect", "alSourcePause", "alSourcePauseDirect", "alSourceStop", "alSourceStopDirect", "alSourceRewind", "alSourceRewindDirect", "alSourcePlayv", "alSourcePlayvDirect", "alSourcePausev", "alSourcePausevDirect", "alSourceStopv", "alSourceStopvDirect", "alSourceRewindv", "alSourceRewindvDirect", "alGenBuffers", "alGenBuffersDirect", "alDeleteBuffers", "alDeleteBuffersDirect", "alIsBuffer", "alIsBufferDirect", "alGetBufferf", "alGetBufferfDirect", "alGetBufferi", "alGetBufferiDirect", "alBufferData", "alBufferDataDirect", "alGetEnumValue", "alGetEnumValueDirect", "alGetProcAddress", "alGetProcAddressDirect", "alIsExtensionPresent", "alIsExtensionPresentDirect") || Checks.reportMissing("AL", "OpenAL10");
    }

    private static boolean check_AL11(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("OpenAL11")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144}, "alListener3i", "alListener3iDirect", "alGetListener3i", "alGetListener3iDirect", "alGetListeneriv", "alGetListenerivDirect", "alSource3i", "alSource3iDirect", "alGetSource3i", "alGetSource3iDirect", "alListeneriv", "alListenerivDirect", "alSourceiv", "alSourceivDirect", "alBufferf", "alBufferfDirect", "alBuffer3f", "alBuffer3fDirect", "alBufferfv", "alBufferfvDirect", "alBufferi", "alBufferiDirect", "alBuffer3i", "alBuffer3iDirect", "alBufferiv", "alBufferivDirect", "alGetBuffer3i", "alGetBuffer3iDirect", "alGetBufferiv", "alGetBufferivDirect", "alGetBuffer3f", "alGetBuffer3fDirect", "alGetBufferfv", "alGetBufferfvDirect", "alSpeedOfSound", "alSpeedOfSoundDirect") || Checks.reportMissing("AL", "OpenAL11");
    }

    private static boolean check_EXT_debug(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_EXT_debug")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164}, "alDebugMessageCallbackEXT", "alDebugMessageCallbackDirectEXT", "alDebugMessageInsertEXT", "alDebugMessageInsertDirectEXT", "alDebugMessageControlEXT", "alDebugMessageControlDirectEXT", "alPushDebugGroupEXT", "alPushDebugGroupDirectEXT", "alPopDebugGroupEXT", "alPopDebugGroupDirectEXT", "alGetDebugMessageLogEXT", "alGetDebugMessageLogDirectEXT", "alObjectLabelEXT", "alObjectLabelDirectEXT", "alGetObjectLabelEXT", "alGetObjectLabelDirectEXT", "alGetPointerEXT", "alGetPointerDirectEXT", "alGetPointervEXT", "alGetPointervDirectEXT") || Checks.reportMissing("AL", "AL_EXT_debug");
    }

    private static boolean check_EXT_EFX(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("ALC_EXT_EFX")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230}, "alGenEffects", "alGenEffectsDirect", "alDeleteEffects", "alDeleteEffectsDirect", "alIsEffect", "alIsEffectDirect", "alEffecti", "alEffectiDirect", "alEffectiv", "alEffectivDirect", "alEffectf", "alEffectfDirect", "alEffectfv", "alEffectfvDirect", "alGetEffecti", "alGetEffectiDirect", "alGetEffectiv", "alGetEffectivDirect", "alGetEffectf", "alGetEffectfDirect", "alGetEffectfv", "alGetEffectfvDirect", "alGenFilters", "alGenFiltersDirect", "alDeleteFilters", "alDeleteFiltersDirect", "alIsFilter", "alIsFilterDirect", "alFilteri", "alFilteriDirect", "alFilteriv", "alFilterivDirect", "alFilterf", "alFilterfDirect", "alFilterfv", "alFilterfvDirect", "alGetFilteri", "alGetFilteriDirect", "alGetFilteriv", "alGetFilterivDirect", "alGetFilterf", "alGetFilterfDirect", "alGetFilterfv", "alGetFilterfvDirect", "alGenAuxiliaryEffectSlots", "alGenAuxiliaryEffectSlotsDirect", "alDeleteAuxiliaryEffectSlots", "alDeleteAuxiliaryEffectSlotsDirect", "alIsAuxiliaryEffectSlot", "alIsAuxiliaryEffectSlotDirect", "alAuxiliaryEffectSloti", "alAuxiliaryEffectSlotiDirect", "alAuxiliaryEffectSlotiv", "alAuxiliaryEffectSlotivDirect", "alAuxiliaryEffectSlotf", "alAuxiliaryEffectSlotfDirect", "alAuxiliaryEffectSlotfv", "alAuxiliaryEffectSlotfvDirect", "alGetAuxiliaryEffectSloti", "alGetAuxiliaryEffectSlotiDirect", "alGetAuxiliaryEffectSlotiv", "alGetAuxiliaryEffectSlotivDirect", "alGetAuxiliaryEffectSlotf", "alGetAuxiliaryEffectSlotfDirect", "alGetAuxiliaryEffectSlotfv", "alGetAuxiliaryEffectSlotfvDirect") || Checks.reportMissing("AL", "ALC_EXT_EFX");
    }

    private static boolean check_EXT_STATIC_BUFFER(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_EXT_STATIC_BUFFER")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{231, 232}, "alBufferDataStatic", "alBufferDataStaticDirect") || Checks.reportMissing("AL", "AL_EXT_STATIC_BUFFER");
    }

    private static boolean check_SOFT_buffer_samples(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_SOFT_buffer_samples")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{233, 234, 235, 236}, "alBufferSamplesSOFT", "alBufferSubSamplesSOFT", "alGetBufferSamplesSOFT", "alIsBufferFormatSupportedSOFT") || Checks.reportMissing("AL", "AL_SOFT_buffer_samples");
    }

    private static boolean check_SOFT_buffer_sub_data(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_SOFT_buffer_sub_data")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{237, 238}, "alBufferSubDataSOFT", "alBufferSubDataDirectSOFT") || Checks.reportMissing("AL", "AL_SOFT_buffer_sub_data");
    }

    private static boolean check_SOFT_callback_buffer(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_SOFT_callback_buffer")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{239, 240, 241, 242, 243, 244, 245, 246}, "alBufferCallbackSOFT", "alBufferCallbackDirectSOFT", "alGetBufferPtrSOFT", "alGetBufferPtrDirectSOFT", "alGetBuffer3PtrSOFT", "alGetBuffer3PtrDirectSOFT", "alGetBufferPtrvSOFT", "alGetBufferPtrvDirectSOFT") || Checks.reportMissing("AL", "AL_SOFT_callback_buffer");
    }

    private static boolean check_SOFT_deferred_updates(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_SOFT_deferred_updates")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{247, 248, 249, 250}, "alDeferUpdatesSOFT", "alDeferUpdatesDirectSOFT", "alProcessUpdatesSOFT", "alProcessUpdatesDirectSOFT") || Checks.reportMissing("AL", "AL_SOFT_deferred_updates");
    }

    private static boolean check_SOFT_events(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_SOFT_events")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{251, 252, 253, 254, 255, 256, 257, 258}, "alEventControlSOFT", "alEventControlDirectSOFT", "alEventCallbackSOFT", "alEventCallbackDirectSOFT", "alGetPointerSOFT", "alGetPointerDirectSOFT", "alGetPointervSOFT", "alGetPointervDirectSOFT") || Checks.reportMissing("AL", "AL_SOFT_events");
    }

    private static boolean check_SOFT_source_latency(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_SOFT_source_latency")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282}, "alSourcedSOFT", "alSourcedDirectSOFT", "alSource3dSOFT", "alSource3dDirectSOFT", "alSourcedvSOFT", "alSourcedvDirectSOFT", "alGetSourcedSOFT", "alGetSourcedDirectSOFT", "alGetSource3dSOFT", "alGetSource3dDirectSOFT", "alGetSourcedvSOFT", "alGetSourcedvDirectSOFT", "alSourcei64SOFT", "alSourcei64DirectSOFT", "alSource3i64SOFT", "alSource3i64DirectSOFT", "alSourcei64vSOFT", "alSourcei64vDirectSOFT", "alGetSourcei64SOFT", "alGetSourcei64DirectSOFT", "alGetSource3i64SOFT", "alGetSource3i64DirectSOFT", "alGetSourcei64vSOFT", "alGetSourcei64vDirectSOFT") || Checks.reportMissing("AL", "AL_SOFT_source_latency");
    }

    private static boolean check_SOFT_source_resampler(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_SOFT_source_resampler")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{283, 284}, "alGetStringiSOFT", "alGetStringiDirectSOFT") || Checks.reportMissing("AL", "AL_SOFT_source_resampler");
    }

    private static boolean check_SOFT_source_start_delay(FunctionProvider provider, PointerBuffer caps, Set<String> ext) {
        if (!ext.contains("AL_SOFT_source_start_delay")) {
            return false;
        }
        return Checks.checkFunctions(provider, caps, new int[]{285, 286, 287, 288}, "alSourcePlayAtTimeSOFT", "alSourcePlayAtTimeDirectSOFT", "alSourcePlayAtTimevSOFT", "alSourcePlayAtTimevDirectSOFT") || Checks.reportMissing("AL", "AL_SOFT_source_start_delay");
    }
}


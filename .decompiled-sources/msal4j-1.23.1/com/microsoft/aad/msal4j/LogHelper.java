/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

final class LogHelper {
    LogHelper() {
    }

    static String createMessage(String originalMessage, String correlationId) {
        return String.format("[Correlation ID: %s] %s", correlationId, originalMessage);
    }

    static String getPiiScrubbedDetails(Throwable ex) {
        StackTraceElement[] stackTraceElements;
        if (ex == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName());
        for (StackTraceElement traceElement : stackTraceElements = ex.getStackTrace()) {
            sb.append(System.getProperty("line.separator") + "\tat " + traceElement);
        }
        if (ex.getCause() != null) {
            sb.append(System.getProperty("line.separator") + "Caused by: " + LogHelper.getPiiScrubbedDetails(ex.getCause()));
        }
        return sb.toString();
    }
}


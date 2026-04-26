/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gametest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;

final class SavingXmlTestReporter
extends JUnitLikeTestReporter {
    SavingXmlTestReporter(File file) throws ParserConfigurationException {
        super(file);
    }

    @Override
    public void save(File file) throws TransformerException {
        try {
            Files.createDirectories(file.toPath().getParent(), new FileAttribute[0]);
        }
        catch (IOException e) {
            throw new TransformerException("Failed to create parent directory", e);
        }
        super.save(file);
    }
}


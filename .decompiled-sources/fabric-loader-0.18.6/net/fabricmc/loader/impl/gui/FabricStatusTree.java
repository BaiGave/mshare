/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import net.fabricmc.loader.impl.FormattedException;

public final class FabricStatusTree {
    public static final String ICON_TYPE_DEFAULT = "";
    public static final String ICON_TYPE_FOLDER = "folder";
    public static final String ICON_TYPE_UNKNOWN_FILE = "file";
    public static final String ICON_TYPE_JAR_FILE = "jar";
    public static final String ICON_TYPE_FABRIC_JAR_FILE = "jar+fabric";
    public static final String ICON_TYPE_FABRIC = "fabric";
    public static final String ICON_TYPE_JSON = "json";
    public static final String ICON_TYPE_FABRIC_JSON = "json+fabric";
    public static final String ICON_TYPE_JAVA_CLASS = "java_class";
    public static final String ICON_TYPE_PACKAGE = "package";
    public static final String ICON_TYPE_JAVA_PACKAGE = "java_package";
    public static final String ICON_TYPE_TICK = "tick";
    public static final String ICON_TYPE_LESSER_CROSS = "lesser_cross";
    public final String title;
    public final String mainText;
    public final List<FabricStatusTab> tabs = new ArrayList<FabricStatusTab>();
    public final List<FabricStatusButton> buttons = new ArrayList<FabricStatusButton>();

    public FabricStatusTree(String title, String mainText) {
        Objects.requireNonNull(title, "null title");
        Objects.requireNonNull(mainText, "null mainText");
        this.title = title;
        this.mainText = mainText;
    }

    public FabricStatusTree(DataInputStream is) throws IOException {
        int i;
        this.title = is.readUTF();
        this.mainText = is.readUTF();
        for (i = is.readInt(); i > 0; --i) {
            this.tabs.add(new FabricStatusTab(is));
        }
        for (i = is.readInt(); i > 0; --i) {
            this.buttons.add(new FabricStatusButton(is));
        }
    }

    public void writeTo(DataOutputStream os) throws IOException {
        os.writeUTF(this.title);
        os.writeUTF(this.mainText);
        os.writeInt(this.tabs.size());
        for (FabricStatusTab tab : this.tabs) {
            tab.writeTo(os);
        }
        os.writeInt(this.buttons.size());
        for (FabricStatusButton button : this.buttons) {
            button.writeTo(os);
        }
    }

    public FabricStatusTab addTab(String name) {
        FabricStatusTab tab = new FabricStatusTab(name);
        this.tabs.add(tab);
        return tab;
    }

    public FabricStatusButton addButton(String text, FabricBasicButtonType type) {
        FabricStatusButton button = new FabricStatusButton(text, type);
        this.buttons.add(button);
        return button;
    }

    public static final class FabricStatusTab {
        public final FabricStatusNode node;
        public FabricTreeWarningLevel filterLevel = FabricTreeWarningLevel.NONE;

        public FabricStatusTab(String name) {
            this.node = new FabricStatusNode(null, name);
        }

        public FabricStatusTab(DataInputStream is) throws IOException {
            this.node = new FabricStatusNode(null, is);
            this.filterLevel = FabricTreeWarningLevel.valueOf(is.readUTF());
        }

        public void writeTo(DataOutputStream os) throws IOException {
            this.node.writeTo(os);
            os.writeUTF(this.filterLevel.name());
        }

        public FabricStatusNode addChild(String name) {
            return this.node.addChild(name);
        }
    }

    public static final class FabricStatusButton {
        public final String text;
        public final FabricBasicButtonType type;
        public String clipboard;
        public boolean shouldClose;
        public boolean shouldContinue;

        public FabricStatusButton(String text, FabricBasicButtonType type) {
            Objects.requireNonNull(text, "null text");
            this.text = text;
            this.type = type;
        }

        public FabricStatusButton(DataInputStream is) throws IOException {
            this.text = is.readUTF();
            this.type = FabricBasicButtonType.valueOf(is.readUTF());
            this.shouldClose = is.readBoolean();
            this.shouldContinue = is.readBoolean();
            if (is.readBoolean()) {
                this.clipboard = is.readUTF();
            }
        }

        public void writeTo(DataOutputStream os) throws IOException {
            os.writeUTF(this.text);
            os.writeUTF(this.type.name());
            os.writeBoolean(this.shouldClose);
            os.writeBoolean(this.shouldContinue);
            if (this.clipboard != null) {
                os.writeBoolean(true);
                os.writeUTF(this.clipboard);
            } else {
                os.writeBoolean(false);
            }
        }

        public FabricStatusButton makeClose() {
            this.shouldClose = true;
            return this;
        }

        public FabricStatusButton makeContinue() {
            this.shouldContinue = true;
            return this;
        }

        public FabricStatusButton withClipboard(String clipboard) {
            this.clipboard = clipboard;
            return this;
        }
    }

    public static enum FabricBasicButtonType {
        CLICK_ONCE,
        CLICK_MANY;

    }

    public static final class FabricStatusNode {
        private FabricStatusNode parent;
        public String name;
        public String iconType = "";
        private FabricTreeWarningLevel warningLevel = FabricTreeWarningLevel.NONE;
        public boolean expandByDefault = false;
        public String details;
        public final List<FabricStatusNode> children = new ArrayList<FabricStatusNode>();

        private FabricStatusNode(FabricStatusNode parent, String name) {
            Objects.requireNonNull(name, "null name");
            this.parent = parent;
            this.name = name;
        }

        public FabricStatusNode(FabricStatusNode parent, DataInputStream is) throws IOException {
            this.parent = parent;
            this.name = is.readUTF();
            this.iconType = is.readUTF();
            this.warningLevel = FabricTreeWarningLevel.valueOf(is.readUTF());
            this.expandByDefault = is.readBoolean();
            if (is.readBoolean()) {
                this.details = is.readUTF();
            }
            for (int i = is.readInt(); i > 0; --i) {
                this.children.add(new FabricStatusNode(this, is));
            }
        }

        public void writeTo(DataOutputStream os) throws IOException {
            os.writeUTF(this.name);
            os.writeUTF(this.iconType);
            os.writeUTF(this.warningLevel.name());
            os.writeBoolean(this.expandByDefault);
            os.writeBoolean(this.details != null);
            if (this.details != null) {
                os.writeUTF(this.details);
            }
            os.writeInt(this.children.size());
            for (FabricStatusNode child : this.children) {
                child.writeTo(os);
            }
        }

        public void moveTo(FabricStatusNode newParent) {
            this.parent.children.remove(this);
            this.parent = newParent;
            newParent.children.add(this);
        }

        public FabricTreeWarningLevel getMaximumWarningLevel() {
            return this.warningLevel;
        }

        public void setWarningLevel(FabricTreeWarningLevel level) {
            if (this.warningLevel == level) {
                return;
            }
            if (this.warningLevel.isHigherThan(level)) {
                throw new Error("Why would you set the warning level multiple times?");
            }
            if (this.parent != null && level.isHigherThan(this.parent.warningLevel)) {
                this.parent.setWarningLevel(level);
            }
            this.warningLevel = level;
            this.expandByDefault |= level.isAtLeast(FabricTreeWarningLevel.WARN);
        }

        public void setError() {
            this.setWarningLevel(FabricTreeWarningLevel.ERROR);
        }

        public void setWarning() {
            this.setWarningLevel(FabricTreeWarningLevel.WARN);
        }

        public void setInfo() {
            this.setWarningLevel(FabricTreeWarningLevel.INFO);
        }

        private FabricStatusNode addChild(String string) {
            if (string.startsWith("\t")) {
                if (this.children.size() == 0) {
                    FabricStatusNode rootChild = new FabricStatusNode(this, FabricStatusTree.ICON_TYPE_DEFAULT);
                    this.children.add(rootChild);
                }
                FabricStatusNode lastChild = this.children.get(this.children.size() - 1);
                lastChild.addChild(string.substring(1));
                lastChild.expandByDefault = true;
                return lastChild;
            }
            FabricStatusNode child = new FabricStatusNode(this, this.cleanForNode(string));
            this.children.add(child);
            return child;
        }

        private String cleanForNode(String string) {
            if ((string = string.trim()).length() > 1 && string.startsWith("-")) {
                string = string.substring(1);
                string = string.trim();
            }
            return string;
        }

        public FabricStatusNode addMessage(String message, FabricTreeWarningLevel warningLevel) {
            String[] lines = message.split("\n");
            FabricStatusNode sub = new FabricStatusNode(this, lines[0]);
            this.children.add(sub);
            sub.setWarningLevel(warningLevel);
            for (int i = 1; i < lines.length; ++i) {
                sub.addChild(lines[i]);
            }
            return sub;
        }

        public FabricStatusNode addException(Throwable exception) {
            return FabricStatusNode.addException(this, Collections.newSetFromMap(new IdentityHashMap()), exception, UnaryOperator.identity(), new StackTraceElement[0]);
        }

        public FabricStatusNode addCleanedException(Throwable exception) {
            return FabricStatusNode.addException(this, Collections.newSetFromMap(new IdentityHashMap()), exception, e -> {
                Throwable cause;
                while ((cause = e.getCause()) != null && e.getSuppressed().length <= 0) {
                    String msg = e.getMessage();
                    if (msg == null) {
                        msg = e.getClass().getName();
                    }
                    if (!msg.equals(cause.getMessage()) && !msg.equals(cause.toString())) break;
                    e = cause;
                }
                return e;
            }, new StackTraceElement[0]);
        }

        private static FabricStatusNode addException(FabricStatusNode node, Set<Throwable> seen, Throwable exception, UnaryOperator<Throwable> filter, StackTraceElement[] parentTrace) {
            if (!seen.add(exception)) {
                return node;
            }
            exception = (Throwable)filter.apply(exception);
            FabricStatusNode sub = node.addException(exception, parentTrace);
            StackTraceElement[] trace = exception.getStackTrace();
            for (Throwable t : exception.getSuppressed()) {
                FabricStatusNode suppressed = FabricStatusNode.addException(sub, seen, t, filter, trace);
                suppressed.name = suppressed.name + " (suppressed)";
                suppressed.expandByDefault = false;
            }
            if (exception.getCause() != null) {
                FabricStatusNode.addException(sub, seen, exception.getCause(), filter, trace);
            }
            return sub;
        }

        private FabricStatusNode addException(Throwable exception, StackTraceElement[] parentTrace) {
            boolean showTrace;
            boolean bl = showTrace = !(exception instanceof FormattedException) || exception.getCause() != null;
            String msg = exception instanceof FormattedException ? Objects.toString(exception.getMessage()) : (exception.getMessage() == null || exception.getMessage().isEmpty() ? exception.toString() : String.format("%s: %s", exception.getClass().getSimpleName(), exception.getMessage()));
            FabricStatusNode sub = this.addMessage(msg, FabricTreeWarningLevel.ERROR);
            if (!showTrace) {
                return sub;
            }
            StackTraceElement[] trace = exception.getStackTrace();
            int uniqueFrames = trace.length - 1;
            for (int i = parentTrace.length - 1; uniqueFrames >= 0 && i >= 0 && trace[uniqueFrames].equals(parentTrace[i]); --uniqueFrames, --i) {
            }
            StringJoiner frames = new StringJoiner("\n");
            int inheritedFrames = trace.length - 1 - uniqueFrames;
            for (int i = 0; i <= uniqueFrames; ++i) {
                frames.add("at " + trace[i]);
            }
            if (inheritedFrames > 0) {
                frames.add("... " + inheritedFrames + " more");
            }
            sub.addChild((String)frames.toString()).iconType = FabricStatusTree.ICON_TYPE_JAVA_CLASS;
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            sub.details = sw.toString();
            return sub;
        }

        public void mergeWithSingleChild(String join) {
            if (this.children.size() != 1) {
                return;
            }
            FabricStatusNode child = this.children.remove(0);
            this.name = this.name + join + child.name;
            for (FabricStatusNode cc : child.children) {
                cc.parent = this;
                this.children.add(cc);
            }
            child.children.clear();
        }

        public void mergeSingleChildFilePath(String folderType) {
            if (!this.iconType.equals(folderType)) {
                return;
            }
            while (this.children.size() == 1 && this.children.get((int)0).iconType.equals(folderType)) {
                this.mergeWithSingleChild("/");
            }
            this.children.sort((a, b) -> a.name.compareTo(b.name));
            this.mergeChildFilePaths(folderType);
        }

        public void mergeChildFilePaths(String folderType) {
            for (FabricStatusNode node : this.children) {
                node.mergeSingleChildFilePath(folderType);
            }
        }

        public FabricStatusNode getFileNode(String file, String folderType, String fileType) {
            FabricStatusNode fileNode = this;
            block0: for (String s : file.split("/")) {
                if (s.isEmpty()) continue;
                for (FabricStatusNode c : fileNode.children) {
                    if (!c.name.equals(s)) continue;
                    fileNode = c;
                    continue block0;
                }
                if (fileNode.iconType.equals(FabricStatusTree.ICON_TYPE_DEFAULT)) {
                    fileNode.iconType = folderType;
                }
                fileNode = fileNode.addChild(s);
            }
            fileNode.iconType = fileType;
            return fileNode;
        }
    }

    public static enum FabricTreeWarningLevel {
        ERROR,
        WARN,
        INFO,
        NONE;

        public final String lowerCaseName = this.name().toLowerCase(Locale.ROOT);

        public boolean isHigherThan(FabricTreeWarningLevel other) {
            return this.ordinal() < other.ordinal();
        }

        public boolean isAtLeast(FabricTreeWarningLevel other) {
            return this.ordinal() <= other.ordinal();
        }

        public static FabricTreeWarningLevel getHighest(FabricTreeWarningLevel a, FabricTreeWarningLevel b) {
            return a.isHigherThan(b) ? a : b;
        }
    }
}


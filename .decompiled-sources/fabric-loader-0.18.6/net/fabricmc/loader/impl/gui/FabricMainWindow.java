/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import net.fabricmc.loader.impl.gui.FabricStatusTree;
import net.fabricmc.loader.impl.util.StringUtil;

class FabricMainWindow {
    static Icon missingIcon = null;

    FabricMainWindow() {
    }

    static void open(FabricStatusTree tree, boolean shouldWait) throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        System.setProperty("apple.awt.application.appearance", "system");
        System.setProperty("apple.awt.application.name", tree.title);
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        FabricMainWindow.open0(tree, shouldWait);
    }

    private static void open0(FabricStatusTree tree, boolean shouldWait) throws Exception {
        CountDownLatch guiTerminatedLatch = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> FabricMainWindow.createUi(guiTerminatedLatch, tree));
        if (shouldWait) {
            guiTerminatedLatch.await();
        }
    }

    private static void createUi(final CountDownLatch onCloseLatch, FabricStatusTree tree) {
        FabricStatusTree.FabricStatusTab tab;
        JFrame window = new JFrame();
        window.setVisible(false);
        window.setTitle(tree.title);
        try {
            BufferedImage image = FabricMainWindow.loadImage("/ui/icon/fabric_x128.png");
            window.setIconImage(image);
            FabricMainWindow.setTaskBarImage(image);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        window.setMinimumSize(new Dimension(640, 480));
        window.setPreferredSize(new Dimension(800, 480));
        window.setLocationByPlatform(true);
        window.setDefaultCloseOperation(2);
        window.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosed(WindowEvent e) {
                onCloseLatch.countDown();
            }
        });
        Container contentPane = window.getContentPane();
        if (tree.mainText != null && !tree.mainText.isEmpty()) {
            JLabel errorLabel = new JLabel(tree.mainText);
            errorLabel.setHorizontalAlignment(0);
            Font font = errorLabel.getFont();
            errorLabel.setFont(font.deriveFont((float)font.getSize() * 2.0f));
            contentPane.add((Component)errorLabel, "North");
        }
        IconSet icons = new IconSet();
        if (tree.tabs.isEmpty()) {
            tab = new FabricStatusTree.FabricStatusTab("Opening Errors");
            tab.addChild("No tabs provided! (Something is very broken)").setError();
            contentPane.add((Component)FabricMainWindow.createTreePanel(tab.node, tab.filterLevel, icons), "Center");
        } else if (tree.tabs.size() == 1) {
            tab = tree.tabs.get(0);
            contentPane.add((Component)FabricMainWindow.createTreePanel(tab.node, tab.filterLevel, icons), "Center");
        } else {
            JTabbedPane tabs = new JTabbedPane();
            contentPane.add((Component)tabs, "Center");
            for (FabricStatusTree.FabricStatusTab tab2 : tree.tabs) {
                tabs.addTab(tab2.node.name, FabricMainWindow.createTreePanel(tab2.node, tab2.filterLevel, icons));
            }
        }
        if (!tree.buttons.isEmpty()) {
            JPanel buttons = new JPanel();
            contentPane.add((Component)buttons, "South");
            buttons.setLayout(new FlowLayout(4));
            for (FabricStatusTree.FabricStatusButton button : tree.buttons) {
                JButton btn = new JButton(button.text);
                buttons.add(btn);
                btn.addActionListener(event -> {
                    if (button.type == FabricStatusTree.FabricBasicButtonType.CLICK_ONCE) {
                        btn.setEnabled(false);
                    }
                    if (button.clipboard != null) {
                        try {
                            StringSelection clipboard = new StringSelection(button.clipboard);
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipboard, clipboard);
                        }
                        catch (IllegalStateException illegalStateException) {
                            // empty catch block
                        }
                    }
                    if (button.shouldClose) {
                        window.dispose();
                    }
                    if (button.shouldContinue) {
                        onCloseLatch.countDown();
                    }
                });
            }
        }
        window.pack();
        window.setVisible(true);
        window.requestFocus();
    }

    private static JPanel createTreePanel(FabricStatusTree.FabricStatusNode rootNode, FabricStatusTree.FabricTreeWarningLevel minimumWarningLevel, IconSet iconSet) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, 1));
        CustomTreeNode treeNode = new CustomTreeNode(null, rootNode, minimumWarningLevel);
        DefaultTreeModel model = new DefaultTreeModel(treeNode);
        JTree tree = new JTree(model);
        tree.setRootVisible(false);
        tree.setRowHeight(0);
        for (int row = 0; row < tree.getRowCount(); ++row) {
            if (!tree.isVisible(tree.getPathForRow(row))) continue;
            CustomTreeNode node = (CustomTreeNode)tree.getPathForRow(row).getLastPathComponent();
            if (!node.node.expandByDefault) continue;
            tree.expandRow(row);
        }
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setCellRenderer(new CustomTreeCellRenderer(iconSet));
        JScrollPane scrollPane = new JScrollPane(tree);
        panel.add(scrollPane);
        return panel;
    }

    private static BufferedImage loadImage(String str) throws IOException {
        return ImageIO.read(FabricMainWindow.loadStream(str));
    }

    private static InputStream loadStream(String str) throws FileNotFoundException {
        InputStream stream = FabricMainWindow.class.getResourceAsStream(str);
        if (stream == null) {
            throw new FileNotFoundException(str);
        }
        return stream;
    }

    private static void setTaskBarImage(Image image) {
        try {
            Class<?> taskbarClass = Class.forName("java.awt.Taskbar");
            Method getTaskbar = taskbarClass.getDeclaredMethod("getTaskbar", new Class[0]);
            Method setIconImage = taskbarClass.getDeclaredMethod("setIconImage", Image.class);
            Object taskbar = getTaskbar.invoke(null, new Object[0]);
            setIconImage.invoke(taskbar, image);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private static Icon missingIcon() {
        if (missingIcon == null) {
            int i;
            BufferedImage img = new BufferedImage(16, 16, 1);
            for (int y = 0; y < 16; ++y) {
                for (int x = 0; x < 16; ++x) {
                    img.setRGB(x, y, 0xFFFFF2);
                }
            }
            for (i = 0; i < 16; ++i) {
                img.setRGB(0, i, 0x222222);
                img.setRGB(15, i, 0x222222);
                img.setRGB(i, 0, 0x222222);
                img.setRGB(i, 15, 0x222222);
            }
            for (i = 3; i < 13; ++i) {
                img.setRGB(i, i, 0x9B0000);
                img.setRGB(i, 16 - i, 0x9B0000);
            }
            missingIcon = new ImageIcon(img);
        }
        return missingIcon;
    }

    private static Icon loadIcon(IconInfo info, int scale) throws IOException {
        BufferedImage img = new BufferedImage(scale, scale, 2);
        Graphics2D imgG2d = img.createGraphics();
        BufferedImage main = FabricMainWindow.loadImage("/ui/icon/" + info.mainPath + "_x" + scale + ".png");
        assert (main.getWidth() == scale);
        assert (main.getHeight() == scale);
        imgG2d.drawImage(main, null, 0, 0);
        int[][] coords = new int[][]{{0, 8}, {8, 8}, {8, 0}};
        for (int i = 0; i < info.decor.length; ++i) {
            String decor = info.decor[i];
            if (decor == null) continue;
            BufferedImage decorImg = FabricMainWindow.loadImage("/ui/icon/decoration/" + decor + "_x" + scale / 2 + ".png");
            assert (decorImg.getWidth() == scale / 2);
            assert (decorImg.getHeight() == scale / 2);
            imgG2d.drawImage(decorImg, null, coords[i][0], coords[i][1]);
        }
        return new ImageIcon(img);
    }

    private static String applyWrapping(String str) {
        if (str.indexOf(10) < 0) {
            return str;
        }
        str = str.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        return "<html>" + str + "</html>";
    }

    static final class IconInfo {
        public final String mainPath;
        public final String[] decor;
        private final int hash;

        IconInfo(String mainPath) {
            this.mainPath = mainPath;
            this.decor = new String[0];
            this.hash = mainPath.hashCode();
        }

        IconInfo(String mainPath, String[] decor) {
            this.mainPath = mainPath;
            this.decor = decor;
            assert (decor.length < 4) : "Cannot fit more than 3 decorations into an image (and leave space for the background)";
            this.hash = decor.length == 0 ? mainPath.hashCode() : mainPath.hashCode() * 31 + Arrays.hashCode(decor);
        }

        public static IconInfo fromNode(FabricStatusTree.FabricStatusNode node) {
            String main;
            String[] split = node.iconType.split("\\+");
            if (split.length == 1 && split[0].isEmpty()) {
                split = new String[]{};
            }
            ArrayList<String> decors = new ArrayList<String>();
            FabricStatusTree.FabricTreeWarningLevel warnLevel = node.getMaximumWarningLevel();
            if (split.length == 0) {
                main = warnLevel == FabricStatusTree.FabricTreeWarningLevel.NONE ? "missing" : "level_" + warnLevel.lowerCaseName;
            } else {
                main = split[0];
                if (warnLevel == FabricStatusTree.FabricTreeWarningLevel.NONE) {
                    decors.add(null);
                } else {
                    decors.add("level_" + warnLevel.lowerCaseName);
                }
                for (int i = 1; i < split.length && i < 3; ++i) {
                    decors.add(split[i]);
                }
            }
            return new IconInfo(main, decors.toArray(new String[0]));
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            IconInfo other = (IconInfo)obj;
            return this.mainPath.equals(other.mainPath) && Arrays.equals(this.decor, other.decor);
        }
    }

    static final class IconSet {
        private final Map<IconInfo, Map<Integer, Icon>> icons = new HashMap<IconInfo, Map<Integer, Icon>>();

        IconSet() {
        }

        public Icon get(IconInfo info) {
            Icon icon;
            int scale = 16;
            Map<Integer, Icon> map = this.icons.get(info);
            if (map == null) {
                map = new HashMap<Integer, Icon>();
                this.icons.put(info, map);
            }
            if ((icon = map.get(scale)) == null) {
                try {
                    icon = FabricMainWindow.loadIcon(info, scale);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    icon = FabricMainWindow.missingIcon();
                }
                map.put(scale, icon);
            }
            return icon;
        }
    }

    static class CustomTreeNode
    implements TreeNode {
        public final TreeNode parent;
        public final FabricStatusTree.FabricStatusNode node;
        public final List<CustomTreeNode> displayedChildren = new ArrayList<CustomTreeNode>();
        private IconInfo iconInfo;

        CustomTreeNode(TreeNode parent, FabricStatusTree.FabricStatusNode node, FabricStatusTree.FabricTreeWarningLevel minimumWarningLevel) {
            this.parent = parent;
            this.node = node;
            for (FabricStatusTree.FabricStatusNode c : node.children) {
                if (minimumWarningLevel.isHigherThan(c.getMaximumWarningLevel())) continue;
                this.displayedChildren.add(new CustomTreeNode(this, c, minimumWarningLevel));
            }
        }

        public IconInfo getIconInfo() {
            if (this.iconInfo == null) {
                this.iconInfo = IconInfo.fromNode(this.node);
            }
            return this.iconInfo;
        }

        public String toString() {
            return FabricMainWindow.applyWrapping(StringUtil.wrapLines(this.node.name, 120));
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return this.displayedChildren.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return this.displayedChildren.size();
        }

        @Override
        public TreeNode getParent() {
            return this.parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return this.displayedChildren.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return !this.isLeaf();
        }

        @Override
        public boolean isLeaf() {
            return this.displayedChildren.isEmpty();
        }

        public Enumeration<CustomTreeNode> children() {
            return new Enumeration<CustomTreeNode>(){
                Iterator<CustomTreeNode> it;
                {
                    this.it = displayedChildren.iterator();
                }

                @Override
                public boolean hasMoreElements() {
                    return this.it.hasNext();
                }

                @Override
                public CustomTreeNode nextElement() {
                    return this.it.next();
                }
            };
        }
    }

    private static final class CustomTreeCellRenderer
    extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = -5621219150752332739L;
        private final IconSet iconSet;

        private CustomTreeCellRenderer(IconSet icons) {
            this.iconSet = icons;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            this.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            if (value instanceof CustomTreeNode) {
                CustomTreeNode c = (CustomTreeNode)value;
                this.setIcon(this.iconSet.get(c.getIconInfo()));
                if (c.node.details == null || c.node.details.isEmpty()) {
                    this.setToolTipText(null);
                } else {
                    this.setToolTipText(FabricMainWindow.applyWrapping(c.node.details));
                }
            }
            return this;
        }
    }
}


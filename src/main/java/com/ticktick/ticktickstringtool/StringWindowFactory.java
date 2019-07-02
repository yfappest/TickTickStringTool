package com.ticktick.ticktickstringtool;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.*;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import com.ticktick.ticktickstringtool.callback.ExecCallback;
import com.ticktick.ticktickstringtool.data.KeyValue;
import com.ticktick.ticktickstringtool.data.StringKeyValue;
import com.ticktick.ticktickstringtool.utlis.Strify;
import com.ticktick.ticktickstringtool.utlis.StringDelete;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class StringWindowFactory implements ToolWindowFactory {

    private WeakReference<Project> project;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.project = new WeakReference<>(project);
        JPanel strifyPanel = createStrifyPanel();
        JPanel delStrPanel = createDelStrPanel();

        JTabbedPane jTabbedPane = new JBTabbedPane();
        jTabbedPane.addTab("添加字符串", strifyPanel);
        jTabbedPane.addTab("删除字符串", delStrPanel);

        JComponent component = toolWindow.getComponent();
        Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        component.setBorder(emptyBorder);
        component.add(jTabbedPane);
    }

    private JPanel createStrifyPanel() {
        JPanel jPanel = new JPanel(new BorderLayout());
        JTextArea strifyTextArea = new JTextArea();
        strifyTextArea.setMargin(JBUI.insets(10));
        strifyTextArea.setLineWrap(true);
        jPanel.add(strifyTextArea, BorderLayout.CENTER);
        JPanel topArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton button = new JButton("去吧！字符串！");
        topArea.add(button);
        JCheckBox jCheckBox = new JCheckBox("不需要除英文外的翻译");
        topArea.add(jCheckBox);
        button.addActionListener(v -> onStrifyClick(strifyTextArea, jCheckBox));
        jPanel.add(topArea, BorderLayout.NORTH);
        return jPanel;
    }

    private JPanel createDelStrPanel() {
        JPanel jPanel = new JPanel(new BorderLayout());
        JTextArea deleteTextArea = new JTextArea();
        deleteTextArea.setMargin(JBUI.insets(10));
        deleteTextArea.setLineWrap(true);
        jPanel.add(deleteTextArea, BorderLayout.CENTER);
        JButton button = new JButton("删除字符串！");
        JRadioButton allRadioButton = new JRadioButton("全部");
        JRadioButton normalOnlyRadioButton = new JRadioButton("除Common");
        JRadioButton commonOnlyRadioButton = new JRadioButton("仅Common");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(allRadioButton);
        buttonGroup.add(normalOnlyRadioButton);
        buttonGroup.add(commonOnlyRadioButton);
        allRadioButton.setSelected(true);
        JPanel topArea = new JPanel(new BorderLayout());
        topArea.add(button, BorderLayout.NORTH);
        JPanel buttons = new JPanel();
        buttons.add(allRadioButton);
        buttons.add(normalOnlyRadioButton);
        buttons.add(commonOnlyRadioButton);
        topArea.add(buttons, BorderLayout.SOUTH);
        jPanel.add(topArea, BorderLayout.NORTH);
        button.addActionListener(v -> onDeleteClick(deleteTextArea, normalOnlyRadioButton, commonOnlyRadioButton));
        return jPanel;
    }

    private void onStrifyClick(JTextArea strifyTextArea, JCheckBox checkBox) {
        strifyTextArea.setEnabled(false);
        Project project = StringWindowFactory.this.project.get();
        if (project == null) {
            return;
        }
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "写入字符串") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                ProgressManager.getInstance().runInReadActionWithWriteActionPriority(() -> {
                    execInBackground(strifyTextArea, checkBox.isSelected(), path -> indicator.setText("正在写入" + path));
                    strifyTextArea.setEnabled(true);
                    strifyTextArea.setText("");
                }, indicator);

            }
        });

    }

    private void execInBackground(JTextArea strifyTextArea, boolean isChecked, ExecCallback callback) {
        Project project = this.project.get();
        if (project == null) {
            return;
        }
        String basePath = project.getBasePath();
        List<StringKeyValue> stringKeyValues = getStringKeyValues(strifyTextArea);
        Strify.newInstance(basePath)
                .setStrifyOriginStringGetter(this::getOriginStrings)
                .setStringKeyValues(stringKeyValues)
                .setExecCallback(callback)
                .setFinishCallback(this::refresh)
                .setCommon(isChecked)
                .exec();
    }

    private String getOriginStrings(String path) {
        Project project = this.project.get();
        if (project == null) {
            return "";
        }
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://" + path);
        if (virtualFile != null) {
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            if (file instanceof XmlFile) {
                XmlDocument document = ((XmlFile) file).getDocument();
                if (document != null) {
                    XmlTag rootTag = document.getRootTag();
                    if (rootTag != null) {
                        XmlTagValue value = rootTag.getValue();
                        return value.getText();
                    }
                }
            }
        }
        return "";
    }

    @NotNull
    private List<StringKeyValue> getStringKeyValues(JTextArea strifyTextArea) {
        List<StringKeyValue> stringKeyValues = new ArrayList<>();
        String text = strifyTextArea.getText();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String[] kv = line.split("\\|");
            if (kv.length > 2) {
                StringKeyValue keyValue = new StringKeyValue(kv);
                stringKeyValues.add(keyValue);
            }
        }
        return stringKeyValues;
    }

    private void onDeleteClick(JTextArea deleteTextArea, JRadioButton normalOnlyRadioButton, JRadioButton commonOnlyRadioButton) {
        Project project = this.project.get();
        if (project == null) {
            return;
        }
        deleteTextArea.setEnabled(false);
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "删除字符串") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                execDeleteInBackground(indicator, deleteTextArea, normalOnlyRadioButton, commonOnlyRadioButton);
            }

        });
    }

    private void execDeleteInBackground(
            @NotNull ProgressIndicator indicator, JTextArea deleteTextArea,
            JRadioButton normalOnlyRadioButton, JRadioButton commonOnlyRadioButton) {
        Project project = this.project.get();
        if (project == null) {
            return;
        }
        ProgressManager.getInstance().runInReadActionWithWriteActionPriority(() -> {
            String text = deleteTextArea.getText();
            String[] strings = text.split("\n");
            Set<String> set = Arrays
                    .stream(strings).map(s -> s.toLowerCase().trim())
                    .collect(Collectors.toSet());
            StringDelete
                    .newInstance(project.getBasePath())
                    .setNeedDeleteKeys(set)
                    .setCommon(!normalOnlyRadioButton.isSelected())
                    .setNormal(!commonOnlyRadioButton.isSelected())
                    .setDeleteOriginStringGetter(StringWindowFactory.this::getOriginStringKeyValue)
                    .setExecCallback(path -> indicator.setText("正在删除" + path))
                    .setFinishCallback(this::refresh)
                    .exec();
            deleteTextArea.setEnabled(true);
            deleteTextArea.setText("");
        }, indicator);
    }

    private List<KeyValue<String, String>> getOriginStringKeyValue(String path) {
        Project project = this.project.get();
        if (project == null) {
            return Collections.emptyList();
        }
        List<KeyValue<String, String>> keyValues = new ArrayList<>();
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://" + path);
        if (virtualFile != null) {
            PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
            if (file instanceof XmlFile) {
                XmlDocument document = ((XmlFile) file).getDocument();
                if (document != null) {
                    XmlTag rootTag = document.getRootTag();
                    if (rootTag != null) {
                        for (XmlTag strTag : rootTag.getSubTags()) {
                            XmlAttribute name = strTag.getAttribute("name");
                            if (name != null) {
                                String key = name.getDisplayValue();
                                keyValues.add(KeyValue.get(key, strTag.getText()));
                            }

                        }
                    }
                }
            }
        }
        return keyValues;
    }

    private void refresh(String path) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(path));
        if (virtualFile != null) {
            Project project = this.project.get();
            if (project != null) {
                ApplicationManager.getApplication().invokeLater(() ->
                        FileEditorManager.getInstance(project).openFile(virtualFile, true));
            }
        }
    }

    @Override
    public void init(ToolWindow window) {
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}

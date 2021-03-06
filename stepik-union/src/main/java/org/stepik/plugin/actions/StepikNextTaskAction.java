package org.stepik.plugin.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.jetbrains.tmp.learning.courseFormat.Task;
import com.jetbrains.tmp.learning.navigation.StudyNavigator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StepikNextTaskAction extends StepikTaskNavigationAction {
    public static final String ACTION_ID = "STEPIK.NextTaskAction";
    public static final String SHORTCUT = "ctrl pressed PERIOD";

    public StepikNextTaskAction() {
        super("Next Task (" + KeymapUtil.getShortcutText(new KeyboardShortcut(KeyStroke.getKeyStroke(SHORTCUT),
                null)) + ")", "Navigate to the next task", AllIcons.Actions.Forward);
    }

    @Override
    protected Task getTargetTask(@NotNull final Task sourceTask) {
        return StudyNavigator.nextTask(sourceTask);
    }

    @NotNull
    @Override
    public String getActionId() {
        return ACTION_ID;
    }

    @Nullable
    @Override
    public String[] getShortcuts() {
        return new String[]{SHORTCUT};
    }
}

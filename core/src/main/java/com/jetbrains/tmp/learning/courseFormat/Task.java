package com.jetbrains.tmp.learning.courseFormat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.annotations.Transient;
import com.jetbrains.tmp.learning.StudyUtils;
import com.jetbrains.tmp.learning.core.EduNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of task which contains task files, tests, input file for tests
 */
public class Task implements StudyItem {
    private int position;
    private String text;
    private Map<String, String> testsText = new HashMap<>();
    private StudyStatus myStatus = StudyStatus.Unchecked;

    @Transient
    private Lesson myLesson;

    @Expose
    private String name;
    @Expose
    private int id;
    @Expose
    @SerializedName("task_files")
    public Map<String, TaskFile> taskFiles = new HashMap<>();

    public Task() {}

    public Task(@NotNull final String name) {
        this.name = name;
    }

    /**
     * Initializes state of task file
     *
     * @param lesson lesson which task belongs to
     */
    public void initTask(final Lesson lesson, boolean isRestarted) {
        setLesson(lesson);
        if (!isRestarted) myStatus = StudyStatus.Unchecked;
        for (TaskFile taskFile : getTaskFiles().values()) {
            taskFile.initTaskFile(this, isRestarted);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public Map<String, String> getTestsText() {
        return testsText;
    }

    public void addTestsTexts(String name, String text) {
        testsText.put(name, text);
    }

    public Map<String, TaskFile> getTaskFiles() {
        return taskFiles;
    }

    @Nullable
    public TaskFile getTaskFile(final String name) {
        return name != null ? taskFiles.get(name) : null;
    }

    public boolean isTaskFile(@NotNull final String fileName) {
        return taskFiles.get(fileName) != null;
    }

    public void addTaskFile(@NotNull final String name, int index) {
        TaskFile taskFile = new TaskFile();
        taskFile.setIndex(index);
        taskFile.setTask(this);
        taskFile.name = name;
        taskFiles.put(name, taskFile);
    }

    public void addTaskFile(@NotNull final TaskFile taskFile) {
        taskFiles.put(taskFile.name, taskFile);
    }

    @Nullable
    public TaskFile getFile(@NotNull final String fileName) {
        return taskFiles.get(fileName);
    }

    @Transient
    public Lesson getLesson() {
        return myLesson;
    }

    @Transient
    public void setLesson(Lesson lesson) {
        myLesson = lesson;
    }

    @Nullable
    public VirtualFile getTaskDir(@NotNull final Project project) {
        VirtualFile courseDir = project.getBaseDir();
        if (courseDir != null) {
            VirtualFile sectionDir = courseDir.findChild(myLesson.getSection().getDirectory());
            if (sectionDir == null) {
                return null;
            }
            VirtualFile lessonDir = sectionDir.findChild(myLesson.getDirectory());
            if (lessonDir != null) {
                return lessonDir.findChild(getDirectory());
            }
        }
        return null;
    }

    @NotNull
    public String getTaskText(@NotNull final Project project) {
        if (!StringUtil.isEmptyOrSpaces(text)) return text;
        final VirtualFile taskDir = getTaskDir(project);
        if (taskDir != null) {
            final VirtualFile file = StudyUtils.findTaskDescriptionVirtualFile(taskDir);
            if (file == null) return "";
            final Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
                return document.getImmutableCharSequence().toString();
            }
        }

        return "";
    }

    @NotNull
    public String getTestsText(@NotNull final Project project) {
        final VirtualFile taskDir = getTaskDir(project);
        if (taskDir != null) {
            final VirtualFile file = taskDir.findChild(EduNames.TESTS_FILE);
            if (file == null) return "";
            final Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
                return document.getImmutableCharSequence().toString();
            }
        }

        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        return id == task.getId();
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getDirectory() {
        return EduNames.TASK + id;
    }

    @SerializedName("stepId")
    @Override
    public int getId() {
        return id;
    }

    @Override
    public StudyStatus getStatus() {
        return myStatus;
    }

    public void setStatus(StudyStatus status) {
        myStatus = status;
        for (TaskFile taskFile : taskFiles.values()) {
            for (AnswerPlaceholder placeholder : taskFile.getAnswerPlaceholders()) {
                placeholder.setStatus(status);
            }
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String getPath() {
        return FileUtil.join(myLesson.getPath(), getDirectory());
    }
}

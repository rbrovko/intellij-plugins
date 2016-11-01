package com.jetbrains.tmp.learning.courseFormat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xmlb.annotations.Transient;
import com.jetbrains.tmp.learning.core.EduNames;
import com.jetbrains.tmp.learning.core.EduUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Lesson implements StudyItem {
    @Transient
    public List<Integer> steps;
    @Expose
    public List<String> tags = new ArrayList<>();
    @Expose
    boolean is_public;
    @Expose
    private int position;
    @Transient
    private Section section = null;

    @Expose
    private int id = NULL_ID;

    @Expose
    @SerializedName("title")
    private String name;

    @Expose
    @SerializedName("task_list")
    private List<Task> taskList = new ArrayList<>();

    public Lesson() {
    }

    public void initLesson(final Section section, boolean isRestarted) {
        setSection(section);
        for (Task task : taskList) {
            task.initTask(this, isRestarted);
        }
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean is_public() {
        return is_public;
    }

    public void setIs_public(boolean is_public) {
        this.is_public = is_public;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void addTask(@NotNull final Task task) {
        taskList.add(task);
    }

    public Task getTask(@NotNull final String name) {
        int id = EduUtils.getIdFromDirectory(name, EduNames.TASK);
        return getTask(id);
    }

    public Task getTask(int id) {
        List<Task> tasks = getTaskList();
        for (Task task : tasks) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null;
    }

    @Override
    public StudyStatus getStatus() {
        for (Task task : taskList) {
            if (task.getStatus() != StudyStatus.Solved) {
                return StudyStatus.Unchecked;
            }
        }
        return StudyStatus.Solved;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getDirectory() {
        return EduNames.LESSON + id;
    }

    @Transient
    public Section getSection() {
        return section;
    }

    @Transient
    public void setSection(Section section) {
        this.section = section;
    }

    public Task getTaskFromPosition(int position) {
        for (Task task : taskList) {
            if (task.getPosition() == position) {
                return task;
            }
        }
        return null;
    }

    @Override
    public String getPath() {
        return FileUtil.join(section.getPath(), getDirectory());
    }
}

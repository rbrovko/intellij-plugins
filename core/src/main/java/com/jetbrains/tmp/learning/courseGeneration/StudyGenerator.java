package com.jetbrains.tmp.learning.courseGeneration;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.tmp.learning.StudyTaskManager;
import com.jetbrains.tmp.learning.StudyUtils;
import com.jetbrains.tmp.learning.core.EduNames;
import com.jetbrains.tmp.learning.courseFormat.Course;
import com.jetbrains.tmp.learning.courseFormat.Lesson;
import com.jetbrains.tmp.learning.courseFormat.Section;
import com.jetbrains.tmp.learning.courseFormat.Task;
import com.jetbrains.tmp.learning.courseFormat.TaskFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StudyGenerator {
    private StudyGenerator() {

    }

    private static final Logger logger = Logger.getInstance(StudyGenerator.class.getName());

    /**
     * Creates task files in its task folder in project user created
     *
     * @param taskDir      project directory of task which task file belongs to
     * @param resourceRoot directory where original task file stored
     * @throws IOException
     */
    public static void createTaskFile(
            @NotNull final VirtualFile taskDir, @NotNull final File resourceRoot,
            @NotNull final String name) throws IOException {
        String systemIndependentName = FileUtil.toSystemIndependentName(name);
        final int index = systemIndependentName.lastIndexOf("/");
        if (index > 0) {
            systemIndependentName = systemIndependentName.substring(index + 1);
        }
        File resourceFile = new File(resourceRoot, name);
        File fileInProject = new File(taskDir.getPath(), systemIndependentName);
        FileUtil.copy(resourceFile, fileInProject);
    }

    /**
     * Creates task directory in its lesson folder in project user created
     *
     * @param lessonDir    project directory of lesson which task belongs to
     * @param resourceRoot directory where original task file stored
     * @throws IOException
     */
    public static void createTask(
            @NotNull final Task task,
            @NotNull final VirtualFile lessonDir,
            @NotNull final File resourceRoot,
            @NotNull final Project project) throws IOException {
        VirtualFile taskDir = lessonDir.createChildDirectory(project, task.getDirectory());
        File newResourceRoot = new File(resourceRoot, taskDir.getName());
        int i = 0;
        for (Map.Entry<String, TaskFile> taskFile : task.getTaskFiles().entrySet()) {
            TaskFile taskFileContent = taskFile.getValue();
            taskFileContent.setIndex(i);
            i++;
            createTaskFile(taskDir, newResourceRoot, taskFile.getKey());
        }
        File[] filesInTask = newResourceRoot.listFiles();
        if (filesInTask != null) {
            for (File file : filesInTask) {
                String fileName = file.getName();
                if (!task.isTaskFile(fileName)) {
                    File resourceFile = new File(newResourceRoot, fileName);
                    File fileInProject = new File(taskDir.getCanonicalPath(), fileName);
                    FileUtil.copy(resourceFile, fileInProject);
                    if (!StudyUtils.isTestsFile(project, fileName) && !StudyUtils.isTaskDescriptionFile(fileName)) {
                        StudyTaskManager.getInstance(project)
                                .addInvisibleFiles(FileUtil.toSystemIndependentName(fileInProject.getPath()));
                    }
                }
            }
        }
    }

    /**
     * Creates lesson directory in its course folder in project user created
     *
     * @param courseDir    project directory of course
     * @param resourceRoot directory where original lesson stored
     * @throws IOException
     */
    public static void createLesson(
            @NotNull final Lesson lesson,
            @NotNull final VirtualFile courseDir,
            @NotNull final File resourceRoot,
            @NotNull final Project project) throws IOException {
        if (EduNames.PYCHARM_ADDITIONAL.equals(lesson.getName())) return;
        String lessonDirName = lesson.getDirectory();
        VirtualFile lessonDir = courseDir.createChildDirectory(project, lessonDirName);
        final List<Task> taskList = lesson.getTaskList();
        for (int i = 1; i <= taskList.size(); i++) {
            Task task = taskList.get(i - 1);
            task.setPosition(i);
            createTask(task, lessonDir, new File(resourceRoot, lessonDir.getName()), project);
        }
    }

    /**
     * Creates course directory in project user created
     *
     * @param baseDir      project directory
     * @param resourceRoot directory where original course is stored
     */
    public static void createCourse(
            @NotNull final Course course,
            @NotNull final VirtualFile baseDir,
            @NotNull final File resourceRoot,
            @NotNull final Project project) {

        try {
            int lessonIndex = 1;
            for (Section section : course.getSections()) {
                VirtualFile sectionDir = baseDir.createChildDirectory(project, section.getDirectory());
                for (Lesson lesson : section.getLessons()) {
                    lesson.setPosition(lessonIndex++);
                    createLesson(lesson, sectionDir, resourceRoot, project);
                }
            }
            baseDir.createChildDirectory(project, EduNames.SANDBOX_DIR);
            File[] files = resourceRoot.listFiles(
                    (dir, name) -> !name.contains(EduNames.LESSON) && !name.equals(EduNames.COURSE_META_FILE) &&
                            !name.equals(EduNames.HINTS));
            if (files != null) {
                for (File file : files) {
                    File dir = new File(baseDir.getPath(), file.getName());
                    if (file.isDirectory()) {
                        FileUtil.copyDir(file, dir);
                        continue;
                    }
                    FileUtil.copy(file, dir);
                }
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

}

package com.jetbrains.tmp.learning.navigation;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.jetbrains.tmp.learning.StudyUtils;
import com.jetbrains.tmp.learning.core.EduNames;
import com.jetbrains.tmp.learning.courseFormat.AnswerPlaceholder;
import com.jetbrains.tmp.learning.courseFormat.Course;
import com.jetbrains.tmp.learning.courseFormat.Lesson;
import com.jetbrains.tmp.learning.courseFormat.StudyStatus;
import com.jetbrains.tmp.learning.courseFormat.Task;
import com.jetbrains.tmp.learning.courseFormat.TaskFile;
import org.jetbrains.annotations.NotNull;

public class StudyNavigator {
    private StudyNavigator() {
    }

    public static Task nextTask(@NotNull final Task task) {
        Lesson currentLesson = task.getLesson();
        int position = task.getPosition() + 1;

        Task nextTask = currentLesson.getTaskFromPosition(position);
        if (nextTask != null) {
            return nextTask;
        }

        Lesson nextLesson = nextLesson(currentLesson);
        if (nextLesson == null) {
            return null;
        }
        return nextLesson.getTaskFromPosition(1);
    }

    public static Task previousTask(@NotNull final Task task) {
        Lesson currentLesson = task.getLesson();
        int position = task.getPosition() - 1;

        Task prevTask = currentLesson.getTaskFromPosition(position);
        if (prevTask != null) {
            return prevTask;
        }

        Lesson prevLesson = previousLesson(currentLesson);
        if (prevLesson == null) {
            return null;
        }
        return prevLesson.getTaskFromPosition(prevLesson.getTaskList().size());
    }

    public static Lesson nextLesson(@NotNull final Lesson lesson) {
        Course course = lesson.getSection().getCourse();
        if (course == null) {
            return null;
        }

        int position = lesson.getPosition();

        Lesson nextLesson = course.getLessonOfPosition(position + 1);

        if (nextLesson == null || EduNames.PYCHARM_ADDITIONAL.equals(nextLesson.getName())) {
            return null;
        }
        return nextLesson;
    }

    public static Lesson previousLesson(@NotNull final Lesson lesson) {
        Course course = lesson.getSection().getCourse();
        if (course == null)
            return null;

        int position = lesson.getPosition();
        if (position <= 0) {
            return null;
        }

        return course.getLessonOfPosition(position - 1);
    }

    public static void navigateToFirstFailedAnswerPlaceholder(
            @NotNull final Editor editor,
            @NotNull final TaskFile taskFile) {
        final Project project = editor.getProject();
        if (project == null) return;
        for (AnswerPlaceholder answerPlaceholder : taskFile.getAnswerPlaceholders()) {
            if (answerPlaceholder.getStatus() != StudyStatus.Failed) {
                continue;
            }
            navigateToAnswerPlaceholder(editor, answerPlaceholder);
            break;
        }
    }

    public static void navigateToAnswerPlaceholder(
            @NotNull final Editor editor,
            @NotNull final AnswerPlaceholder answerPlaceholder) {
        if (editor.isDisposed()) {
            return;
        }
        editor.getCaretModel().moveToOffset(answerPlaceholder.getOffset());
    }


    public static void navigateToFirstAnswerPlaceholder(
            @NotNull final Editor editor,
            @NotNull final TaskFile taskFile) {
        if (!taskFile.getAnswerPlaceholders().isEmpty()) {
            AnswerPlaceholder firstAnswerPlaceholder = StudyUtils.getFirst(taskFile.getAnswerPlaceholders());
            navigateToAnswerPlaceholder(editor, firstAnswerPlaceholder);
        }
    }

}

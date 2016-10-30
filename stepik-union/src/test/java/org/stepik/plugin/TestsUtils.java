package org.stepik.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.tmp.learning.core.EduNames;
import com.jetbrains.tmp.learning.courseFormat.Course;
import com.jetbrains.tmp.learning.courseFormat.Lesson;
import com.jetbrains.tmp.learning.courseFormat.Section;
import com.jetbrains.tmp.learning.courseFormat.Task;
import org.powermock.api.mockito.PowerMockito;

/**
 * @author meanmail
 */
public class TestsUtils {
    public static final String TEST_COURSE_NAME = "Test course";
    public static final String TEST_SECTION_NAME = "Section1";
    public static final String TEST_LESSON_NAME = "Lesson1";
    public static final String TEST_STEP_NAME = "Step1";
    public static final String TEST_PROJECT_NAME = "TEST PROJECT";

    public static Course getCourse() {
        Course course = new Course();
        course.setName(TEST_COURSE_NAME);
        Section section = new Section();
        section.setIndex(1);
        section.setName(TEST_SECTION_NAME);
        section.setCourse(course);
        Lesson lesson = new Lesson();
        lesson.setIndex(1);
        lesson.setName(TEST_LESSON_NAME);
        Task task = new Task();
        task.setName(TEST_STEP_NAME);
        task.setIndex(1);
        lesson.addTask(task);
        section.addLesson(lesson);
        course.addSection(section);
        course.setCourseMode(EduNames.STEPIK_CODE);

        return course;
    }

    public static PsiDirectory createPsiDirectory(Project project, String name, PsiDirectory parent) {
        PsiDirectory psiDirectory = PowerMockito.mock(PsiDirectory.class);
        VirtualFile virtualFile = PowerMockito.mock(VirtualFile.class);

        PowerMockito.when(psiDirectory.getProject()).thenReturn(project);
        PowerMockito.when(psiDirectory.getVirtualFile()).thenReturn(virtualFile);
        PowerMockito.when(psiDirectory.getName()).thenReturn(name);
        PowerMockito.when(psiDirectory.getParent()).thenReturn(parent);

        return psiDirectory;
    }

    public static Project createMockProject(VirtualFile projectBaseDir) {
        Project project = PowerMockito.mock(Project.class);

        PowerMockito.when(project.getBaseDir()).thenReturn(projectBaseDir);

        return project;
    }
}

package org.stepik.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.tmp.learning.StudyTaskManager;
import com.jetbrains.tmp.learning.core.EduNames;
import com.jetbrains.tmp.learning.courseFormat.Course;
import com.jetbrains.tmp.learning.courseFormat.StudyStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.stepik.plugin.projectView.StepikNavBarModelExtension;
import org.stepik.plugin.utils.PresentationUtils;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.stepik.plugin.TestsUtils.TEST_COURSE_NAME;
import static org.stepik.plugin.TestsUtils.TEST_LESSON_NAME;
import static org.stepik.plugin.TestsUtils.TEST_PROJECT_NAME;
import static org.stepik.plugin.TestsUtils.TEST_SECTION_NAME;
import static org.stepik.plugin.TestsUtils.TEST_STEP_NAME;
import static org.stepik.plugin.TestsUtils.createMockProject;
import static org.stepik.plugin.TestsUtils.createPsiDirectory;
import static org.stepik.plugin.TestsUtils.getCourse;

/**
 * @author meanmail
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({StudyTaskManager.class})
public class StepikNavBarModelExtensionTest {
    private final VirtualFile projectBaseDir = PowerMockito.mock(VirtualFile.class);
    private final Project project = createMockProject(projectBaseDir);
    private final StepikNavBarModelExtension extension = new StepikNavBarModelExtension();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(StudyTaskManager.class);
        StudyTaskManager taskManager = PowerMockito.mock(StudyTaskManager.class);
        PowerMockito.when(StudyTaskManager.getInstance(project)).thenReturn(taskManager);

        Course course = getCourse();

        PowerMockito.when(taskManager.getCourse()).thenReturn(course);
        PowerMockito.when(projectBaseDir.getName()).thenReturn(TEST_PROJECT_NAME);
        PowerMockito.spy(PresentationUtils.class);
        PowerMockito.when(PresentationUtils.class,
                MemberMatcher.method(PresentationUtils.class, "getIconMap", Object.class))
                .withArguments(any(HashMap.class))
                .thenReturn(null);
        PowerMockito.when(PresentationUtils.class,
                MemberMatcher.method(PresentationUtils.class, "getColor", StudyStatus.class))
                .withArguments(StudyStatus.Unchecked)
                .thenReturn(null);
    }

    @Test
    public void getPresentableTextWithProject() {
        String text = extension.getPresentableText(project);
        assertEquals(TEST_COURSE_NAME, text);
    }

    @Test
    public void getPresentableTextWithProjectPsiDirectory() {
        PsiDirectory projectDirectory = PowerMockito.mock(PsiDirectory.class);

        PowerMockito.when(projectDirectory.getVirtualFile()).thenReturn(projectBaseDir);
        PowerMockito.when(projectDirectory.getProject()).thenReturn(project);
        PowerMockito.when(projectDirectory.getName()).thenReturn(TEST_PROJECT_NAME);

        String text = extension.getPresentableText(projectDirectory);
        assertEquals(TEST_COURSE_NAME, text);
    }

    @Test
    public void getPresentableTextWithSectionPsiDirectory() {
        PsiDirectory psiDirectory = createPsiDirectory(project, EduNames.SECTION + 1, null);

        String text = extension.getPresentableText(psiDirectory);
        assertEquals(TEST_SECTION_NAME, text);
    }

    @Test
    public void getPresentableTextWithLessonPsiDirectory() {
        PsiDirectory section = createPsiDirectory(project, EduNames.SECTION + 1, null);
        PsiDirectory psiDirectory = createPsiDirectory(project, EduNames.LESSON + 1, section);

        String text = extension.getPresentableText(psiDirectory);
        assertEquals(TEST_LESSON_NAME, text);
    }

    @Test
    public void getPresentableTextWithStepPsiDirectory() {
        PsiDirectory section = createPsiDirectory(project, EduNames.SECTION + 1, null);
        PsiDirectory lessonPsiDirectory = createPsiDirectory(project, EduNames.LESSON + 1, section);
        PsiDirectory psiDirectory = createPsiDirectory(project, EduNames.TASK + 1, lessonPsiDirectory);

        String text = extension.getPresentableText(psiDirectory);
        assertEquals(TEST_STEP_NAME, text);
    }

    @Test
    public void getPresentableTextWithNull() {
        String text = extension.getPresentableText(null);
        assertNull(text);
    }

    @Test
    public void getPresentableTextWithNotNullString() {
        String text = extension.getPresentableText("STRING");
        assertNull(text);
    }
}

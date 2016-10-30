package org.stepik.plugin;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.JBColor;
import com.jetbrains.tmp.learning.StudyTaskManager;
import com.jetbrains.tmp.learning.core.EduNames;
import com.jetbrains.tmp.learning.courseFormat.Course;
import com.jetbrains.tmp.learning.courseFormat.StudyStatus;
import icons.InteractiveLearningIcons;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.stepik.plugin.projectView.StepikTreeStructureProvider;
import org.stepik.plugin.utils.PresentationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.stepik.plugin.TestsUtils.*;

/**
 * @author meanmail
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceManager.class, StudyTaskManager.class, PresentationUtils.class})
public class StepikTreeStructureProviderTest {
    private final StepikTreeStructureProvider treeStructureProvider = new StepikTreeStructureProvider();
    private final VirtualFile projectBaseDir = PowerMockito.mock(VirtualFile.class);
    private final Project project = createMockProject(projectBaseDir);
    private final AbstractTreeNode<PsiDirectory> node;
    private final ArrayList<AbstractTreeNode> children = new ArrayList<>();
    private final AbstractTreeNode<PsiDirectory> stepikParent;
    private final AbstractTreeNode<PsiDirectory> parent;

    public StepikTreeStructureProviderTest() {
        node = new AbstractTreeNode<PsiDirectory>(project, null) {
            @NotNull
            @Override
            public Collection<? extends AbstractTreeNode> getChildren() {
                return Collections.emptyList();
            }

            @Override
            protected void update(PresentationData presentation) {

            }
        };
        stepikParent = new AbstractTreeNode<PsiDirectory>(project, null) {
            @NotNull
            @Override
            public Collection<? extends AbstractTreeNode> getChildren() {
                return children;
            }

            @Override
            protected void update(PresentationData presentation) {

            }
        };
        parent = new AbstractTreeNode<PsiDirectory>(null, null) {
            @NotNull
            @Override
            public Collection<? extends AbstractTreeNode> getChildren() {
                return children;
            }

            @Override
            protected void update(PresentationData presentation) {

            }
        };
    }

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(ServiceManager.class);
        PowerMockito.mockStatic(StudyTaskManager.class);
        StudyTaskManager taskManager = PowerMockito.mock(StudyTaskManager.class);
        PowerMockito.when(StudyTaskManager.getInstance(project)).thenReturn(taskManager);

        Course course = getCourse();

        PowerMockito.when(taskManager.getCourse()).thenReturn(course);
        PowerMockito.when(projectBaseDir.getName()).thenReturn(TEST_PROJECT_NAME);

        children.add(node);
        Mockito.reset(project);

        PowerMockito.spy(PresentationUtils.class);
        PowerMockito.when(PresentationUtils.class,
                MemberMatcher.method(PresentationUtils.class, "getIconMap", Object.class))
                .withArguments(any())
                .thenReturn(null);
        PowerMockito.when(PresentationUtils.class,
                MemberMatcher.method(PresentationUtils.class, "getColor", StudyStatus.class))
                .withArguments(any(StudyStatus.class))
                .thenReturn(null);
    }

    @Test
    public void modifyNotNeedModify() throws Exception {
        Collection<AbstractTreeNode> list = treeStructureProvider.modify(parent, children, null);
        assertEquals(list, children);
    }

    @Test
    public void modifySectionNode() throws Exception {
        PsiDirectory psiDirectory = createPsiDirectory(project, EduNames.SECTION + 1, null);
        node.setValue(psiDirectory);
        Collection<AbstractTreeNode> list = treeStructureProvider.modify(stepikParent, children, null);
        assertEquals(list.size(), 1);
        AbstractTreeNode node = list.iterator().next();
        assertEquals(TEST_SECTION_NAME, node.getPresentation().getPresentableText());
    }

    @Test
    public void modifyLessonNode() throws Exception {
        PsiDirectory section = createPsiDirectory(project, EduNames.LESSON + 1, null);
        PsiDirectory psiDirectory = createPsiDirectory(project, EduNames.LESSON + 1, section);
        node.setValue(psiDirectory);
        Collection<AbstractTreeNode> list = treeStructureProvider.modify(stepikParent, children, null);
        assertEquals(list.size(), 1);
        AbstractTreeNode node = list.iterator().next();
        assertEquals(TEST_LESSON_NAME, node.getPresentation().getPresentableText());
    }

    @Test
    public void modifyStepNode() throws Exception {
        PsiDirectory lessonPsiDirectory = createPsiDirectory(project, EduNames.LESSON + 1, null);
        PsiDirectory psiDirectory = createPsiDirectory(project, EduNames.TASK + 1, lessonPsiDirectory);

        node.setValue(psiDirectory);
        Collection<AbstractTreeNode> list = treeStructureProvider.modify(stepikParent, children, null);
        assertEquals(list.size(), 1);
        AbstractTreeNode node = list.iterator().next();
        assertEquals(TEST_STEP_NAME, node.getPresentation().getPresentableText());
    }
}
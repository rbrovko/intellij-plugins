package com.jetbrains.edu.utils.generation;

import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.facet.ui.ValidationResult;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DefaultProjectFactory;
import com.intellij.openapi.project.DefaultProjectFactoryImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.FixedSizeButton;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.PanelWithAnchor;
import com.jetbrains.edu.learning.StudyUtils;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseGeneration.StudyProjectGenerator;
import com.jetbrains.edu.learning.stepic.*;
import icons.InteractiveLearningIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * author: liana
 * data: 7/31/14.
 */
public class StepicProjectPanel extends JPanel implements PanelWithAnchor {
    private static final Logger LOG = Logger.getInstance(StepicSectionDirBuilder.class);
    private List<CourseInfo> myAvailableCourses = new ArrayList<CourseInfo>();
    private JButton myBrowseButton;
    private ComboBox<CourseInfo> myCoursesComboBox;
    private JButton myRefreshButton;
    private JLabel myAuthorLabel;
    private JPanel myInfoPanel;
    private JTextPane myDescriptionPane;
    private JComponent myAnchor;
    private final StepikProjectGenerator myGenerator;
    private static final String CONNECTION_ERROR = "<html>Failed to download courses.<br>Check your Internet connection.</html>";
    private static final String INVALID_COURSE = "Selected course is invalid";
    private FacetValidatorsManager myValidationManager;
    private boolean isComboboxInitialized;
    private static final String LOGIN_TO_STEPIC_MESSAGE = "<html><u>Login to Stepic</u> to open the adaptive course </html>";
    private static final String LOGIN_TO_STEPIC = "Login to Stepic";

    public StepicProjectPanel(@NotNull final StepikProjectGenerator generator) {
        super(new VerticalFlowLayout(true, true));
        myGenerator = generator;

        layoutPanel();
        initListeners();
    }

    private void layoutPanel() {
        myCoursesComboBox = new ComboBox<CourseInfo>();

        final JPanel coursesPanel = new JPanel(new BorderLayout());
        final LabeledComponent<ComboBox> coursesCombo = LabeledComponent.create(myCoursesComboBox, "Courses:", BorderLayout.WEST);

        myRefreshButton = new FixedSizeButton(coursesCombo);
        myRefreshButton.setIcon(AllIcons.Actions.Refresh);
        myBrowseButton = new FixedSizeButton(coursesCombo);

        final JPanel comboPanel = new JPanel(new BorderLayout());

        comboPanel.add(coursesCombo, BorderLayout.CENTER);
        comboPanel.add(myRefreshButton, BorderLayout.EAST);

        coursesPanel.add(comboPanel, BorderLayout.CENTER);
        coursesPanel.add(myBrowseButton, BorderLayout.EAST);

        add(coursesPanel);
        myAnchor = coursesCombo;

        final JPanel panel = new JPanel(new BorderLayout());
        final JLabel invisibleLabel = new JLabel();
        invisibleLabel.setPreferredSize(new JLabel("Location: ").getPreferredSize());
        panel.add(invisibleLabel, BorderLayout.WEST);

        myInfoPanel = new JPanel(new VerticalFlowLayout());
        myAuthorLabel = new JLabel();
        myDescriptionPane = new JTextPane();
        myDescriptionPane.setEditable(true);
        myDescriptionPane.setEnabled(true);
        myAuthorLabel.setEnabled(true);
        myDescriptionPane.setPreferredSize(new Dimension(150, 100));
        myDescriptionPane.setFont(coursesCombo.getFont());
        myInfoPanel.add(myAuthorLabel);
        myInfoPanel.add(myDescriptionPane);
        myInfoPanel.setBorder(BorderFactory.createLineBorder(new JBColor(10067616, 10067616)));

        panel.add(myInfoPanel, BorderLayout.CENTER);
        add(panel);
    }

    private void initCoursesCombobox() {
        StepicConnectorLogin.loginFromDialog(DefaultProjectFactory.getInstance().getDefaultProject());
        myAvailableCourses =
                myGenerator.getCoursesUnderProgress(false, "Getting Available Courses", ProjectManager.getInstance().getDefaultProject());
        if (myAvailableCourses.contains(CourseInfo.INVALID_COURSE)) {
            setError(CONNECTION_ERROR);
        }
        else {
            addCoursesToCombobox(myAvailableCourses);
            final CourseInfo selectedCourse = StudyUtils.getFirst(myAvailableCourses);
            final String authorsString = Course.getAuthorsString(selectedCourse.getAuthors());
            myAuthorLabel.setText(!StringUtil.isEmptyOrSpaces(authorsString) ? "Author: " + authorsString : "");
            myDescriptionPane.setText(selectedCourse.getDescription());
            myDescriptionPane.setEditable(false);
            myDescriptionPane.setContentType("text/html");
            //setting the first course in list as selected
            myGenerator.setSelectedCourse(selectedCourse);

            if (selectedCourse.isAdaptive() && !myGenerator.isLoggedIn()) {
                setError(LOGIN_TO_STEPIC_MESSAGE);
            }
            else {
                setOK();
            }
        }
    }

    private void setupBrowseButton() {
        myBrowseButton.setIcon(InteractiveLearningIcons.InterpreterGear);
        final FileChooserDescriptor fileChooser = new FileChooserDescriptor(true, false, false, true, false, false) {
            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                return file.isDirectory() || StudyUtils.isZip(file.getName());
            }

            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return StudyUtils.isZip(file.getName());
            }
        };
        myBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final BaseListPopupStep<String> popupStep = new BaseListPopupStep<String>("", "Add local course", LOGIN_TO_STEPIC) {
                    @Override
                    public PopupStep onChosen(final String selectedValue, boolean finalChoice) {
                        return doFinalStep(() -> {
                            if ("Add local course".equals(selectedValue)) {

                                Project[] projects = ProjectManager.getInstance().getOpenProjects();
                                FileChooser.chooseFile(fileChooser, null, projects.length == 0 ? null : projects[0].getBaseDir(),
                                        file -> {
                                            String fileName = file.getPath();
                                            int oldSize = myAvailableCourses.size();
                                            CourseInfo courseInfo = myGenerator.addLocalCourse(fileName);
                                            if (courseInfo != null) {
                                                if (oldSize != myAvailableCourses.size()) {
                                                    myCoursesComboBox.addItem(courseInfo);
                                                }
                                                myCoursesComboBox.setSelectedItem(courseInfo);
                                                setOK();
                                            }
                                            else {
                                                setError(INVALID_COURSE);
                                                myCoursesComboBox.removeAllItems();
                                                myCoursesComboBox.addItem(CourseInfo.INVALID_COURSE);
                                                for (CourseInfo course : myAvailableCourses) {
                                                    myCoursesComboBox.addItem(course);
                                                }
                                                myCoursesComboBox.setSelectedItem(CourseInfo.INVALID_COURSE);
                                            }
                                        });
                            }
                            else if (LOGIN_TO_STEPIC.equals(selectedValue)) {
                                showLoginDialog(true, "Signing In And Getting Stepic Course List");
                            }
                        });
                    }
                };
                final ListPopup popup = JBPopupFactory.getInstance().createListPopup(popupStep);
                popup.showInScreenCoordinates(myBrowseButton, myBrowseButton.getLocationOnScreen());
            }
        });
    }

    public void showLoginDialog(final boolean refreshCourseList, @NotNull final String progressTitle) {
        final AddRemoteDialog dialog = new AddRemoteDialog(refreshCourseList, progressTitle);
        dialog.show();
    }

    private void initListeners() {
        myRefreshButton.addActionListener(new RefreshActionListener());
        myCoursesComboBox.addActionListener(new CourseSelectedListener());
        setupBrowseButton();
        addAncestorListener(new AncestorListenerAdapter() {
            @Override
            public void ancestorMoved(AncestorEvent event) {
                if (!isComboboxInitialized && isVisible()) {
                    isComboboxInitialized = true;
                    initCoursesCombobox();
                }
            }
        });
    }

    private void setError(@NotNull final String errorMessage) {
        myGenerator.fireStateChanged(new ValidationResult(errorMessage));
        if (myValidationManager != null) {
            myValidationManager.validate();
        }
    }

    private void setOK() {
        myGenerator.fireStateChanged(ValidationResult.OK);
        if (myValidationManager != null) {
            myValidationManager.validate();
        }
    }

    public void registerValidators(final FacetValidatorsManager manager) {
        myValidationManager = manager;
    }

    @Override
    public JComponent getAnchor() {
        return myAnchor;
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        myAnchor = anchor;
    }

    /**
     * Handles refreshing courses
     * Old courses added to new courses only if their
     * meta file still exists in local file system
     */
    private class RefreshActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            final List<CourseInfo> courses =
                    myGenerator.getCoursesUnderProgress(true, "Refreshing Course List", DefaultProjectFactory.getInstance().getDefaultProject());
            if (!courses.contains(CourseInfo.INVALID_COURSE)) {
                refreshCoursesList(courses);
            }
        }
    }

    private void refreshCoursesList(@NotNull final List<CourseInfo> courses) {
        if (courses.isEmpty()) {
            setError(CONNECTION_ERROR);
            return;
        }
        myCoursesComboBox.removeAllItems();

        addCoursesToCombobox(courses);
        myGenerator.setSelectedCourse(StudyUtils.getFirst(courses));

        myGenerator.setCourses(courses);
        myAvailableCourses = courses;
        StudyProjectGenerator.flushCache(myAvailableCourses);
    }

    private void addCoursesToCombobox(@NotNull List<CourseInfo> courses) {
        for (CourseInfo courseInfo : courses) {
            myCoursesComboBox.addItem(courseInfo);
            LOG.warn(courseInfo.toString());
        }
    }


    /**
     * Handles selecting course in combo box
     * Sets selected course in combo box as selected in
     * {@link StepicProjectPanel#myGenerator}
     */
    private class CourseSelectedListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox)e.getSource();
            CourseInfo selectedCourse = (CourseInfo)cb.getSelectedItem();
            if (selectedCourse == null || selectedCourse.equals(CourseInfo.INVALID_COURSE)) {
                myAuthorLabel.setText("");
                myDescriptionPane.setText("");
                setError(INVALID_COURSE);
                return;
            }
            final String authorsString = Course.getAuthorsString(selectedCourse.getAuthors());
            myAuthorLabel.setText(!StringUtil.isEmptyOrSpaces(authorsString) ? "Author: " + authorsString : "");
            myCoursesComboBox.removeItem(CourseInfo.INVALID_COURSE);
            myDescriptionPane.setText(selectedCourse.getDescription());
            myGenerator.setSelectedCourse(selectedCourse);

            setOK();
            if (selectedCourse.isAdaptive()) {
                if (!myGenerator.isLoggedIn()) {
                    setError(LOGIN_TO_STEPIC_MESSAGE);
                }
            }
        }
    }

    public JComboBox<CourseInfo> getCoursesComboBox() {
        return myCoursesComboBox;
    }

    public JPanel getInfoPanel() {
        return myInfoPanel;
    }

    private class AddRemoteDialog extends LoginDialog {

        private final boolean myRefreshCourseList;
        private final String myProgressTitle;

        protected AddRemoteDialog(final boolean refreshCourseList, @NotNull final String progressTitle) {
            super();
            myRefreshCourseList = refreshCourseList;
            myProgressTitle = progressTitle;
        }

        @Override
        protected void doOKAction() {
            if (!validateLoginAndPasswordFields()) return;
            super.doJustOkAction();

            ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);

                final StepicUser stepicUser =
                        StudyUtils.execCancelable(() -> StepicConnectorLogin.minorLogin(new StepicUser(myLoginPanel.getLogin(),
                                myLoginPanel.getPassword())));
                if (stepicUser != null) {
                    stepicUser.setEmail(myLoginPanel.getLogin());
                    stepicUser.setPassword(myLoginPanel.getPassword());
                    myGenerator.myUser = stepicUser;
                    myGenerator.setEnrolledCoursesIds(StepicConnectorGet.getEnrolledCoursesIds());

                    final List<CourseInfo> courses = myGenerator.getCourses(true);
                    if (courses != null && myRefreshCourseList) {
                        ApplicationManager.getApplication().invokeLater(() -> refreshCoursesList(courses));
                    }
                    setOK();
                }
                else {
                    setError("Failed to login");
                }
            }, myProgressTitle, true, new DefaultProjectFactoryImpl().getDefaultProject());
        }
    }
}
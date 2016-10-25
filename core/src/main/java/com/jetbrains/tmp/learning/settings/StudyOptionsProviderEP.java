package com.jetbrains.tmp.learning.settings;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.options.ConfigurableEP;

public class StudyOptionsProviderEP extends ConfigurableEP<StudyOptionsProvider> {
    public static final ExtensionPointName<StudyOptionsProviderEP>
            EP_NAME = ExtensionPointName.create("SCore.optionsProvider");
}

package com.jetbrains.tmp.learning.courseFormat;

public interface StudyItem {
    String getName();

    void setName(String name);

    int getIndex();

    void setIndex(int index);

    StudyStatus getStatus();
}

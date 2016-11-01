package com.jetbrains.tmp.learning.courseFormat;

public interface StudyItem {
    int NULL_ID = -1;

    String getName();
    void setName(String name);

    int getPosition();
    void setPosition(int position);

    StudyStatus getStatus();

    int getId();
    void setId(int id);

    String getDirectory();
    String getPath();
}

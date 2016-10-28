package com.jetbrains.tmp.learning.stepik

data class  Pagination<T>(
val meta: Meta, val objects: Array<T>)
{

}

data class Meta(val page: Int, val hasNext: Boolean, val hasPrevious: Boolean) {}

interface StepikUnit{
    val apiPoint: String
    val id: Int
}

data class Course() : StepikUnit{
    override val id: Int
        get() = id

    override val apiPoint: String
        get() = "/courses/"

}
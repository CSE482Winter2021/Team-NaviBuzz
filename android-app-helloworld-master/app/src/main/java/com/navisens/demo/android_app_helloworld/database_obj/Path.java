package com.navisens.demo.android_app_helloworld.database_obj;

@Entity(indices = {@Index(value = {"name"},
        unique = true)})
public class Path {
    @PrimaryKey
    public int pid;

    @ColumnInfo(name = "name")
    public string name;

}

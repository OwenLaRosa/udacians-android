package com.owenlarosa.udacians.contentprovider;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Owen LaRosa on 12/17/16.
 */

@Database(version = JobsDatabase.VERSION)
public class JobsDatabase {

    public static final int VERSION = 1;

    @Table(JobsListColumns.class) public static final String JOBS = "jobs";

}

package com.owenlarosa.udaciansapp.contentprovider;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Owen LaRosa on 12/17/16.
 */

public interface JobsListColumns {

    // primary key of a job entry
    @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID = "_id";
    // link to job posting on Dice
    @DataType(DataType.Type.TEXT) @NotNull String URL = "url";
    // title of the role
    @DataType(DataType.Type.TEXT) @NotNull String TITLE = "title";
    // company that posted the job
    @DataType(DataType.Type.TEXT) @NotNull String COMPANY = "company";
    // city name followed by state/province
    @DataType(DataType.Type.TEXT) @NotNull String LOCATION = "location";
    // Unix timestamp of the job posting
    @DataType(DataType.Type.INTEGER) @NotNull String DATE = "date";

}

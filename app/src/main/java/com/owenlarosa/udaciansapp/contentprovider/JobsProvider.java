package com.owenlarosa.udaciansapp.contentprovider;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by Owen LaRosa on 12/17/16.
 */

@ContentProvider(authority = JobsProvider.AUTHORITY, database = JobsDatabase.class)
public class JobsProvider {

    public static final String AUTHORITY = "com.owenlarosa.udaciansapp";

    @TableEndpoint(table = JobsDatabase.JOBS) public static class Jobs {
        @ContentUri(path = "jobs",
        type = "vnd.android.cursor.dir/job",
        defaultSort = JobsListColumns.DATE + " ASC")
        public static final Uri JOBS = Uri.parse("content://" + AUTHORITY + "/jobs");
    }
}

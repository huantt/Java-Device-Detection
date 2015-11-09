package MetaData;

import common.Utils;
import fiftyone.mobile.detection.factories.StreamFactory;
import java.io.IOException;
import static org.junit.Assert.fail;
import org.junit.Before;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patent 
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent Application No. 13192291.6; and
 * United States Patent Application Nos. 14/085,223 and 14/085,301.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 * 
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */

public abstract class FileBase extends Base {

    public FileBase(String dataFile) {
        super(dataFile);
    }

    /**
     * Creates the data set to be used for the tests.
     */
    @Before
    public void setUp() {
        Utils.checkFileExists(super.dataFile);
        try {
            this.dataSet = StreamFactory.create(super.dataFile, false);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }
}
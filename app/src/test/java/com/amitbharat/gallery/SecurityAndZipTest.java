package com.amitbharat.gallery;

import com.amitbharat.gallery.utils.DateUtils;
import com.amitbharat.gallery.utils.MediaUtils;
import com.amitbharat.gallery.utils.SecurityUtils;
import com.amitbharat.gallery.utils.ZipHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

public class SecurityAndZipTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPinHashingAndVerification() {
        String pin = "1234";
        String hash = SecurityUtils.hashPin(pin);
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertTrue(SecurityUtils.verifyPin("1234", hash));
        assertFalse(SecurityUtils.verifyPin("0000", hash));
        assertFalse(SecurityUtils.verifyPin("12345", hash));
    }

    @Test
    public void testZipAndUnzip() throws IOException {
        File workDir = tempFolder.newFolder("zip_test");
        File sampleFile = new File(workDir, "sample.txt");
        try (FileWriter fw = new FileWriter(sampleFile)) {
            fw.write("Gallery Vault Zip Test Payload");
        }

        File zipOutput = new File(tempFolder.getRoot(), "archive.zip");
        assertTrue(ZipHelper.zipFiles(Collections.singletonList(sampleFile), zipOutput));
        assertTrue(zipOutput.exists());
        assertTrue(zipOutput.length() > 0);

        File extractDir = new File(tempFolder.getRoot(), "extracted");
        assertTrue(ZipHelper.unzip(zipOutput, extractDir));
        File extractedFile = new File(extractDir, "sample.txt");
        assertTrue(extractedFile.exists());
        assertEquals(sampleFile.length(), extractedFile.length());
    }

    @Test
    public void testFormatDuration() {
        assertEquals("00:05", MediaUtils.formatDuration(5000));
        assertEquals("01:05", MediaUtils.formatDuration(65000));
        assertEquals("01:01:05", MediaUtils.formatDuration(3665000));
    }

    @Test
    public void testDateFormatting() {
        long now = System.currentTimeMillis();
        assertEquals("Today", DateUtils.formatDate(now));
        assertFalse(DateUtils.formatDateTime(now).isEmpty());
        assertFalse(DateUtils.formatTime(now).isEmpty());
    }
}

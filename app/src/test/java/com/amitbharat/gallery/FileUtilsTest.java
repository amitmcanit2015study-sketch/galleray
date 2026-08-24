package com.amitbharat.gallery;

import com.amitbharat.gallery.utils.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileUtilsTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testFormatFileSize() {
        assertEquals("0 B", FileUtils.formatFileSize(0));
        assertEquals("500 B", FileUtils.formatFileSize(500));
        assertEquals("1 KB", FileUtils.formatFileSize(1024));
        assertEquals("1.5 KB", FileUtils.formatFileSize(1536));
        assertEquals("1 MB", FileUtils.formatFileSize(1024 * 1024));
        assertEquals("2.5 MB", FileUtils.formatFileSize((long) (2.5 * 1024 * 1024)));
        assertEquals("1 GB", FileUtils.formatFileSize(1024L * 1024L * 1024L));
    }

    @Test
    public void testCopyAndMoveAndRecursiveDelete() throws IOException {
        File srcDir = tempFolder.newFolder("source_dir");
        File file1 = new File(srcDir, "test1.txt");
        try (FileWriter writer = new FileWriter(file1)) {
            writer.write("Hello World 123");
        }

        File destDir = new File(tempFolder.getRoot(), "dest_dir");
        assertTrue(FileUtils.copyFile(srcDir, destDir));

        File copiedFile = new File(destDir, "test1.txt");
        assertTrue(copiedFile.exists());
        assertEquals(file1.length(), copiedFile.length());

        File moveDest = new File(tempFolder.getRoot(), "moved_dir");
        assertTrue(FileUtils.moveFile(destDir, moveDest));
        assertFalse(destDir.exists());
        assertTrue(new File(moveDest, "test1.txt").exists());

        assertTrue(FileUtils.deleteRecursive(moveDest));
        assertFalse(moveDest.exists());
    }

    @Test
    public void testCalculateDirectorySize() throws IOException {
        File testDir = tempFolder.newFolder("calc_dir");
        File fileA = new File(testDir, "a.txt");
        try (FileWriter writer = new FileWriter(fileA)) {
            writer.write("1234567890"); // 10 bytes
        }
        File subDir = new File(testDir, "sub");
        subDir.mkdirs();
        File fileB = new File(subDir, "b.txt");
        try (FileWriter writer = new FileWriter(fileB)) {
            writer.write("12345"); // 5 bytes
        }

        assertEquals(15, FileUtils.calculateDirectorySize(testDir));
    }
}

package io.github.giulong.spectrum.it.tests;

import io.github.giulong.spectrum.SpectrumTest;
import io.github.giulong.spectrum.it.pages.DownloadPage;
import io.github.giulong.spectrum.it.pages.UploadPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

@DisplayName("Files Test")
@SuppressWarnings("unused")
public class FilesIT extends SpectrumTest<Void> {

    private static final String FILE_TO_DOWNLOAD = "empty.txt"; // this must be different from the downloaded file since herokuapp will randomly serve exactly the files used to test the upload
    private static final String FILE_TO_UPLOAD = "spectrum-logo.png";

    private DownloadPage downloadPage;

    private UploadPage uploadPage;

    @Test
    @DisplayName("download")
    public void download() {
        // We call the inherited helper method to ensure a fresh download
        deleteDownloadsFolder();

        downloadPage.open();
        downloadPage.getDownloadLinks().getFirst().click();

        // We call the inherited helper method to check if the downloaded file is the one we expect
        // This is expected to fail since we're comparing it with a wrong file
        assertThrows(TimeoutException.class, () -> checkDownloadedFile(FILE_TO_DOWNLOAD));
    }

    @Test
    @DisplayName("upload")
    public void upload() {
        uploadPage
                .open()
                .upload(uploadPage.getFileUpload(), FILE_TO_UPLOAD)
                .getSubmit().click();

        pageLoadWait.until(visibilityOf(uploadPage.getUploadedFiles()));
        assertEquals(FILE_TO_UPLOAD, uploadPage.getUploadedFiles().getText());
    }
}

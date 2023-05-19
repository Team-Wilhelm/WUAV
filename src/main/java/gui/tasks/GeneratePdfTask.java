package gui.tasks;

import be.Document;
import bll.pdf.PdfGenerator;
import gui.nodes.DocumentPropertyCheckboxWrapper;
import javafx.concurrent.Task;
import utils.enums.ResultState;

import java.nio.file.Path;
import java.util.List;

public class GeneratePdfTask extends Task<ResultState> implements TaskCallback {
    private final Document document;
    private final List<DocumentPropertyCheckboxWrapper> checkboxWrappers;
    private final PdfGenerator pdfGenerator;
    private Path pdfPath;
    private TaskCallback callback;

    public GeneratePdfTask(Document document, List<DocumentPropertyCheckboxWrapper> checkboxWrappers) {
        this.document = document;
        this.checkboxWrappers = checkboxWrappers;
        pdfGenerator = new PdfGenerator();
    }

    @Override
    protected ResultState call() throws Exception {
        try {
            /* Pre-generate the first pdf so we know the final number of pages and can use it in place for
             * ordering the final pdf's pages
             */
            int numberOfPages = pdfGenerator.getNumberOfPages(document, checkboxWrappers);
            updateProgress(50, 100); // Halfway there

            // Generate the actual pdf
            pdfPath = pdfGenerator.generatePdf(document, checkboxWrappers, numberOfPages);
            updateProgress(100, 100); // Done

            return ResultState.SUCCESSFUL;
        } catch (Exception e) {
            e.printStackTrace();
            return ResultState.FAILED;
        }
    }

    @Override
    public void onTaskCompleted(ResultState resultState) {
        if (callback != null) {
            callback.onTaskCompleted(resultState);
        }
    }

    public void getCallback() {
        if (callback != null) {
            callback.onTaskCompleted(getValue());
        }
    }

    public void setCallback(TaskCallback callback) {
        this.callback = callback;
    }

    public Document getDocument() {
        return document;
    }

    public Path getPdfPath() {
        return pdfPath;
    }
}

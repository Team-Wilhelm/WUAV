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
            pdfGenerator.progressProperty().addListener((observable, oldValue, newValue) -> updateProgress(newValue.doubleValue(), 100));
            // Generate the document
            pdfPath = pdfGenerator.generatePdf(document, checkboxWrappers);
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

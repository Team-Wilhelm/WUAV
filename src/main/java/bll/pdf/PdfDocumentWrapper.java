package bll.pdf;

import be.Document;
import gui.nodes.DocumentPropertyCheckboxWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Wrapper class created in order to reuse the same method for generating PDFs regardless of if its purpose, which is
 * either generating a PDF to determine the number of pages or to generate the final PDF. This class is used to encapsulate
 * the PDF document, the path to the PDF document, the document object and the list of checkboxes from the first generated PDF
 * and is used to delete the PDF document when the final PDF is generated and the other PDF is no longer needed.
 */
public class PdfDocumentWrapper {
    private com.itextpdf.layout.Document pdfDocument;
    private Path path;
    private Document document;
    private List<DocumentPropertyCheckboxWrapper> checkBoxes;
    private int numberOfPages;

    public PdfDocumentWrapper(com.itextpdf.layout.Document pdfDocument, Path path, Document document, List<DocumentPropertyCheckboxWrapper> checkBoxes) {
        this.pdfDocument = pdfDocument;
        this.path = path;
        this.document = document;
        this.checkBoxes = checkBoxes;
        this.numberOfPages = pdfDocument.getPdfDocument().getNumberOfPages();
    }

    public com.itextpdf.layout.Document getPdfDocument() {
        return pdfDocument;
    }

    public void setPdfDocument(com.itextpdf.layout.Document pdfDocument) {
        this.pdfDocument = pdfDocument;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public List<DocumentPropertyCheckboxWrapper> getCheckBoxes() {
        return checkBoxes;
    }

    public void setCheckBoxes(List<DocumentPropertyCheckboxWrapper> checkBoxes) {
        this.checkBoxes = checkBoxes;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    /**
     * Deletes the PDF document.
     * Should be called when the final PDF is generated and the other PDF is no longer needed.
     */
    public void delete() {
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

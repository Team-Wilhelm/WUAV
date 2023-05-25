package bll.pdf;

import be.Document;
import gui.nodes.DocumentPropertyCheckboxWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    public void delete() {
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

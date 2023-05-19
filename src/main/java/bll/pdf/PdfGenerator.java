package bll.pdf;

import be.Address;
import be.Document;
import utils.enums.DocumentPropertyType;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.*;
import gui.nodes.DocumentPropertyCheckboxWrapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

public class PdfGenerator {
    private static PdfFont FONT;
    private static final int FONT_SIZE = 12;
    PdfPageEventHandler pageNumberHandler;

    //Create formatting elements
    Paragraph lineBreak = new Paragraph();
    Paragraph lineBreak3 = new Paragraph("\n" + "\n" + "\n");
    AreaBreak pageBreak = new AreaBreak(AreaBreakType.NEXT_AREA);
    float margin = 75;

    public Path generatePdf(Document document, List<DocumentPropertyCheckboxWrapper> checkBoxes, int numberOfPages) {
        try {
            FONT = PdfFontFactory.createFont(FontConstants.HELVETICA);
            // Open a new PDF document
            String home = System.getProperty("user.home");
            PdfWriter writer = new PdfWriter(home + "/Downloads/" + document.getDocumentID() + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(writer);

            // Create and set the event handler on the PdfDocument
            pageNumberHandler = new PdfPageEventHandler(pdfDoc, numberOfPages);
            pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, pageNumberHandler);

            com.itextpdf.layout.Document doc = new com.itextpdf.layout.Document(pdfDoc);
            doc.setMargins(margin, margin, margin, margin);
            doc.add(getHeaderTable(document));
            doc.add(lineBreak);
            doc.add(getLogoTable());
            doc.add(lineBreak3);

            // Determine which properties to add to the document
            for (DocumentPropertyCheckboxWrapper checkboxWrapper: checkBoxes) {
                if (checkboxWrapper.getProperty() == DocumentPropertyType.DATE_OF_CREATION) {
                    doc.add(new Paragraph(String.valueOf(document.getDateOfCreation())));
                    doc.add(lineBreak);
                }
                if (checkboxWrapper.getProperty() == DocumentPropertyType.JOB_TITLE) {
                    doc.add(new Paragraph(document.getJobTitle()).setBold().setFontSize(14));
                }
                if (checkboxWrapper.getProperty() == DocumentPropertyType.JOB_DESCRIPTION) {
                    doc.add(new Paragraph(document.getJobDescription()));
                }
                if (checkboxWrapper.getProperty() == DocumentPropertyType.NOTES) {
                    doc.add(getOptionalNotes(document));
                }
                if (checkboxWrapper.getProperty() == DocumentPropertyType.TECHNICIANS) {
                    doc.add(new Paragraph(document.getTechnicianNames()));
                }
            }
            //Add images
            List<DocumentPropertyCheckboxWrapper> imageCheckboxes = checkBoxes.stream().filter(checkbox -> checkbox.getProperty() == DocumentPropertyType.IMAGE).toList();
            if (!imageCheckboxes.isEmpty()) {
                doc.add(pageBreak);
                 for (DocumentPropertyCheckboxWrapper image: imageCheckboxes) {
                    doc.add(createImageTable(image));
                }
            }
            doc.close();
            //TODO: Change to a more appropriate path if we decide to keep more than one version of the document
            return Path.of(home + "/Downloads/" + document.getDocumentID() + ".pdf");
            } catch(IOException e){
                throw new RuntimeException(e);
            }
        }

    private Table createImageTable(DocumentPropertyCheckboxWrapper image) {
        Table imageTable = new Table(1);

        ImageData imageData;
        try {
            imageData = ImageDataFactory.create(image.getImage().getUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Image documentImage = new Image(imageData);
        documentImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
        documentImage.setAutoScale(true);
        imageTable.addCell(documentImage);

        Cell footerCell = new Cell();
        String description = image.getImage().getDescription() == null ? "" : image.getImage().getDescription();
        footerCell.add(description).setTextAlignment(TextAlignment.CENTER);
        footerCell.setBorder(Border.NO_BORDER);
        imageTable.addFooterCell(footerCell);

        imageTable.setHorizontalAlignment(HorizontalAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
        removeBorder(imageTable);
        imageTable.setMarginTop(20);
        return imageTable;
    }

    private Paragraph getOptionalNotes(Document document) {
        String optionalNotes;
        if (!document.getOptionalNotes().isEmpty()) {
            optionalNotes = "Additional notes: " + document.getOptionalNotes();
        } else optionalNotes = "";
        return new Paragraph(optionalNotes);
    }

    private Table getHeaderTable(Document document) {
        //Left side header, customer info
        Paragraph customerParagraph = new Paragraph(getCustomerInfo(document));
        //Right side header, WUAV info
        Paragraph WUAVheader = new Paragraph(getWUAVinfo());
        WUAVheader.setTextAlignment(TextAlignment.RIGHT);
        //Create pdf header
        Table headerTable = new Table(2);

        Cell customerCell = new Cell();
        customerCell.add(customerParagraph);

        Cell WUAVCell = new Cell();
        WUAVCell.add(WUAVheader);

        headerTable.addCell(customerCell);
        headerTable.addCell(WUAVCell);
        removeBorder(headerTable);

        return headerTable;
    }

    private Table getLogoTable(){
        //Add WUAV logo
        ImageData data;
        try {
            data = ImageDataFactory.create("https://easvprojects.blob.core.windows.net/wuav/9e112cc6-1487-426a-9bdc-2a4fd7b91861/7e7d9e00-507b-47ee-989b-8686859b41aa-wuav.png");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Image logoImage = new Image(data);
        logoImage.setHeight(50).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        Table logoTable = new Table(1);
        logoTable.addCell(logoImage).setPageNumber(1);
        removeBorder(logoTable);

        return logoTable;
    }

    private String getWUAVinfo() {
        return           "WUAV"
                + "\n" + "Murervej 7a"
                + "\n" + "6710 Esbjerg V"
                + "\n" + "+45 7511 9191"
                + "\n" + "info@wuav.dk";
    }

    private String getCustomerInfo(Document document) {
        Address customerAddress = document.getCustomer().getCustomerAddress();
        return document.getCustomer().getCustomerName()
                + "\n" + customerAddress.getStreetName()+ " " + customerAddress.getStreetNumber()
                + "\n" + customerAddress.getPostcode() + " " + customerAddress.getTown()
                + "\n" + document.getCustomer().getCustomerPhoneNumber()
                + "\n" + document.getCustomer().getCustomerEmail();
    }

    private void removeBorder(Table table) {
        for (IElement iElement : table.getChildren()) {
            ((Cell)iElement).setBorder(Border.NO_BORDER);
        }
    }

    public int getNumberOfPages(Document document, List<DocumentPropertyCheckboxWrapper> checkBoxes) {
        int numberOfPages;
        try {
            FONT = PdfFontFactory.createFont(FontConstants.HELVETICA);

            // Open a new PDF document
            String home = System.getProperty("user.home");
            PdfWriter writer = new PdfWriter(home + "/Downloads/" + document.getDocumentID() + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(writer);
            com.itextpdf.layout.Document doc = new com.itextpdf.layout.Document(pdfDoc);

            doc.setMargins(margin, margin, margin, margin);
            doc.add(getHeaderTable(document));
            doc.add(lineBreak);
            doc.add(getLogoTable());
            doc.add(lineBreak3);

            // Determine which properties to add to the document
            for (DocumentPropertyCheckboxWrapper checkboxWrapper: checkBoxes) {
                if (checkboxWrapper.getProperty() == DocumentPropertyType.DATE_OF_CREATION) {
                    doc.add(new Paragraph(String.valueOf(document.getDateOfCreation())));
                    doc.add(lineBreak);
                }
                if (checkboxWrapper.getProperty() == DocumentPropertyType.JOB_TITLE) {
                    doc.add(new Paragraph(document.getJobTitle()).setBold().setFontSize(14));
                }
                if (checkboxWrapper.getProperty() == DocumentPropertyType.JOB_DESCRIPTION) {
                    doc.add(new Paragraph(document.getJobDescription()));
                }
                if (checkboxWrapper.getProperty() == DocumentPropertyType.NOTES) {
                    doc.add(getOptionalNotes(document));
                }
                if (checkboxWrapper.getProperty() == DocumentPropertyType.TECHNICIANS) {
                    doc.add(new Paragraph(document.getTechnicianNames()));
                }
            }
            //Add images
            List<DocumentPropertyCheckboxWrapper> imageCheckboxes = checkBoxes.stream().filter(checkbox -> checkbox.getProperty() == DocumentPropertyType.IMAGE).toList();
            if (!imageCheckboxes.isEmpty()) {
                doc.add(pageBreak);
                for (DocumentPropertyCheckboxWrapper image: imageCheckboxes) {
                    doc.add(createImageTable(image));
                }
            }
            numberOfPages = pdfDoc.getNumberOfPages();
            doc.close();

        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return numberOfPages;
    }
}

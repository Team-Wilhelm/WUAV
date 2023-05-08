package bll;

import be.Address;
import be.Document;
import be.ImageWrapper;
import be.User;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.renderer.IRenderer;
import com.itextpdf.layout.renderer.TableRenderer;

import javax.swing.text.html.parser.Element;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


public class PdfGenerator {
    private static PdfFont FONT;
    private static final int FONT_SIZE = 12;

    public void generatePdf(Document document) {
        try {
            FONT = PdfFontFactory.createFont(FontConstants.HELVETICA);

            // Open a new PDF document
            String home = System.getProperty("user.home");
            PdfWriter writer = new PdfWriter(home + "/Downloads/" + document.getDocumentID() + ".pdf");
            PdfDocument pdfDoc = new PdfDocument(writer);
            com.itextpdf.layout.Document doc = new com.itextpdf.layout.Document(pdfDoc);
            float margin = 75;
            doc.setMargins(margin, margin, margin, margin);

            //Create formatting elements
            Paragraph lineBreak = new Paragraph();
            Paragraph lineBreak3 = new Paragraph("\n" + "\n" + "\n");
            AreaBreak pageBreak = new AreaBreak(AreaBreakType.NEXT_AREA);

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

            doc.add(headerTable);
            doc.add(lineBreak);

            //Add WUAV logo
            ImageData data = ImageDataFactory.create(getLogo());
            Image logoImage = new Image(data);
            logoImage.setHeight(50).setHorizontalAlignment(HorizontalAlignment.RIGHT);

            Table logoTable = new Table(1);
            logoTable.addCell(logoImage).setPageNumber(1);
            removeBorder(logoTable);

            doc.add(logoTable);

            doc.add(lineBreak3);

            //Create job contents for document
            Paragraph date = new Paragraph(String.valueOf(document.getDateOfCreation()));
            Paragraph jobTitle = new Paragraph(document.getJobTitle());
            jobTitle.setBold();
            jobTitle.setFontSize(14);
            Paragraph jobDescription = new Paragraph(document.getJobDescription());

            String optionalNotes;
            if (!document.getOptionalNotes().isEmpty()) {
                optionalNotes = "Additional notes: " + document.getOptionalNotes();
            } else optionalNotes = "";
            Paragraph notes = new Paragraph(optionalNotes);

            doc.add(date);
            doc.add(lineBreak);
            doc.add(jobTitle);
            doc.add(jobDescription);
            doc.add(notes);
            doc.add(lineBreak3);

            //Add images
            Table imageTable;
            if (!document.getDocumentImages().isEmpty()) {
                for (int i = 0; i < document.getDocumentImages().size(); i++) {
                    if (i % 2 != 0) {
                        doc.add(pageBreak);
                    }
                    imageTable = new Table(1);

                    ImageData imageData = ImageDataFactory.create(document.getDocumentImages().get(i).getUrl());
                    Image documentImage = new Image(imageData);
                    documentImage.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    documentImage.setAutoScale(true);

                    documentImage.setHeight(150).setHorizontalAlignment(HorizontalAlignment.CENTER);
                    imageTable.addCell(documentImage);

                    imageTable.setHorizontalAlignment(HorizontalAlignment.CENTER).setVerticalAlignment(VerticalAlignment.MIDDLE);
                    removeBorder(imageTable);
                    imageTable.setMarginTop(20);
                    doc.add(imageTable);
                }
            }
                //List of technicians that did the job
                String technicians = getTechnicianNames(document);
                Paragraph technicianList = new Paragraph(technicians);
                doc.add(technicianList);


            //Create page number header and website footer and add contents
            Paragraph footerText = new Paragraph("www.wuav.dk");
            footerText.setTextAlignment(TextAlignment.CENTER);
            Rectangle footer = new Rectangle(0, 0, pdfDoc.getDefaultPageSize().getWidth(), 50);
            int numberOfPages = doc.getPdfDocument().getNumberOfPages();
            for (int i = 1; i <= numberOfPages; i++) {
                PdfPage page = pdfDoc.getPage(i);
                PdfCanvas pdfCanvas = new PdfCanvas(page);
                pdfCanvas.rectangle(footer);
                Canvas canvas = new Canvas(pdfCanvas, pdfDoc, footer);
                canvas.add(footerText);
                if (numberOfPages > 1) {
                    canvas.add(new Paragraph(i + " of " + numberOfPages).setTextAlignment(TextAlignment.CENTER).setFixedPosition(0, pdfDoc.getDefaultPageSize().getTop() - 50, pdfDoc.getDefaultPageSize().getWidth()));
                }
                canvas.close();
            }

            doc.close();


            } catch(IOException e){
                throw new RuntimeException(e);
            }
        }


    private boolean isOverHalfPageAvailable(com.itextpdf.layout.Document doc){
        float halfPage = (doc.getPdfDocument().getDefaultPageSize().getHeight()/2)-75;
        float currentPos = doc.getPdfDocument().getWriter().getCurrentPos();
        System.out.println("halfpage: "+halfPage + "current pos: " + currentPos);
        return halfPage > currentPos;
    }
    private String getLogo(){
        return "https://easvprojects.blob.core.windows.net/wuav/9e112cc6-1487-426a-9bdc-2a4fd7b91861/7e7d9e00-507b-47ee-989b-8686859b41aa-wuav.png";
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

    private String getTechnicianNames(Document document){
        StringBuilder technicians = new StringBuilder();
        if(!document.getTechnicians().isEmpty()) {
            technicians.append("Technician(s): ");
            for (User user : document.getTechnicians()) {
                technicians.append(user.getFullName());
            }
        }
        return technicians.toString();
    }

    private void removeBorder(Table table)
    {
        for (IElement iElement : table.getChildren()) {
            ((Cell)iElement).setBorder(Border.NO_BORDER);
        }
    }
}

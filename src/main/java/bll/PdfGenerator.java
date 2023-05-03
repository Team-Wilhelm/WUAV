package bll;

import be.Address;
import be.Document;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public class PdfGenerator {
    private static PdfFont FONT;
    private static final int FONT_SIZE = 12;


    public void generatePdf(Document document){
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
            Paragraph lineBreak3 = new Paragraph("\n"+"\n"+"\n");
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
                if(document.getOptionalNotes() != null){
                    optionalNotes = "Additional notes: " + document.getOptionalNotes();
                }
                else optionalNotes = "";
            Paragraph notes = new Paragraph(optionalNotes);

            doc.add(date);
            doc.add(lineBreak);
            doc.add(jobTitle);
            doc.add(jobDescription);
            doc.add(notes);

            //List of technicians that did the job
            Paragraph technicianList = new Paragraph(getTechnicianNames(document));
            technicianList.setVerticalAlignment(VerticalAlignment.BOTTOM);
            doc.add(technicianList);

            //Create pagenumber header and website footer and add contents
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
                    if(numberOfPages>1) {
                        canvas.add(new Paragraph(i + " of " + numberOfPages).setTextAlignment(TextAlignment.CENTER).setFixedPosition(0,pdfDoc.getDefaultPageSize().getTop()-50, pdfDoc.getDefaultPageSize().getWidth()));
                    }
                    canvas.close();
                }
            doc.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        String technicians = "";
        if(!document.getTechnicians().isEmpty()) {
            technicians = "Technician(s): ";
            for (User user : document.getTechnicians()) {
                technicians += user.getFullName();
            }
        }
        return technicians;
    }

    private void removeBorder(Table table)
    {
        for (IElement iElement : table.getChildren()) {
            ((Cell)iElement).setBorder(Border.NO_BORDER);
        }
    }
}

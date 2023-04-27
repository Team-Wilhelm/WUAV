package bll;

import be.Document;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.BlockElement;
import com.itextpdf.layout.element.Paragraph;

import java.io.IOException;


public class PdfGenerator {
    // add: header, customer contactinfo, WUAV contactinfo
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


            //Create header for document
            BlockElement<Paragraph> header = new Paragraph(document.getJobTitle());

            doc.add(header);
            doc.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }



}

package bll.pdf;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.io.IOException;


public class PdfPageEventHandler implements IEventHandler {
    protected PdfDocument pdfDoc;
    protected int numberOfPages;

    public PdfPageEventHandler(PdfDocument pdfDoc, int numberOfPages) {
        this.pdfDoc = pdfDoc;
        this.numberOfPages = numberOfPages;
    }
       @Override
    public void handleEvent(Event event) {
        try {
            PdfFont FONT = PdfFontFactory.createFont(FontConstants.HELVETICA);

            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);

            // Create a canvas to add the page number and header
            PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);

            // Set the text and position of the header
            String webSiteText = "wwww.wuav.dk";
            float webSiteTextCenter = (page.getPageSize().getWidth() - (FONT.getWidth(webSiteText, 10))) / 2;

            // Add the header and page numbers to all pages except the first
            if (pageNumber != 1) {
                // Set the text and position of the page number
                String pageNumberText = (pageNumber + " of " + numberOfPages);
                float pageNumberTextCenter = (page.getPageSize().getWidth() - (FONT.getWidth(pageNumberText, 10))) / 2;

                canvas.beginText().setFontAndSize(FONT, 10)
                        .moveText(pageNumberTextCenter, page.getPageSize().getBottom()+35)
                        .showText(pageNumberText)
                        .endText();

                canvas.beginText().setFontAndSize(FONT, 10)
                        .moveText(webSiteTextCenter, page.getPageSize().getTop()-40)
                        .showText(webSiteText)
                        .endText();
            }
        } catch (IOException e) {
               throw new RuntimeException(e);
           }
    }
}

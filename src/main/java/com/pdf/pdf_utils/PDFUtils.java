package com.pdf.pdf_utils;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Hello world!
 *
 */
public class PDFUtils
{

    public String cropPDFMM(float x, float y, float width, float height, String srcFilePath) throws IOException {
        // helper functions to convert between mm <-> units
        Function<Float, Float> mmToUnits = (Float a) -> a / 0.352778f;
        Function<Float, Float> unitsToMm = (Float a) -> a * 0.352778f;

        // convert mm to units
        float xUnits = mmToUnits.apply(x);
        float yUnits = mmToUnits.apply(y);
        float widthUnits = mmToUnits.apply(width);
        float heightUnits = mmToUnits.apply(height);

        // extract the doc's file name
        File srcFile = new File(srcFilePath);
        String fileName = srcFile.getName();
        int dotIndex = fileName.lastIndexOf('.');
        String fileNameWithoutExtension =  (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);

        // crop each page
        PDDocument doc = PDDocument.load(srcFile);
        int nrOfPages = doc.getNumberOfPages();
        PDRectangle newBox = new PDRectangle(
                xUnits,
                yUnits,
                widthUnits,
                heightUnits);
        for (int i = 0; i < nrOfPages; i++) {
            doc.getPage(i).setCropBox(newBox);
        }

        // save the result & append -cropped to the file name
        File outFile = new File(fileNameWithoutExtension + "-cropped.pdf");
        doc.save(outFile);
        doc.close();

        return outFile.getCanonicalPath();
    }

    public void removePages(String srcFilePath, Integer[] pageRanges) throws IOException {
        // a helper function to test if a page is within a range
        BiPredicate<Integer, Integer[]> pageInInterval = (Integer page, Integer[] allPages) -> {
            for (int j = 0; j < allPages.length; j+=2) {
                int startPage = allPages[j];
                int endPage = allPages[j+1];
                if (page >= startPage-1 && page < endPage) {
                    return true;
                }
            }
            return false;
        };

        File srcFile = new File(srcFilePath);
        PDDocument pdfDocument = PDDocument.load(srcFile);
        PDDocument tmpDoc = new PDDocument();

        // test if a page is within a range
        // if not, append the page to a temp. doc.
        for (int i = 0; i < pdfDocument.getNumberOfPages(); i++) {
            if (pageInInterval.test(i, pageRanges)) {
                continue;
            }
            tmpDoc.addPage(pdfDocument.getPage(i));
        }

        // save the temporary doc.
        tmpDoc.save(new File(srcFilePath));
        tmpDoc.close();
        pdfDocument.close();
    }

    public void mergePages(String srcFilePath) throws IOException {
        // SOURCE: https://stackoverflow.com/questions/12093408/pdfbox-merge-2-portrait-pages-onto-a-single-side-by-side-landscape-page
        File srcFile = new File(srcFilePath);
        PDDocument pdfDocument = PDDocument.load(srcFile);
        PDDocument outPdf = new PDDocument();

        for (int i = 0; i < pdfDocument.getNumberOfPages(); i+=2) {
            PDPage page1 = pdfDocument.getPage(i);
            PDPage page2 = pdfDocument.getPage(i+1);

            PDRectangle pdf1Frame = page1.getCropBox();
            PDRectangle pdf2Frame = page2.getCropBox();
            PDRectangle outPdfFrame = new PDRectangle(pdf1Frame.getWidth()+pdf2Frame.getWidth(), Math.max(pdf1Frame.getHeight(), pdf2Frame.getHeight()));

            // Create output page with calculated frame and add it to the document
            COSDictionary dict = new COSDictionary();
            dict.setItem(COSName.TYPE, COSName.PAGE);
            dict.setItem(COSName.MEDIA_BOX, outPdfFrame);
            dict.setItem(COSName.CROP_BOX, outPdfFrame);
            dict.setItem(COSName.ART_BOX, outPdfFrame);
            PDPage newP = new PDPage(dict);
            outPdf.addPage(newP);

            // Source PDF pages has to be imported as form XObjects to be able to insert them at a specific point in the output page
            LayerUtility layerUtility = new LayerUtility(outPdf);
            PDFormXObject formPdf1 = layerUtility.importPageAsForm(pdfDocument, page1);
            PDFormXObject formPdf2 = layerUtility.importPageAsForm(pdfDocument, page2);

            AffineTransform afLeft = new AffineTransform();
//            AffineTransform afLeft2 = AffineTransform.getTranslateInstance(85, -10);
            layerUtility.appendFormAsLayer(newP, formPdf1, afLeft, "left" + i);
            AffineTransform afRight = AffineTransform.getTranslateInstance(pdf1Frame.getWidth(), 0);
            layerUtility.appendFormAsLayer(newP, formPdf2, afRight, "right" + i);
        }

        outPdf.save(srcFile);
        outPdf.close();
        pdfDocument.close();
    }



    public void splitPDF(String srcFilePath, int nrOfPages) throws IOException {
        // extract file's name
        File srcFile = new File(srcFilePath);
        String fileName = srcFile.getName();
        int dotIndex = fileName.lastIndexOf('.');
        String fileNameWithoutExtension =  (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);

        PDDocument pdfDocument = PDDocument.load(srcFile);

        // extract every nrOfPages to a temporary document
        // append an index to its name and save it
        for (int i = 1; i < pdfDocument.getNumberOfPages(); i+=nrOfPages) {
            Splitter splitter = new Splitter();

            int fromPage = i;
            int toPage = i+nrOfPages;
            splitter.setStartPage(fromPage);
            splitter.setEndPage(toPage);
            splitter.setSplitAtPage(toPage - fromPage );

            List<PDDocument> lst = splitter.split(pdfDocument);

            PDDocument pdfDocPartial = lst.get(0);
            File f = new File(fileNameWithoutExtension + "-" + i + ".pdf");
            pdfDocPartial.save(f);
            pdfDocPartial.close();
        }
        pdfDocument.close();
    }



    public static void main( String[] args )
    {
        String srcFilePath = "/Users/user/projects/pdf_utils/file.pdf";
        PDFUtils app = new PDFUtils();

        try {
            ///// crop pdf
            float x = 25f;
            float y = 10f;
            float width = 140f;
            float height = 400f;
            String resultFilePath = app.cropPDFMM(x, y, width, height, srcFilePath);

            ///// remove pages
            app.removePages(resultFilePath, new Integer[] {1, 18, 310, 322});

            ///// split pages
            app.splitPDF(resultFilePath, 20);

            System.out.println( "Done!" );
        } catch (Exception e) {
            System.out.println(e);
            System.err.println(e.getStackTrace());
            System.err.println(e.getCause());
        }
    }
}

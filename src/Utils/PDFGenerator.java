package Utils;

import Model.GameInfo;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/// Helper class for PDF generation.
public class PDFGenerator {

    /// Generates a PDF with the games provided.
    /// @param file File where the PDF will be saved (provided by the FileChooser).
    /// @param games List of games (provided by lstAddedGames).
    public static void generatePdfFile(File file, List<GameInfo> games) throws Exception {

        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(file));

        document.open();

        // Title
        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);

        com.lowagie.text.Paragraph title =
                new com.lowagie.text.Paragraph("Cardápio de Board Games", titleFont);

        title.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
        document.add(title);
        document.add(new com.lowagie.text.Paragraph(" "));


        // Table
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        table.setWidths(new float[]{2f, 2f, 3f});

        // Header font
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(
                com.lowagie.text.Font.HELVETICA, 12, com.lowagie.text.Font.BOLD);

        // Header cells
        // Idea: Choose language and change depending on language?
        String[] headers = {"Imagem", "Nome", "Descrição"};

        for (String header : headers) {
            com.lowagie.text.pdf.PdfPCell headerCell =
                    new com.lowagie.text.pdf.PdfPCell(
                            new com.lowagie.text.Phrase(header, headerFont));

            headerCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
            headerCell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            headerCell.setPadding(5);
            table.addCell(headerCell);
        }

        // Data Rows
        for (int i = 0; i < games.size(); i++) {

            GameInfo game = games.get(i);

            try {
                // Load image from path
                Image img = Image.getInstance(game.getPhotoPath());

                // Optional: scale the image to fit cell
                img.scaleToFit(100, 100); // width, height in points
                PdfPCell imageCell = new PdfPCell(img, true); // true = fit to cell
                imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                imageCell.setPadding(5);

                table.addCell(imageCell);

            } catch (Exception e) {
                // If image fails to load, just leave cell blank or add placeholder
                table.addCell("No Image");
            }

            PdfPCell nameCell = new PdfPCell(new com.lowagie.text.Phrase(game.getName()));
            nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            nameCell.setPadding(5);
            table.addCell(nameCell);

            PdfPCell descCell = new PdfPCell(new com.lowagie.text.Phrase(game.getDescription()));
            descCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            descCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            descCell.setPadding(5);
            table.addCell(descCell);
        }

        document.add(table);

        document.close();
    }
}

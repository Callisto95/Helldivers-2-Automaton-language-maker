package net.callisto;

import javax.imageio.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main {
    private static final int PRIMARY_COLOUR   = 0xFF0000;
    private static final int SECONDARY_COLOUR = 0x000000;
    private static final int SCALE            = 25;
    
    public static void main(String[] args) throws IOException {
        final List<String> lines = Arrays.asList(args);
        
        if (lines.isEmpty()) {
            System.out.println("No input.");
            System.exit(0);
        }
        
        List<List<byte[][]>> convertedLines = convertLines(lines);
        
        final int lineCount = lines.size();
        final int maxCharCount = lines // NOSONAR: always available
            .stream().map(String::length).max(Comparator.comparingInt(a -> a)).get();
        
        // letters + empty space + 1 needed for padding
        // it's read up-down then left-right, so width and height are switched
        final int width  = Alphabet.LETTER_SIZE * lineCount + lineCount + 1;
        final int height = Alphabet.LETTER_SIZE * maxCharCount + maxCharCount + 1;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics().setBackground(new Color(SECONDARY_COLOUR));
        
        drawLines(convertedLines, image);
        
        final BufferedImage scaledImage = scaleImage(image);
        
        ImageIO.write(scaledImage, "PNG", new File("out.png"));
    }
    
    private static BufferedImage scaleImage(final BufferedImage image) {
        final AffineTransform transformation = new AffineTransform();
        transformation.scale(SCALE, SCALE);
        final AffineTransformOp operation = new AffineTransformOp(
            transformation,
            AffineTransformOp.TYPE_NEAREST_NEIGHBOR
        );
        return operation.filter(
            image,
            new BufferedImage(
                image.getWidth() * SCALE,
                image.getHeight() * SCALE,
                BufferedImage.TYPE_INT_RGB
            )
        );
    }
    
    private static void drawLines(
        final List<List<byte[][]>> convertedLines,
        final BufferedImage image
    ) {
        for (int x = 0; x < convertedLines.size(); x++) {
            final List<byte[][]> currentLine = convertedLines.get(x);
            for (int subLetterY = 0; subLetterY < Alphabet.LETTER_SIZE; subLetterY++) {
                drawLine(currentLine, x, subLetterY, image);
            }
        }
    }
    
    private static void drawLine(
        final List<byte[][]> line,
        final int x,
        final int subLetterY,
        final BufferedImage image
    ) {
        for (int y = 0; y < line.size(); y++) {
            for (int subLetterX = 0; subLetterX < Alphabet.LETTER_SIZE; subLetterX++) {
                final int xOffset = x * 6 + 1 + subLetterX;
                final int yOffset = y * 6 + 1 + subLetterY;
                
                image.setRGB(
                    xOffset,
                    yOffset,
                    line.get(y)[subLetterY][subLetterX] == 1 ? PRIMARY_COLOUR : SECONDARY_COLOUR
                );
            }
        }
    }
    
    private static List<List<byte[][]>> convertLines(final List<String> lines) {
        return lines
            .stream()
            .map(String::toLowerCase)
            .map(String::chars)
            .map(stream -> stream
                .boxed()
                .map(charValue -> Alphabet.of((char) (charValue & 0xFF)))
                .map(optional -> optional.orElseGet(() -> Alphabet.EMPTY))
                .toList())
            .toList();
    }
}

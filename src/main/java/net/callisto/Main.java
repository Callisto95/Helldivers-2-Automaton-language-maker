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
            for (int y = 0; y < currentLine.size(); y++) {
                drawLetter(x, y, currentLine.get(y), image);
            }
        }
    }
    
    private static void drawLetter(
        final int x,
        final int y,
        final byte[][] letter,
        BufferedImage image
    ) {
        for (int subX = 0; subX < Alphabet.LETTER_SIZE; subX++) {
            for (int subY = 0; subY < Alphabet.LETTER_SIZE; subY++) {
                final int xPosition = x * (Alphabet.LETTER_SIZE + 1) + 1 + subX;
                final int yPosition = y * (Alphabet.LETTER_SIZE + 1) + 1 + subY;
                
                image.setRGB(
                    xPosition,
                    yPosition,
                    letter[subY][subX] == 1 ? PRIMARY_COLOUR : SECONDARY_COLOUR
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

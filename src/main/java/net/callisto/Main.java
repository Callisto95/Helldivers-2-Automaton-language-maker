package net.callisto;

import javax.imageio.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class Main {
    // this is a bad name
    private record DrawConfig(
        int primary,
        int secondary,
        BufferedImage image,
        boolean isVertical
    ) {}
    
    public static void main(String[] args) throws IOException {
        final List<String> lines = Arrays.asList(args);
        
        if (lines.isEmpty()) {
            System.out.println("No input.");
            System.exit(0);
        }
        
        final boolean isVertical = System.getenv().getOrDefault("VERTICAL", "0").equals("1");
        final int     scale      = Integer.parseInt(System.getenv().getOrDefault("SCALE", "10"));
        
        final int primary_colour = Integer.parseInt(
            System
                .getenv()
                .getOrDefault("PRIMARY", "FF0000"), 16
        ) & 0xFFFFFF;
        final int secondary_colour = Integer.parseInt(
            System
                .getenv()
                .getOrDefault("SECONDARY", "000000"), 16
        ) & 0XFFFFFF;
        
        List<List<byte[][]>> convertedLines = convertLines(lines);
        
        final int lineCount = lines.size();
        final int maxCharCount = lines // NOSONAR: always available
            .stream().map(String::length).max(Comparator.comparingInt(a -> a)).get();
        
        // letters + empty space for each letter + 1 padding
        final int width = (Alphabet.LETTER_SIZE + 1) * (isVertical ? lineCount : maxCharCount) + 1;
        final int height = (Alphabet.LETTER_SIZE + 1) * (isVertical ? maxCharCount : lineCount) + 1;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(secondary_colour));
        graphics.fill(new Rectangle(width, height));
        graphics.dispose();
        
        final DrawConfig drawConfig = new DrawConfig(
            primary_colour,
            secondary_colour,
            image,
            isVertical
        );
        
        drawLines(convertedLines, drawConfig);
        
        final BufferedImage scaledImage = scaleImage(drawConfig.image(), scale);
        
        ImageIO.write(scaledImage, "PNG", new File("out.png"));
        System.out.println("write done");
    }
    
    private static BufferedImage scaleImage(final BufferedImage image, final int scale) {
        final AffineTransform transformation = new AffineTransform();
        transformation.scale(scale, scale);
        final AffineTransformOp operation = new AffineTransformOp(
            transformation,
            AffineTransformOp.TYPE_NEAREST_NEIGHBOR
        );
        return operation.filter(
            image,
            new BufferedImage(
                image.getWidth() * scale,
                image.getHeight() * scale,
                BufferedImage.TYPE_INT_RGB
            )
        );
    }
    
    private static void drawLines(
        final List<List<byte[][]>> convertedLines,
        final DrawConfig drawConfig
    ) {
        for (int x = 0; x < convertedLines.size(); x++) {
            final List<byte[][]> currentLine = convertedLines.get(x);
            for (int y = 0; y < currentLine.size(); y++) {
                drawLetter(
                    drawConfig.isVertical() ? x : y,
                    drawConfig.isVertical() ? y : x,
                    currentLine.get(y),
                    drawConfig
                );
            }
        }
    }
    
    private static void drawLetter(
        final int x,
        final int y,
        final byte[][] letter,
        final DrawConfig drawConfig
    ) {
        for (int subX = 0; subX < Alphabet.LETTER_SIZE; subX++) {
            for (int subY = 0; subY < Alphabet.LETTER_SIZE; subY++) {
                final int xPosition = x * (Alphabet.LETTER_SIZE + 1) + 1 + subX;
                final int yPosition = y * (Alphabet.LETTER_SIZE + 1) + 1 + subY;
                
                drawConfig.image().setRGB(
                    xPosition,
                    yPosition,
                    letter[subY][subX] == 1 ? drawConfig.primary() : drawConfig.secondary()
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

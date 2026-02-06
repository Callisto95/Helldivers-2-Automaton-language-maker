package net.callisto;

import javax.imageio.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        final List<String> lines = Arrays.asList(args);
        
        if (lines.isEmpty()) {
            System.out.println("No input.");
            System.exit(0);
        }
        
        final int lineCount = lines.size();
        
        final int charCount = lines // NOSONAR: always available
            .stream().map(String::length).max(Comparator.comparingInt(a -> a)).get();
        
        // letters + empty space + 1 needed for padding  
        final int width = Alphabet.LETTER_SIZE * lineCount + lineCount + 1;
        final int height  = Alphabet.LETTER_SIZE * charCount + charCount + 1;
        
        List<List<byte[][]>> convertedLines = lines
            .stream()
            .map(String::toLowerCase)
            .map(String::chars)
            .map(stream -> stream
                .boxed()
                .map(c -> Alphabet.of((char) (c & 0xFF)))
                .map(o -> o.orElseGet(() -> Alphabet.EMPTY))
                .toList())
            .toList();
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < convertedLines.size(); x++) {
            final List<byte[][]> currentLine = convertedLines.get(x);
            for (int iy = 0; iy < Alphabet.LETTER_SIZE; iy++) {
                for (int y = 0; y < currentLine.size(); y++) {
                    
                    for (int ix = 0; ix < Alphabet.LETTER_SIZE; ix++) {
                        final int xOffset = x * 6 + 1 + ix;
                        final int yOffset = y * 6 + 1 + iy;
                        image.setRGB(
                            xOffset,
                            yOffset,
                            currentLine.get(y)[iy][ix] == 1 ? 0xFF0000 : 0x000000
                        );
                    }
                }
            }
        }
        
        final int scale = 25;
        
        final AffineTransform transformation = new AffineTransform();
        transformation.scale(scale, scale);
        final AffineTransformOp operation = new AffineTransformOp(transformation, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        
        ImageIO.write(operation.filter(image, new BufferedImage(image.getWidth() * scale, image.getHeight() * scale, BufferedImage.TYPE_INT_RGB)), "PNG", new File("out.png"));
    }
}

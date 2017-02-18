package tc.oc.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import tc.oc.net.UriUtils;

public interface ImageUtils {

    static BufferedImage resize(BufferedImage image, int width, int height) {
        final BufferedImage resized = new BufferedImage(width, 64, image.getType());
        final Graphics2D g = resized.createGraphics();
        g.addRenderingHints(ImmutableMap.of(RenderingHints.KEY_INTERPOLATION,
                                            RenderingHints.VALUE_INTERPOLATION_BILINEAR));
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    static byte[] png(BufferedImage image) {
        try {
            final ByteArrayOutputStream data = new ByteArrayOutputStream();
            ImageIO.write(image, "png", data);
            return data.toByteArray();
        } catch(IOException e) {
            throw Throwables.propagate(e);
        }
    }

    static byte[] thumbnail(Path path, int height) {
        try {
            final BufferedImage image = ImageIO.read(path.toFile());
            final BufferedImage thumb = resize(image, height * image.getWidth() / image.getHeight(), height);
            return png(thumb);
        } catch(IOException e) {
            throw Throwables.propagate(e);
        }
    }

    static String thumbnailUri(Path path, int height) {
        return UriUtils.dataUri("image/png", thumbnail(path, height));
    }
}

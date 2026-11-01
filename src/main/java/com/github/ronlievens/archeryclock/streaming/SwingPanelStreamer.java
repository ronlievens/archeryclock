package com.github.ronlievens.archeryclock.streaming;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SwingPanelStreamer {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int FPS = 25;

    private final JComponent component;
    private volatile boolean running;

    public SwingPanelStreamer(JComponent component) {
        this.component = component;
    }

    public void start() {
        Thread thread = new Thread(this::stream, "vlc-stream");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
    }

    private void stream() {
        running = true;

        BufferedImage image = new BufferedImage(
            WIDTH, HEIGHT, BufferedImage.TYPE_3BYTE_BGR
        );

        try (Java2DFrameConverter converter = new Java2DFrameConverter();
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                 "udp://127.0.0.1:1234?pkt_size=1316",
                 WIDTH,
                 HEIGHT
             )) {

            recorder.setFormat("mpegts");
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setFrameRate(FPS);
            recorder.setVideoBitrate(2_000_000);

            recorder.setOption("preset", "ultrafast");
            recorder.setOption("tune", "zerolatency");

            recorder.start();

            long frameTime = 1_000_000_000L / FPS;

            while (running) {
                long start = System.nanoTime();

                // Swing-component tekenen op de EDT
                SwingUtilities.invokeAndWait(() -> {
                    Graphics2D g = image.createGraphics();

                    try {
                        g.setColor(Color.BLACK);
                        g.fillRect(0, 0, WIDTH, HEIGHT);

                        double sx = WIDTH / (double) component.getWidth();
                        double sy = HEIGHT / (double) component.getHeight();

                        g.scale(sx, sy);
                        component.printAll(g);

                    } finally {
                        g.dispose();
                    }
                });

                Frame frame = converter.convert(image);
                recorder.record(frame);

                long remaining = frameTime - (System.nanoTime() - start);

                if (remaining > 0) {
                    Thread.sleep(remaining / 1_000_000,
                        (int) (remaining % 1_000_000));
                }
            }

            recorder.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

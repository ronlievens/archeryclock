package com.github.ronlievens.archeryclock.ui.stream;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public abstract class StreamableJPanel extends JPanel implements Runnable {

    private static final String THREAD_NAME = "vlc-stream";
    private static final String RECORD_FORMAT = "mpegts";
    private static final String RECORD_URL = "udp://127.0.0.1:1234?pkt_size=1316";
    private static final long ONE_SECOND_IN_NANOSECONDS = 1_000_000_000L;
    private static final int STREAM_VIDEO_BIT_RATE = 2_000_000;
    private volatile boolean running;

    private final int height;
    private final int width;
    private final int fps;

    public StreamableJPanel(final int height,
                            final int width,
                            final int fps,
                            final LayoutManager layout) {
        super(layout, true);
        this.height = height;
        this.width = width;
        this.fps = fps;
        this.running = false;
    }

    public void startStream() {
        val thread = new Thread(this, THREAD_NAME);
        thread.setDaemon(true);
        thread.start();
    }

    public void stopStream() {
        running = false;
    }

    public void run() {
        running = true;

        val image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        try (val converter = new Java2DFrameConverter(); val recorder = new FFmpegFrameRecorder(RECORD_URL, width, height)) {
            recorder.setFormat(RECORD_FORMAT);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.setFrameRate(fps);
            recorder.setVideoBitrate(STREAM_VIDEO_BIT_RATE);
            recorder.start();

            long frameTime = ONE_SECOND_IN_NANOSECONDS / fps;

            while (running) {
                val start = System.nanoTime();
                val g = image.createGraphics();

                try {
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, width, height);

                    val sx = width / (double) getWidth();
                    val sy = height / (double) getHeight();

                    g.scale(sx, sy);
                    printAll(g);
                } finally {
                    g.dispose();
                }

                val frame = converter.convert(image);
                recorder.record(frame);

                val remaining = frameTime - (System.nanoTime() - start);
                if (remaining > 0) {
                    Thread.sleep(remaining / 1_000_000, (int) (remaining % 1_000_000));
                }
            }

            recorder.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

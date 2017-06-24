package com.webcamapp;


import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamListener;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.video.ConverterFactory;
import javafx.concurrent.Task;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Majkel on 21.05.2017.
 */
public class WebcamHandler extends Task implements WebcamListener {
    private Webcam webcam;
    private volatile boolean endRecording=false;
    private volatile boolean endThread=false;
    private volatile boolean keepRecording=false;
    private volatile boolean saveRecording=false;
    private volatile boolean cancelRecording=false;
    private volatile long startTime =0;
    private volatile long messured=0;
    private String filename;
    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
    private File file;
    private IMediaWriter writer;
    public WebcamHandler(Webcam webcam) {
        this.webcam=webcam;
        this.webcam.addWebcamListener(this);

    }


    public void startRecording(){
        keepRecording=true;
        endRecording=false;
    }

    public void resumeRecording(){
        startTime =System.currentTimeMillis()-messured;
        keepRecording=true;
        endRecording=false;
    }

    public void pauseRecording(){
        keepRecording=false;
    }


    public void cancelRecording(){
        keepRecording=false;
        cancelRecording=true;
        saveRecording=false;
        messured=0;
    }

    public void saveRecording(){
        keepRecording=false;
        saveRecording=true;
    }

    private void save(){
        if(writer!=null && writer.isOpen()){
        //    System.out.println("save");
            writer.close();
            writer=null;
            file=null;
            messured=0;
            updateMessage(formatInterval(messured));
        }
    }


    public Webcam getWebcam() {
        return webcam;
    }

    public void setWebcam(Webcam webcam) {
        if(!webcam.equals(this.getWebcam())) {
            this.webcam.close();
            this.webcam = webcam;
            this.webcam.open();
            this.webcam.addWebcamListener(this);
        }
    }

    public void setResoultion(Dimension dim){
        webcam.close();
        webcam.setViewSize(dim);
        webcam.open();
    }


    private void initRecord(){
        if(file==null || writer==null) {
            Date date = new Date();
            filename=df.format(date);
            file = new File(filename + ".mp4");
            writer = ToolFactory.makeWriter(file.getName());
            Dimension size = webcam.getViewSize();
            writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, size.width, size.height);
            startTime =System.currentTimeMillis();
        }/*
        try {
            BufferedImage image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
            messured = System.currentTimeMillis() - startTime;
            writer.encodeVideo(0, image, messured,
                    TimeUnit.MILLISECONDS);
            updateMessage(formatInterval(messured));
        }catch (Exception ex){
            ex.printStackTrace();
        }*/
        }



    public void run() {
        updateMessage(formatInterval(0));
        while (!endThread){
            if(keepRecording){
                initRecord();
            }
            else {
                if(saveRecording){
                    saveRecording=false;
                    save();
                }
                else if(cancelRecording){
                    messured=0;
                    updateMessage(formatInterval(messured));
                    writer.close();
                    file.delete();
                    file=null;
                    cancelRecording=false;
                }
                else if(endThread) break;
              /*  try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        }
    }

    public  void controlBrightness(float i){
        if(i!=0.0f) {
            i = i>0.0f ? 10*i:i;
            WebcamImageTransformer wit = new ImageTransformer(i);
            webcam.setImageTransformer(wit);
        }
        else webcam.setImageTransformer(null);
    }

    @Override
    protected Object call() throws Exception {
        run();
        return null;
    }

    @Override
    public void webcamOpen(WebcamEvent webcamEvent) {

    }

    @Override
    public void webcamClosed(WebcamEvent webcamEvent) {

    }

    @Override
    public void webcamDisposed(WebcamEvent webcamEvent) {

    }

    @Override
    public void webcamImageObtained(WebcamEvent webcamEvent) {
        messured = System.currentTimeMillis() - startTime;
        if(writer!=null && keepRecording && startTime>0){
            BufferedImage image = ConverterFactory.convertToType(webcamEvent.getImage(), BufferedImage.TYPE_3BYTE_BGR);
            if(writer.isOpen()) {
                writer.encodeVideo(0, image, messured,
                        TimeUnit.MILLISECONDS);
                updateMessage(formatInterval(messured));
            }

        }
    }

    private class ImageTransformer implements WebcamImageTransformer{
        float  brightness;
        public ImageTransformer(float brightness){
            this.brightness=brightness;
        }
        @Override
        public BufferedImage transform(BufferedImage bufferedImage) {
            RescaleOp rescaleOp= new RescaleOp(1+brightness, 0.0f, null);
            return rescaleOp.filter(bufferedImage, bufferedImage);
        }
    }


    public void endThread(){
        endThread=true;
    }

    private String formatInterval(final long l)
    {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
      //  final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }

    protected void updateProgress(long workDone, long max) {
        // Date date = new Date(messured);
        updateMessage(formatInterval(messured));
        super.updateProgress(workDone, max);
    }
}


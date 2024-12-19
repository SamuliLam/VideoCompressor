package org.example.model;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class VideoCompressor {
    public static void compressVideo(String inputFilePath, String outputFilePath, long maxSizeMB) {
        try {
            int duration = getVideoDuration(inputFilePath);
            int audioBitrate = getAudioBitrate(inputFilePath);
            double totalBitrate =  (double) (maxSizeMB * 8 * 1024 * 1024) / duration;
            double targetVideoBitrate = totalBitrate - audioBitrate;
            System.out.println("Max bitrate: " + targetVideoBitrate);

            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-y", "-i", inputFilePath, "-c:v", "libx264",  "-b:v", String.valueOf(targetVideoBitrate), "-pass", "1", "-f", "null", "NUL");
            Process process = processBuilder.start();
            logProcessOutput(process);
            process.waitFor();

            processBuilder = new ProcessBuilder("ffmpeg",  "-i", inputFilePath, "-c:v", "libx264", "-b:v", String.valueOf(targetVideoBitrate), "-pass", "2",  "-c:a", "aac", outputFilePath);
            process = processBuilder.start();
            logProcessOutput(process);
            process.waitFor();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logProcessOutput(Process process) {
        // Log the standard error
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static int getAudioBitrate(String inputFilePath){
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ffprobe", "-v", "error", "-select_streams", "a:0", "-show_entries", "stream=bit_rate", "-of", "default=noprint_wrappers=1:nokey=1", inputFilePath);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            int bitrate = (int) Math.round(Double.parseDouble(line));
            System.out.println("Audio bitrate: " + bitrate);
            return bitrate;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    public static int getVideoDuration(String inputFilePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", inputFilePath);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            int duration = (int) Math.round(Double.parseDouble(line));
            return duration;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\samul\\Videos\\nice.mp4";
        String outputFilePath = "C:\\Users\\samul\\Videos\\nice_compressed.mp4";
        int targetSizeMB = 9;
        compressVideo(inputFilePath, outputFilePath, targetSizeMB);
    }
}

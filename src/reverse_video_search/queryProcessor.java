package reverse_video_search;

import javax.sound.sampled.*;
import java.io.*;
import java.util.*;

public class queryProcessor
{
    static String[] DATABASE_VIDEOS = {"flowers","interview","movie","musicvideo","sports","starcraft","traffic"};
    static int DATABASE_FRAME_COUNT = 600;

    // Expected input:
    // arg0: first rgb frame of query video
    // arg1: wav file of query
    // arg2: number of frames in video

    // Expected file structure:
    // Database > tag > rgb + wav + metric files
    // query > all rgb + all wav + all metric files

    public static void main (String[] args) throws IOException
    {
        int runMode = 1;    // Live query processing mode
        String startingFrame = args[0];
        String audioFile = args[1];
        int numFrames = 0;
        int duration_1;

        String filePath = "src/reverse_video_search/query/"+audioFile;
        try
        {
            AudioInputStream a1;
            AudioFormat format1;
            a1 = AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
            Clip c1 = AudioSystem.getClip();
            c1.open(a1);
            format1 = a1.getFormat();
            duration_1 = (int) Math.round(a1.getFrameLength() + 0.0);
            numFrames = (int)(duration_1/format1.getFrameRate())*30;
            c1.close();
            a1.close();
        }
        catch (LineUnavailableException | UnsupportedAudioFileException e1)
        {
            e1.printStackTrace();
        }

        double[] asdValues = frameComparator.extractMotion(startingFrame, numFrames, runMode);
        int[][] avgColors = colorComparator.extractColor(startingFrame, numFrames, runMode);

        // Compare query metrics to database
        Map<String,Double[]> motion = frameComparator.compareMotion(numFrames, asdValues);
        Map<String,Double[]> color = colorComparator.compareColor(numFrames,avgColors);
        Map<String, Double> audio = audioMetric.compareAudio(audioFile);

        // Compute total weighted similarity score and sort results above threshold 1/3
        Map <Double, String> order = new TreeMap<>(Collections.reverseOrder());

        for (String dbVideo: DATABASE_VIDEOS)
        {
            double motionScore = motion.get(dbVideo)[0];
            double colorScore = color.get(dbVideo)[0];
            double audioScore = audio.get(dbVideo);
            double weightedScore;

            if(audioScore < 0.5)
                weightedScore = (motionScore + colorScore) / 2;
            else
                weightedScore = (motionScore + colorScore + audioScore) / 3;

            order.put(weightedScore, dbVideo);
        }

        ArrayList<String> dbList = new ArrayList<>();
        ArrayList<Double[]> scoresOut = new ArrayList<>();
        ArrayList<Integer> motionIndex = new ArrayList<>();
        ArrayList<Integer> colorIndex = new ArrayList<>();

        for (Map.Entry<Double, String> entry : order.entrySet())
        {
            Double combinedWeight = entry.getKey();

            if (combinedWeight < 0.25)
                break;

            String dbVideo = entry.getValue();
            Double motionInd = motion.get(dbVideo)[1];
            Double colorInd = color.get(dbVideo)[1];
            dbList.add(dbVideo);
            Double[] metrics = {color.get(dbVideo)[0],motion.get(dbVideo)[0],audio.get(dbVideo)};
            scoresOut.add(metrics);
            motionIndex.add(motionInd.intValue());
            colorIndex.add(colorInd.intValue());
        }

        // Load interface with top results
        player.main(args[0],args[1], dbList, scoresOut, motionIndex, colorIndex, numFrames);
    }

}

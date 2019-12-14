// CSCI 576 Team Project - Reverse Video Search
// Avishkar Kolahalu; Michael Cappuccio; Weichen Yao
package reverse_video_search;

import java.io.*;
import java.util.*;

public class frameComparator
{
    private static int width = 352;
    private static int height = 288;

    private static double[] compareFrames(String tag, int numFrames, int mode)
    {
        double[][] prevYMatrix = new double[width][height];
        double[][] currYMatrix;
        double[] absoluteSumDifferences = new double[numFrames - 1];
        String folder;

        if (mode == 0)
            folder = "src/reverse_video_search/database/" + tag;
        else
            folder = "src/reverse_video_search/query";  // Mode is 1 for query processing

        for (int frameCounter = 0; frameCounter < numFrames; ++frameCounter)
        {
            int frameNum = frameCounter + 1;    // Add 1 for zero based loop index
            String fileName = folder + "/" + tag + String.format("%03d",frameNum)+".rgb";
            File frame = new File(fileName);

            if (frameCounter == 0)
                prevYMatrix = getYMatrix(frame);    // Populate initial frame;
            else
            {
                // Populate current frame
                currYMatrix = getYMatrix(frame);

                // Get absolute sum differences and store in array (-1 for zero based index)
                absoluteSumDifferences[frameCounter-1] = getASD(prevYMatrix,currYMatrix);

                // Store curr frame as prev for next comparison
                prevYMatrix = currYMatrix;
            }
        }
        return absoluteSumDifferences;
    }

    private static double[][] getYMatrix(File file)
    {
        double [][] yValues = new double [width][height];
        try {

            InputStream is = new FileInputStream(file.getAbsoluteFile());

            long len = file.length();
            byte[] bytes = new byte[(int)len];

            int offset = 0;
            int numRead;

            while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0)
                offset += numRead;

            int ind = 0;
            for(int y = 0; y < height; ++y)
                for(int x = 0; x < width; ++x)
                {
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    //populate y matrix
                    yValues[x][y] = 0.299*(r&0xff) + 0.587*(g&0xff) + 0.114*(b&0xff);

                    ind++;
                }

            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return yValues;
    }

    private static double getASD(double[][] prevFrame, double[][] currFrame)
    {
        double retValue = 0;
        for (int i = 0; i < width; ++i)
            for (int j = 0; j < height; ++j)
                retValue += Math.abs(currFrame[i][j] - prevFrame[i][j]);

        return retValue;
    }

    // Input arguments:
    // arg1: first frame of vid to process e.g. flowers001.rgb
    // arg2: integer number of frames to process
    // arg3: 0 for offline processing (writes to db video folder), 1 for qry processing (writes to query folder)
    static double[] extractMotion(String inputFrame, int numFrames, int mode)
    {
        double[] asdValues = new double[numFrames - 1];
        try {

            // Remove ###.rgb from input file name
            String tag = inputFrame.substring(0 , inputFrame.length() - 7);

            String fileName;

            if (mode == 1)
                fileName = "src/reverse_video_search/query/" + tag + "_ASD.txt";
            else
                fileName = "src/reverse_video_search/database/" + tag + "/" + tag + "_ASD.txt";

            FileWriter fw = new FileWriter(new File(fileName).getAbsoluteFile());
            PrintWriter  pw = new PrintWriter(fw);
            asdValues = compareFrames(tag,numFrames,mode);

            for (double asdValue : asdValues)
                pw.println(asdValue);

            pw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return asdValues;
    }

    static Map<String, Double[]> compareMotion(int numFrames, double[] queryASD) throws IOException
    {
        // Called after tag_ASD.txt file created for database videos and query video
        double[] databaseASD = new double[queryProcessor.DATABASE_FRAME_COUNT -1];
        int window = numFrames - 1;
        Map<String, Double[]> motionSimilarityScores = new HashMap<>();

        String line;
        int readCounter;

        for (String dbVideo: queryProcessor.DATABASE_VIDEOS)
        {
            // Load dbvid_asd.txt
            File database = new File("src/reverse_video_search/database/" + dbVideo + "/" + dbVideo + "_ASD.txt");
            BufferedReader dbBR = new BufferedReader(new FileReader(database));
            readCounter = 0;

            while ((line = dbBR.readLine()) != null)
                databaseASD[readCounter++] = Double.parseDouble(line);

            // Iterate through sliding window and find min difference
            double minDifference = Double.MAX_VALUE;
            int minWindowStart = -1;

            for (int i = 0; i < databaseASD.length - window; ++i)
            {
                // Outer loop controls the sliding window
                double relDifference = 0;

                for (int j = 0; j < window; ++j)    // Inner loop calculates asd difference at this window position
                    relDifference += Math.abs((databaseASD[i + j] - queryASD[j]) / databaseASD[i + j]);

                // Check minDifference
                if (relDifference < minDifference)
                {
                    minDifference = relDifference;
                    minWindowStart = i;
                }

            }

            minDifference /= window;
            double similarity = 1 - minDifference;

            if (similarity < 0) // Cap min value to 0 which whill contribute 0 weight to motion
                similarity = 0;

            // Store result in map <database video, similarity score>
            Double[] output = {similarity, (double)minWindowStart};
            motionSimilarityScores.put(dbVideo, output);
        }

        return motionSimilarityScores;
    }

    // Main method: used when running offline process for each database folder
    // No input required for offline mode
    public static void main(String[] args)
    {
        int runMode = 0;
        for (String dbVideo : queryProcessor.DATABASE_VIDEOS) // Adding suffix to match syntax for live query processing
            extractMotion(dbVideo + "001.rgb", queryProcessor.DATABASE_FRAME_COUNT, runMode);
    }

}

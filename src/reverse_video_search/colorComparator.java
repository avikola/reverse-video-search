package reverse_video_search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

public class colorComparator
{
    public static void main(String[] args) throws IOException
    {
        int runMode = 0;
        for(String dbVideo : queryProcessor.DATABASE_VIDEOS)
            extractColor(dbVideo + "001.rgb", queryProcessor.DATABASE_FRAME_COUNT, runMode);
    }

    static int[][] extractColor(String inputFrame, int numFrames, int mode) throws IOException
    {
        int[][] avgColors;

        String tag = inputFrame.substring(0, inputFrame.length()-7);

        String fileName;

        if(mode == 1)
            fileName = "src/reverse_video_search/query/" + tag + "_AVC.txt";

        else
            fileName = "src/reverse_video_search/database/" + tag + "/" + tag + "_AVC.txt";

        FileWriter fw = new FileWriter(fileName);
        PrintWriter pw = new PrintWriter(fw);
        avgColors = getAverageColor(tag,numFrames,mode);

        for (int[] avgColor : avgColors)
            for (int j = 0; j < 3; ++j)
                pw.println(avgColor[j]);

        pw.close();
        return avgColors;
    }

    private static int[][] getAverageColor(String tag, int numFrames, int mode)
    {
        int[][] avgColor = new int[numFrames][3];
        String folder;

        if(mode == 0)
            folder = "src/reverse_video_search/database/" + tag;
        else
            folder = "src/reverse_video_search/query";

        for(int frameCounter = 0; frameCounter < numFrames; ++frameCounter)
        {
            int frameNum = frameCounter + 1;
            String fileName = folder + "/" + tag + String.format("%03d",frameNum) + ".rgb";

            try {
                File frame = new File(fileName);
                InputStream is = new FileInputStream(frame);
                long len = frame.length();

                byte[] bytes = new byte[(int)len];
                int offset = 0;
                int numRead;

                while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0)
                    offset += numRead;

                int ind = 0, r_sum = 0, g_sum = 0, b_sum = 0, height = 288, width = 352;

                for(int y = 0; y < height; ++y)
                    for(int x = 0; x < width; ++x)
                    {
                        int r = bytes[ind];
                        int g = bytes[ind + height * width];
                        int b = bytes[ind + height * width *2];

                        r = (r < 0)?r+256:r;
                        g = (g < 0)?g+256:g;
                        b = (b < 0)?b+256:b;

                        r_sum += r;
                        g_sum += g;
                        b_sum += b;

                        ++ind;
                    }

                int total = width * height;
                avgColor[frameCounter][0] = r_sum/total;
                avgColor[frameCounter][1] = g_sum/total;
                avgColor[frameCounter][2] = b_sum/total;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return avgColor;
    }

    static Map<String,Double[]> compareColor(int numFrames, int[][] queryAVC) throws IOException
    {
        int[][] databaseAVC = new int[queryProcessor.DATABASE_FRAME_COUNT][3];
        Map<String, Double[]> colorSimilarityScores = new HashMap<>();

        String line;
        int colornum;
        int framenum;

        for(String dbVideo : queryProcessor.DATABASE_VIDEOS)
        {
            File database = new File("src/reverse_video_search/database/" + dbVideo + "/" + dbVideo + "_AVC.txt");
            BufferedReader dbBR = new BufferedReader(new FileReader(database));

            framenum = 0;
            colornum = 0;
            while((line = dbBR.readLine()) != null)
            {
                databaseAVC[framenum][colornum++] = Integer.parseInt(line);
                if(colornum == 3)
                {
                    colornum = 0;
                    ++framenum;
                }
            }

            double minDifference= Double.MAX_VALUE;
            int minWindowStart = -1;
            for(int i = 0; i < databaseAVC.length- numFrames +1; ++i)
            {
                double relDifference = 0;
                for(int j = 0; j < numFrames; ++j)
                {
                    double difr = Math.abs((double)(databaseAVC[i+j][0] - queryAVC[j][0]) / (double)(databaseAVC[i+j][0]));
                    double difg = Math.abs((double)(databaseAVC[i+j][1] - queryAVC[j][1]) / (double)(databaseAVC[i+j][1]));
                    double difb = Math.abs((double)(databaseAVC[i+j][2] - queryAVC[j][2]) / (double)(databaseAVC[i+j][2]));
                    double diff = (difr + difg + difb) / 3.0;
                    relDifference += diff;
                }

                if(relDifference < minDifference)
                {
                    minDifference = relDifference;
                    minWindowStart = i;
                }
            }

            minDifference /= numFrames;
            double similarity = 1-minDifference;

            if(similarity < 0)
                similarity = 0;

            Double[] result = {similarity, (double)minWindowStart};
            colorSimilarityScores.put(dbVideo, result);
        }

        return colorSimilarityScores;
    }

}

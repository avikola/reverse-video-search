package reverse_video_search;

import java.util.*;

// musicg library

import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.wave.Wave;

class audioMetric
{
    static Map<String, Double> compareAudio(String audioInput)
    {
        Map<String, Double> audioSimilarity = new HashMap<>();
        Wave queryAudio = new Wave("src/reverse_video_search/query/" + audioInput);

        for (String dbVideo : queryProcessor.DATABASE_VIDEOS)
        {
            Wave dbAudio = new Wave("src/reverse_video_search/database/" + dbVideo + "/" + dbVideo + ".wav");
            FingerprintSimilarity fingerprintSimilarity = queryAudio.getFingerprintSimilarity(dbAudio);
            double similarity = fingerprintSimilarity.getSimilarity();

            audioSimilarity.put(dbVideo, similarity);
        }

        return audioSimilarity;
    }

}

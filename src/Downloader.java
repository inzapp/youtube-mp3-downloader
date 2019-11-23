import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Downloader {

    public static void main(String[] args) {
        System.out.println("Start converting...");
        String youtubeVideoUrl = "https://www.youtube.com/watch?v=97Onz7v-vXY";
        String uuid = getUuid(youtubeVideoUrl);
        if (uuid == null) {
            System.out.println("ERROR while converting youtube video : invalid youtube video url");
            return;
        }

        boolean bGetTitle = false;
        boolean bGetDuration = false;
        boolean bGetThumbnail = false;
        String fileUrl;
        while (true) {
            JSONObject json = getConvertingStatus(uuid);

            String title = getJsonValue(json, "title");
            if(title != null && !bGetTitle) {
                System.out.println("title : " + title);
                bGetTitle = true;
            }

            String duration = getJsonValue(json, "duration");
            if(duration != null && !bGetDuration) {
                System.out.println("duration : " + duration);
                bGetDuration = true;
            }

            String thumbnailUrl = getJsonValue(json, "thumbnail");
            if(thumbnailUrl != null && !bGetThumbnail) {
                System.out.println("thumbnail url : " + thumbnailUrl);
                bGetThumbnail = true;
            }

            String percentage = getJsonValue(json, "percent");
            if(percentage != null) {
                System.out.println("percentage : " + percentage);
            }

            fileUrl = getJsonValue(json, "fileUrl");
            if(getJsonValue(json, "fileUrl") != null) {
                break;
            }

            String newUuid = getJsonValue(json, "uuid");
            if(!uuid.equals(newUuid)) {
                System.out.println("UUID DIFFER !!!!!");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }

        System.out.println(fileUrl);
    }

    private static String getUuid(String youtubeVideoUrl) {
        try {
            String url = "https://mp3-youtube.download/download/start";
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("url", youtubeVideoUrl);
            json.put("extension", "mp3");

            OutputStream os = httpURLConnection.getOutputStream();
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                sb.append(line).append('\n');
            }
            br.close();

            JSONObject httpRes = new JSONObject(sb.toString());
            return httpRes.getJSONObject("data").getString("uuid");
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject getConvertingStatus(String uuid) {
        try {
            String url = "https://mp3-youtube.download/download/" + uuid;
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);

            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                sb.append(line).append('\n');
            }
            br.close();

            return new JSONObject(sb.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private static String getJsonValue(JSONObject json, String key) {
        try {
            if(key.equals("percent"))
                return String.valueOf(json.getJSONObject("data").getInt(key));
            else
                return json.getJSONObject("data").getString(key);
        } catch (Exception e) {
            return null;
        }
    }
}

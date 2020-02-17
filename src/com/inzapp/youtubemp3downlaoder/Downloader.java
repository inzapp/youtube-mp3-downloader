package com.inzapp.youtubemp3downlaoder;

import java.io.BufferedReader;
import java.io.FileReader;

import com.inzapp.youtubemp3downlaoder.api.Api;
import com.inzapp.youtubemp3downlaoder.api.Mp3YoutubeDownload;

public class Downloader {

	public static void main(String[] args) {
		try {
			Api api = new Mp3YoutubeDownload();
			BufferedReader br = new BufferedReader(new FileReader("youtube-urls.txt"));
			while (true) {
				String youtubeVideoUrl = br.readLine();
				if (youtubeVideoUrl == null) {
					break;
				}

				int res;
				do {
					res = api.download(youtubeVideoUrl);
				} while (res == Api.SUCCESS);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

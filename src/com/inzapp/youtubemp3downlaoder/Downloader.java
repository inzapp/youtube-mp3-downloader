package com.inzapp.youtubemp3downlaoder;

import java.io.BufferedReader;
import java.io.FileReader;

import com.inzapp.youtubemp3downlaoder.api.Api;
import com.inzapp.youtubemp3downlaoder.api.ApiResult;
import com.inzapp.youtubemp3downlaoder.api.impl.Mp3YoutubeDownloadApi;

public class Downloader {

	public static void main(String[] args) {
		try {
			Api api = new Mp3YoutubeDownloadApi();
			BufferedReader br = new BufferedReader(new FileReader("youtube-urls.txt"));
			while (true) {
				String youtubeVideoUrl = br.readLine();
				if (youtubeVideoUrl == null) {
					break;
				}

				while (true) {
					if (api.download(youtubeVideoUrl) == ApiResult.FAILURE) {
						break;
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

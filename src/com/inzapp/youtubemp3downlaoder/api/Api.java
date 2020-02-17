package com.inzapp.youtubemp3downlaoder.api;

public interface Api {
	public static final int SUCCESS = 1;
	public static final int FAILURE = -1;
	public int download(String youtubeVideoUrl);
}

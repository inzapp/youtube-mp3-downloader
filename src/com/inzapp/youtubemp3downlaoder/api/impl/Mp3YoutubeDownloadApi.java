package com.inzapp.youtubemp3downlaoder.api.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import com.inzapp.youtubemp3downlaoder.api.Api;
import com.inzapp.youtubemp3downlaoder.api.ApiResult;

public class Mp3YoutubeDownloadApi implements Api {

	private void test() {
		try {
			String url = "http://mp3-youtube.download/download/access/20200217071937_331e9262-d7b4-45ff-9a01-2bf621ce169e?expires=1581924289&signature=066f07a17ee4c1735ce25aa377d6bc4866caf5ba1cc33f0117f5102a49363e51";
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setDoInput(true);
			conn.setRequestMethod("GET");

			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				sb.append(line).append('\n');
			}
			String redirectHtml = sb.toString();
			System.out.println(redirectHtml);

			String redirectUrl = getRedirectUrlFromHtml(redirectHtml);
			System.out.println(redirectUrl);

			conn = (HttpURLConnection) new URL(redirectUrl).openConnection();
			conn.setDoInput(true);
			conn.setRequestMethod("GET");

			sb = new StringBuilder();
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				sb.append(line).append('\n');
			}
			System.out.println(sb.toString());
		


			byte[] buffer = new byte[8192];
			BufferedInputStream bis = new BufferedInputStream(new URL(redirectUrl).openStream());
			
			while (true) {
				int res = bis.read(buffer, 0, buffer.length);
				if (res == -1) {
					break;
				}
				System.out.println(res);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	} 
	
	private String getRedirectUrlFromHtml(String redirectHtml) {
		String redirectUrlStartStr = "url='";
		int redirectUrlStartIdx = redirectHtml.indexOf(redirectUrlStartStr) + redirectUrlStartStr.length();
		redirectHtml = redirectHtml.substring(redirectUrlStartIdx, redirectHtml.length() - 1);

		String redirectUrlEndStr = "'\"";
		int redirectUrlEndIdx = redirectHtml.indexOf(redirectUrlEndStr);
		redirectHtml = redirectHtml.substring(0, redirectUrlEndIdx);

		String redirectUrl = redirectHtml;
		redirectUrl = redirectUrl.replaceAll("https://", "http://");
		return redirectUrl;
	}

	@Override
	public ApiResult download(String youtubeVideoUrl) {
		test();
		System.out.println("Start api requesting : " + youtubeVideoUrl);
		String uuid = getUuid(youtubeVideoUrl);
		if (uuid == null) {
			System.out.println("ERROR while converting youtube video : invalid youtube video url");
			return ApiResult.SUCCESS;
		}

		boolean bGetTitle = false;
		boolean bGetDuration = false;
		boolean bGetThumbnail = false;
		String fileUrl;
		String fileName = null;
		while (true) {
			JSONObject json = getConvertingStatus(uuid);

			String title = getJsonValue(json, "title");
			if (title != null && !bGetTitle) {
				System.out.println("title : " + title);
				fileName = title;
				bGetTitle = true;
			}

			String duration = getJsonValue(json, "duration");
			if (duration != null && !bGetDuration) {
				System.out.println("duration : " + duration);
				bGetDuration = true;
			}

			String thumbnailUrl = getJsonValue(json, "thumbnail");
			if (thumbnailUrl != null && !bGetThumbnail) {
				System.out.println("thumbnail url : " + thumbnailUrl);
				bGetThumbnail = true;
			}

			String percentage = getJsonValue(json, "percent");
			if (percentage != null) {
				System.out.println("percentage : " + percentage);
			}

			fileUrl = getJsonValue(json, "fileUrl");
			if (getJsonValue(json, "fileUrl") != null) {
				break;
			}

			String newUuid = getJsonValue(json, "uuid");
			try {
				if (!uuid.equals(newUuid)) {
					System.out.println("UUID DIFFER !!!!!");
					uuid = newUuid;
				}
			} catch (Exception e) {
				return ApiResult.FAILURE;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {
			}
		}

		return downloadMp3ViaFileUrl(fileUrl, fileName);
	}

	private String getUuid(String youtubeVideoUrl) {
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
				if (line == null) {
					break;
				}
				sb.append(line).append('\n');
			}
			br.close();

			JSONObject httpRes = new JSONObject(sb.toString());
			return httpRes.getJSONObject("data").getString("uuid");
		} catch (Exception e) {
			return null;
		}
	}

	private JSONObject getConvertingStatus(String uuid) {
		try {
			String url = "https://mp3-youtube.download/download/" + uuid;
			HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setDoInput(true);

			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				sb.append(line).append('\n');
			}
			br.close();
			return new JSONObject(sb.toString());
		} catch (Exception e) {
			return null;
		}
	}

	private String getJsonValue(JSONObject json, String key) {
		try {
			if (key.equals("percent")) {
				return String.valueOf(json.getJSONObject("data").getInt(key));
			} else {
				return json.getJSONObject("data").getString(key);
			}
		} catch (Exception e) {
			return null;
		}
	}

	private ApiResult downloadMp3ViaFileUrl(String fileUrl, String fileName) {
		try {
			System.out.println("downloading mp3 file : " + fileName);
			if (!new File("mp3").exists() && !new File("mp3").isDirectory()) {
				new File("mp3").mkdir();
			}

			FileOutputStream fos = new FileOutputStream(new File("mp3\\" + fileName + ".mp3"));
			BufferedInputStream bis = new BufferedInputStream(new URL(fileUrl).openStream());
			byte[] buffer = new byte[8192];
			while (true) {
				int res = bis.read(buffer, 0, buffer.length);
				if (res == -1) {
					break;
				}
				fos.write(buffer, 0, res);
			}

			fos.flush();
			fos.close();
			System.out.println("***** download success : " + fileName + " *****");
			return ApiResult.SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResult.FAILURE;
		}
	}
}

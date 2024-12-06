package ai_handwriting.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ComparisonService {

    public String compareImages(File handwrittenImage, File generatedImage) throws IOException {
        // 1. Python 서버 URL 설정
        String pythonServerUrl = "http://192.168.200.106:5000/compare";

        // 2. HTTP 요청 생성
        HttpURLConnection connection = (HttpURLConnection) new URL(pythonServerUrl).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data");

        try (OutputStream os = connection.getOutputStream()) {
            // 3. 이미지 데이터 첨부
            writeMultipartData(os, "handwrittenImage", handwrittenImage);
            writeMultipartData(os, "generatedImage", generatedImage);
        }

        // 4. Python 서버 응답 처리
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }

        return response.toString();
    }

    private void writeMultipartData(OutputStream os, String paramName, File file) throws IOException {
        // 파일을 멀티파트로 작성하는 코드 구현
    }
}

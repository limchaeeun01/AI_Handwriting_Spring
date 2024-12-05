package ai_handwriting.backend.controller;

import java.nio.file.Paths;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Value("${python.server.url}")  // Python 서버 URL을 application.properties에 설정
    private String pythonServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/")
    public String home() {
        return "OCR API is running!";
    }

    @GetMapping("/test-ocr")
    public String testOcr() {
        try {
            // 테스트용 이미지 파일 경로 (정적으로 설정된 경로)
            Path imagePath = Paths.get("src/main/resources/static/ocr_test_img.jpg");

            // 파일을 바이트 배열로 읽어오기
            byte[] imageBytes = Files.readAllBytes(imagePath);

            // HttpHeaders 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // MultipartEntityBuilder로 파일 추가
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "test-image.jpg"; // 파일 이름 설정
                }
            });

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // Python 서버에 요청 전송
            ResponseEntity<String> response = restTemplate.exchange(
                    pythonServerUrl + "/process-image", // Python 서버의 엔드포인트
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Python 서버에서 받은 텍스트 반환
            System.out.println(response.getBody());
            return response.getBody();
        } catch (Exception e) {
            return "Error processing image: " + e.getMessage();
        }
    }

    @PostMapping("/extract-text")
    public String extractTextFromImage(@RequestParam("file") MultipartFile file) {
        try {
            // 이미지를 Python 서버로 전송하여 텍스트 추출 요청
            byte[] imageBytes = file.getBytes();

            // HttpHeaders 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // MultipartEntityBuilder로 파일 추가 (보통은 MultipartFile로 처리 가능)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename(); // 파일 이름 설정
                }
            });

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // Python 서버에 요청 전송
            ResponseEntity<String> response = restTemplate.exchange(
                    pythonServerUrl + "/process-image", // Python 서버의 엔드포인트
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Python 서버에서 받은 텍스트 반환
            return response.getBody();
        } catch (Exception e) {
            return "Error processing image: " + e.getMessage();
        }
    }
}

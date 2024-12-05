package ai_handwriting.backend.controller;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/handwriting")
public class Controller {

    @PostMapping
    public ResponseEntity<String> uploadFile(
            @RequestParam("handwritingImage") MultipartFile image,
            @RequestParam("fontWritingImage") MultipartFile image2) {

        // 파일 저장
        Path filePath1 = Paths.get("uploads/" + image.getOriginalFilename());
        Path filePath2 = Paths.get("uploads/" + image2.getOriginalFilename());

        try {
            Files.write(filePath1, image.getBytes());
            Files.write(filePath2, image2.getBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 실패");
        }

        // HttpClient로 Python 서버 호출
        try {
            HttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost("http://localhost:5000/process");

            // MultipartEntityBuilder로 요청 구성
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart("image1", new FileBody(filePath1.toFile())) // 첫 번째 이미지
                    .addPart("image2", new FileBody(filePath2.toFile())) // 두 번째 이미지
                    .build();

            // HttpPost에 엔티티 추가
            post.setEntity(entity);

            // 요청 실행
            HttpResponse response = client.execute(post);

            // 응답 확인
            if (response.getStatusLine().getStatusCode() == 200) {
                // 서버 응답을 처리하고 반환
                return ResponseEntity.ok("서버로부터 응답을 받았습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 응답 실패");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Python 서버 호출 실패");
        }
    }

}

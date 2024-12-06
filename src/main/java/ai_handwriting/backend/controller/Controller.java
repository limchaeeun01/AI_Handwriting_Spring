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

import ai_handwriting.backend.service.OcrService;
import ai_handwriting.backend.service.ComparisonService;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/handwriting")
@RequiredArgsConstructor
public class Controller {

    private final OcrService ocrService;
    private final ComparisonService comparisonService;

    @PostMapping("/ocr")
    public ResponseEntity<String> processOcr(@RequestParam("file") MultipartFile file) {
        // 파일 유효성 검사
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("파일이 업로드되지 않았습니다.");
        }
        try {
            // OCR 결과 가져오기
            String ocrResult = ocrService.getOcrResult(file);
            return ResponseEntity.ok(ocrResult); // 성공 시 HTTP 200 상태로 반환
        } catch (IOException e) {
            // 예외 발생 시 로그 출력
            System.err.println("파일 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // 클라이언트에 HTTP 500 상태와 오류 메시지 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            // 기타 예외 처리
            System.err.println("예기치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예기치 못한 오류가 발생했습니다.");
        }
    }

    // @PostMapping("/correction")
    // public ResponseEntity<String> compareHandwriting(
    //         @RequestParam("handwrittenImage") MultipartFile handwrittenImage,
    //         @RequestParam("generatedImage") MultipartFile generatedImage) {
    //     try {
    //         // 서비스 계층에서 비교 로직 수행
    //         String feedback = comparisonService.compareImages(handwrittenImage, generatedImage);
    //         // 피드백 반환
    //         return ResponseEntity.ok(feedback);
    //     } catch (IOException e) {
    //         System.err.println("이미지 처리 중 오류 발생: " + e.getMessage());
    //         e.printStackTrace();
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("이미지 처리 중 오류가 발생했습니다.");
    //     } catch (Exception e) {
    //         System.err.println("예기치 못한 오류 발생: " + e.getMessage());
    //         e.printStackTrace();
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    //                 .body("예기치 못한 오류가 발생했습니다.");
    //     }
    // }
    @PostMapping("/correction")
    public ResponseEntity<String> compareHandwriting(
            @RequestParam("handwrittenImage") MultipartFile handwrittenImage,
            @RequestParam("generatedImage") MultipartFile generatedImage) {
        try {
            // 1. 파일 정보 디버깅
            System.out.println("Handwritten Image: ");
            debugMultipartFile(handwrittenImage);
            System.out.println("Generated Image: ");
            debugMultipartFile(generatedImage);

            // 2. 임시 저장하여 확인
            saveFileToTemp(handwrittenImage, "handwritten_debug.png");
            saveFileToTemp(generatedImage, "generated_debug.png");

            return ResponseEntity.ok("이미지 파일이 성공적으로 수신되었습니다.");
        } catch (IOException e) {
            System.err.println("이미지 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이미지 처리 중 오류가 발생했습니다.");
        } catch (Exception e) {
            System.err.println("예기치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예기치 못한 오류가 발생했습니다.");
        }
    }

    private void debugMultipartFile(MultipartFile file) {
        System.out.println("Original Filename: " + file.getOriginalFilename());
        System.out.println("Content Type: " + file.getContentType());
        System.out.println("File Size: " + file.getSize() + " bytes");
    }

    private void saveFileToTemp(MultipartFile file, String outputFilename) throws IOException {
        Path tempDir = Files.createTempDirectory("debug_files");
        Path outputPath = tempDir.resolve(outputFilename);
        Files.write(outputPath, file.getBytes());
        System.out.println("File saved for debugging: " + outputPath.toAbsolutePath());
    }

}

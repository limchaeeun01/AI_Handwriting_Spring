package ai_handwriting.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import ai_handwriting.backend.domain.OcrResponseDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service

public class OcrService {

    private static final Logger logger = LoggerFactory.getLogger(OcrService.class);

    public String getOcrResult(MultipartFile file) throws IOException {
        // OCR 요청 후 결과 받기
        String ocrResult = sendOcrRequest(file);
        logger.info("OCR Result: {}", ocrResult); // OCR 결과 로깅

        // OCR 결과를 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        OcrResponseDTO ocrResponse = null;
        try {
            ocrResponse = objectMapper.readValue(ocrResult, OcrResponseDTO.class);
        } catch (IOException e) {
            logger.error("OCR 결과 파싱 오류", e);
            throw new RuntimeException("OCR 결과 파싱 오류: " + e.getMessage());
        }

        logger.info("OCRResponseDTO: {}", ocrResponse); // OCRResponseDTO 객체 로깅

        // OCR에서 추출한 텍스트를 하나의 문자열로 합침
        StringBuilder ocrText = new StringBuilder();
        for (OcrResponseDTO.OCRImage image : ocrResponse.getImages()) {
            for (OcrResponseDTO.Field field : image.getFields()) {
                // 현재 ocrText 상태와 field의 inferText를 로그로 출력
                logger.debug("Current ocrText: {}", ocrText.toString()); // ocrText의 상태 로그
                logger.debug("Appending OCR Field Text: {}", field.getInferText()); // field.getInferText() 값 로그

                ocrText.append(field.getInferText()).append(" "); // 텍스트를 공백으로 구분하여 합침

                // 합친 후의 ocrText 상태도 로그로 출력
                logger.debug("Updated ocrText: {}", ocrText.toString()); // 업데이트된 ocrText 상태
            }
        }

        return ocrText.toString();
    }

    public String sendOcrRequest(MultipartFile file) throws IOException {
        String url = "https://5rslwycgnn.apigw.ntruss.com/custom/v1/36600/1fbdd274a0e46c20e6c6760e1af35689f6d302f37f98dcd2a55fa7fdc1cbca77/general";

        // 파일을 Base64 인코딩
        String base64Image = Base64Utils.encodeToString(file.getBytes());
        logger.info("Base64 encoded image (first 100 chars): {}", base64Image.substring(0, 100)); // 처음 100글자만 로깅

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-OCR-SECRET", "S0lMcFdJenZWWVpheGtpcVRURHN4VktGYWtOSXlMREs=");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", "V2");
        requestBody.put("requestId", "1234");
        requestBody.put("timestamp", System.currentTimeMillis());
        requestBody.put("lang", "ko");
        requestBody.put("images", List.of(Map.of(
                "format", "jpg",
                "name", "uploaded_image",
                "data", base64Image)));
        requestBody.put("enableTableDetection", false);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();
            return responseBody;
        } catch (Exception e) {
            logger.error("OCR 요청 중 오류 발생", e);
            throw new RuntimeException("OCR 요청 중 오류 발생: " + e.getMessage());
        }
    }

}

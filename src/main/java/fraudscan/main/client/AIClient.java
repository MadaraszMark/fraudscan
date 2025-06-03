package fraudscan.main.client;

import fraudscan.main.dto.TransactionRequestDto;
import fraudscan.main.dto.FraudResponseDto;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AIClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String URL = "http://127.0.0.1:8000/predict";

    public boolean isFraudulent(TransactionRequestDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<TransactionRequestDto> request = new HttpEntity<>(dto, headers);

        try {
            ResponseEntity<FraudResponseDto> response =
                    restTemplate.postForEntity(URL, request, FraudResponseDto.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getFraud() == 1;
            } else {
                System.err.println("⚠️ AI válasz nem megfelelő: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Hiba az AI hívás során: " + e.getMessage());
        }

        return false;
    }
}



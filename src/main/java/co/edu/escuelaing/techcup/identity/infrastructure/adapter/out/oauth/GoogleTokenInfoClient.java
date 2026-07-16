package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.oauth;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "google-oauth", url = "https://oauth2.googleapis.com")
public interface GoogleTokenInfoClient {

    @GetMapping("/tokeninfo")
    Map<String, Object> getTokenInfo(@RequestParam("id_token") String idToken);
}

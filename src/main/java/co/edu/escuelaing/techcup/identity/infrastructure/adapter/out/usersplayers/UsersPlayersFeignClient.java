package co.edu.escuelaing.techcup.identity.infrastructure.adapter.out.usersplayers;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "users-players-service", url = "${users.service.base-url}")
public interface UsersPlayersFeignClient {

    @GetMapping("/internal/players/{userId}/profile")
    PlayerProfileResponse getProfile(@PathVariable("userId") UUID userId);
}

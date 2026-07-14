package co.edu.escuelaing.techcup.identity.service;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class RandomTemporaryPasswordGenerator implements TemporaryPasswordGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
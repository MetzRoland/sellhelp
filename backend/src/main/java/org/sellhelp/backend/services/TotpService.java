package org.sellhelp.backend.services;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.*;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    private final CodeVerifier verifier;
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();

    public TotpService() {
        DefaultCodeVerifier codeVerifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(),
                new SystemTimeProvider()
        );

        codeVerifier.setAllowedTimePeriodDiscrepancy(0);

        this.verifier = codeVerifier;
    }

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public boolean verify(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}


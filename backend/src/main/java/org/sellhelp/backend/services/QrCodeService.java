package org.sellhelp.backend.services;

import org.sellhelp.backend.security.QrCodeUtil;
import org.springframework.stereotype.Service;

@Service
public class QrCodeService {

    public String generateQrBase64(String data) {
        try {
            return QrCodeUtil.generateBase64QrCode(data, 300, 300);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
}

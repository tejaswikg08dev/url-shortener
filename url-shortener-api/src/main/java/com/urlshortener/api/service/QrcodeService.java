package com.urlshortener.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrcodeService {

    public byte[] generateQrcode(String url, int size){
        try{
            QRCodeWriter writer = new QRCodeWriter();

            BitMatrix bitMatrix = writer.encode(url, BarcodeFormat.QR_CODE, size, size);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return outputStream.toByteArray();

        } catch(Exception e){
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

}

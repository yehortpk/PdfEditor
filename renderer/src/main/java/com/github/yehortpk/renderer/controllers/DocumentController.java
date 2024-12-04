package com.github.yehortpk.renderer.controllers;

import com.github.yehortpk.renderer.services.HashService;
import com.github.yehortpk.renderer.services.RenderService;
import jakarta.servlet.http.HttpSession;
import lombok.Cleanup;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DocumentController {
    @PostMapping("/process")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<Map<String, String>> processFile(@RequestParam("file") MultipartFile file, HttpSession session)
            throws IOException, TransformerException {

        String hashedFilename =
                new HashService(session.getId() + file.getOriginalFilename()).getHashValue();
        session.setAttribute("file-hash", hashedFilename);

        RenderService renderService = new RenderService(file.getInputStream());
        renderService.savePdf(hashedFilename);

        String generatedRedirectUrl = "/file/" + hashedFilename; // This could be dynamically generated

        // Create a response object with the redirect URL
        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", generatedRedirectUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/file/{file_url}")
    @CrossOrigin(origins = "http://localhost:4200")
    public ResponseEntity<byte[]> getDocument(@PathVariable("file_url") String fileURL)
            throws IOException, TransformerException {

        @Cleanup RenderService renderService = new RenderService(fileURL);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        @Cleanup ByteArrayOutputStream outputStream = renderService.renderDOMToHTML();

        return new ResponseEntity<>(
                outputStream.toByteArray(),
                headers,
                HttpStatus.OK
        );
    }
}

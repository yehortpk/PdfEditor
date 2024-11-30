package com.github.yehortpk.renderer.controllers;

import com.github.yehortpk.renderer.services.HashService;
import com.github.yehortpk.renderer.services.RenderService;
import jakarta.servlet.http.HttpServletResponse;
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

@RestController
public class DocumentController {
    @PostMapping("/process")
    public void  processFile(@RequestParam("file") MultipartFile file, HttpSession session, HttpServletResponse response)
            throws IOException, TransformerException {

        String hashedFilename =
                new HashService(session.getId() + file.getOriginalFilename()).getHashValue();
        session.setAttribute("file-hash", hashedFilename);

        RenderService renderService = new RenderService(file.getInputStream());
        renderService.savePdf(hashedFilename);

        response.sendRedirect("/file/" + hashedFilename);
    }

    @GetMapping("/file/{file_url}")
    public ResponseEntity<byte[]> getDocument(@PathVariable("file_url") String fileURL, HttpSession session)
            throws IOException, TransformerException {

        if (!session.getAttribute("file-hash").equals(fileURL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied".getBytes());
        }

        RenderService renderService = new RenderService(fileURL);

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

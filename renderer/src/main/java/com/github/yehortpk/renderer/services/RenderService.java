package com.github.yehortpk.renderer.services;

import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.PDFDomTree;
import org.w3c.dom.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RenderService implements AutoCloseable {
    private static final String RESOURCE_DOCUMENTS_FILEPATH = "documents";

    @Getter
    private final Document DOM;
    private final Transformer transformer;
    private final PDDocument pdf;

    public RenderService(InputStream file) throws IOException, TransformerConfigurationException {
        pdf = PDDocument.load(file);

        DOM = createDOM(pdf);
        transformer = createTransformer(DOM);
    }

    public RenderService(String fileUrl) throws IOException, TransformerConfigurationException {
        String documentAbsoluteFilePath = createAbsolutePDFFilepath(fileUrl);
        File file = new File(documentAbsoluteFilePath);
        pdf = PDDocument.load(file);

        DOM = createDOM(pdf);
        transformer = createTransformer(DOM);
    }

    private Transformer createTransformer(Document doc) throws TransformerConfigurationException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        DOMImplementation domImpl = doc.getImplementation();
        DocumentType doctype = domImpl.createDocumentType("doctype",
                "-//W3C//DTD XHTML 1.0 Strict//EN",
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());

        Node html = doc.getChildNodes().item(1);
        Element head = (Element) html.getFirstChild();
        Element style = doc.createElement("style");
        style.setTextContent("@page {size: A4; margin:0; padding: 0;} body{margin: 0;} .page{margin:0; border: none;}");
        head.appendChild(style);

        return transformer;
    }

    public ByteArrayOutputStream renderDOMToHTML() throws TransformerException {
        DOMSource domSource = new DOMSource(DOM);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        transformer.transform(domSource, new StreamResult(outputStream));
        return outputStream;
    }

    private String createAbsolutePDFFilepath(String fileUrl) {
        String projectRoot = System.getProperty("user.dir");

        Path uploadPath = Paths.get(projectRoot, RESOURCE_DOCUMENTS_FILEPATH);

        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String finalPath = uploadPath + "/" + fileUrl;


        if(!fileUrl.endsWith(".pdf")) {
            finalPath += ".pdf";
        }

        return finalPath;
    }

    private Document createDOM(PDDocument pdf) throws IOException {
        PDFDomTree tree = new PDFDomTree();

        return tree.createDOM(pdf);
    }

    public void savePdf(String fileURL) throws IOException {
        pdf.save(createAbsolutePDFFilepath(fileURL));
    }

    @Override
    public void close() throws Exception {
        pdf.close();
    }
}

package org.xlsx.validator.services;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Singleton;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class FileService {

    @ConfigProperty(name = "temp.directory")
    String TEMP_DIR;

    public List<String> uploadFiles(String path, MultipartFormDataInput input) throws Exception {
        List<String> filePaths = new ArrayList<>();
        List<InputPart> inputParts = getInputParts(input);
        for (InputPart inputPart : inputParts) {
            MultivaluedMap<String, String> header = inputPart.getHeaders();
            String fileName = getFileName(header);
            InputStream inputStream = inputPart.getBody(InputStream.class, null);
            File file = writeFile(path, inputStream, fileName);
            filePaths.add(file.getAbsolutePath());
        }
        return filePaths;
    }

    public List<String> getFileNames(MultipartFormDataInput input) {
        List<String> fileNames = new ArrayList<>();
        List<InputPart> inputParts = getInputParts(input);
        String fileName;
        for (InputPart inputPart : inputParts) {
            MultivaluedMap<String, String> header = inputPart.getHeaders();
            fileName = getFileName(header);
            fileNames.add(fileName);
        }
        return fileNames;
    }

    private static List<InputPart> getInputParts(MultipartFormDataInput input) {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> inputParts = uploadForm.get("file");
        if (inputParts == null) {
            throw new RuntimeException("No form key 'file' was found!");
        }
        return inputParts;
    }

    public File writeFile(String path, InputStream inputStream, String fileName) throws Exception {
        if (path.endsWith(".xlsx")) {
            throw new RuntimeException("Cannot process files that are not of type '.xlsx'!");
        }
        byte[] bytes = IOUtils.toByteArray(inputStream);
        File customDir = new File(path);
        fileName = customDir.getAbsolutePath() + File.separator + fileName;
        Path filePath = Paths.get(fileName);
        try{
            return Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW).toFile();
        } catch (FileAlreadyExistsException e) {
            // Delete if temp file was not cleaned up
            if(path.equals(TEMP_DIR)){
                File file = this.getFile(path, fileName);
                if(!file.delete()){
                    throw e;
                }
            }
        }
        return Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW).toFile();
    }

    public void copyFileUsingStream(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    public File getFile(String path, String fileName) {
        return getFileByPath(path + fileName);
    }

    public File getFileByPath(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.
                getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                return name[1].trim().replaceAll("\"", "");
            }
        }
        return "";
    }
}
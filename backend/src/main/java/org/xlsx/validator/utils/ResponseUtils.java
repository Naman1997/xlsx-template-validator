package org.xlsx.validator.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.xlsx.validator.services.FileService;
import org.xlsx.validator.services.ResponseService;
import org.xlsx.validator.services.XlsxService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ResponseUtils {
    @ConfigProperty(name = "consolidation.directory")
    String CONSOLIDATION_DIR;
    @ConfigProperty(name = "temp.directory")
    String TEMP_DIR;
    @ConfigProperty(name = "template.directory")
    String TEMPLATE_DIR;
    private final static String CONSOLIDATED_EXTENSION = "Consolidated-";
    @Inject
    FileService fileService;
    @Inject
    ResponseService responseService;
    @Inject
    XlsxService xlsxService;

    public Response listFiles(String dir) {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            Set<String> files = stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(fileName -> "\"" + fileName + "\"")
                    .collect(Collectors.toSet());
            return responseService.responseOk(files);
        } catch (Exception e) {
            return responseService.serverException(e);
        }
    }

    public Response deleteFile(String fileName, String directoryPath) {
        File file = fileService.getFile(directoryPath, fileName);
        if (file == null) {
            return responseService.badRequest("Unable to find file with name: " + fileName);
        }
        if (!file.delete()) {
            return responseService.badRequest("Unable to delete file with name: " + fileName);
        }
        return responseService.responseOk("Successfully deleted file: " + fileName);
    }

    public Response downloadFile(String fileName, String directoryPath) {
        File fileDownload = fileService.getFile(directoryPath, fileName);
        if (!fileDownload.exists()) {
            return responseService.badRequest("Unable to find file with name: " + fileName);
        }
        return responseService.downloadResponse(fileName, fileDownload);
    }

    public Response uploadFiles(String directoryPath, MultipartFormDataInput input) {
        try {
            List<String> filesToUpload = fileService.getFileNames(input);
            validateAndUploadFiles(filesToUpload, input, directoryPath);
            return responseService.responseOk(filesToUpload);
        } catch (Exception e) {
            return responseService.serverException(e);
        }
    }

    public Response consolidateFiles(MultipartFormDataInput input, String templateName, Boolean isMerged) {
        if (StringUtils.isEmpty(templateName)) {
            return responseService.badRequest("Template name cannot be empty");
        } else if (!templateName.endsWith(".xlsx")) {
            return responseService.badRequest("Template file: '" + templateName + "' needs to be of type '.xlsx'!");
        } else if (isMerged == null) {
            return responseService.badRequest("isMerged name cannot be empty");
        }

        // Find the consolidated and template files
        String consolidatedFilename = CONSOLIDATED_EXTENSION + templateName;
        File consolidatedFile = fileService.getFile(CONSOLIDATION_DIR, consolidatedFilename);
        boolean tempConsolidationCreated = false;
        List<String> filesToMerge = new ArrayList<>();

        try {
            // Upload each file in a temp dir
            List<String> filesToUpload = fileService.getFileNames(input);
            List<String> filePaths = validateAndUploadFiles(filesToUpload, input, TEMP_DIR);
            filesToMerge.addAll(filePaths);


            // Validate that the template file exists
            File templateFile = fileService.getFile(TEMPLATE_DIR, templateName);
            if (templateFile == null) {
                return responseService.badRequest("Template file: '" + templateName + "' does not exist!");
            }

            // If isMerged is true, find the consolidated file for that template - fail if no consolidated file found
            if (consolidatedFile == null) {
                if (isMerged) {
                    cleanupTempFiles(consolidatedFilename, null, false, filesToMerge);
                    return responseService.badRequest("Unable to merge as consolidated file does not exist for template: " + templateName);
                } else {
                    consolidatedFile = fileService.writeFile(CONSOLIDATION_DIR, InputStream.nullInputStream(), consolidatedFilename);
                    fileService.copyFileUsingStream(templateFile, consolidatedFile);
                    tempConsolidationCreated = true;
                }
            }

            // Validate template and extract headers
            Sheet templateWorksheet = xlsxService.validateTemplate(templateFile.getAbsolutePath());
            Map<Integer, String> templateHeaders = xlsxService.getHeadersFromFirstRow(templateWorksheet);

            // Start copying the consolidated file items to the temp template first if it exists
            for (String filePath : filesToMerge) {
                XSSFWorkbook consolidatedWorkbook = xlsxService.copyFileUsingTemplate(filePath, consolidatedFile.getAbsolutePath(), templateHeaders);
                OutputStream outputStream = new FileOutputStream(consolidatedFile.getAbsolutePath());
                consolidatedWorkbook.write(outputStream);
                consolidatedWorkbook.close();
            }

            // Cleanup temp files and exit
            // Consolidated File is no longer temporary at this point
            cleanupTempFiles(consolidatedFilename, consolidatedFile, false, filesToMerge);
            return responseService.responseOk("Successfully created consolidated report: " + consolidatedFilename);
        } catch (Exception e) {
            cleanupTempFiles(consolidatedFilename, consolidatedFile, tempConsolidationCreated, filesToMerge);
            return responseService.serverException(e);
        }
    }

    private List<String> validateAndUploadFiles(List<String> filesToUpload, MultipartFormDataInput input, String directory) throws Exception {
        boolean inValidFileType = filesToUpload.stream().anyMatch(f -> !f.endsWith(".xlsx"));
        if (inValidFileType) {
            throw new RuntimeException("Cannot process files that are not of type 'xlsx'!");
        }
        return fileService.uploadFiles(directory, input);
    }

    private void cleanupTempFiles(String consolidatedFilename, File consolidatedFile, boolean tempConsolidationCreated, List<String> filesToMerge) {
        if (tempConsolidationCreated) {
            boolean delete = consolidatedFile.delete();
            System.out.println("Deleted temp consolidated file " + consolidatedFilename + ": " + delete);
        }
        filesToMerge.forEach(filePath -> {
            File tempFile = fileService.getFileByPath(filePath);
            boolean delete = tempFile.delete();
            System.out.println("Deleted temp file " + filePath + ": " + delete);
        });
    }
}

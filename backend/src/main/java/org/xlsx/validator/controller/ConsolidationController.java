package org.xlsx.validator.controller;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.xlsx.validator.utils.ResponseUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/consolidation")
public class ConsolidationController {

    @ConfigProperty(name = "consolidation.directory")
    String CONSOLIDATION_DIR;
    @Inject
    ResponseUtils responseUtils;

    @GET
    @Path("/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadTemplate(@PathParam("fileName") String fileName) {
        return responseUtils.downloadFile(fileName, CONSOLIDATION_DIR);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public Response listTemplates() {
        return responseUtils.listFiles(CONSOLIDATION_DIR);
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadFilesForConsolidation(@MultipartForm MultipartFormDataInput input, @QueryParam("templateName") String templateName, @QueryParam("isMerged") Boolean isMerged) {
        return responseUtils.consolidateFiles(input, templateName, isMerged);
    }

    @DELETE
    @Path("/delete/{fileName}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteTemplate(@PathParam("fileName") String fileName) {
        return responseUtils.deleteFile(fileName, CONSOLIDATION_DIR);
    }
}

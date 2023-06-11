package org.xlsx.validator.controller;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.xlsx.validator.utils.ResponseUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/template")
public class TemplateController {

    @ConfigProperty(name = "template.directory")
    String TEMPLATE_DIR;
    @Inject
    ResponseUtils responseUtils;

    @GET
    @Path("/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadTemplate(@PathParam("fileName") String fileName) {
        return responseUtils.downloadFile(fileName, TEMPLATE_DIR);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public Response listTemplates() {
        return responseUtils.listFiles(TEMPLATE_DIR);
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadTemplates(@MultipartForm MultipartFormDataInput input) {
        return responseUtils.uploadFiles(TEMPLATE_DIR, input);
    }

    @DELETE
    @Path("/delete/{fileName}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteTemplate(@PathParam("fileName") String fileName) {
        return responseUtils.deleteFile(fileName, TEMPLATE_DIR);
    }
}

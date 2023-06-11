package org.xlsx.validator.services;
import org.apache.commons.lang3.StringUtils;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import java.io.File;

@Singleton
public class ResponseService {

    public Response responseOk(Object entity) {
        return Response.ok().entity(entity).build();
    }
    public Response downloadResponse(String fileName, File fileDownload) {
        Response.ResponseBuilder response = Response.ok(fileDownload);
        response.header("Content-Disposition", "attachment;filename=" + fileName);
        return response.build();
    }
    public Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
    }
    public Response serverException(Exception e) {
        e.printStackTrace();
        if(StringUtils.isNotEmpty(e.getMessage())){
            return Response.serverError().entity("An exception occurred: " + e.getMessage()).build();
        }
        return Response.serverError().entity("An exception occurred: " + e.getCause()).build();
    }
}

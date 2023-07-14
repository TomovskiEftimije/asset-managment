package org.asset.resource;


import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.asset.dto.*;
import org.asset.model.Asset;
import org.asset.model.AssetAttribute;
import org.asset.model.AssetLink;
import org.asset.repository.AssetAttributeRepository;
import org.asset.repository.AssetLinkRepository;
import org.asset.repository.AssetRepository;
import org.asset.service.AssetService;
import org.asset.service.AuthenticationService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/assets")
public class AssetResource {

    @Inject
    AuthenticationService authenticationService;

    @Inject
    AssetService resourceService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AssetDto> getAllAssets() {
        return resourceService.getAllAssets();
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAssetById(@PathParam("id") Long id) {
        try {
            AssetDto assetDto = resourceService.getAssetById(id);
            return Response.ok(assetDto).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAsset(AssetDto assetDto) {
        return resourceService.createAsset(assetDto);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAsset(@PathParam("id") Long id, AssetDto updatedAsset) {
        try {
            Asset asset = resourceService.updateAsset(id, updatedAsset);
            return Response.ok(asset).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{id}/attributes")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAssetAttribute(@PathParam("id") Long assetId, AssetAttributeDto attributeDto) {
        try {
            return resourceService.addAssetAttribute(assetId, attributeDto);
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }


    @GET
    @Path("/{id}/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssetAttributes(@PathParam("id") Long assetId) {
        try {
            List<AssetAttributeDto> attributeDtos = resourceService.getAssetAttributes(assetId);
            return Response.ok(attributeDtos).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/connected-assets")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnectedAssets(@PathParam("id") Long assetId) {
        try {
            List<ConnectedAssetDto> connectedAssets = resourceService.getConnectedAssets(assetId);
            return Response.ok(connectedAssets).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/export")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportAssetToExcel(@PathParam("id") Long assetId) {
        try {
            return resourceService.exportAssetToExcel(assetId);
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) {
        try {
            String token = authenticationService.login(loginRequest.getEmail(), loginRequest.getPassword());
            return Response.ok(new AuthResponse(token)).build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(RegisterRequest registerRequest) {
        try {
            authenticationService.registerUser(registerRequest.getEmail(), registerRequest.getPassword());
            return Response.ok("Registracija je uspela.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

}

package org.asset.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.asset.dto.AssetAttributeDto;
import org.asset.dto.AssetDto;
import org.asset.dto.AssetLinkDto;
import org.asset.dto.ConnectedAssetDto;
import org.asset.model.Asset;
import org.asset.model.AssetAttribute;
import org.asset.model.AssetLink;
import org.asset.repository.AssetAttributeRepository;
import org.asset.repository.AssetLinkRepository;
import org.asset.repository.AssetRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AssetService {

    @Inject
    private AssetRepository assetRepository;
    @Inject
    private AssetAttributeRepository attributeRepository;

    @Inject
    private AssetLinkRepository assetLinkRepository;


    public List<AssetDto> getAllAssets() {
        List<Asset> assets = assetRepository.listAll();
        return mapToDtoList(assets);
    }

    public AssetDto getAssetById(Long id) {
        Asset asset = assetRepository.findById(id);
        if (asset == null) {
            throw new NotFoundException("Asset not found");
        }
        return mapToDto(asset);
    }

    @Transactional
    public Response createAsset(AssetDto assetDto) {
        Asset asset = mapToEntity(assetDto);

        for (AssetAttribute attribute : asset.getAttributes()) {
            attributeRepository.persist(attribute);
        }

        for (AssetLink link : asset.getOutgoingLinks()) {
            assetLinkRepository.persist(link);
        }
        for (AssetLink link : asset.getIncomingLinks()) {
            assetLinkRepository.persist(link);
        }

        assetRepository.persist(asset);

        return Response.status(Response.Status.CREATED).entity(mapToDto(asset)).build();
    }

    @Transactional
    public Asset updateAsset(Long id, AssetDto updatedAsset) {
        Asset asset = assetRepository.findById(id);

        if (asset == null) {
            throw new NotFoundException("Asset not found");
        }

        asset.setTitle(updatedAsset.getTitle());

        List<AssetLink> updatedOutgoingLinks = mapToEntityLinkList(updatedAsset.getOutgoingLinks());
        List<AssetLink> outgoingLinksToRemove = new ArrayList<>();
        for (AssetLink existingOutgoingLink : asset.getOutgoingLinks()) {
            if (!updatedOutgoingLinks.contains(existingOutgoingLink)) {
                outgoingLinksToRemove.add(existingOutgoingLink);
            }
        }
        asset.getOutgoingLinks().removeAll(outgoingLinksToRemove);
        asset.getOutgoingLinks().addAll(updatedOutgoingLinks);

        List<AssetLink> updatedIncomingLinks = mapToEntityLinkList(updatedAsset.getIncomingLinks());
        List<AssetLink> incomingLinksToRemove = new ArrayList<>();
        for (AssetLink existingIncomingLink : asset.getIncomingLinks()) {
            if (!updatedIncomingLinks.contains(existingIncomingLink)) {
                incomingLinksToRemove.add(existingIncomingLink);
            }
        }
        asset.getIncomingLinks().removeAll(incomingLinksToRemove);
        asset.getIncomingLinks().addAll(updatedIncomingLinks);

        List<AssetAttribute> attributeList = mapToEntityAttributeList(updatedAsset.getAttributes());
        List<AssetAttribute> savedAttributes = new ArrayList<>();
        for (AssetAttribute attribute : attributeList) {
            if (attribute.getId() == null) {
                attributeRepository.persist(attribute);
                savedAttributes.add(attribute);
            } else {
                savedAttributes.add(attribute);
            }
        }
        asset.setAttributes(savedAttributes);

        return asset;
    }

    @Transactional
    public Response addAssetAttribute(Long assetId, AssetAttributeDto attributeDto) {
        Asset asset = assetRepository.findById(assetId);

        if (asset == null) {
            throw new NotFoundException("Asset not found");
        }

        AssetAttribute attribute = mapToEntity(attributeDto);
        attributeRepository.persist(attribute);

        assetRepository.addAttributeToAsset(asset, attribute);

        return Response.status(Response.Status.CREATED).entity(mapToDto(asset)).build();
    }

    public List<AssetAttributeDto> getAssetAttributes(Long assetId) {
        Asset asset = assetRepository.findById(assetId);
        if (asset == null) {
            throw new NotFoundException("Asset not found");
        }
        List<AssetAttribute> attributes = asset.getAttributes();
        return mapToDtoAttributeList(attributes);
    }

    public List<ConnectedAssetDto> getConnectedAssets(Long assetId) {
        Asset asset = assetRepository.findById(assetId);
        if (asset == null) {
            throw new NotFoundException("Asset not found");
        }
        List<AssetLink> outgoingLinks = asset.getOutgoingLinks();
        List<AssetLink> incomingLinks = asset.getIncomingLinks();

        List<ConnectedAssetDto> connectedAssets = new ArrayList<>();

        for (AssetLink outgoingLink : outgoingLinks) {
            connectedAssets.add(mapToConnectedAssetDto(outgoingLink.getTargetAsset()));
        }

        for (AssetLink incomingLink : incomingLinks) {
            connectedAssets.add(mapToConnectedAssetDto(incomingLink.getSourceAsset()));
        }

        return connectedAssets;
    }

    public Response exportAssetToExcel(Long assetId) {
        Asset asset = assetRepository.findById(assetId);

        if (asset == null) {
            throw new NotFoundException("Asset not found");
        }

        List<AssetAttributeDto> assetDetails = asset.getAttributes().stream()
                .map(attribute -> {
                    AssetAttributeDto detailsDto = new AssetAttributeDto();
                    detailsDto.setName(attribute.getName());
                    detailsDto.setType(attribute.getType());
                    detailsDto.setRequired(attribute.isRequired());
                    detailsDto.setMinValue(attribute.getMinValue());
                    detailsDto.setMaxValue(attribute.getMaxValue());
                    return detailsDto;
                })
                .collect(Collectors.toList());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Asset Attributes");

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Attribute");
            headerRow.createCell(1).setCellValue("Type");
            headerRow.createCell(2).setCellValue("Required");
            headerRow.createCell(3).setCellValue("Min value");
            headerRow.createCell(4).setCellValue("Max value");

            int rowNum = 1;
            for (AssetAttributeDto detailsDto : assetDetails) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(detailsDto.getName());
                row.createCell(1).setCellValue(detailsDto.getType());
                row.createCell(2).setCellValue(detailsDto.isRequired());
                row.createCell(3).setCellValue(detailsDto.getMinValue());
                row.createCell(4).setCellValue(detailsDto.getMaxValue());
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);

            return Response.ok(baos.toByteArray()).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Metode za preslikavo entitet v DTO-je
    private AssetDto mapToDto(Asset asset) {
        AssetDto assetDto = new AssetDto();
        assetDto.setTitle(asset.getTitle());
        assetDto.setAttributes(mapToDtoAttributeList(asset.getAttributes()));
        assetDto.setOutgoingLinks(mapToDtoLinkList(asset.getOutgoingLinks()));
        assetDto.setIncomingLinks(mapToDtoLinkList(asset.getIncomingLinks()));
        return assetDto;
    }

    private List<AssetDto> mapToDtoList(List<Asset> assets) {
        return assets.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private AssetAttributeDto mapToDto(AssetAttribute attribute) {
        AssetAttributeDto attributeDto = new AssetAttributeDto();
        attributeDto.setName(attribute.getName());
        attributeDto.setType(attribute.getType());
        attributeDto.setRequired(attribute.isRequired());
        attributeDto.setMinValue(attribute.getMinValue());
        attributeDto.setMaxValue(attribute.getMaxValue());
        return attributeDto;
    }

    private List<AssetAttributeDto> mapToDtoAttributeList(List<AssetAttribute> attributes) {
        return attributes.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private AssetLinkDto mapToDto(AssetLink link) {
        AssetLinkDto linkDto = new AssetLinkDto();
        linkDto.setId(link.getId());
        linkDto.setSourceAssetId(link.getSourceAsset().getId());
        linkDto.setTargetAssetId(link.getTargetAsset().getId());
        return linkDto;
    }

    private List<AssetLinkDto> mapToDtoLinkList(List<AssetLink> links) {
        return links.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ConnectedAssetDto mapToConnectedAssetDto(Asset asset) {
        ConnectedAssetDto connectedAssetDto = new ConnectedAssetDto();
        connectedAssetDto.setId(asset.getId());
        connectedAssetDto.setTitle(asset.getTitle());
        return connectedAssetDto;
    }

    // Metode za preslikavo entitet v DTO-je
    private Asset mapToEntity(AssetDto assetDto) {
        Asset asset = new Asset();
        asset.setTitle(assetDto.getTitle());
        asset.setAttributes(mapToEntityAttributeList(assetDto.getAttributes()));
        asset.setOutgoingLinks(mapToEntityLinkList(assetDto.getOutgoingLinks()));
        asset.setIncomingLinks(mapToEntityLinkList(assetDto.getIncomingLinks()));
        return asset;
    }

    private List<AssetAttribute> mapToEntityAttributeList(List<AssetAttributeDto> attributeDtos) {
        return attributeDtos.stream().map(this::mapToEntity).collect(Collectors.toList());
    }

    private AssetAttribute mapToEntity(AssetAttributeDto attributeDto) {
        AssetAttribute attribute = new AssetAttribute();
        attribute.setName(attributeDto.getName());
        attribute.setType(attributeDto.getType());
        attribute.setRequired(attributeDto.isRequired());
        attribute.setMinValue(attributeDto.getMinValue());
        attribute.setMaxValue(attributeDto.getMaxValue());
        return attribute;
    }

    private List<AssetLink> mapToEntityLinkList(List<AssetLinkDto> linkDtos) {
        return linkDtos.stream().map(this::mapToEntity).collect(Collectors.toList());
    }

    private AssetLink mapToEntity(AssetLinkDto linkDto) {
        AssetLink link = new AssetLink();
        link.setId(linkDto.getId());
        link.setSourceAsset(assetRepository.findById(linkDto.getSourceAssetId()));
        link.setTargetAsset(assetRepository.findById(linkDto.getTargetAssetId()));
        return link;
    }

}

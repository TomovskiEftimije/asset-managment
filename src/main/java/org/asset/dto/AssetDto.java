package org.asset.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssetDto {

    private String title;

    private List<AssetAttributeDto> attributes;

    private List<AssetLinkDto> outgoingLinks;

    private List<AssetLinkDto> incomingLinks;

}

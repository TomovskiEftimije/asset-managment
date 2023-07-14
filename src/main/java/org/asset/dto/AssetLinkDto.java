package org.asset.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssetLinkDto {

    private Long id;

    private Long sourceAssetId;

    private Long targetAssetId;

}

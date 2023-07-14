package org.asset.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.asset.model.Asset;
import org.asset.model.AssetAttribute;

@ApplicationScoped
public class AssetRepository  implements PanacheRepository<Asset> {

    @Transactional
    public void addAttributeToAsset(Asset asset, AssetAttribute attribute) {
        asset.getAttributes().add(attribute);
        getEntityManager().merge(asset);
    }

}

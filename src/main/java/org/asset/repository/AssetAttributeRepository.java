package org.asset.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.asset.model.Asset;
import org.asset.model.AssetAttribute;

@ApplicationScoped
public class AssetAttributeRepository implements PanacheRepository<AssetAttribute> {
}

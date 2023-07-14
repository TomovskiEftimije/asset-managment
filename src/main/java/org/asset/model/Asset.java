package org.asset.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToMany
    private List<AssetAttribute> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "sourceAsset", cascade = CascadeType.ALL)
    private List<AssetLink> outgoingLinks = new ArrayList<>();

    @OneToMany(mappedBy = "targetAsset", cascade = CascadeType.ALL)
    private List<AssetLink> incomingLinks = new ArrayList<>();

}

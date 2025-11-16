package org.sellhelp.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "counites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class County {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "county_name", nullable = false, unique = true)
    private String countyName;

    @OneToMany(mappedBy = "county")
    private List<City> cities;
}

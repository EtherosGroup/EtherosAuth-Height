package cn.skilfully.etheros.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Entity
@Table(name = "player_location",
        indexes = {
                @Index(name = "idx_world", columnList = "world")
        })
@Accessors(chain = true)
public class PlayerLocationEntity {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 255)
    private String world = "world";

    @Column(nullable = false)
    private Double x = 0.0;

    @Column(nullable = false)
    private Double y = 0.0;

    @Column(nullable = false)
    private Double z = 0.0;

    @Column(nullable = false)
    private Float yaw = 0F;

    @Column(nullable = false)
    private Float pitch = 0F;

}

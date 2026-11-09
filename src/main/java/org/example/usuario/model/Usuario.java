package org.example.usuario.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.agencia.model.Agencia;
import org.example.cliente.model.Persona;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "persona_id", nullable = false, unique = true)
    private Persona persona;  // ajusta al nombre de tu entidad de persona

    @ManyToOne
    @JoinColumn(name = "agencia_id")
    private Agencia agencia;  // puede ser null

    @Column(nullable = false, unique = true, length = 70)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();
}
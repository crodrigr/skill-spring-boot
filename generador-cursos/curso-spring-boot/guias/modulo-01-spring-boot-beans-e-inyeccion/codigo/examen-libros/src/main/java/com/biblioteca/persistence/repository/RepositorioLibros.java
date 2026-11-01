package com.biblioteca.persistence.repository;

import com.biblioteca.negocio.model.Libro;
import java.util.List;
import java.util.Optional;

public interface RepositorioLibros {

    List<Libro> listar();

    Optional<Libro> buscarPorId(Long id);
}

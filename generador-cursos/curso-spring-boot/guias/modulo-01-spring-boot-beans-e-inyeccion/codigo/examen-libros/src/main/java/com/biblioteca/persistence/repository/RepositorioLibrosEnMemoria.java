package com.biblioteca.persistence.repository;

import com.biblioteca.negocio.model.Libro;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioLibrosEnMemoria implements RepositorioLibros {

    private final List<Libro> libros = new ArrayList<>();

    public RepositorioLibrosEnMemoria() {
        libros.add(new Libro(1L, "Cien años de soledad", "Gabriel García Márquez", 1967));
        libros.add(new Libro(2L, "El amor en los tiempos del cólera", "Gabriel García Márquez", 1985));
        libros.add(new Libro(3L, "La ciudad y los perros", "Mario Vargas Llosa", 1963));
        libros.add(new Libro(4L, "Rayuela", "Julio Cortázar", 1963));
        libros.add(new Libro(5L, "Ficciones", "Jorge Luis Borges", 1944));
    }

    @Override
    public List<Libro> listar() {
        return new ArrayList<>(libros);
    }

    @Override
    public Optional<Libro> buscarPorId(Long id) {
        for (Libro libro : libros) {
            if (libro.getId().equals(id)) {
                return Optional.of(libro);
            }
        }
        return Optional.empty();
    }
}

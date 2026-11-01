package com.biblioteca.negocio.model;

public class Libro {

    private final Long id;
    private final String titulo;
    private final String autor;
    private final int anioPublicacion;

    public Libro(Long id, String titulo, String autor, int anioPublicacion) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.anioPublicacion = anioPublicacion;
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public int getAnioPublicacion() { return anioPublicacion; }
}

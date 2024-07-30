package com.alura.Screenmatch.dto;

import com.alura.Screenmatch.model.Categoria;

public record SerieDTO(Long id,String titulo, Integer totalTemporadas,
                       Double evaluacion, String poster, Categoria genero,
                       String actores, String sinopsis) {
}

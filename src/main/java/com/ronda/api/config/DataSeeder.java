package com.ronda.api.config;

import com.ronda.api.entity.Categoria;
import com.ronda.api.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() == 0) {
            List.of("Hogar", "Electrodomésticos", "Tecnología", "Indumentaria", "Deportes", "Muebles", "Libros", "Juguetes")
                    .forEach(nombre -> categoriaRepository.save(Categoria.builder().nombre(nombre).build()));
        }
    }
}

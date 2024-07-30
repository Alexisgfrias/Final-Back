package com.alura.Screenmatch.principal;

import com.alura.Screenmatch.model.*;
import com.alura.Screenmatch.Services.ConsumoAPI;
import com.alura.Screenmatch.Services.ConvierteDatos;
import com.alura.Screenmatch.repository.SerieRepository;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=17b2e725";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private  List<Serie> series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repositorio=repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar series por titulo
                    5 - Top 5 mejores series
                    6 - Buscar series por categoria
                    7 - Buscar series por No°Temporadas y evaluacion
                    8 - Buscar episodios por titulo
                    9 - Top 5 episodios por serie
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriresBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriePorCategoria();
                    break;
                case 7:
                    buscarSeriesPorTemporadaYTitulo();
                    break;
                case 8:
                    buscarEpisodiosPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }


    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        mostrarSeriresBuscadas();
        System.out.println("Escribe el nombre de la serie de la cual deseas los episodios: ");
        var nombreSerie = teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()){
            var serieEncontrada = serie.get();

            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(),e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }



    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        repositorio.save(serie);
        //datosSeries.add(datos);
        System.out.println(datos);
    }
    private void mostrarSeriresBuscadas() {
       series = repositorio.findAll();

       series.stream()
               .sorted(Comparator.comparing(Serie::getGenero))
               .forEach(System.out::println);

    }
    private void buscarSeriesPorTitulo(){
        System.out.println("Escribe el nombre de la serie: ");
        var nombreSerie = teclado.nextLine();

        serieBuscada = repositorio.findBytituloContainsIgnoreCase(nombreSerie);

        if (serieBuscada.isPresent()){
            System.out.println("La serie buscada es: "+serieBuscada.get());
        }else{
            System.out.println("Serie no encontrada");
        }

    }
    private void buscarTop5Series(){
        List<Serie> topSeries = repositorio.findTop4ByOrderByEvaluacionDesc();
        topSeries.forEach(s -> System.out.println("Serie: "+s.getTitulo()+", Evaluacion: "+s.getEvaluacion()));
    }

    private void buscarSeriePorCategoria(){
        System.out.println("Escribe el genero-categoria de la serie a buscra: ");
        var genero = teclado.nextLine();
        var categoria = Categoria.fromEspañol(genero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Las series de la categoria "+categoria);
        seriesPorCategoria.forEach(System.out::println);
    }
    private void buscarSeriesPorTemporadaYTitulo(){
        System.out.println("¿Cuantas temporadas maximas?");
        var temporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("Calificacion mínima: ");
        var evaluacion = teclado.nextDouble();
        List<Serie> seriesBusqueda = repositorio.seriesPorTemporadaYEvaluacion(temporadas,evaluacion);
        System.out.println("****SERIES FILTRADAS ******");
        seriesBusqueda.forEach(System.out::println);
    }

    private void buscarEpisodiosPorTitulo(){
        System.out.println("Escribe el nombre del epiosdio que desea buscar: ");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e -> System.out.printf("Titulo: %s Temporada: %s Episodio: %s Evaluación: %s\n",
                e.getTitulo(), e.getTemporada(),e.getNumeroEpisodio(), e.getEvaluacion()));
    }

    private void buscarTop5Episodios(){
        buscarSeriesPorTitulo();
        if (serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List<Episodio> top5Episodios = repositorio.top5Episodios(serie);
            System.out.printf("TOP 5 episodios de "+serie.getTitulo()+"\n");
            top5Episodios.forEach(e -> System.out.printf("Titulo: %s Temporada: %s Episodio: %s Evaluación: %s\n",
                    e.getTitulo(), e.getTemporada(),e.getNumeroEpisodio(), e.getEvaluacion()));
        }

    }


}
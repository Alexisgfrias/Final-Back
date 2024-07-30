package com.alura.Screenmatch.Services;

public interface IConvierteDatos {
   <T> T obtenerDatos(String json, Class<T> clase);

}

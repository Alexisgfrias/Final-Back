package com.alura.Screenmatch.Services;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.service.OpenAiService;


public class ConsultaGPT {

    public static String obtenerTraduccion(String texto) {
        OpenAiService service = new OpenAiService("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");

        CompletionRequest requisicion = CompletionRequest.builder()
                .model("gpt-3.5-turbo-instruct")
                .prompt("traduce a español el siguiente texto: " + texto)
                .maxTokens(1000)
                .temperature(0.7)
                .build();

        var respuesta = service.createCompletion(requisicion);
        return respuesta.getChoices().get(0).getText();
    }
}



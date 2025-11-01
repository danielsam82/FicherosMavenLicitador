package com.licitador.jar;

import com.licitador.model.LicitacionData;
import com.licitador.model.ArticuloAnexo;
import com.licitador.jar.model.RequerimientoLicitador;
import java.util.*;

/**
 * A simulation class for the Licitador application.
 * This class is used for testing and demonstrating the functionality of the AnexoGenerator.
 * It simulates the process of loading tender data, collecting bidder responses, and generating the final annex.
 */
public class LicitadorApp {

    // Simulación de los datos fijos del Licitador (NIF, Razón Social, etc.)
    private static Map<String, String> getDatosSimuladosLicitador() {
        Map<String, String> datos = new HashMap<>();
        // Estos datos se usan para sustituir tags como <DATO_LICITADOR ETQ="RAZON_SOCIAL"/>
        datos.put("NIF_CIF", "B12345678");
        datos.put("RAZON_SOCIAL", "Empresa Ejemplo S.A.");
        datos.put("CARGO_REPRESENTANTE", "Apoderado Legal");
        return datos;
    }

    // SIMULACIÓN: Crea y devuelve un objeto LicitacionData con artículos para la prueba.
    private static LicitacionData cargarLicitacionDataSimulado() {
        // En una app real, aquí se usaría ObjectInputStream para leer el LicitacionData.dat
        
        // Creamos los artículos interactivos y declarativos
        
        // 1. Artículo Declarativo (orden 1)
        ArticuloAnexo art1 = new ArticuloAnexo("ART_1_DECLARATIVO", 1, "Artículo Primero: Objeto y Adhesión", 
            "El licitador <DATO_LICITADOR ETQ=\"RAZON_SOCIAL\"/>, con NIF <DATO_LICITADOR ETQ=\"NIF_CIF\"/>, se adhiere de forma incondicional al objeto del expediente <DATO_LICITADOR ETQ=\"EXPEDIENTE\"/>.", 
            false, "", ArticuloAnexo.ACCION_NINGUNA, new String[0]);
        
        // 2. Artículo Interactivo: Pide Campos (orden 3)
        ArticuloAnexo art2 = new ArticuloAnexo("ART_2_SOLVENCIA", 3, "Artículo Segundo: Declaración de Solvencia", 
            "La presente declaración sustituye la documentación de Solvencia. [Aquí se insertará la declaración detallada].", 
            true, "¿Certifica que cumple con el requisito de Solvencia Técnica?", 
            ArticuloAnexo.ACCION_PEDIR_CAMPOS, new String[]{"Ingresos Anuales (2024)", "Personal Medio"});
            
        // 3. Artículo Interactivo: Pide Fichero (orden 2)
        ArticuloAnexo art3 = new ArticuloAnexo("ART_3_CERTIF", 2, "Artículo Tercero: Certificaciones Opcionales", 
            "La empresa declara contar con la certificación ISO 9001.", 
            true, "¿Aporta la Certificación ISO 9001?", // 'true' es si requiere firma, no la interactividad
            ArticuloAnexo.ACCION_PEDIR_FICHERO, new String[0]);
            
        // 4. Artículo Interactivo: NO cumple (orden 4) -> No debería aparecer en el final
        ArticuloAnexo art4 = new ArticuloAnexo("ART_4_OTRO", 4, "Artículo Cuarto: Requisito Adicional", 
            "Cumple con el requisito adicional de la cláusula 7.2.", 
            false, "¿Acepta las condiciones del Anexo Z?", 
            ArticuloAnexo.ACCION_PEDIR_FICHERO, new String[0]);
            
        // Creamos LicitacionData. Nota: Asumo que tienes los setters o constructor para Expediente/Objeto
        LicitacionData data = new LicitacionData("EXP-2025-001", "Suministro de Materiales de Oficina", false, 1, new com.licitador.model.ArchivoRequerido[0], new com.licitador.model.ArchivoRequerido[0], new ArticuloAnexo[]{art1, art3, art2, art4});
        
        return data;
    }

    public static void main(String[] args) {
        System.out.println("--- 🚀 INICIO: PROCESO DE LICITACIÓN ---");
        
        // 1. CARGAR DATOS Y CREAR GENERADOR
        LicitacionData licitacionData = cargarLicitacionDataSimulado();
        Map<String, String> datosLicitador = getDatosSimuladosLicitador();
        
        // Instanciamos el AnexoGenerator
        AnexoGenerator generator = new AnexoGenerator(licitacionData, datosLicitador);

        // 2. FASE DE INTERACCIÓN (Simulación de la GUI)
        // Obtenemos una lista de todos los Requerimientos que deben ser contestados
        List<RequerimientoLicitador> reqs = generator.obtenerRequerimientosInteractivos();
        System.out.println("\n--- FASE INTERACTIVA: " + reqs.size() + " Requerimientos encontrados ---");
        
        // Iteramos sobre los requerimientos y SIMULAMOS la respuesta del usuario
        List<RequerimientoLicitador> respuestasLicitador = new ArrayList<>();
        
        for (RequerimientoLicitador req : reqs) {
            System.out.println("\nPREGUNTA (" + req.getAccionSi() + "): " + req.getPregunta());
            
            // Lógica de simulación:
            if (req.getIdArticulo().equals("ART_2_SOLVENCIA")) {
                // RESPUESTA SÍ + Campos Rellenados
                req.setRespuestaSi(true);
                Map<String, String> campos = new HashMap<>();
                campos.put(req.getEtiquetasCampos()[0], "1.500.000,00 EUR");
                campos.put(req.getEtiquetasCampos()[1], "25");
                req.setValoresCampos(campos);
                System.out.println("   -> RESPUESTA SIMULADA: SÍ + Campos rellenados.");
            }
            else if (req.getIdArticulo().equals("ART_3_CERTIF")) {
                // RESPUESTA SÍ + Fichero Subido
                req.setRespuestaSi(true);
                req.setRutaFichero("/home/licitador/documentos/ISO_9001.pdf");
                System.out.println("   -> RESPUESTA SIMULADA: SÍ + Fichero subido.");
            }
            else if (req.getIdArticulo().equals("ART_4_OTRO")) {
                // RESPUESTA NO
                req.setRespuestaSi(false);
                System.out.println("   -> RESPUESTA SIMULADA: NO. (El artículo se omitirá)");
            }
            
            respuestasLicitador.add(req);
        }

        // 3. GENERACIÓN DEL ANEXO FINAL
        // Pasamos las respuestas ya cumplimentadas al generador
        generator.setRespuestasFinales(respuestasLicitador);
        String anexoFinal = generator.generarContenidoFinal();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("        ANEXO GLOBAL DE ADHESIÓN (Simulación de contenido)");
        System.out.println("=".repeat(60));
        System.out.println(anexoFinal);
        System.out.println("=".repeat(60));
        
        System.out.println("\n--- ✅ FIN: PROCESO DE LICITACIÓN ---");
    }
}
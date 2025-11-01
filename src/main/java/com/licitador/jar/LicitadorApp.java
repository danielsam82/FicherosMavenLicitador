package com.licitador.jar;

import com.licitador.model.LicitacionData;
import com.licitador.model.ArticuloAnexo;
import com.licitador.jar.model.RequerimientoLicitador;
import com.licitador.service.Configuracion; // Importar la clase Configuracion
import java.util.*;

public class LicitadorApp {

    // Simulación de los datos fijos del Licitador (NIF, Razón Social, etc.)
    private static Map<String, String> getDatosSimuladosLicitador() {
        Map<String, String> datos = new HashMap<>();
        datos.put("NIF_CIF", "B12345678");
        datos.put("RAZON_SOCIAL", "Empresa Ejemplo S.A.");
        datos.put("CARGO_REPRESENTANTE", "Apoderado Legal");
        return datos;
    }

    // SIMULACIÓN: Crea y devuelve un objeto LicitacionData con artículos para la prueba.
    private static LicitacionData cargarLicitacionDataSimulado() {
        // En una app real, aquí se usaría ObjectInputStream para leer el LicitacionData.dat
        
        // --- CONSTRUCTORES CORREGIDOS (10 argumentos) ---
        
        // 1. Artículo Declarativo (orden 1)
        ArticuloAnexo art1 = new ArticuloAnexo(
            "ART_1_DECLARATIVO", 1, "Artículo Primero: Objeto y Adhesión", 
            false, "", // esInteractivo=false, pregunta=""
            "El licitador <DATO_LICITADOR ETQ=\"RAZON_SOCIAL\"/>, con NIF <DATO_LICITADOR ETQ=\"NIF_CIF\"/>, se adhiere de forma incondicional al objeto del expediente <DATO_LICITADOR ETQ=\"EXPEDIENTE\"/>.", // contenidoFormato (SÍ)
            "", // contenidoFormatoRespuestaNo (vacío, no aplica)
            ArticuloAnexo.ACCION_NINGUNA, new String[0], false // accionSi, etiquetas, requiereFirma
        );
        
        // 2. Artículo Interactivo: Pide Campos (orden 3)
        ArticuloAnexo art2 = new ArticuloAnexo(
            "ART_2_SOLVENCIA", 3, "Artículo Segundo: Declaración de Solvencia", 
            true, "¿Certifica que cumple con el requisito de Solvencia Técnica?", // esInteractivo=true, pregunta
            "La presente declaración sustituye la documentación de Solvencia.", // contenidoFormato (SÍ)
            "DECLARA NO CUMPLIR con el requisito de Solvencia Técnica.", // contenidoFormatoRespuestaNo (NO)
            ArticuloAnexo.ACCION_PEDIR_CAMPOS, new String[]{"Ingresos Anuales (2024)", "Personal Medio"}, true // accionSi, etiquetas, requiereFirma
        );
            
        // 3. Artículo Interactivo: Pide Fichero (orden 2)
        ArticuloAnexo art3 = new ArticuloAnexo(
            "ART_3_CERTIF", 2, "Artículo Tercero: Certificaciones Opcionales", 
            true, "¿Aporta la Certificación ISO 9001?", // esInteractivo=true, pregunta
            "La empresa declara contar con la certificación ISO 9001.", // contenidoFormato (SÍ)
            "La empresa NO aporta la Certificación ISO 9001.", // contenidoFormatoRespuestaNo (NO)
            ArticuloAnexo.ACCION_PEDIR_FICHERO, new String[0], true // accionSi, etiquetas, requiereFirma
        );
            
        // 4. Artículo Interactivo: NO cumple (orden 4)
        ArticuloAnexo art4 = new ArticuloAnexo(
            "ART_4_OTRO", 4, "Artículo Cuarto: Requisito Adicional", 
            true, "¿Acepta las condiciones del Anexo Z?", // esInteractivo=true, pregunta
            "Cumple con el requisito adicional de la cláusula 7.2.", // contenidoFormato (SÍ)
            "NO ACEPTA las condiciones del Anexo Z.", // contenidoFormatoRespuestaNo (NO)
            ArticuloAnexo.ACCION_NINGUNA, new String[0], false // accionSi, etiquetas, requiereFirma
        );
            
        // Creamos LicitacionData (el objeto serializado)
        LicitacionData data = new LicitacionData("EXP-2025-001", "Suministro de Materiales de Oficina", false, 1, 
            new com.licitador.model.ArchivoRequerido[0], 
            new com.licitador.model.ArchivoRequerido[0], 
            new ArticuloAnexo[]{art1, art3, art2, art4});
        
        return data;
    }

    public static void main(String[] args) {
        System.out.println("--- 🚀 INICIO: PROCESO DE LICITACIÓN ---");
        
        // 1. CARGAR DATOS Y CREAR GENERADOR
        
        // 1a. Simulamos la carga del objeto LicitacionData (como si viniera del config.dat)
        LicitacionData licitacionData = cargarLicitacionDataSimulado();
        Map<String, String> datosLicitador = getDatosSimuladosLicitador();
        
        // 1b. Simulamos la conversión que hace MainWindow: LicitacionData -> Configuracion
        Configuracion configuracionSimulada = new Configuracion(
            licitacionData.getObjeto(),
            licitacionData.getExpediente(),
            licitacionData.tieneLotes(),
            licitacionData.getNumLotes(),
            new String[0], // Archivos comunes (irrelevantes para esta prueba)
            new boolean[0], // ...
            new boolean[0], // ...
            new Configuracion.ArchivoOferta[0], // ...
            new String[0], // Supuestos (irrelevantes)
            licitacionData.getArticulosAnexos() // ¡El dato importante!
        );
        
        // 1c. Instanciamos el AnexoGenerator (Ahora SÍ pasamos Configuracion)
        AnexoGenerator generator = new AnexoGenerator(configuracionSimulada, datosLicitador);

        // 2. FASE DE INTERACCIÓN (Simulación de la GUI)
        List<RequerimientoLicitador> reqs = generator.obtenerRequerimientosInteractivos();
        System.out.println("\n--- FASE INTERACTIVA: " + reqs.size() + " Requerimientos encontrados ---");
        
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
package io.guillermoamadodiaz.javacalcfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigInteger;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * JavaFX SelectorDeOpciones
 */
public class SelectorDeOpciones extends Application {
    
    // Crear contenedores como campos de clase
    private final VBox layout = new VBox();
    private final GridPane gridPane = new GridPane();

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Configurar la ventana
        primaryStage.setTitle("Selector de Opciones");
        
        // Configurar el espaciado y relleno para los contenedores
        layout.setPadding(new Insets(20));
        layout.setSpacing(10);

        gridPane.setPadding(new Insets(20));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        
        // Mostrar la pantalla de inicio al inicio de la aplicación
        mostrarPantallaInicio();

        // Crear la escena
        Scene scene = new Scene(gridPane, 640, 480);

        // Establecer la escena en la ventana
        primaryStage.setScene(scene);

        // Mostrar la ventana
        primaryStage.show();
    }
    
    private void mostrarPantallaInicio() {
        // Limpiar cualquier contenido previo
        gridPane.getChildren().clear();

        // Crear botones para las opciones y asignarles acciones
        Button calcularTeoremaPitagorasButton = crearBoton("Calcular Teorema de Pitágoras", this::calcularTeoremaPitagoras);
        Button calcularCilindroButton = crearBoton("Calcular Área de Cilindro", this::calcularAreaCilindro);
        Button determinarBisiestoButton = crearBoton("Determinar Año Bisiesto", this::determinarBisiesto);
        Button calcularFactorialButton = crearBoton("Calcular Factorial", this::calcularFactorial);
        Button determinarMultiploButton = crearBoton("Determinar Múltiplo", this::determinarMultiplo);
        Button determinarAprobadoButton = crearBoton("Determinar Aprobado", this::determinarAprobado);
        
        // Añadir los botones al GridPane
        gridPane.add(calcularTeoremaPitagorasButton, 0, 0);
        gridPane.add(calcularCilindroButton, 1, 0);
        gridPane.add(determinarBisiestoButton, 2, 0);
        gridPane.add(calcularFactorialButton, 0, 1);
        gridPane.add(determinarMultiploButton, 1, 1);
        gridPane.add(determinarAprobadoButton, 2, 1);
    }
    
    // Método para crear un botón con texto y acción especificados
    private Button crearBoton(String texto, Runnable action) {
        Button button = new Button(texto);
        button.setOnAction(event -> action.run());
        return button;
    }
    
    // Método para mostrar una pantalla específica
    private void mostrarPantalla(VBox pantalla) {
        gridPane.getChildren().clear();
        gridPane.add(pantalla, 0, 0);
    }
    
    // Método para crear una nueva pantalla (VBox)
    private VBox crearPantalla() {
        VBox pantalla = new VBox();
        pantalla.setPadding(new Insets(20));
        pantalla.setSpacing(10);
        return pantalla;
    }
    
    // Método para calcular el Teorema de Pitágoras
    private void calcularTeoremaPitagoras() {
        // Crear una nueva pantalla (VBox)
        VBox pantalla = crearPantalla();

        // Crear etiquetas y campos de texto
        Label instruccionesLabel = new Label("Ingresa las longitudes de los catetos para calcular la hipotenusa:");
        TextField catetoALabel = new TextField();
        catetoALabel.setPromptText("Longitud del cateto a");
        TextField catetoBLabel = new TextField();
        catetoBLabel.setPromptText("Longitud del cateto b");
        Button calcularButton = new Button("Calcular");
        Label resultadoLabel = new Label();
        
        // Definir la acción del botón calcular
        calcularButton.setOnAction(event -> {
            try {
                // Obtener las longitudes de los catetos
                double catetoA = Double.parseDouble(catetoALabel.getText());
                double catetoB = Double.parseDouble(catetoBLabel.getText());
                
                // Calcular la hipotenusa, área, perímetro y ángulos
                double hipotenusa = Math.sqrt(catetoA * catetoA + catetoB * catetoB);
                double area = (catetoA * catetoB) / 2;
                double perimetro = catetoA + catetoB + hipotenusa;
                double alfa = Math.toDegrees(Math.atan(catetoB / catetoA));
                double beta = Math.toDegrees(Math.atan(catetoA / catetoB));
                
                // Mostrar los resultados
                resultadoLabel.setText("Hipotenusa: " + hipotenusa + "\n" +
                        "Otros datos:\n" +
                        "  Área: " + area + "\n" +
                        "  Perímetro: " + perimetro + "\n" +
                        "  α ≅ " + alfa + "\n" +
                        "  β ≅ " + beta);
            } catch (NumberFormatException e) {
                resultadoLabel.setText("Por favor ingresa números válidos en ambos campos.");
            }
        });

        // Establecer el tamaño de la fuente solo para el texto de "Otros datos"
        resultadoLabel.setStyle("-fx-font-size: 16;");

        // Crear un botón para volver a la pantalla de inicio
        Button volverButton = crearBoton("Volver", this::mostrarPantallaInicio);

        // Añadir los elementos al layout
        pantalla.getChildren().addAll(instruccionesLabel, catetoALabel, catetoBLabel, calcularButton, resultadoLabel, volverButton);
        
        // Mostrar la pantalla creada
        mostrarPantalla(pantalla);
    }
    
    private void calcularAreaCilindro() {
        // Crear una nueva pantalla (VBox)
        VBox pantalla = crearPantalla();
        
        // Crear etiquetas y campos de texto
        Label instruccionesLabel = new Label("Ingresa el radio y la altura del cilindro:");
        TextField radioLabel = new TextField();
        radioLabel.setPromptText("Radio");
        TextField alturaLabel = new TextField();
        alturaLabel.setPromptText("Altura");
        Button calcularButton = new Button("Calcular");
        Label resultadoLabel = new Label();
        
        // Definir la acción del botón calcular
        calcularButton.setOnAction(event -> {
            try {
                // Obtener el radio y la altura del cilindro
                double radio = Double.parseDouble(radioLabel.getText());
                double altura = Double.parseDouble(alturaLabel.getText());
                
                // Calcular el área del cilindro
                double area = 2 * Math.PI * radio * (radio + altura);
                
                // Mostrar el resultado
                resultadoLabel.setText("Área del cilindro: " + area);
            } catch (NumberFormatException e) {
                resultadoLabel.setText("Por favor ingresa números válidos en ambos campos.");
            }
        });
        
        // Crear un botón para volver a la pantalla de inicio
        Button volverButton = crearBoton("Volver", this::mostrarPantallaInicio);
        
        // Añadir los elementos al layout
        pantalla.getChildren().addAll(instruccionesLabel, radioLabel, alturaLabel, calcularButton, resultadoLabel, volverButton);
        
        // Mostrar la pantalla creada
        mostrarPantalla(pantalla);
    }
    
    private void determinarBisiesto() {
        // Crear una nueva pantalla (VBox)
        VBox pantalla = crearPantalla();
        
        // Crear etiquetas y campos de texto
        Label instruccionesLabel = new Label("Ingresa un año para determinar si es bisiesto:");
        TextField añoLabel = new TextField();
        añoLabel.setPromptText("Año");
        Button determinarButton = new Button("Determinar");
        Label resultadoLabel = new Label();
        
        // Definir la acción del botón determinar
        determinarButton.setOnAction(event -> {
            try {
                // Obtener el año ingresado
                int año = Integer.parseInt(añoLabel.getText());
                
                // Determinar si el año es bisiesto
                boolean esBisiesto = (año % 4 == 0 && año % 100 != 0) || (año % 400 == 0);
                
                // Mostrar el resultado
                resultadoLabel.setText("El año " + año + (esBisiesto ? " es bisiesto." : " no es bisiesto."));
            } catch (NumberFormatException e) {
                resultadoLabel.setText("Por favor ingresa un año válido.");
            }
        });
        
        // Crear un botón para volver a la pantalla de inicio
        Button volverButton = crearBoton("Volver", this::mostrarPantallaInicio);
        
        // Añadir los elementos al layout
        pantalla.getChildren().addAll(instruccionesLabel, añoLabel, determinarButton, resultadoLabel, volverButton);
        
        // Mostrar la pantalla creada
        mostrarPantalla(pantalla);
    }
    
    private void calcularFactorial() {
        // Crear una nueva pantalla (VBox)
        VBox pantalla = crearPantalla();
        
        // Crear etiquetas y campos de texto
        Label instruccionesLabel = new Label("Ingresa un número para calcular su factorial:");
        TextField numeroLabel = new TextField();
        numeroLabel.setPromptText("Número");
        Button calcularButton = new Button("Calcular");
        Label resultadoLabel = new Label();
        
        // Definir la acción del botón calcular
        calcularButton.setOnAction(event -> {
            try {
                // Obtener el número para calcular su factorial
                BigInteger numero = new BigInteger(numeroLabel.getText());
                
                // Verificar si el número es negativo
                if (numero.compareTo(BigInteger.ZERO) < 0) {
                    // Calcular el factorial del número
                    resultadoLabel.setText("Error: El factorial solo se puede calcular para números enteros positivos.");
                } else {
                    BigInteger factorial = BigInteger.ONE;
                    for (BigInteger i = BigInteger.ONE; i.compareTo(numero) <= 0; i = i.add(BigInteger.ONE)) {
                        factorial = factorial.multiply(i);
                    }
                    // Formatear el resultado con separadores de miles
                    String resultadoFormateado = String.format("%,d", factorial); // Formatear el resultado con separadores de miles
                    resultadoLabel.setText("El factorial de " + numero + " es " + resultadoFormateado);
                }
            } catch (NumberFormatException e) {
                resultadoLabel.setText("Por favor ingresa un número válido.");
            }
        });
        
        // Crear un botón para volver a la pantalla de inicio
        Button volverButton = crearBoton("Volver", this::mostrarPantallaInicio);
        
        // Añadir los elementos al layout
        pantalla.getChildren().addAll(instruccionesLabel, numeroLabel, calcularButton, resultadoLabel, volverButton);
        
        // Mostrar la pantalla creada
        mostrarPantalla(pantalla);
    }
    
    private void determinarMultiplo() {
        // Crear una nueva pantalla (VBox)
        VBox pantalla = crearPantalla();
        
        // Crear etiquetas y campos de texto
        Label instruccionesLabel = new Label("Ingresa dos números para determinar si uno es múltiplo del otro:");
        TextField numero1Label = new TextField();
        numero1Label.setPromptText("Primer número");
        TextField numero2Label = new TextField();
        numero2Label.setPromptText("Segundo número");
        Button determinarButton = new Button("Determinar");
        Label resultadoLabel = new Label();
        
        // Definir la acción del botón determinar
        determinarButton.setOnAction(event -> {
            try {
                // Obtener los números ingresados
                int numero1 = Integer.parseInt(numero1Label.getText());
                int numero2 = Integer.parseInt(numero2Label.getText());
                
                // Determinar si uno es múltiplo del otro
                if (numero1 % numero2 == 0 || numero2 % numero1 == 0) {
                    resultadoLabel.setText(numero1 + " es múltiplo de " + numero2 + " o " + numero2 + " es múltiplo de " + numero1);
                } else {
                    resultadoLabel.setText(numero1 + " no es múltiplo de " + numero2 + " ni " + numero2 + " es múltiplo de " + numero1);
                }
            } catch (NumberFormatException e) {
                resultadoLabel.setText("Por favor ingresa números válidos en ambos campos.");
            }
        });

        // Crear un botón para volver a la pantalla de inicio
        Button volverButton = crearBoton("Volver", this::mostrarPantallaInicio);

        // Añadir los elementos al layout
        pantalla.getChildren().addAll(instruccionesLabel, numero1Label, numero2Label, determinarButton, resultadoLabel, volverButton);

        // Mostrar la pantalla creada
        mostrarPantalla(pantalla);
    }
    
    private void determinarAprobado() {
        // Crear una nueva pantalla (VBox)
        VBox pantalla = crearPantalla();
        
        // Crear etiquetas y campos de texto
        Label instruccionesLabel = new Label("Ingresa las 5 notas del alumno para determinar si es aprobado:");
        TextField nota1Label = new TextField();
        nota1Label.setPromptText("Nota 1");
        TextField nota2Label = new TextField();
        nota2Label.setPromptText("Nota 2");
        TextField nota3Label = new TextField();
        nota3Label.setPromptText("Nota 3");
        TextField nota4Label = new TextField();
        nota4Label.setPromptText("Nota 4");
        TextField nota5Label = new TextField();
        nota5Label.setPromptText("Nota 5");
        Button determinarButton = new Button("Determinar");
        Label resultadoLabel = new Label();
        
        // Definir la acción del botón determinar
        determinarButton.setOnAction(event -> {
            try {
                // Obtener las 5 notas del alumno
                double nota1 = Double.parseDouble(nota1Label.getText());
                double nota2 = Double.parseDouble(nota2Label.getText());
                double nota3 = Double.parseDouble(nota3Label.getText());
                double nota4 = Double.parseDouble(nota4Label.getText());
                double nota5 = Double.parseDouble(nota5Label.getText());
                
                // Calcular la media de las notas
                double media = (nota1 + nota2 + nota3 + nota4 + nota5) / 5;
                String mediaFormateada = String.format("%.2f", media);
                
                // Determinar si el alumno está aprobado
                if (media >= 5) {
                    resultadoLabel.setText("El alumno está aprobado con una nota media de: " + mediaFormateada);
                } else {
                    resultadoLabel.setText("El alumno está suspendido con una nota media de: " + mediaFormateada);
                }
            } catch (NumberFormatException e) {
                resultadoLabel.setText("Por favor ingresa notas válidas en todos los campos.");
            }
        });

        // Crear un botón para volver a la pantalla de inicio
        Button volverButton = crearBoton("Volver", this::mostrarPantallaInicio);

        // Añadir los elementos al layout
        pantalla.getChildren().addAll(instruccionesLabel, nota1Label, nota2Label, nota3Label, nota4Label, nota5Label, determinarButton, resultadoLabel, volverButton);

        // Mostrar la pantalla creada
        mostrarPantalla(pantalla);
    }
    
    public static void main(String[] args) {
        launch();
    }

}
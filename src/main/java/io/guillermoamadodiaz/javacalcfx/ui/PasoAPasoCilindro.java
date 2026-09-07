package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Desarrollo paso a paso del área total de un cilindro, en notación lineal:
 * sustituye el radio y la altura en {@code A = 2·π·r·(r + h)} y desglosa la
 * aportación de las dos bases ({@code 2·π·r²}) y de la superficie lateral
 * ({@code 2·π·r·h}).
 *
 * <p>Solo rellena plantillas fijas con los números; es determinista y está
 * cubierto por tests.
 */
public final class PasoAPasoCilindro {

    private PasoAPasoCilindro() {}

    /** @throws IllegalArgumentException si el radio o la altura no son finitos y &ge; 0 */
    public static String desarrollo(double radio, double altura) {
        double area = Calculator.cylinderArea(radio, altura); // valida radio y altura
        double bases = 2 * Math.PI * radio * radio;
        double lateral = 2 * Math.PI * radio * altura;

        StringBuilder sb = new StringBuilder();
        sb.append(Messages.get("cilindro.pasos.formula")).append('\n');
        sb.append("A = 2·π·r·(r + h)\n");
        sb.append("A = 2·π·(%s)·((%s) + (%s))\n".formatted(n(radio), n(radio), n(altura)));
        sb.append("A = 2·π·(%s)·(%s)\n".formatted(n(radio), n(radio + altura)));
        sb.append("A ≅ %s\n\n".formatted(n(area)));

        sb.append(Messages.get("cilindro.pasos.desglose")).append('\n');
        sb.append("bases   = 2·π·(%s)² ≅ %s\n".formatted(n(radio), n(bases)));
        sb.append("lateral = 2·π·(%s)·(%s) ≅ %s".formatted(n(radio), n(altura), n(lateral)));
        return sb.toString();
    }

    private static String n(double valor) {
        return Formato.numero(valor);
    }
}

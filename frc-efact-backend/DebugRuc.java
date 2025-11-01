public class DebugRuc {
    public static void main(String[] args) {
        String ruc = "80016875";
        
        // Algoritmo original (método anterior)
        System.out.println("=== Algoritmo Original ===");
        int[] multiplicadores = {2, 3, 4, 5, 6, 7, 2, 3};
        int suma = 0;
        
        for (int i = 0; i < ruc.length(); i++) {
            int digito = Character.getNumericValue(ruc.charAt(i));
            int producto = digito * multiplicadores[i];
            suma += producto;
            System.out.println("Posición " + i + ": " + digito + " * " + multiplicadores[i] + " = " + producto);
        }
        
        System.out.println("Suma total: " + suma);
        int resto = suma % 11;
        System.out.println("Resto (suma % 11): " + resto);
        
        int dv = 11 - resto;
        System.out.println("DV inicial (11 - resto): " + dv);
        
        if (dv == 11) {
            dv = 0;
        } else if (dv == 10) {
            dv = 1;
        }
        
        System.out.println("DV final (método original): " + dv);
        
        // Nuevo algoritmo
        System.out.println("\n=== Nuevo Algoritmo ===");
        String alRevez = new StringBuilder(ruc).reverse().toString();
        System.out.println("RUC al revés: " + alRevez);
        
        int k = 2;
        int total = 0;
        
        for (char numero : alRevez.toCharArray()) {
            int digito = numero - '0';
            int producto = digito * k;
            total += producto;
            System.out.println("Dígito " + digito + " * " + k + " = " + producto);
            k++;
            if (k > 11) k = 2;
        }
        
        System.out.println("Total: " + total);
        int restoNuevo = total % 11;
        System.out.println("Resto (total % 11): " + restoNuevo);
        
        int dvNuevo = restoNuevo > 1 ? 11 - restoNuevo : 0;
        System.out.println("DV final (método nuevo): " + dvNuevo);
    }
}
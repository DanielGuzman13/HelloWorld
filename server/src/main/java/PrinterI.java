import Demo.Response;
import com.zeroc.Ice.Current;
import java.io.*;
import java.net.*;
import java.util.*;

public class PrinterI implements Demo.Printer {

    @Override
    public Response printString(String s, Current current) {
        long start = System.currentTimeMillis();

        // Separar prefijo y mensaje
        String[] parts = s.split(":", 3);
        String userHost = parts[0] + ":" + parts[1];
        String message = parts.length > 2 ? parts[2] : "";

        String result;

        try {
            if (message.matches("\\d+")) {
                // Caso 2a: número entero positivo
                int n = Integer.parseInt(message);
                String fib = fibonacciSeries(n);
                System.out.println(userHost + " -> Fibonacci(" + n + "): " + fib);
                result = "Factores primos: " + primeFactors(n);

            } else if (message.startsWith("listifs")) {
                // Caso 2b: listar interfaces
                StringBuilder sb = new StringBuilder();
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                while (nets.hasMoreElements()) {
                    NetworkInterface ni = nets.nextElement();
                    sb.append(ni.getName()).append(" - ").append(ni.getDisplayName()).append("\n");
                }
                System.out.println(userHost + " -> Interfaces:\n" + sb);
                result = sb.toString();

            } else if (message.startsWith("listports")) {
                // Caso 2c: listar puertos abiertos
                String[] tokens = message.split(" ");
                if (tokens.length < 2) {
                    result = "Uso: listports <IPv4>";
                } else {
                    String ip = tokens[1];
                    result = scanPorts(ip, 1024); // hasta puerto 1024
                    System.out.println(userHost + " -> Puertos abiertos en " + ip + ":\n" + result);
                }

            } else if (message.startsWith("!")) {
                // Caso 2d: ejecutar comando
                String command = message.substring(1);
                result = execCommand(command);
                System.out.println(userHost + " -> Ejecutando comando: " + command);

            } else {
                result = "Comando no reconocido: " + message;
            }
        } catch (Exception e) {
            result = "Error procesando mensaje: " + e.getMessage();
        }

        long end = System.currentTimeMillis();
        return new Response(end - start, result);
    }

    // Generar serie Fibonacci hasta n
    private String fibonacciSeries(int n) {
        List<Integer> fib = new ArrayList<>();
        int a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            fib.add(a);
            int temp = a + b;
            a = b;
            b = temp;
        }
        return fib.toString();
    }

    // Factores primos de n
    private String primeFactors(int n) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i <= n / i; i++) {
            while (n % i == 0) {
                factors.add(i);
                n /= i;
            }
        }
        if (n > 1) factors.add(n);
        return factors.toString();
    }

    // Escaneo de puertos
    private String scanPorts(String ip, int maxPort) {
        StringBuilder sb = new StringBuilder();
        for (int port = 1; port <= maxPort; port++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, port), 50);
                sb.append("Puerto ").append(port).append(" abierto\n");
            } catch (IOException ignored) {
            }
        }
        return sb.length() == 0 ? "No se encontraron puertos abiertos hasta " + maxPort : sb.toString();
    }

    // Ejecutar comando en SO
    private String execCommand(String cmd) throws IOException {
        StringBuilder output = new StringBuilder();
        Process p = Runtime.getRuntime().exec(cmd);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
}

import org.apache.commons.math4.util.MathUtils; 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math4.linear.Array2DRowRealMatrix;
import org.apache.commons.math4.linear.ArrayRealVector;
import org.apache.commons.math4.linear.RealMatrix;
import org.apache.commons.math4.linear.RealVector;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShamirSecretSharing {
    public static void main(String[] args) throws IOException {
        // Read the JSON input file
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(new File("input.json"));
        Map<String, Object> jsonMap = mapper.convertValue(rootNode, Map.class);

        // Extract the keys and roots from the JSON input
        Map<String, Object> keys = (Map<String, Object>) jsonMap.get("keys");
        int n = (int) keys.get("n");
        int k = (int) keys.get("k");
        Map<String, Map<String, String>> roots = new HashMap<>();
        for (int i = 1; i <= n; i++) {
            roots.put(String.valueOf(i), (Map<String, String>) jsonMap.get(String.valueOf(i)));
        }

        // Decode the Y values
        Map<String, Integer> decimalRoots = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : roots.entrySet()) {
            int x = Integer.parseInt(entry.getKey());
            String baseStr = entry.getValue().get("base");
            int base = Integer.parseInt(baseStr);
            String valueStr = entry.getValue().get("value");
            int y = convertToDecimal(valueStr, base);
            decimalRoots.put(String.valueOf(x), y);
        }

        // Find the constant term 'c' of the polynomial
        int c = findConstantTerm(decimalRoots, k);

        // Print the result
        System.out.println("The constant term 'c' of the polynomial is: " + c);
    }

    private static int convertToDecimal(String value, int base) {
        int decimalValue = 0;
        for (char c : value.toCharArray()) {
            decimalValue = decimalValue * base + (c - '0');
        }
        return decimalValue;
    }

    private static int findConstantTerm(Map<String, Integer> decimalRoots, int k) {
        int n = decimalRoots.size();
        RealMatrix coefficients = new Array2DRowRealMatrix(n, k);
        RealVector constants = new ArrayRealVector(n);

        int i = 0;
        for (Map.Entry<String, Integer> entry : decimalRoots.entrySet()) {
            int x = Integer.parseInt(entry.getKey());
            int y = entry.getValue();
            for (int j = 0; j < k; j++) {
                coefficients.setEntry(i, j, Math.pow(x, k - 1 - j));
            }
            constants.setEntry(i, y);
            i++;
        }

        RealVector solution = new ArrayRealVector(k);
        try {
            solution = ((Array2DRowRealMatrix) coefficients).solve(constants);
        } catch (Exception e) {
            // handle exception
        }

        return (int) solution.getEntry(k - 1);
    }
}
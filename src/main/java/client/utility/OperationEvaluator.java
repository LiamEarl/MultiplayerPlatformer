package client.utility;

import java.util.Map;
import java.util.function.Function;

/**
 * Static class that evaluates custom functions
 */
public class OperationEvaluator {

    private static final double TWO_PI = Math.PI * 2; // 2PI ease of use field

    // Map where the key is the short name for the function, and the value is the function itself, defined below.
    private static final Map<String, Function<Double, Double>> OPERATIONS = Map.of(
            "picn", OperationEvaluator::piecewiseNormal,
            "picf", OperationEvaluator::piecewiseFast,
            "sin", Math::sin,
            "cos", Math::cos,
            "fpls", OperationEvaluator::frequentPulse,
            "npls", OperationEvaluator::pulse,
            "lins", OperationEvaluator::linearSpike,
            "lind", OperationEvaluator::linearDropoff
    );
    /**
     * Evaluates custom functions passed in to some GameObjects for the purpose of dynamic movement.
     *
     * @param value the input of the function. The value would be x if your function is f(x) = 10x + 5
     * @param expression the expression to apply to the input, example: "-1426lind0.55" would apply the function
     *                   linear dropoff with a coefficient of -1426 and a frequency of 0.55 and add that to the output
     * @param currentTime the current time in milliseconds. See System.currentTimeMillis() for an example
     */
    static public double performCalculation(double value, String expression, long currentTime) {
        if(expression.equals("#")) return value; // If the expression to evaluate is nothing, do nothing.
        double newValue = value;

        // Multiplied to the frequency of a function to allow for the fact that the currentTimeMillis() function changes
        // very rapidly in increments of 2PI
        double timeFactor = ((double) currentTime / 1000) * TWO_PI;

        // Split the expression by the regex ';', allowing for multiple sequential operations
        String[] operations = expression.split(";");
        for(String operation : operations) { // Loop through each of those operations.
            for (Map.Entry<String, Function<Double, Double>> entry : OPERATIONS.entrySet()) { // Loop through all functions
                if(operation.contains(entry.getKey())) { // If the function is the proper function, apply that function
                    String[] factors = operation.split(entry.getKey());

                    double coefficient = Float.parseFloat(factors[0]); // Get the coefficient from the operation

                    double frequency = Float.parseFloat(factors[1]) * timeFactor; // Frequency of the operation
                    double toApply = coefficient * entry.getValue().apply(frequency); // Value to apply to the output
                    char mathSign = operation.charAt(0); // Whether to add, subtract, multiply or divide

                    if(mathSign == '+' || mathSign == '-')
                        newValue += toApply; // No -= toApply because the - sign is already interpreted in the parseFloat
                    else if(mathSign == '*')
                        newValue *= toApply;
                    else if(mathSign == '/')
                        newValue /= toApply;

                    break; // Don't apply future functions on the same operation
                }
            }
        }

        return newValue;
    }
    // Sin wave like but linear across each sub-function and rests at 1 and -1
    private static Double piecewiseNormal(Double value) {
        double mapped = value % TWO_PI;
        if(mapped < 0.628) return 1.0;
        else if(mapped < 2.51) {
            return (1 - (1.06157 * (mapped - 0.628)));
        }else if(mapped < 3.76) {
            return -1.0;
        }else if(mapped < 5.652) {
            return (-1 + (1.06157 * (mapped - 3.768)));
        }else {
            return 1.0;
        }
    }
    // Sin wave like but linear across each sub-function and rests at 1 and -1, higher slope than piecewiseNormal
    private static Double piecewiseFast(Double value) {
        double mapped = value % TWO_PI;
        if(mapped < 1.255) return 1.0;
        else if(mapped < 1.885) {
            return (1 - 3.1831 * (mapped - 1.2566));
        }else if(mapped < 4.3982) {
            return -1.0;
        }else if(mapped < 5.0266) {
            return (-1 + 3.1831 * (mapped - 4.39823));
        }else {
            return 1.0;
        }
    }
    // Spike-like function that starts at 0, increases until it outputs 1 when value = pi, then decreases down to 0 at 2PI
    private static Double linearSpike(Double value) {
        double mapped = value % TWO_PI;
        if(mapped < Math.PI) return (1 / Math.PI) * mapped;
        return -(1 / Math.PI) * (mapped - (2 * Math.PI));
    }
    // Pulses centered about value pi output is 1
    private static Double pulse(Double value) {
        double mapped = value % TWO_PI;
        return Math.pow(2.7182, -50 * Math.pow(mapped - Math.PI, 2));
    }
    // Pulses centered about value pi / 4 output is 1. Pulses twice as frequently as the "pulse" function
    private static Double frequentPulse(Double value) {
        double mapped = value % (Math.PI / 2);
        return Math.pow(2.7182, -50 * Math.pow(mapped - (Math.PI / 4), 2));
    }
    // Used for projectiles mostly, slopes up until 1 at 2PI, then reverts back to 0 instantly.
    private static Double linearDropoff(Double value) {
        double mapped = value % TWO_PI;
        return (1 / (2 * Math.PI)) * mapped;
    }
}
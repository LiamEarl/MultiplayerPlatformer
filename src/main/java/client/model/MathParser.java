package client.model;

public class MathParser {
    static public float performCalculation(float value, String expression) {
        if(expression.equals("#")) return value;

        String[] operations = expression.split(";");
        for(String operation : operations) {
            if(operation.charAt(0) == '+') {
                if(operation.contains("picn")) {
                    operation.replace("+", "");
                    String[] factors = operation.split("picn");
                    value += (float) (Float.parseFloat(factors[0]) * pieceWiseNormal((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("picf")) {
                    operation.replace("+", "");
                    String[] factors = operation.split("picf");
                    value += (float) (Float.parseFloat(factors[0]) * pieceWiseFast((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("sin")) {
                    operation.replace("+", "");
                    String[] factors = operation.split("sin");
                    value += (float) (Float.parseFloat(factors[0]) * Math.sin((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("cos")) {
                    operation.replace("+", "");
                    String[] factors = operation.split("cos");
                    value += (float) (Float.parseFloat(factors[0]) * Math.sin((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
                    continue;
                }
                value += Float.parseFloat(operation.substring(1));
            } else if(operation.charAt(0) == '*') {
                value *= Float.parseFloat(operation.substring(1));
            } else if(operation.charAt(0) == '/') {
                value /= Float.parseFloat(operation.substring(1));
            }
        }
        return value;
    }

    static public double pieceWiseNormal(double value) {
        double mapped = value % 6.28;
        if(mapped < 0.628) return 1;
        else if(mapped < 2.51) {
            return (float) (1 - (1.06157 * (mapped - 0.628)));
        }else if(mapped < 3.76) {
            return -1;
        }else if(mapped < 5.652) {
            return (float) (-1 + (1.06157 * (mapped - 3.768)));
        }else {
            return 1;
        }
    }

    static public double pieceWiseFast(double value) {
        double mapped = value % 6.28;
        if(mapped < 1.255) return 1;
        else if(mapped < 1.885) {
            return (float) (1 - 3.1831 * (mapped - 1.2566));
        }else if(mapped < 4.3982) {
            return -1;
        }else if(mapped < 5.0266) {
            return (float) (-1 + 3.1831 * (mapped - 4.39823));
        }else {
            return 1;
        }
    }
}
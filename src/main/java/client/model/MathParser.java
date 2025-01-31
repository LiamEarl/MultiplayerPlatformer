package client.model;

public class MathParser {
    static public float performCalculation(float value, String expression) {
        if(expression.equals("#")) return value;

        String[] operations = expression.split(";");
        for(String operation : operations) {
            if(operation.charAt(0) == '+' || operation.charAt(0) == '-') {

                int modifier = 1;
                if(operation.charAt(0) == '+') {
                    operation.replace("+", "");
                }else {
                    operation.replace("-", "");
                    modifier = -1;
                }

                if(operation.contains("picn")) {
                    String[] factors = operation.split("picn");
                    value += (float) (modifier * Float.parseFloat(factors[0]) * pieceWiseNormal((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("picf")) {
                    String[] factors = operation.split("picf");
                    value += (float) (modifier * Float.parseFloat(factors[0]) * pieceWiseFast((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("sin")) {
                    String[] factors = operation.split("sin");
                    value += (float) (modifier * Float.parseFloat(factors[0]) * Math.sin((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("cos")) {
                    String[] factors = operation.split("cos");
                    value += (float) (modifier * Float.parseFloat(factors[0]) * Math.cos((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("pls")) {
                    String[] factors = operation.split("pls");
                    value += (float) (modifier * Float.parseFloat(factors[0]) * pulse((Float.parseFloat(factors[1]) * ((double) System.currentTimeMillis() / 1000) * (3.14*2))));
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
    static public double pulse(double value) {
        double mapped = value % 6.28;
        return Math.pow(2.7182, -1000 * Math.pow(mapped - 3.14159, 2));
    }
}
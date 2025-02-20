package client.model;

public class MathParser {
    static public double performCalculation(double value, String expression, long currentTime) {
        if(expression.equals("#")) return value;

        String[] operations = expression.split(";");
        for(String operation : operations) {
            if(operation.charAt(0) == '+' || operation.charAt(0) == '-') {

                if(operation.charAt(0) == '+') {
                    operation.replace("+", "");
                }else {
                    operation.replace("-", "");
                }

                if(operation.contains("picn")) {
                    String[] factors = operation.split("picn");
                    value += (float) (Float.parseFloat(factors[0]) * pieceWiseNormal((Float.parseFloat(factors[1]) * ((double) currentTime / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("picf")) {
                    String[] factors = operation.split("picf");
                    value += (float) (Float.parseFloat(factors[0]) * pieceWiseFast((Float.parseFloat(factors[1]) * ((double) currentTime / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("sin")) {
                    String[] factors = operation.split("sin");
                    value += (float) (Float.parseFloat(factors[0]) * Math.sin((Float.parseFloat(factors[1]) * ((double) currentTime / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("cos")) {
                    String[] factors = operation.split("cos");
                    value += (float) (Float.parseFloat(factors[0]) * Math.cos((Float.parseFloat(factors[1]) * ((double) currentTime / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("fpls")) {
                    String[] factors = operation.split("fpls");
                    value += (float) (Float.parseFloat(factors[0]) * frequentPulse((Float.parseFloat(factors[1]) * ((double) currentTime / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("pls")) {
                    String[] factors = operation.split("pls");
                    value += (float) (Float.parseFloat(factors[0]) * pulse((Float.parseFloat(factors[1]) * ((double) currentTime / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("lins")) {
                    String[] factors = operation.split("lins");
                    value += (float) (Float.parseFloat(factors[0]) * linearSpike((Float.parseFloat(factors[1]) * ((double) currentTime / 1000) * (3.14*2))));
                    continue;
                }else if(operation.contains("lind")) {
                    String[] factors = operation.split("lind");
                    value += (float) (Float.parseFloat(factors[0]) * linearDropoff((Float.parseFloat(factors[1]) * ((double) currentTime / 1000) * (3.14*2))));
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
            return (1 - (1.06157 * (mapped - 0.628)));
        }else if(mapped < 3.76) {
            return -1;
        }else if(mapped < 5.652) {
            return (-1 + (1.06157 * (mapped - 3.768)));
        }else {
            return 1;
        }
    }

    static public double pieceWiseFast(double value) {
        double mapped = value % 6.28;
        if(mapped < 1.255) return 1;
        else if(mapped < 1.885) {
            return (1 - 3.1831 * (mapped - 1.2566));
        }else if(mapped < 4.3982) {
            return -1;
        }else if(mapped < 5.0266) {
            return (-1 + 3.1831 * (mapped - 4.39823));
        }else {
            return 1;
        }
    }
    static public double linearSpike(double value) {
        double mapped = value % 6.28;
        if(mapped < Math.PI) return (1 / Math.PI) * mapped;
        return -(1 / Math.PI) * (mapped - (2 * Math.PI));
    }
    static public double pulse(double value) {
        double mapped = value % 6.28;
        return Math.pow(2.7182, -50 * Math.pow(mapped - 3.14159, 2));
    }
    static public double frequentPulse(double value) {
        double mapped = (value) % (Math.PI / 2);
        return Math.pow(2.7182, -50 * Math.pow(mapped - (Math.PI / 4), 2));
    }
    static public double linearDropoff(double value) {
        double mapped = (value) % (Math.PI * 2);
        return (1 / (2 * Math.PI)) * mapped;
    }
}
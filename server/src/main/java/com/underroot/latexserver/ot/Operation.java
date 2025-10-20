package com.underroot.latexserver.ot;

public class Operation {
    private final String type;
    private final int position;
    private final String text; // For insert
    private final int length; // For delete

    public Operation(String type, int position, String text, int length) {
        this.type = type;
        this.position = position;
        this.text = text;
        this.length = length;
    }

    public String apply(String content) {
        if ("INSERT".equals(type)) {
            return content.substring(0, position) + text + content.substring(position);
        }
        if ("DELETE".equals(type)) {
            return content.substring(0, position) + content.substring(position + length);
        }
        return content;
    }

    public static Operation transform(Operation op1, Operation op2) {
        // This is a simplified transformation function. A real implementation would be more complex.
        if (op1.type.equals("INSERT") && op2.type.equals("INSERT")) {
            if (op1.position < op2.position) {
                return new Operation(op2.type, op2.position + op1.text.length(), op2.text, op2.length);
            } else {
                return new Operation(op2.type, op2.position, op2.text, op2.length);
            }
        }
        // Add more transformation logic for other combinations (INSERT/DELETE, DELETE/INSERT, DELETE/DELETE)
        return op2; // Default case
    }

    public String getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public int getLength() {
        return length;
    }
}

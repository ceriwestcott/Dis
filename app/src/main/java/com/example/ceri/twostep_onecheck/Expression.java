package com.example.ceri.twostep_onecheck;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Created by Ceri on 06-Jan-17.
 */



public class Expression {

    /**
     * Definition of PI as a constant, can be used in expressions as variable.
     */
    public static final BigDecimal PI = new BigDecimal(
            "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679");

    /**
     * Definition of e: "Euler's number" as a constant, can be used in expressions as variable.
     */
    public static final BigDecimal e = new BigDecimal(
            "2.71828182845904523536028747135266249775724709369995957496696762772407663");


    /**
     * The {@link MathContext} to use for calculations.
     */
    private MathContext mc = null;

    /**
     * The original infix expression.
     */
    private final String originalExpression;

    /**
     * The current infix expression, with optional variable substitutions.
     */
    private String expression = null;

    /**
     * The cached RPN (Reverse Polish Notation) of the expression.
     */
    private List<String> rpn = null;

    /**
     * All defined operators with name and implementation.
     */
    private Map<String, Operator> operators = new TreeMap<String, Operator>(String.CASE_INSENSITIVE_ORDER);

    /**
     * All defined functions with name and implementation.
     */
    private Map<String,LazyFunction> functions = new TreeMap<String, LazyFunction>(String.CASE_INSENSITIVE_ORDER);

    /**
     * All defined variables with name and value.
     */
    private Map<String, BigDecimal> variables = new TreeMap<String, BigDecimal>(String.CASE_INSENSITIVE_ORDER);

    /**
     * What character to use for decimal separators.
     */
    private static final char decimalSeparator = '.';

    /**
     * What character to use for minus sign (negative values).
     */
    private static final char minusSign = '-';

    /**
     * The BigDecimal representation of the left parenthesis,
     * used for parsing varying numbers of function parameters.
     */
    private static final LazyNumber PARAMS_START = new LazyNumber() {
        public BigDecimal eval() {
            return null;
        }
    };

    /**
     * The expression evaluators exception class.
     */
    public static class ExpressionException extends RuntimeException {
        private static final long serialVersionUID = 1118142866870779047L;

        public ExpressionException(String message) {
            super(message);
        }
    }


    /**
     * LazyNumber interface created for lazily evaluated functions
     */
    interface LazyNumber {
        BigDecimal eval();
    }

    public abstract class LazyFunction {
        /**
         * Name of this function.
         */
        private String name;
        /**
         * Number of parameters expected for this function.
         * <code>-1</code> denotes a variable number of parameters.
         */
        private int numParams;

        /**
         * Creates a new function with given name and parameter count.
         *
         * @param name
         *            The name of the function.
         * @param numParams
         *            The number of parameters for this function.
         *            <code>-1</code> denotes a variable number of parameters.
         */
        public LazyFunction(String name, int numParams) {
            this.name = name.toUpperCase(Locale.ROOT);
            this.numParams = numParams;
        }

        public String getName() {
            return name;
        }

        public int getNumParams() {
            return numParams;
        }

        public boolean numParamsVaries() {
            return numParams < 0;
        }
        public abstract LazyNumber lazyEval(List<LazyNumber> lazyParams);
    }



    /**
     * Abstract definition of a supported expression function. A function is
     * defined by a name, the number of parameters and the actual processing
     * implementation.
     */
    public abstract class Function extends LazyFunction {

        public Function(String name, int numParams) {
            super(name, numParams);
        }

        public LazyNumber lazyEval(List<LazyNumber> lazyParams) {
            final List<BigDecimal> params = new ArrayList<BigDecimal>();
            for (LazyNumber lazyParam : lazyParams) {
                params.add(lazyParam.eval());
            }
            return new LazyNumber() {
                public BigDecimal eval() {
                    return Function.this.eval(params);
                }
            };
        }

        /**
         * Implementation for this function.
         *
         * @param parameters
         *            Parameters will be passed by the expression evaluator as a
         *            {@link List} of {@link BigDecimal} values.
         * @return The function must return a new {@link BigDecimal} value as a
         *         computing result.
         */
        public abstract BigDecimal eval(List<BigDecimal> parameters);
    }
    /**
     * Abstract definition of a supported operator. An operator is defined by
     * its name (pattern), precedence and if it is left- or right associative.
     */
    public abstract class Operator {
        /**
         * This operators name (pattern).
         */
        private String oper;
        /**
         * Operators precedence.
         */
        private int precedence;
        /**
         * Operator is left associative.
         */
        private boolean leftAssoc;

        /**
         * Creates a new operator.
         *
         * @param oper
         *            The operator name (pattern).
         * @param precedence
         *            The operators precedence.
         * @param leftAssoc
         *            <code>true</code> if the operator is left associative,
         *            else <code>false</code>.
         */
        public Operator(String oper, int precedence, boolean leftAssoc) {
            this.oper = oper;
            this.precedence = precedence;
            this.leftAssoc = leftAssoc;
        }

        public String getOper() {
            return oper;
        }

        public int getPrecedence() {
            return precedence;
        }

        public boolean isLeftAssoc() {
            return leftAssoc;
        }

        /**
         * Implementation for this operator.
         *
         * @param v1
         *            Operand 1.
         * @param v2
         *            Operand 2.
         * @return The result of the operation.
         */
        public abstract BigDecimal eval(BigDecimal v1, BigDecimal v2);
    }

    /**
     * Expression tokenizer that allows to iterate over a {@link String}
     * expression token by token. Blank characters will be skipped.
     */
    private class Tokenizer implements Iterator<String> {

        /**
         * Actual position in expression string.
         */
        private int pos = 0;

        /**
         * The original input expression.
         */
        private String input;
        /**
         * The previous token or <code>null</code> if none.
         */
        private String previousToken;

        /**
         * Creates a new tokenizer for an expression.
         *
         * @param input
         *            The expression string.
         */
        public Tokenizer(String input) {
            this.input = input.trim();
        }

        @Override
        public boolean hasNext() {
            return (pos < input.length());
        }

        /**
         * Peek at the next character, without advancing the iterator.
         *
         * @return The next character or character 0, if at end of string.
         */
        private char peekNextChar() {
            if (pos < (input.length() - 1)) {
                return input.charAt(pos + 1);
            } else {
                return 0;
            }
        }

        @Override
        public String next() {
            StringBuilder token = new StringBuilder();
            if (pos >= input.length()) {
                return previousToken = null;
            }
            char ch = input.charAt(pos);
            while (Character.isWhitespace(ch) && pos < input.length()) {
                ch = input.charAt(++pos);
            }
            if (Character.isDigit(ch)) {
                while ((Character.isDigit(ch) || ch == decimalSeparator
                        || ch == 'e' || ch == 'E'
                        || (ch == minusSign && token.length() > 0
                        && ('e'==token.charAt(token.length()-1) || 'E'==token.charAt(token.length()-1)))
                        || (ch == '+' && token.length() > 0
                        && ('e'==token.charAt(token.length()-1) || 'E'==token.charAt(token.length()-1)))
                ) && (pos < input.length())) {
                    token.append(input.charAt(pos++));
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                }
            } else if (ch == minusSign
                    && Character.isDigit(peekNextChar())
                    && ("(".equals(previousToken) || ",".equals(previousToken)
                    || previousToken == null || operators
                    .containsKey(previousToken))) {
                token.append(minusSign);
                pos++;
                token.append(next());
            } else if (Character.isLetter(ch) || (ch == '_')) {
                while ((Character.isLetter(ch) || Character.isDigit(ch) || (ch == '_'))
                        && (pos < input.length())) {
                    token.append(input.charAt(pos++));
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                }
            } else if (ch == '(' || ch == ')' || ch == ',') {
                token.append(ch);
                pos++;
            } else {
                while (!Character.isLetter(ch) && !Character.isDigit(ch)
                        && ch != '_' && !Character.isWhitespace(ch)
                        && ch != '(' && ch != ')' && ch != ','
                        && (pos < input.length())) {
                    token.append(input.charAt(pos));
                    pos++;
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                    if (ch == minusSign) {
                        break;
                    }
                }
                if (!operators.containsKey(token.toString())) {
                    throw new ExpressionException("Unknown operator '" + token
                            + "' at position " + (pos - token.length() + 1));
                }
            }
            return previousToken = token.toString();
        }

        @Override
        public void remove() {
            throw new ExpressionException("remove() not supported");
        }

        /**
         * Get the actual character position in the string.
         *
         * @return The actual character position.
         */
        public int getPos() {
            return pos;
        }

    }

    /**
     * Creates a new expression instance from an expression string with a given
     * default match context of {@link MathContext#DECIMAL32}.
     *
     * @param expression
     *            The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
     *            <code>"sin(y)>0 & max(z, 3)>3"</code>
     */
    public Expression(String expression) {
        this(expression, MathContext.DECIMAL32);
    }

    /**
     * Creates a new expression instance from an expression string with a given
     * default match context.
     *
     * @param expression
     *            The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
     *            <code>"sin(y)>0 & max(z, 3)>3"</code>
     * @param defaultMathContext
     *            The {@link MathContext} to use by default.
     */
    public Expression(String expression, MathContext defaultMathContext) {
        this.mc = defaultMathContext;
        this.expression = expression;
        this.originalExpression = expression;
        addOperator(new Operator("+", 20, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.add(v2, mc);
            }
        });
        addOperator(new Operator("-", 20, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.subtract(v2, mc);
            }
        });
        addOperator(new Operator("*", 30, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.multiply(v2, mc);
            }
        });
        addOperator(new Operator("/", 30, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.divide(v2, mc);
            }
        });
        addOperator(new Operator("%", 30, true) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.remainder(v2, mc);
            }
        });
        addOperator(new Operator("^", 40, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
				/*-
				 * Thanks to Gene Marin:
				 * http://stackoverflow.com/questions/3579779/how-to-do-a-fractional-power-on-bigdecimal-in-java
				 */
                int signOf2 = v2.signum();
                double dn1 = v1.doubleValue();
                v2 = v2.multiply(new BigDecimal(signOf2)); // n2 is now positive
                BigDecimal remainderOf2 = v2.remainder(BigDecimal.ONE);
                BigDecimal n2IntPart = v2.subtract(remainderOf2);
                BigDecimal intPow = v1.pow(n2IntPart.intValueExact(), mc);
                BigDecimal doublePow = new BigDecimal(Math.pow(dn1,
                        remainderOf2.doubleValue()));

                BigDecimal result = intPow.multiply(doublePow, mc);
                if (signOf2 == -1) {
                    result = BigDecimal.ONE.divide(result, mc.getPrecision(),
                            RoundingMode.HALF_UP);
                }
                return result;
            }
        });
        addOperator(new Operator("&&", 4, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                boolean b1 = !v1.equals(BigDecimal.ZERO);
                boolean b2 = !v2.equals(BigDecimal.ZERO);
                return b1 && b2 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("||", 2, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                boolean b1 = !v1.equals(BigDecimal.ZERO);
                boolean b2 = !v2.equals(BigDecimal.ZERO);
                return b1 || b2 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator(">", 10, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) == 1 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator(">=", 10, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) >= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("<", 10, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) == -1 ? BigDecimal.ONE
                        : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("<=", 10, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) <= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("=", 7, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) == 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });
        addOperator(new Operator("==", 7, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return operators.get("=").eval(v1, v2);
            }
        });

        addOperator(new Operator("!=", 7, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return v1.compareTo(v2) != 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });
        addOperator(new Operator("<>", 7, false) {
            @Override
            public BigDecimal eval(BigDecimal v1, BigDecimal v2) {
                return operators.get("!=").eval(v1, v2);
            }
        });

        addFunction(new Function("NOT", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                boolean zero = parameters.get(0).compareTo(BigDecimal.ZERO) == 0;
                return zero ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addLazyFunction(new LazyFunction("IF", 3) {
            @Override
            public LazyNumber lazyEval(List<LazyNumber> lazyParams) {
                boolean isTrue = !lazyParams.get(0).eval().equals(BigDecimal.ZERO);
                return isTrue ? lazyParams.get(1) : lazyParams.get(2);
            }
        });

        addFunction(new Function("RANDOM", 0) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.random();
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("SIN", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.sin(Math.toRadians(parameters.get(0)
                        .doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("COS", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.cos(Math.toRadians(parameters.get(0)
                        .doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("TAN", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.tan(Math.toRadians(parameters.get(0)
                        .doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("ASIN", 1) { // added by av
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.toDegrees(Math.asin(parameters.get(0)
                        .doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("ACOS", 1) { // added by av
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.toDegrees(Math.acos(parameters.get(0)
                        .doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("ATAN", 1) { // added by av
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.toDegrees(Math.atan(parameters.get(0)
                        .doubleValue()));
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("SINH", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.sinh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("COSH", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.cosh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("TANH", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.tanh(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("RAD", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.toRadians(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("DEG", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.toDegrees(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("MAX", -1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                if (parameters.size() == 0) {
                    throw new ExpressionException("MAX requires at least one parameter");
                }
                BigDecimal max = null;
                for (BigDecimal parameter : parameters) {
                    if (max == null || parameter.compareTo(max) > 0) {
                        max = parameter;
                    }
                }
                return max;
            }
        });
        addFunction(new Function("MIN", -1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                if (parameters.size() == 0) {
                    throw new ExpressionException("MIN requires at least one parameter");
                }
                BigDecimal min = null;
                for (BigDecimal parameter : parameters) {
                    if (min == null || parameter.compareTo(min) < 0) {
                        min = parameter;
                    }
                }
                return min;
            }
        });
        addFunction(new Function("ABS", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                return parameters.get(0).abs(mc);
            }
        });
        addFunction(new Function("LOG", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.log(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("LOG10", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                double d = Math.log10(parameters.get(0).doubleValue());
                return new BigDecimal(d, mc);
            }
        });
        addFunction(new Function("ROUND", 2) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                BigDecimal toRound = parameters.get(0);
                int precision = parameters.get(1).intValue();
                return toRound.setScale(precision, mc.getRoundingMode());
            }
        });
        addFunction(new Function("FLOOR", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                BigDecimal toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.FLOOR);
            }
        });
        addFunction(new Function("CEILING", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                BigDecimal toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.CEILING);
            }
        });
        addFunction(new Function("SQRT", 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
				/*
				 * From The Java Programmers Guide To numerical Computing
				 * (Ronald Mak, 2003)
				 */
                BigDecimal x = parameters.get(0);
                if (x.compareTo(BigDecimal.ZERO) == 0) {
                    return new BigDecimal(0);
                }
                if (x.signum() < 0) {
                    throw new ExpressionException(
                            "Argument to SQRT() function must not be negative");
                }
                BigInteger n = x.movePointRight(mc.getPrecision() << 1)
                        .toBigInteger();

                int bits = (n.bitLength() + 1) >> 1;
                BigInteger ix = n.shiftRight(bits);
                BigInteger ixPrev;

                do {
                    ixPrev = ix;
                    ix = ix.add(n.divide(ix)).shiftRight(1);
                    // Give other threads a chance to work;
                    Thread.yield();
                } while (ix.compareTo(ixPrev) != 0);

                return new BigDecimal(ix, mc.getPrecision());
            }
        });

        variables.put("e", e);
        variables.put("PI", PI);
        variables.put("TRUE", BigDecimal.ONE);
        variables.put("FALSE", BigDecimal.ZERO);

    }

    /**
     * Is the string a number?
     *
     * @param st
     *            The string.
     * @return <code>true</code>, if the input string is a number.
     */
    private boolean isNumber(String st) {
        if (st.charAt(0) == minusSign && st.length() == 1) return false;
        if (st.charAt(0) == '+' && st.length() == 1) return false;
        if (st.charAt(0) == 'e' ||  st.charAt(0) == 'E') return false;
        for (char ch : st.toCharArray()) {
            if (!Character.isDigit(ch) && ch != minusSign
                    && ch != decimalSeparator
                    && ch != 'e' && ch != 'E' && ch != '+')
                return false;
        }
        return true;
    }

    /**
     * Implementation of the <i>Shunting Yard</i> algorithm to transform an
     * infix expression to a RPN expression.
     *
     * @param expression
     *            The input expression in infx.
     * @return A RPN representation of the expression, with each token as a list
     *         member.
     */
    private List<String> shuntingYard(String expression) {
        List<String> outputQueue = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();

        Tokenizer tokenizer = new Tokenizer(expression);

        String lastFunction = null;
        String previousToken = null;
        while (tokenizer.hasNext()) {
            String token = tokenizer.next();
            if (isNumber(token)) {
                outputQueue.add(token);
            } else if (variables.containsKey(token)) {
                outputQueue.add(token);
            } else if (functions.containsKey(token.toUpperCase(Locale.ROOT))) {
                stack.push(token);
                lastFunction = token;
            } else if (Character.isLetter(token.charAt(0))) {
                stack.push(token);
            } else if (",".equals(token)) {
                if (operators.containsKey(previousToken)) {
                    throw new ExpressionException("Missing parameter(s) for operator " + previousToken +
                            " at character position " + (tokenizer.getPos() - 1 - previousToken.length()));
                }
                while (!stack.isEmpty() && !"(".equals(stack.peek())) {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new ExpressionException("Parse error for function '"
                            + lastFunction + "'");
                }
            } else if (operators.containsKey(token)) {
                if (",".equals(previousToken) || "(".equals(previousToken)) {
                    throw new ExpressionException("Missing parameter(s) for operator " + token +
                            " at character position " + (tokenizer.getPos() - token.length()));
                }
                Operator o1 = operators.get(token);
                String token2 = stack.isEmpty() ? null : stack.peek();
                while (token2!=null &&
                        operators.containsKey(token2)
                        && ((o1.isLeftAssoc() && o1.getPrecedence() <= operators
                        .get(token2).getPrecedence()) || (o1
                        .getPrecedence() < operators.get(token2)
                        .getPrecedence()))) {
                    outputQueue.add(stack.pop());
                    token2 = stack.isEmpty() ? null : stack.peek();
                }
                stack.push(token);
            } else if ("(".equals(token)) {
                if (previousToken != null) {
                    if (isNumber(previousToken)) {
                        throw new ExpressionException(
                                "Missing operator at character position "
                                        + tokenizer.getPos());
                    }
                    // if the ( is preceded by a valid function, then it
                    // denotes the start of a parameter list
                    if (functions.containsKey(previousToken.toUpperCase(Locale.ROOT))) {
                        outputQueue.add(token);
                    }
                }
                stack.push(token);
            } else if (")".equals(token)) {
                if (operators.containsKey(previousToken)) {
                    throw new ExpressionException("Missing parameter(s) for operator " + previousToken +
                            " at character position " + (tokenizer.getPos() - 1 - previousToken.length()));
                }
                while (!stack.isEmpty() && !"(".equals(stack.peek())) {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new ExpressionException("Mismatched parentheses");
                }
                stack.pop();
                if (!stack.isEmpty()
                        && functions.containsKey(stack.peek().toUpperCase(
                        Locale.ROOT))) {
                    outputQueue.add(stack.pop());
                }
            }
            previousToken = token;
        }
        while (!stack.isEmpty()) {
            String element = stack.pop();
            if ("(".equals(element) || ")".equals(element)) {
                throw new ExpressionException("Mismatched parentheses");
            }
            if (!operators.containsKey(element)) {
                throw new ExpressionException("Unknown operator or function: "
                        + element);
            }
            outputQueue.add(element);
        }
        return outputQueue;
    }

    /**
     * Evaluates the expression.
     *
     * @return The result of the expression.
     */
    public BigDecimal eval() {

        Stack<LazyNumber> stack = new Stack<LazyNumber>();

        for (final String token : getRPN()) {
            if (operators.containsKey(token)) {
                final LazyNumber v1 = stack.pop();
                final LazyNumber v2 = stack.pop();
                LazyNumber number = new LazyNumber() {
                    public BigDecimal eval() {
                        return operators.get(token).eval(v2.eval(), v1.eval());
                    }
                };
                stack.push(number);
            } else if (variables.containsKey(token)) {
                stack.push(new LazyNumber() {
                    public BigDecimal eval() {
                        return variables.get(token).round(mc);
                    }
                });
            } else if (functions.containsKey(token.toUpperCase(Locale.ROOT))) {
                LazyFunction f = functions.get(token.toUpperCase(Locale.ROOT));
                ArrayList<LazyNumber> p = new ArrayList<LazyNumber>(
                        !f.numParamsVaries() ? f.getNumParams() : 0);
                // pop parameters off the stack until we hit the start of
                // this function's parameter list
                while (!stack.isEmpty() && stack.peek() != PARAMS_START) {
                    p.add(0, stack.pop());
                }
                if (stack.peek() == PARAMS_START) {
                    stack.pop();
                }
                LazyNumber fResult = f.lazyEval(p);
                stack.push(fResult);
            } else if ("(".equals(token)) {
                stack.push(PARAMS_START);
            } else {
                stack.push(new LazyNumber() {
                    public BigDecimal eval() {
                        return new BigDecimal(token, mc);
                    }
                });
            }
        }
        return stack.pop().eval().stripTrailingZeros();
    }

    /**
     * Sets the precision for expression evaluation.
     *
     * @param precision
     *            The new precision.
     *
     * @return The expression, allows to chain methods.
     */
    public Expression setPrecision(int precision) {
        this.mc = new MathContext(precision);
        return this;
    }

    /**
     * Sets the rounding mode for expression evaluation.
     *
     * @param roundingMode
     *            The new rounding mode.
     * @return The expression, allows to chain methods.
     */
    public Expression setRoundingMode(RoundingMode roundingMode) {
        this.mc = new MathContext(mc.getPrecision(), roundingMode);
        return this;
    }

    /**
     * Adds an operator to the list of supported operators.
     *
     * @param operator
     *            The operator to add.
     * @return The previous operator with that name, or <code>null</code> if
     *         there was none.
     */
    public Operator addOperator(Operator operator) {
        return operators.put(operator.getOper(), operator);
    }

    /**
     * Adds a function to the list of supported functions
     *
     * @param function
     *            The function to add.
     * @return The previous operator with that name, or <code>null</code> if
     *         there was none.
     */
    public Function addFunction(Function function) {
        return (Function) functions.put(function.getName(), function);
    }

    /**
     * Adds a lazy function function to the list of supported functions
     *
     * @param function
     *            The function to add.
     * @return The previous operator with that name, or <code>null</code> if
     *         there was none.
     */
    public LazyFunction addLazyFunction(LazyFunction function) {
        return  functions.put(function.getName(), function);
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable name.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression setVariable(String variable, BigDecimal value) {
        variables.put(variable, value);
        return this;
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression setVariable(String variable, String value) {
        if (isNumber(value))
            variables.put(variable, new BigDecimal(value));
        else {
            expression = expression.replaceAll("(?i)\\b" + variable + "\\b", "("
                    + value + ")");
            rpn = null;
        }
        return this;
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with(String variable, BigDecimal value) {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression and(String variable, String value) {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression and(String variable, BigDecimal value) {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable
     *            The variable to set.
     * @param value
     *            The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with(String variable, String value) {
        return setVariable(variable, value);
    }

    /**
     * Get an iterator for this expression, allows iterating over an expression
     * token by token.
     *
     * @return A new iterator instance for this expression.
     */
    public Iterator<String> getExpressionTokenizer() {
        return new Tokenizer(this.expression);
    }

    /**
     * Cached access to the RPN notation of this expression, ensures only one
     * calculation of the RPN per expression instance. If no cached instance
     * exists, a new one will be created and put to the cache.
     *
     * @return The cached RPN instance.
     */
    private List<String> getRPN() {
        if (rpn == null) {
            rpn = shuntingYard(this.expression);
            validate(rpn);
        }
        return rpn;
    }

    /**
     * Check that the expression has enough numbers and variables to fit the
     * requirements of the operators and functions, also check
     * for only 1 result stored at the end of the evaluation.
     */
    private void validate(List<String> rpn) {
		/*-
		* Thanks to Norman Ramsey:
		* http://http://stackoverflow.com/questions/789847/postfix-notation-validation
		*/
        // each push on to this stack is a new function scope, with the value of each
        // layer on the stack being the count of the number of parameters in that scope
        Stack<Integer> stack = new Stack<Integer>();

        // push the 'global' scope
        stack.push(0);

        for (final String token : rpn) {
            if (operators.containsKey(token)) {
                if (stack.peek() < 2) {
                    throw new ExpressionException("Missing parameter(s) for operator " + token);
                }
                // pop the operator's 2 parameters and add the result
                stack.set(stack.size() - 1, stack.peek() - 2 + 1);
            } else if (variables.containsKey(token)) {
                stack.set(stack.size() - 1, stack.peek() + 1);
            } else if (functions.containsKey(token.toUpperCase(Locale.ROOT))) {
                LazyFunction f = functions.get(token.toUpperCase(Locale.ROOT));
                int numParams = stack.pop();
                if (!f.numParamsVaries() && numParams != f.getNumParams()) {
                    throw new ExpressionException("Function " + token + " expected " + f.getNumParams() + " parameters, got " + numParams);
                }
                if (stack.size() <= 0) {
                    throw new ExpressionException("Too many function calls, maximum scope exceeded");
                }
                // push the result of the function
                stack.set(stack.size() - 1, stack.peek() + 1);
            } else if ("(".equals(token)) {
                stack.push(0);
            } else {
                stack.set(stack.size() - 1, stack.peek() + 1);
            }
        }

        if (stack.size() > 1) {
            throw new ExpressionException("Too many unhandled function parameter lists");
        } else if (stack.peek() > 1) {
            throw new ExpressionException("Too many numbers or variables");
        } else if (stack.peek() < 1) {
            throw new ExpressionException("Empty expression");
        }
    }

    /**
     * Get a string representation of the RPN (Reverse Polish Notation) for this
     * expression.
     *
     * @return A string with the RPN representation for this expression.
     */
    public String toRPN() {
        StringBuilder result = new StringBuilder();
        for (String st : getRPN()) {
            if (result.length() != 0)
                result.append(" ");
            result.append(st);
        }
        return result.toString();
    }

    /**
     * Exposing declared variables in the expression.
     * @return All declared variables.
     */
    public Set<String> getDeclaredVariables() {
        return Collections.unmodifiableSet(variables.keySet());
    }

    /**
     * Exposing declared operators in the expression.
     * @return All declared operators.
     */
    public Set<String> getDeclaredOperators() {
        return Collections.unmodifiableSet(operators.keySet());
    }

    /**
     * Exposing declared functions.
     * @return All declared functions.
     */
    public Set<String> getDeclaredFunctions() {
        return Collections.unmodifiableSet(functions.keySet());
    }

    /**
     * @return The original expression string
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Returns a list of the variables in the expression.
     *
     * @return A list of the variable names in this expression.
     */
    public List<String> getUsedVariables() {
        List<String> result = new ArrayList<String>();
        Tokenizer tokenizer = new Tokenizer(expression);
        while (tokenizer.hasNext()) {
            String token = tokenizer.next();
            if (functions.containsKey(token) || operators.containsKey(token)
                    || token.equals("(") || token.equals(")")
                    || token.equals(",") || isNumber(token)
                    || token.equals("PI") || token.equals("e")
                    || token.equals("TRUE") || token.equals("FALSE")) {
                continue;
            }
            result.add(token);
        }
        return result;
    }


    /**
     * The original expression used to construct this expression, without
     * variables substituted.
     */
    public String getOriginalExpression() {
        return this.originalExpression;
    }


    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expression that = (Expression) o;
        if (this.expression == null) {
            return that.expression == null;
        } else {
            return this.expression.equals(that.expression);
        }
    }


    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return this.expression == null ? 0 : this.expression.hashCode();
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return this.expression;
    }

}


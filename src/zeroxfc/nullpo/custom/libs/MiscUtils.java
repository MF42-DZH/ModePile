package zeroxfc.nullpo.custom.libs;

import java.math.BigInteger;
import java.util.LinkedList;

public final class MiscUtils {
    public static final class Numerics {
        private static boolean numIs(BigInteger num, long val) {
            return num.compareTo(BigInteger.valueOf(val)) == 0;
        }

        private static String nameTo9(BigInteger num) {
            if (numIs(num, 1)) return "ONE";
            else if (numIs(num, 2)) return "TWO";
            else if (numIs(num, 3)) return "THREE";
            else if (numIs(num, 4)) return "FOUR";
            else if (numIs(num, 5)) return "FIVE";
            else if (numIs(num, 6)) return "SIX";
            else if (numIs(num, 7)) return "SEVEN";
            else if (numIs(num, 8)) return "EIGHT";
            else if (numIs(num, 9)) return "NINE";
            else return "";
        }

        private static String nameTens(BigInteger num) {
            if (numIs(num, 2)) return "TWENTY";
            else if (numIs(num, 3)) return "THIRTY";
            else if (numIs(num, 4)) return "FORTY";
            else if (numIs(num, 5)) return "FIFTY";
            else if (numIs(num, 6)) return "SIXTY";
            else if (numIs(num, 7)) return "SEVENTY";
            else if (numIs(num, 8)) return "EIGHTY";
            else if (numIs(num, 9)) return "NINETY";
            else return "";
        }

        // PRE: 1 <= num < 100.
        private static String nameTo99(BigInteger num) {
            // Special cases:
            if (numIs(num, 10)) return "TEN";
            else if (numIs(num, 11)) return "ELEVEN";
            else if (numIs(num, 12)) return "TWELVE";
            else if (numIs(num, 13)) return "THIRTEEN";
            else if (numIs(num, 14)) return "FOURTEEN";
            else if (numIs(num, 15)) return "FIFTEEN";
            else if (numIs(num, 16)) return "SIXTEEN";
            else if (numIs(num, 17)) return "SEVENTEEN";
            else if (numIs(num, 18)) return "EIGHTEEN";
            else if (numIs(num, 19)) return "NINETEEN";

            final String unitComponent = nameTo9(num.mod(BigInteger.TEN));
            final String tensComponent = nameTens(num.divide(BigInteger.TEN));

            if (unitComponent.isEmpty()) return tensComponent;
            else if (tensComponent.isEmpty()) return unitComponent;
            else return tensComponent + "-" + unitComponent;
        }

        // PRE: 1 <= num < 1000
        private static String nameTo999(BigInteger num, boolean and) {
            final String hundreds = nameTo9(num.divide(BigInteger.valueOf(100)));
            final String remainder = nameTo99(num.mod(BigInteger.valueOf(100)));

            if (hundreds.isEmpty()) return remainder;
            else if (remainder.isEmpty()) return hundreds;
            else {
                final String sep = and ? " HUNDRED AND " : " HUNDRED ";
                return hundreds + sep + remainder;
            }
        }

        private static String nameTo999999(BigInteger num, boolean and) {
            final String thousands = nameTo999(num.divide(BigInteger.valueOf(1000)), and);
            final String remainder = nameTo999(num.mod(BigInteger.valueOf(1000)), and);

            if (thousands.isEmpty()) return remainder;
            else if (remainder.isEmpty()) return thousands;
            else return thousands + " THOUSAND " + remainder;
        }

        // Minus or Negative?
        public enum BelowZeroPrefix {
            MINUS, NEGATIVE
        }

        private static final String[] BELOW_DECI = {
            "", "MI", "BI", "TRI", "QUADRI", "QUINTI", "SEXTI", "SEPTI", "OCTI", "NONI"
        };

        private static String getMainAffix(long n) {
            if (n <= 0) return "";
            else if (n < 10L) return BELOW_DECI[(int) n] + "LLION";
            else if (n < 1000L) {
                final UnitAffixes unit = UnitAffixes.values()[(int) (n % 10L)];
                final TensAffixes ten = TensAffixes.values()[(int) ((n / 10L) % 10L)];
                final HundredsAffixes hundred = HundredsAffixes.values()[(int) ((n / 100L) % 10L)];

                if (ten == TensAffixes.ZERO) return unit.getAffix(hundred) + hundred.getAffix(null) + "LLION";
                else if (hundred == HundredsAffixes.ZERO) {
                    String replaced = ten.getAffix(null);
                    if (replaced.endsWith("A")) replaced = replaced.substring(0, replaced.length() - 1) + "I";

                return unit.getAffix(ten) + replaced + "LLION";
                }
                else return unit.getAffix(ten) + ten.getAffix(null) + hundred.getAffix(null) + "LLION";
            } else {
                throw new NumberFormatException("Index of main affix too large!");
            }
        }

        private static String getFullAffix(BigInteger n) {
            if (n.compareTo(BigInteger.ZERO) < 0) throw new NumberFormatException("Invalid index.");
            else if (n.compareTo(BigInteger.valueOf(1000)) < 0) return getMainAffix(n.longValueExact());
            else {
                final StringBuilder sb = new StringBuilder();
                final LinkedList<Long> groups = new LinkedList<>();

                // First initialize the groups.
                for (BigInteger cN = n; cN.compareTo(BigInteger.ZERO) > 0; cN = cN.divide(BigInteger.valueOf(1000))) {
                    groups.addFirst(cN.mod(BigInteger.valueOf(1000)).longValueExact());
                }

                // Get the group name for the current index.
                for (int i = 0; i < groups.size(); ++i) {
                    final long group = groups.get(i);

                    String suffix = "ILLI";
                    if (i == groups.size() - 1) suffix = suffix + "ON";
                    if (group == 0) suffix = "N" + suffix;

                    if (group == 0) {
                        sb.append(suffix);
                    } else {
                        final String baseAffix = getMainAffix(group);
                        sb.append(baseAffix, 0, baseAffix.length() - 6).append(suffix);
                    }
                }

                return sb.toString();
            }
        }

        private static String nameAbove999999(BigInteger num, boolean and) {
            final StringBuilder sb = new StringBuilder(64);

            BigInteger currentMainAffixIndex = BigInteger.ONE;

            for (BigInteger current = num; !current.equals(BigInteger.ZERO); current = current.divide(BigInteger.valueOf(1000))) {
                final BigInteger rem = current.remainder(BigInteger.valueOf(1000));
                final String remString = nameTo999(rem, and);

                if (!remString.isEmpty()) {
                    sb
                        .insert(0, ' ')
                        .insert(0, getFullAffix(currentMainAffixIndex))
                        .insert(0, ' ')
                        .insert(0, remString);
                }

                currentMainAffixIndex = currentMainAffixIndex.add(BigInteger.ONE);
            }

            return sb.toString();
        }

        private static String baseNumName(BigInteger num, boolean and) {
            if (num.compareTo(BigInteger.valueOf(1_000_000)) < 0) return nameTo999999(num, and);
            else {
                final BigInteger aboveNum = num.subtract(num.remainder(BigInteger.valueOf(1_000_000))).divide(BigInteger.valueOf(1_000_000));

                final String above = nameAbove999999(aboveNum, and);
                final String below = nameTo999999(num.remainder(BigInteger.valueOf(1_000_000)), and);

                return above + below;
            }
        }

        // Affixes for large numbers.
        private interface Affix {
            // Affix properties.
            int PROP_N = (1 << 0);
            int PROP_M = (1 << 1);
            int PROP_S = (1 << 2);
            int PROP_X = (1 << 3);

            int getProperties();
            default boolean hasProperty(int property) {
                return (getProperties() & property) > 0;
            }

            String getAffix(Affix nextAffix);
        }

        // Unit affixes.
        private enum UnitAffixes implements Affix {
            ZERO(""),
            ONE("UN"),
            TWO("DUO"),
            THREE("TRE"),
            FOUR("QUATTUOR"),
            FIVE("QUIN"),
            SIX("SE"),
            SEVEN("SEPTE"),
            EIGHT("OCTO"),
            NINE("NOVE");

            private final String baseAffix;

            UnitAffixes(String baseAffix) {
                this.baseAffix = baseAffix;
            }

            @Override
            public int getProperties() {
                return 0;
            }

            @Override
            public boolean hasProperty(int property) {
                return false;
            }

            @Override
            public String getAffix(Affix nextAffix) {
                if (nextAffix == null) return baseAffix;

                switch (this) {
                    case THREE: return (nextAffix.hasProperty(PROP_S) || nextAffix.hasProperty(PROP_X)) ? baseAffix + "S" : baseAffix;
                    case SIX: {
                        if (nextAffix.hasProperty(PROP_S)) return baseAffix + "S";
                        else if (nextAffix.hasProperty(PROP_X)) return baseAffix + "X";
                        else return baseAffix;
                    }
                    case SEVEN:
                    case NINE: {
                        if (nextAffix.hasProperty(PROP_M)) return baseAffix + "M";
                        else if (nextAffix.hasProperty(PROP_N)) return baseAffix + "N";
                        else return baseAffix;
                    }
                    default: return baseAffix;
                }
            }
        }

        private enum TensAffixes implements Affix {
            ZERO("", 0),
            ONE("DECI", PROP_N),
            TWO("VIGINTI", PROP_M | PROP_S),
            THREE("TRIGINTA", PROP_N | PROP_S),
            FOUR("QUADRAGINTA", PROP_N | PROP_S),
            FIVE("QUINQUAGINTA", PROP_N | PROP_S),
            SIX("SEXAGINTA", PROP_N),
            SEVEN("SEPTUAGINTA", PROP_N),
            EIGHT("OCTOGINTA", PROP_M | PROP_X),
            NINE("NONAGINTA", 0);

            private final String baseAffix;
            private final int property;

            TensAffixes(String baseAffix, int property) {
                this.baseAffix = baseAffix;
                this.property = property;
            }

            @Override
            public int getProperties() {
                return property;
            }

            @Override
            public String getAffix(Affix nextAffix) {
                return baseAffix;
            }
        }

        private enum HundredsAffixes implements Affix {
            ZERO("", 0),
            ONE("CENTI", PROP_N | PROP_X),
            TWO("DUCENTI", PROP_N),
            THREE("TRECENTI", PROP_N | PROP_S),
            FOUR("QUADRINGENTI", PROP_N | PROP_S),
            FIVE("QUINGENTI", PROP_N | PROP_S),
            SIX("SESGENTI", PROP_N),
            SEVEN("SEPTIGENTI", PROP_N),
            EIGHT("OCTIGENTI", PROP_M | PROP_X),
            NINE("NONGENTI", 0);

            private final String baseAffix;
            private final int property;

            HundredsAffixes(String baseAffix, int property) {
                this.baseAffix = baseAffix;
                this.property = property;
            }

            @Override
            public int getProperties() {
                return property;
            }

            @Override
            public String getAffix(Affix nextAffix) {
                return baseAffix;
            }
        }

        /**
         * Get the name of a number using the Short system of counting.
         *
         * @param num         Number to get the name of
         * @param ifBelowZero "MINUS" or "NEGATIVE" for numbers below zero
         * @param and         Insert "AND" between hundreds and tens components
         * @return Name of number given those parameters.
         */
        public static String nameOfNumber(BigInteger num, BelowZeroPrefix ifBelowZero, boolean and) {
            if (numIs(num, 0)) return "ZERO";
            else if (num.compareTo(BigInteger.ZERO) < 0) return ifBelowZero.name() + nameOfNumber(num.abs(), ifBelowZero, and);
            else return baseNumName(num, and);
        }

        /**
         * Overload of {@link Numerics#nameOfNumber(BigInteger, BelowZeroPrefix, boolean)} that uses the default of
         * "MINUS" for negative numbers and the Short system for large numbers, not showing "AND" between each of the
         * hundreds and tens components.
         */
        public static String nameOfNumber(BigInteger num) {
            return nameOfNumber(num, BelowZeroPrefix.MINUS, false);
        }
    }

    // We don't need to instantiate this helper class.
    private MiscUtils() {}
}

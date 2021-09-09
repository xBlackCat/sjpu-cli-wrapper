package org.xblackcat.sjpu.cli;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.xblackcat.sjpu.cli.progress.LogPrintStream;
import org.xblackcat.sjpu.cli.progress.NullPrintStream;
import org.xblackcat.sjpu.cli.progress.Verbosity;
import org.xblackcat.sjpu.cli.reader.ProgressOutStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class OptionUtils {
    private static final char[] multipliers = new char[]{' ', 'k', 'm', 'g', 't'};
    /**
     * Checker for positive value
     */
    public static final Function<Number, Function<String, String>> POSITIVE_VALUE_CHECK = n ->
            n.doubleValue() > 0 ? null : name -> name + " should be positive";
    /**
     * Checker for value to be positive, zero or -1
     */
    public static final Function<Number, Function<String, String>> LIMIT_VALUE_CHECK = n ->
            n.longValue() >= -1 ? null : name -> name + " should be positive, zero or -1";
    public static final Function<Number, Function<String, String>> ANY_VALUE_CHECK = n -> null;
    public static final Function<Number, Function<String, String>> PERCENT_VALUE_CHECK = n ->
            n.doubleValue() < 0 ? name -> name + " can not be negative" :
                    n.doubleValue() > 1 ? name -> name + " can not be greater than 1" :
                            null;


    public static Function<String, LocalDateTime> TO_TIME_PERIOD_BOUND_PARSER = s -> {
        if ("now".equalsIgnoreCase(s)) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            // Fall through
        }

        try {
            return LocalDate.parse(s).atStartOfDay();
        } catch (DateTimeParseException e) {
            // Fall through
        }

        try {
            return LocalDateTime.now().plus(Duration.parse(s));
        } catch (DateTimeParseException e) {
            // Fall through
        }

        throw new IllegalArgumentException("Failed to parse date/time bound: " + s);
    };

    public static Function<String, Duration> TO_DURATION_BOUND_PARSER = s -> {
        if ("0".equalsIgnoreCase(s)) {
            return Duration.ZERO;
        }

        try {
            final Duration duration = Duration.parse(s);
            if (duration.isNegative()) {
                throw new IllegalArgumentException("Duration bound can't be negative: " + s);
            }
            return duration;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Unparsable time shift: " + s, e);
        }
    };

    public static LogPrintStream parseProgressOutputStream(CommandLine line, String optionName) throws InvalidOptionException {
        final Verbosity verbosity = parseVerbosity(line);
        final ProgressOutStream str;
        if (line.hasOption(optionName)) {
            String value = line.getOptionValue(optionName);

            str = ProgressOutStream.parseValue(value);
            if (str == null) {
                throw new InvalidOptionException(
                        "Can not parse options. Please, check the command line. Invalid option value for option progress [" +
                                optionName + "]: " + value
                );
            }
        } else {
            str = ProgressOutStream.Default;
        }
        return new LogPrintStream(verbosity, str.getStream());
    }

    public static Verbosity parseVerbosity(CommandLine cmd) {
        final boolean quiet = cmd.hasOption("q");
        final boolean verbose = cmd.hasOption("v");
        final boolean brief = cmd.hasOption("b");

        final Verbosity verbosity;
        if (verbose) {
            verbosity = Verbosity.Verbose;
        } else if (brief) {
            verbosity = Verbosity.Brief;
        } else if (quiet) {
            verbosity = Verbosity.Quiet;
        } else {
            verbosity = Verbosity.Normal;
        }
        return verbosity;
    }

    public static BufferedReader getReader(CommandLine line, String optionName) throws IOException {
        if (line.hasOption(optionName)) {
            String value = line.getOptionValue(optionName);

            if (!Objects.equals("-", value)) {
                return Files.newBufferedReader(Paths.get(value));
            }
        }

        return new BufferedReader(new InputStreamReader(System.in));
    }

    public static PrintStream getPrintStream(CommandLine line, String fileOptionName) throws IOException {
        return getPrintStream(line, true, fileOptionName);
    }

    public static PrintStream getPrintStream(
            CommandLine line,
            boolean stdOutAsDefault,
            String fileOptionName
    ) throws IOException {
        if (line.hasOption(fileOptionName)) {
            String outFileName = line.getOptionValue(fileOptionName);
            if (Objects.equals("-", outFileName)) {
                return System.out;
            } else {
                return new PrintStream(new BufferedOutputStream(
                        Files.newOutputStream(
                                Paths.get(outFileName),
                                StandardOpenOption.CREATE,
                                StandardOpenOption.TRUNCATE_EXISTING,
                                StandardOpenOption.WRITE
                        )
                ));
            }
        } else if (stdOutAsDefault) {
            return System.out;
        }
        return new NullPrintStream();
    }

    public static <T extends Comparable<? super T>> Bounds<T> getBounds(
            CommandLine line,
            String optionName,
            String description,
            Function<String, T> convert,
            String defaultLowerBound,
            String defaultUpperBound
    ) throws InvalidOptionException {
        return getBounds(line, optionName, description, convert, convert.apply(defaultLowerBound), convert.apply(defaultUpperBound));
    }

    public static <T extends Comparable<? super T>> Bounds<T> getBounds(
            CommandLine line,
            String optionName,
            String description,
            Function<String, T> convert,
            T defaultLowerBound,
            T defaultUpperBound
    ) throws InvalidOptionException {
        String[] limitOption = line.getOptionValues(optionName);
        try {
            T limitLower;
            T limitUpper;
            if (limitOption == null || limitOption.length == 0) {
                limitLower = defaultLowerBound;
                limitUpper = defaultUpperBound;
            } else if (limitOption.length == 1) {
                // Limit bounds are the same
                limitLower = limitUpper = convert.apply(limitOption[0]);
            } else {
                limitLower = convert.apply(limitOption[0]);
                limitUpper = convert.apply(limitOption[1]);
            }

            return new Bounds<>(limitLower, limitUpper);
        } catch (NullPointerException | NumberFormatException e) {
            throw new InvalidOptionException("Can not parse " + description + " limits: " + e.getMessage(), e);
        }
    }

    public static Bounds<Integer> getBounds(
            CommandLine line,
            String optionName,
            String description,
            int defaultLowerBound,
            int defaultUpperBound
    ) throws InvalidOptionException {
        return getBounds(line, optionName, description, Integer::parseInt, defaultLowerBound, defaultUpperBound);
    }

    /**
     * Parse option from command line to read string list. Values could be specified as comma-separated argument of option or file name.
     * To pass file name as source of string list use @ prefix for option value. IllegalArgumentException will be thrown if option is not set.
     * Examples:
     * <p>
     * -&lt;option&gt; "value1,value2,value3"
     * <p>
     * -&lt;option&gt; "@source_file_name"
     *
     * @param line       parsed command line
     * @param optionName option name
     * @return string list parsed from argument or read from external file
     */
    public static String[] getStringList(
            CommandLine line,
            String optionName
    ) throws InvalidOptionException {
        return getStringList(line, optionName, null);
    }

    /**
     * Parse option from command line to read string list. Values could be specified as comma-separated argument of option or file name.
     * To pass file name as source of string list use @ prefix for option value. Examples:
     * <p>
     * -&lt;option&gt; "value1,value2,value3"
     * <p>
     * -&lt;option&gt; "@source_file_name"
     *
     * @param line         parsed command line
     * @param optionName   option name
     * @param defaultValue default value. If default value is null - IllegalArgumentException will be thrown if option is not set
     * @return string list parsed from argument or read from external file
     */
    public static String[] getStringList(
            CommandLine line,
            String optionName,
            String[] defaultValue
    ) throws InvalidOptionException {
        return getObjectList(line, optionName, defaultValue, UnaryOperator.identity(), String[]::new);
    }

    /**
     * Parse option from command line to read list of custom objects. Values could be specified as comma-separated argument of option or file name.
     * To pass file name as source of string list use @ prefix for option value. Examples:
     * <p>
     * -&lt;option&gt; "value1,value2,value3"
     * <p>
     * -&lt;option&gt; "@source_file_name"
     * <p>
     * Values are parsed by parser function and arrayGenerator should provide correct result array for storing values.
     *
     * @param line           parsed command line
     * @param optionName     option name
     * @param defaultValue   default value. If default value is null - IllegalArgumentException will be thrown if option is not set
     * @param parser         string-to-object parser
     * @param arrayGenerator result array generator
     * @return string list parsed from argument or read from external file
     */
    public static <T> T[] getObjectList(
            CommandLine line,
            String optionName,
            T[] defaultValue,
            Function<String, T> parser,
            IntFunction<T[]> arrayGenerator
    ) throws InvalidOptionException {
        final String optionValue = StringUtils.trimToNull(line.getOptionValue(optionName));
        if (optionValue == null) {
            if (defaultValue == null) {
                throw new IllegalArgumentException(optionName + " is not set");
            }
            return defaultValue;
        }

        List<T> result = new ArrayList<>();
        if (!optionValue.startsWith("@")) {
            result = Arrays.stream(StringUtils.splitByWholeSeparator(optionValue, ","))
                           .filter(StringUtils::isNotBlank)
                           .map(parser)
                           .collect(Collectors.toList());
        } else {
            // Read from file or STDIN
            final String fileName = optionValue.substring(1);
            final Reader r;
            if (Objects.equals("-", fileName)) {
                r = new InputStreamReader(System.in);
            } else {
                try {
                    r = new FileReader(fileName);
                } catch (FileNotFoundException e) {
                    throw new InvalidOptionException("Can't open file " + fileName, e);
                }
            }

            try (BufferedReader reader = new BufferedReader(r)) {
                String s;
                while ((s = reader.readLine()) != null) {
                    if (StringUtils.isNotBlank(s)) {
                        result.add(parser.apply(s));
                    }
                }
            } catch (IOException e) {
                throw new InvalidOptionException("Filed to read from " + (Objects.equals("-", fileName) ? "STDIN" : fileName), e);
            }
        }
        return result.stream().toArray(arrayGenerator);
    }

    public static int getIntOption(CommandLine line, String optionName, String name) throws ParseException {
        return getIntOption(line, optionName, null, name);
    }

    public static int getIntOption(
            CommandLine line,
            String optionName,
            Integer defVal,
            String valueName
    ) throws ParseException {
        return getIntOption(line, optionName, POSITIVE_VALUE_CHECK, defVal, valueName);
    }

    public static int getIntOption(
            CommandLine line,
            String optionName,
            Function<Number, Function<String, String>> valueChecker,
            Integer defVal,
            String valueName
    ) throws ParseException {
        return getNumericOption(line, optionName, Number::intValue, valueChecker, defVal, valueName);
    }

    public static long getLongOption(CommandLine line, String optionName, String name) throws ParseException {
        return getLongOption(line, optionName, null, name);
    }

    public static long getLongOption(
            CommandLine line,
            String optionName,
            Long defVal,
            String valueName
    ) throws ParseException {
        return getLongOption(line, optionName, LIMIT_VALUE_CHECK, defVal, valueName);
    }

    public static long getLongOption(
            CommandLine line,
            String optionName,
            Function<Number, Function<String, String>> valueTester,
            Long defVal,
            String valueName
    ) throws ParseException {
        return getNumericOption(line, optionName, Number::longValue, valueTester, defVal, valueName);
    }

    public static double getDoubleOption(CommandLine line, String optionName, String name) throws ParseException {
        return getDoubleOption(line, optionName, null, name);
    }

    public static double getDoubleOption(
            CommandLine line,
            String optionName,
            Double defVal,
            String valueName
    ) throws ParseException {
        return getDoubleOption(line, optionName, Number::doubleValue, defVal, valueName);
    }

    public static double getDoubleOption(
            CommandLine line,
            String optionName,
            Function<Number, Double> valueChecker,
            Double defVal,
            String valueName
    ) throws ParseException {
        return getNumericOption(line, optionName, valueChecker, PERCENT_VALUE_CHECK, defVal, valueName);
    }

    public static <T extends Number> T getNumericOption(
            CommandLine line,
            String optionName,
            Function<Number, T> fieldExtractor,
            Function<? super T, Function<String, String>> valueTester,
            T defVal,
            String valueName
    ) throws ParseException {
        final Number value = (Number) line.getParsedOptionValue(optionName);
        if (value != null) {
            final Function<String, String> exceptionText = valueTester.apply(defVal);
            if (exceptionText == null) {
                return fieldExtractor.apply(value);
            }
            throw new IllegalArgumentException(exceptionText.apply(valueName));
        }

        if (defVal != null) {
            return defVal;
        }
        throw new IllegalArgumentException("No value specified for " + valueName);
    }

    public static CommandLine parseCommandLine(
            String[] args,
            Options options,
            String cmdLineSyntax,
            String description,
            String... requiredAnyOpt
    ) {
        return parseCommandLine(args, true, options, cmdLineSyntax, description, requiredAnyOpt);
    }

    public static CommandLine parseCommandLine(
            String[] args,
            boolean showHelpWithoutArgs,
            Options options,
            String cmdLineSyntax,
            String description,
            String... requiredAnyOpt
    ) {
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        boolean showHelp;
        try {
            line = parser.parse(options, args);
            showHelp = showHelpWithoutArgs && args.length == 0 || line.hasOption('h');

            if (!showHelp && ArrayUtils.isNotEmpty(requiredAnyOpt)) {
                showHelp = true;
                for (String opt : requiredAnyOpt) {
                    if (line.hasOption(opt)) {
                        showHelp = false;
                        break;
                    }
                }
                if (showHelp) {
                    System.err.println("Missing one of required options: " + String.join(", ", (CharSequence[]) requiredAnyOpt));
                }
            }
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            showHelp = true;
        } catch (Throwable e) {
            System.err.println("Unexpected exception: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(5);
            return null;
        }

        if (showHelp) {
            HelpFormatter f = new HelpFormatter();
            final String suffix;
            if (SystemUtils.IS_OS_WINDOWS) {
                suffix = ".exe";
            } else {
                suffix = "";
            }
            PrintWriter pw = new PrintWriter(System.out);

            f.printHelp(
                    pw,
                    HelpFormatter.DEFAULT_WIDTH,
                    cmdLineSyntax + suffix,
                    description,
                    options,
                    HelpFormatter.DEFAULT_LEFT_PAD,
                    HelpFormatter.DEFAULT_DESC_PAD,
                    "",
                    true
            );
            pw.flush();
            System.exit(1);
            return null;
        }
        return line;
    }

    public static Set<String> getStringsAsSet(CommandLine cmd, String opt) {
        final String[] values = cmd.getOptionValues(opt);
        return values != null ? new HashSet<>(Arrays.asList(values)) : Collections.emptySet();
    }

    public static long parseSize(String str) throws NumberFormatException {
        if (str == null) {
            throw new NullPointerException("Empty string");
        }

        if (str.length() == 0) {
            throw new NumberFormatException("Empty string");
        }

        int shift = 0;
        char lastChar = str.charAt(str.length() - 1);
        if (!Character.isDigit(lastChar)) {
            str = str.substring(0, str.length() - 1);

            lastChar = Character.toLowerCase(lastChar);
            // Check for multipliers
            int i = 0;
            do {
                i++;
                shift += 10;
            } while (i < multipliers.length && multipliers[i] != lastChar);

            if (i >= multipliers.length) {
                throw new NumberFormatException("Unknown multiplier specified: " + lastChar);
            }
        }

        return Long.parseLong(str) << shift;
    }
}

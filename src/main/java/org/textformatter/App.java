package org.textformatter;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App implements Closeable {

    private PrintStream out;

    App(PrintStream out) {
        this.out = out;
    }

    void invoke(final String... args) throws IOException {
        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("too few args");
        }
        if (args.length != 3) {
            throw new IllegalArgumentException(String.format("expect 3 args. [FUNCTION] [OUTPUT_LENGTH] [INPUT]. got %d args", args.length));
        }

        final String funcName = args[0];

        switch (funcName) {
            case "j":
            case "h":
                throw new UnsupportedOperationException("not implemented");
            default:
        }

        final int width;
        try {
            width = Integer.parseInt(args[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("could not parse width. got '%s'", args[1]));
        }

        if (width < 1) {
            throw new IllegalArgumentException(String.format("width must be more than zero. got '%d'", width));
        }

        final String textIn;
        if ("-".equals(args[2])) {
            try (BufferedInputStream bis = new BufferedInputStream(System.in)) {
                textIn = new String(bis.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IOException("error reading from stdin", e);
            }
        } else {
            textIn = args[2];
        }

        try (Scanner lineScanner = new Scanner(textIn)) {
            while (lineScanner.hasNextLine()) {
                final String line = lineScanner.nextLine();
                try (Scanner wordScanner = new Scanner(line)) {
                    List<String> words = new ArrayList<>();
                    while (wordScanner.hasNext()) {
                        words.add(wordScanner.next());
                    }
                    // TODO: implement justify with calculated whitespaces
                    // instead of assuming single space word separators

                    int sentenceLen = 0;
                    int lastWordIdx = 0;
                    for (int wordIdx = 0; wordIdx < words.size(); wordIdx++) {
                        sentenceLen += words.get(wordIdx).length();
                        // TODO: handle words longer than desired width. break or cut?
                        boolean flushSentence = false;
                        if (wordIdx >= (words.size() - 1)) {
                            flushSentence = true;
                        } else if (wordIdx < (words.size() - 1)) {
                            // check next word
                            if (sentenceLen + words.get(wordIdx + 1).length() > width) {
                                flushSentence = true;
                            } else {
                                // single space separator
                                sentenceLen += 1;
                            }
                        } else {
                            throw new IllegalStateException("should not happen");
                        }

                        if (flushSentence) {
                            // flush line limited by width
                            StringBuilder sb = new StringBuilder();
                            while (lastWordIdx < wordIdx + 1) {
                                sb.append(words.get(lastWordIdx));
                                lastWordIdx++;
                                if (lastWordIdx < (wordIdx + 1)) {
                                    sb.append(' ');
                                }
                            }
                            switch (funcName) {
                                case "l":
                                    out.print(String.format("%-" + width + "s", sb.toString()));
                                    break;
                                case "r":
                                    out.print(String.format("%" + width + "s", sb.toString()));
                                    break;
                                case "c":
                                    final String s = sb.toString();
                                    final double maxPad = (width - s.length());
                                    final int leftPad = (int)Math.floor(maxPad / 2);
                                    final int rightPad = (int)Math.ceil(maxPad / 2);
                                    String lps = "", rps = "";
                                    if(leftPad > 0) {
                                        lps = String.format("%-"+ leftPad+"s", "");
                                    }
                                    if(rightPad > 0) {
                                        rps = String.format("%-"+ rightPad+"s", "");
                                    }
                                    out.printf("%s%s%s", lps, s, rps);
                                    break;
                                default:
                                    throw new IllegalArgumentException(String.format("unknown single-space function '%s'", funcName));
                            }

                            if (wordIdx < (words.size() - 1)) {
                                out.print('\n');
                            }

                            sentenceLen = 0;
                        }
                    }
                }
                if (lineScanner.hasNext()) {
                    out.print('\n');
                }
            }
        }
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    public static void main(String[] args) throws IOException {
        new App(System.out).invoke(args);
    }
}

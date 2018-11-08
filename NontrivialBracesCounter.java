/*
 * File: proj8AbramsDeutschDurstJones.NontrivialBracesCounter.java
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Class: CS 361
 * Project 8
 * Date: November 9, 2018
 */

//package proj8AbramsDeutschDurstJones;

import java.io.*;

/**
 * This class contains a method for counting non-trivial left braces
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class NontrivialBracesCounter {

    public int getNumNontrivialLeftBraces(String fileName) {
        try {
            File file = new File(fileName);
            Reader buffer = new BufferedReader(new FileReader(file));
            return this.getNumLeftBraces(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getNumLeftBraces(Reader reader) throws IOException {
        int braceCounter = 0;
        int r;

        while ((r = reader.read()) != -1) {
            char ch = (char) r;
            switch (ch) {
                case '/':
                    ch = (char) reader.read();
                    if (ch == '*') {
                        ignoreMultiLineComment(reader);
                    } else if (ch == '/') {
                        ignoreSingleLineComment(reader);
                    }
                    break;
                case '\'':
                    ignoreSingleQuotation(reader);
                    break;
                case '\"':
                    ignoreDoubleQuotation(reader);
                    break;
                case '{':
                    braceCounter++;
                    break;
            }
        }
        return braceCounter;
    }

    private void ignoreSingleLineComment(Reader reader) throws IOException {
        int r;
        while (((r = reader.read()) != -1)) {
            if ((char) r == '\n') {
                return;
            }
        }
    }

    private void ignoreMultiLineComment(Reader reader) throws IOException {
        int r;
        boolean startEndComment = false;
        while ((r = reader.read()) != -1) {
            if (((char) r == '/') && (startEndComment)) {
                return;
            }
            startEndComment = (char) r == '*';
        }
    }

    private void ignoreSingleQuotation(Reader reader) throws IOException {
        int r;
        boolean ignoreNext = false;
        while (((r = reader.read()) != -1)) {
            if (ignoreNext) {
                ignoreNext = false;
                continue;
            }
            if ((char) r == '\\') {
                ignoreNext = true;
            }
            if ((char) r == '\'') {
                return;
            }
        }
    }

    private void ignoreDoubleQuotation(Reader reader) throws IOException {
        int r;
        boolean ignoreNext = false;
        while (((r = reader.read()) != -1)) {
            if (ignoreNext) {
                ignoreNext = false;
                continue;
            }
            if ((char) r == '\\') {
                ignoreNext = true;
            }
            if ((char) r == '\"') {
                return;
            }
        }
    }

    public static void main(String[] args) {
        NontrivialBracesCounter nbc = new NontrivialBracesCounter();
        System.out.println(nbc.getNumNontrivialLeftBraces("Test.java"));
    }
}

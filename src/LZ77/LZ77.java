package LZ77;

import java.io.*;
import java.nio.charset.StandardCharsets;
import BitUtils.BitReader;
import BitUtils.BitWriter;


public class LZ77 {

    // 12 bits to store maximum offset distance.
    public static final int MAX_WINDOW_SIZE = (1 << 12) - 1;
    // 4 bits to store length of the match.
    public static final int MAX_LENGTH = 4;
    public static final int MIN_LENGTH = 2;

    // sliding window size
    private int windowSize = LZ77.MAX_WINDOW_SIZE;
    private int bufferSizeBytes = 12;
    private int maxLength = LZ77.MAX_LENGTH;
    private int minLength = LZ77.MIN_LENGTH;


    public LZ77(int m, int n, int k) {
        this.bufferSizeBytes = m;
        this.windowSize = (1 << m) - 1;
        this.maxLength = n;
        this.minLength = k;
    }

    /**
     * Compress given input file as follows
     * <p>
     * A 1 bit followed by eight bits means just copy the eight bits to the output directly.
     * A 0 bit is followed by a pointer followed by a length encoded. This is to be interpreted
     * as "copy the <length> bytes from <pointer> bytes start in the output to the current location" .
     *
     * @param inputFileName  name of the input File name to be compressed
     * @param outputFileName compressed input file file will be written to
     */

    public void compress(String inputFileName, String outputFileName) throws IOException {
        BitWriter out = new BitWriter(outputFileName);
        StringBuffer buffer = new StringBuffer(windowSize);

        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(inputFileName), StandardCharsets.ISO_8859_1);
        BufferedReader inputFile = new BufferedReader(inputStreamReader);

        out.writeBits(bufferSizeBytes, 8);
        out.writeBits(maxLength, 8);
        out.writeBits(minLength, 8);
        try {
            String currentMatch = "";
            int matchIndex = 0, tempIndex = 0;
            int nextChar;

            while ((nextChar = inputFile.read()) != -1) {
                tempIndex = buffer.indexOf(currentMatch + (char) nextChar);
                if (tempIndex != -1 && currentMatch.length() < maxLength) {
                    currentMatch += (char) nextChar;
                    matchIndex = tempIndex;
                } else {
                    // is coded string longer than minimum?
                    if (currentMatch.length() >= minLength) {
                        out.writeBit(0);
                        //System.out.println("OLD: " + matchIndex + ";" + (currentMatch.length()));
                        out.writeBits(matchIndex, bufferSizeBytes);
                        out.writeBits(currentMatch.length(), maxLength);
                        buffer.append(currentMatch); // append to the search buffer
                        currentMatch = "" + (char) nextChar;
                        matchIndex = 0;

                    } else {
                        // otherwise, output chars one at a time from currentMatch until we find a new match or run out of chars
                        currentMatch += (char) nextChar;
                        matchIndex = -1;
                        while (currentMatch.length() > -1 && matchIndex == -1) {
                            //System.out.println("NEW: " + currentMatch.charAt(0) + " " + (int) currentMatch.charAt(0));
                            out.writeBit(1);
                            out.writeByte((byte) currentMatch.charAt(0));
                            buffer.append(currentMatch.charAt(0));
                            currentMatch = currentMatch.substring(1, currentMatch.length());
                            matchIndex = buffer.indexOf(currentMatch);
                        }
                    }
                    if (buffer.length() > windowSize) {
                        buffer = buffer.delete(0, buffer.length() - windowSize);
                    }
                }
            }
            //Check what left
            while (currentMatch.length() > 0) {
                if (currentMatch.length() >= minLength) {
                    out.writeBit(0);
                    //System.out.println("OLD: " + matchIndex + ";" + (currentMatch.length()));
                    out.writeBits(matchIndex, bufferSizeBytes);
                    out.writeBits(currentMatch.length(), maxLength);
                    buffer.append(currentMatch); // append to the search buffer
                    currentMatch = "";
                    matchIndex = 0;

                } else {
                    // otherwise, output chars one at a time from currentMatch until we find a new match or run out of chars
                    matchIndex = -1;
                    while (currentMatch.length() > 0 && matchIndex == -1) {
                        //System.out.println("NEW: " + currentMatch.charAt(0) + " " + (int) currentMatch.charAt(0));
                        out.writeBit(1);
                        out.writeByte((byte) currentMatch.charAt(0));
                        buffer.append(currentMatch.charAt(0));
                        currentMatch = currentMatch.substring(1, currentMatch.length());
                        matchIndex = buffer.indexOf(currentMatch);
                    }
                }
                if (buffer.length() > windowSize) {
                    buffer = buffer.delete(0, buffer.length() - windowSize);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.flush();
        }
    }


    /**
     * decompress input file and writes to output file
     *
     * @param inputFileName  compressed input file
     * @param outputFileName decompressed output file
     * @throws IOException
     */
    public void decompress(String inputFileName, String outputFileName) throws IOException {

        BitReader bitReader = new BitReader(inputFileName);
        FileOutputStream out = new FileOutputStream(outputFileName);
        int fileLen = bitReader.length() * 8;

        int bufferSizeBytes = bitReader.readByte();
        int windowSize = (1 << bufferSizeBytes) - 1;
        int maxLength = bitReader.readByte();
        int minLength = bitReader.readByte();
        fileLen -= 24;
        StringBuffer buffer = new StringBuffer(windowSize);
        while (fileLen >= 8) {
            int flag = bitReader.readBit();
            //System.out.println("Flag: " + flag);
            if (flag == 1) {
                int s = bitReader.readBits(8);
                //System.out.println("Char: " + (char) s);
                buffer.append((char) s);
                out.write(s);
                fileLen -= 9;
            } else {

                int offsetValue = bitReader.readBits(bufferSizeBytes);
                int lengthValue = bitReader.readBits(maxLength);

                //System.out.println("<" + offsetValue + ";" + lengthValue + ">");
                int start = offsetValue;
                int end = start + lengthValue;

                String temp = buffer.substring(start, end);
                for (int k = 0; k < temp.length(); k++) {
                    out.write(temp.charAt(k));
                }
                buffer.append(temp);
                fileLen -= 17;
            }
            if (buffer.length() > windowSize) {
                buffer = buffer.delete(0, buffer.length() - windowSize);
            }
        }
    }

}

package LZ77;

import java.io.*;
import java.nio.charset.StandardCharsets;

import BitUtils.BitReader;
import BitUtils.BitWriter;


public class LZ77 {

    // 12 bits to store maximum offset distance.
    public static final int MAX_WINDOW_SIZE = (1 << 12) - 1;
    // 4 bits to store length of the match.
    public static final int MAX_LENGTH = (1 << 4) - 1;
    public static final int MIN_LENGTH = 2;

    // sliding window size
    private int windowSize;
    private int maxLength;
    private int minLength;

    public LZ77(int windowSize) {
        this.windowSize = Math.min(windowSize, MAX_WINDOW_SIZE);
    }

    public LZ77(int windowSize, int maxLength, int minLength) {
        this.windowSize = windowSize;
        this.maxLength = maxLength;
        this.minLength = minLength;
    }

    /**
     * Compress given input file as follows
     * <p>
     * A 1 bit followed by eight bits means just copy the eight bits to the output directly.
     * A 0 bit is followed by a pointer of 12 bits followed by a length encoded in 4 bits. This is to be interpreted as "copy the <length> bytes from <pointer> bytes ago in the output to the current location" .
     *
     * @param inputFileName  name of the input File name to be compressed
     * @param outputFileName compressed input file file will be written to
     */

    public void compress(String inputFileName, String outputFileName) throws IOException {
        BitWriter out = new BitWriter(outputFileName);
        StringBuffer buffer = new StringBuffer(windowSize);
        BitReader bitReader = new BitReader(inputFileName);
        InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(inputFileName), StandardCharsets.ISO_8859_1);
        BufferedReader inputFile = new BufferedReader(inputStreamReader);
        //Reader inputFile = new BufferedReader(new FileReader(inputFileName));
        try {

            String currentMatch = "";
            int matchIndex = 0, tempIndex = 0;
            int nextChar;
            String concat = currentMatch;

            while ((nextChar = inputFile.read()) != -1) {
                tempIndex = buffer.indexOf(currentMatch + (char) nextChar);
                if (tempIndex != -1 && currentMatch.length() < maxLength) {
                    currentMatch += (char) nextChar;
                    matchIndex = tempIndex;

                } else {
                    //System.out.println("Nerado sutapimo: " + currentMatch  + (char) nextChar);
                    // is coded string longer than minimum?
                    if (currentMatch.length() >= minLength) {
                        out.writeBit(0);
                        //System.out.println("OLD: " + matchIndex + ";" + (currentMatch.length()));
                        out.writeBits(matchIndex, 12);
                        out.writeBits(currentMatch.length(), 4);
                        buffer.append(currentMatch); // append to the search buffer
                        currentMatch = "" + (char) nextChar;
                        matchIndex = 0;

                    } else {
                        // otherwise, output chars one at a time from currentMatch until we find a new match or run out of chars
                        currentMatch += (char) nextChar;
                        matchIndex = -1;
                        while (currentMatch.length() > -1 && matchIndex == -1) {
                            //  System.out.println("NEW: " + currentMatch.charAt(0) + " " + (int) currentMatch.charAt(0));
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


            for (int i = 0; i < currentMatch.length(); ++i) {
                //System.out.println("NEW: " + currentMatch.charAt(i) + " " + (int) currentMatch.charAt(i));
                out.writeBit(1);
                out.writeByte((byte) currentMatch.charAt(i));
            }
            /*
            System.out.println("------------------");
            System.out.println("Last BUFFER: " + buffer);
            System.out.println("CURR " + currentMatch);
            System.out.println("CON " + concat);
            */
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
        StringBuffer buffer = new StringBuffer(windowSize);
        BitReader bitReader = new BitReader(inputFileName);
        FileOutputStream out = new FileOutputStream(outputFileName);
        //System.out.println("Buffer size: " + buffer.capacity());
        int fileLen = bitReader.length() * 8;
        while (fileLen > 8) {// when end of file reached, inputStream throws End Of file Exception
            //System.out.println("Buffer: " + buffer.toString());
            int flag = bitReader.readBit();
            //System.out.println("Flag: " + flag);
            if (flag == 1) {
                char s = (char) bitReader.readBits(8);
                //System.out.println("Char: " + s);
                buffer.append(s);
                out.write(s);
                fileLen -= 9;
            } else {

                byte bytes[] = new byte[2];
                bytes[0] = (byte) bitReader.readByte();
                bytes[1] = (byte) bitReader.readByte();

                int offsetValue = getOffsetFromBytes(bytes, 12);
                int lengthValue = getLengthFromBytes(bytes, 12, 4);

                //System.out.println("<" + offsetValue + ";" + lengthValue + ">");
                int start = offsetValue;
                int end = start + lengthValue;

                String temp = buffer.substring(start, end);
                //System.out.println("Temp: " + temp);
                for (int k = 0; k < temp.length(); k++) {
                    out.write(temp.charAt(k));
                    // System.out.print(temp.charAt(k));
                }
                //System.out.println();
                buffer.append(temp);
                if (buffer.length() > windowSize) {
                    buffer = buffer.delete(0, buffer.length() - windowSize);
                }
                temp = "";
                fileLen -= 17;
            }
        }
    }


    private static int getLengthFromBytes(byte[] bytes, int offsetSize, int lengthSize) {
        StringBuilder sb = new StringBuilder(); // sukuriam nauja stringa bitams sudeti
        for (byte c : bytes) {
            for (int n = 128; n > 0; n >>= 1) {
                if ((c & n) == 0)
                    sb.append('0');
                else sb.append('1');
            }
        }

        String sBytes = sb.toString(); // sumetam bitus i stringa
        String length = sBytes.substring(offsetSize, offsetSize + lengthSize); // nustatom ilgio dydi

        int currentLenght = 0;

        for (int i = length.length() - 1; i >= 0; i--) { //paverciam i desimtaine ilgius
            if (length.charAt(i) != '0')
                currentLenght += (int) Math.pow(2, (length.length() - 1) - i);
            //System.out.println((length.length()-1)-i + "  " + length.charAt(i));
        }
        return currentLenght;
    }

    private static int getOffsetFromBytes(byte[] bytes, int offsetSize) {
        StringBuilder sb = new StringBuilder(); // sukuriam nauja stringa bitams sudeti
        for (byte c : bytes) {
            for (int n = 128; n > 0; n >>= 1) {
                if ((c & n) == 0)
                    sb.append('0');
                else sb.append('1');
            }
        }

        String sBytes = sb.toString(); // sumetam bitus i stringa
        String offset = sBytes.substring(0, offsetSize); // nustatom offset dydi

        int currentOffset = 0;

        for (int i = offset.length() - 1; i >= 0; i--) { // paverciam i desimtaine offsetus
            if (offset.charAt(i) != '0')
                currentOffset += (int) Math.pow(2, (offset.length() - 1) - i);
        }
        return currentOffset;
    }

}

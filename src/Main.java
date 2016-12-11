import LZ77.LZ77;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {

        int windowSize = LZ77.MAX_WINDOW_SIZE, maxLength = LZ77.MAX_LENGTH, minLength = LZ77.MIN_LENGTH;

        if (args.length > 2) {
            try {
                windowSize = Integer.valueOf(args[2]);
                maxLength = Integer.valueOf(args[3]);
                minLength = Integer.valueOf(args[4]);
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid numbers or use default values.");
                helpMenu();
                return;
            }
        }

        LZ77 lz77 = new LZ77(windowSize, maxLength, minLength);

        String fileName = args[1];

        if(!Files.exists(Paths.get(fileName))){
            System.out.println("File doesn't exists");
        }

        StringBuilder fileNameBuilder = new StringBuilder();
        int extension = fileName.lastIndexOf(".");
        switch (args[0]) {
            case "c":
                if (extension > -1) {
                    fileNameBuilder.append(fileName.substring(0, extension));
                    fileNameBuilder.append("-c");
                    fileNameBuilder.append(fileName.substring(extension));
                } else {
                    fileNameBuilder.append(fileName);
                    fileNameBuilder.append("-c");
                }
                String compressedFileName = fileNameBuilder.toString();
                checkFile(compressedFileName);
                compression(lz77, fileName, compressedFileName);
                break;
            case "d":
                if (extension > -1) {
                    fileNameBuilder.append(fileName.substring(0, extension));
                    fileNameBuilder.append("-d");
                    fileNameBuilder.append(fileName.substring(extension));
                } else {
                    fileNameBuilder.append(fileName);
                    fileNameBuilder.append("-d");
                }
                String decompressedFileName = fileNameBuilder.toString();
                checkFile(decompressedFileName);
                decompression(lz77, fileName, decompressedFileName);
                break;
            default:
                helpMenu();
                break;
        }

    }

    private static void checkFile(String filename) throws IOException {
        if(Files.exists(Paths.get(filename))){
            Files.delete(Paths.get(filename));
        }
    }

    private static void compression(LZ77 lz77, String inputFileName, String compressedFileName) throws IOException {
        System.out.println("Compression started...");
        long startTime = System.currentTimeMillis();
        lz77.compress(inputFileName, compressedFileName);
        long endTime = System.currentTimeMillis();
        System.out.println("Compression Done in : " + (endTime - startTime) + " ms");
    }

    private static void decompression(LZ77 lz77, String inputFileName, String decompressedFileName) throws IOException {
        long startTime = System.currentTimeMillis();
        System.out.println("Decompression started...");
        lz77.decompress(inputFileName, decompressedFileName);
        long endTime = System.currentTimeMillis();
        System.out.println("Decompression Done in: " + (endTime - startTime) + " ms");
    }

    private static void helpMenu(){
        System.out.println("Usage : java -jar LZ77.jar c|d inputfile [windowSize maximumMatch minimumMatch]");
        System.out.println("windowsize is optional. default size is :" + LZ77.MAX_WINDOW_SIZE + ".if Window size gets bigger, then compression time increases.");
        System.out.println("Compressed file will be written into input_file_name-compressed.extension");
        System.out.println("Decompressed file will be written into input_file_name-decompressed.extension");
        System.exit(1);
    }
}


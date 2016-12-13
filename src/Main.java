import LZ77.LZ77;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException {

        int m = 12, n = 4, k = 2;

        if (args.length > 2) {
            try {
                m = Integer.valueOf(args[2]);
                n = Integer.valueOf(args[3]);
                k = Integer.valueOf(args[4]);
            } catch (NumberFormatException e) {
                System.out.println("Please enter valid numbers or use default values.");
                helpMenu();
                return;
            }
        }

        LZ77 lz77 = new LZ77(m, n, k);

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
        System.out.println("Compression done in: " + (endTime - startTime)/1000 + "s (" +(endTime - startTime)+"ms)");
    }

    private static void decompression(LZ77 lz77, String inputFileName, String decompressedFileName) throws IOException {

        System.out.println("Decompression started...");
        long startTime = System.currentTimeMillis();
        lz77.decompress(inputFileName, decompressedFileName);
        long endTime = System.currentTimeMillis();
        System.out.println("Decompression done in: " + (endTime - startTime)/1000 + "s (" +(endTime - startTime)+"ms)");
    }

    private static void helpMenu(){
        System.out.println("Usage : java -jar LZ77.jar c|d inputFileName [history_size_bits maximum_Match_size_bits minimumMatch]");
        System.out.println("History is optional. Default size is :" + LZ77.MAX_WINDOW_SIZE+1 + ". If history size gets bigger, then compression time increases.");
        System.out.println("Compressed file will be written into input_file_name-compressed.extension");
        System.out.println("Decompressed file will be written into input_file_name-decompressed.extension");
        System.exit(1);
    }
}


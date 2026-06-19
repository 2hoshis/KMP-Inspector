import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        String fileName = args.length >= 1 ? args[0] : "course.kmp";
        byte[] data = Files.readAllBytes(Path.of(fileName));

        if (data.length < 0x4C) {
            System.out.println("File is too small. This may not be a KMP file.");
            return;
        }

        String magic = readAscii(data, 0x00, 4);
        int fileSize = readU32BE(data, 0x04);
        int sectionCount = readU16BE(data, 0x08);
        int headerSize = readU16BE(data, 0x0A);
        int version = readU32BE(data, 0x0C);

        System.out.println("KMP Inspector");
        System.out.println("File: " + fileName);
        System.out.println("Magic: " + magic);
        System.out.println("File size in header: " + fileSize);
        System.out.println("Actual file size: " + data.length);
        System.out.println("Section count: " + sectionCount);
        System.out.println("Header size: " + headerSize);
        System.out.println("Version: " + version);
        System.out.println();

        if (!magic.equals("RKMD")) {
            System.out.println("Warning: Magic is not RKMD. This may not be a KMP file.");
            return;
        }

        int sectionTableOffset = 0x10;

        for (int i = 0; i < sectionCount; i++) {
    int offsetPos = sectionTableOffset + i * 4;

    if (offsetPos + 4 > data.length) {
        System.out.println("Section table points outside the file.");
        break;
    }

    int relativeOffset = readU32BE(data, offsetPos);
    int sectionOffset = headerSize + relativeOffset;

    if (sectionOffset < 0 || sectionOffset + 8 > data.length) {
        System.out.println("[" + i + "] invalid offset: 0x" + toHex(sectionOffset));
        continue;
    }

    String sectionName = readAscii(data, sectionOffset, 4);
    int entryCount = readU16BE(data, sectionOffset + 4);

    System.out.println("[" + i + "] "
            + sectionName
            + " offset=0x" + toHex(sectionOffset)
            + " entries=" + entryCount);
}
    }

    static String readAscii(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append((char) data[offset + i]);
        }

        return sb.toString();
    }

    static int readU16BE(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 8)
                | (data[offset + 1] & 0xFF);
    }

    static int readU32BE(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    static String toHex(int value) {
        return String.format("%08X", value);
    }
}
package co.casterlabs.flv4j.actionscript.io;

public class ASAssert {

    public static void bit(int value, String name) {
        assert value == 0 || value == 1 : name + " must be between 0 and 1";
    }

    public static void u2(int value, String name) {
        assert value >= 0 && value <= 3 : name + " must be between 0 and 3";
    }

    public static void u4(int value, String name) {
        assert value >= 0 && value <= 0xF : name + " must be between 0 and 15";
    }

    public static void u8(int value, String name) {
        assert value >= 0 && value <= 0xFF : name + " must be between 0 and 255";
    }

    public static void u16(int value, String name) {
        assert value >= 0 && value <= 0xFFFF : name + " must be between 0 and 65535";
    }

    public static void u24(int value, String name) {
        assert value >= 0 && value <= 0xFFFFFF : name + " must be between 0 and 16777215";
    }

    public static void u29(int value, String name) {
        assert value >= 0 && value <= 536870911 : name + " must be between 0 and 536870911";
    }

    public static void u32(long value, String name) {
        assert value >= 0 && value <= 0xFFFFFFFFL : name + " must be between 0 and 4294967295";
    }

}

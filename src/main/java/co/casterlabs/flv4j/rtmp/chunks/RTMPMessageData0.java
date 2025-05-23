package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.flv4j.EndOfStreamException;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=24
// I'm assuming this is the same (or similar) to Command0
public record RTMPMessageData0(
    List<AMF0Type> arguments
) implements RTMPMessage {

    @Override
    public int rawType() {
        return 18;
    }

    @Override
    public int size() {
        int argumentSize = 0;
        for (AMF0Type argument : this.arguments) {
            argumentSize += argument.size();
        }
        return argumentSize;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        for (AMF0Type argument : this.arguments) {
            argument.serialize(writer);
        }
    }

    /**
     * @apiNote You MUST use a limited reader, otherwise you will get an infinite
     *          loop or garbage data.
     */
    public static RTMPMessageData0 parse(ASReader reader) throws IOException {
        List<AMF0Type> arguments = new LinkedList<>();
        while (true) {
            try {
                arguments.add(AMF0Type.parse(reader));
            } catch (EndOfStreamException e) {
                break;
            }
        }

        return new RTMPMessageData0(Collections.unmodifiableList(arguments));
    }

    @Override
    public final String toString() {
        return String.format("RTMPMessageData0%s", this.arguments);
    }

}

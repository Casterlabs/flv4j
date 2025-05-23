package co.casterlabs.flv4j.rtmp.chunks;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.flv4j.EndOfStreamException;
import co.casterlabs.flv4j.actionscript.amf0.AMF0Type;
import co.casterlabs.flv4j.actionscript.amf0.Number0;
import co.casterlabs.flv4j.actionscript.amf0.String0;
import co.casterlabs.flv4j.actionscript.io.ASReader;
import co.casterlabs.flv4j.actionscript.io.ASWriter;

// https://rtmp.veriskope.com/pdf/rtmp_specification_1.0.pdf#page=24
public record RTMPMessageCommand0(
    String0 commandName,
    Number0 transactionId,
    List<AMF0Type> arguments
) implements RTMPMessage {

    @Override
    public int rawType() {
        return 20;
    }

    @Override
    public int size() {
        int argumentSize = 0;
        for (AMF0Type argument : this.arguments) {
            argumentSize += argument.size();
        }
        return this.commandName.size() + this.transactionId.size() + argumentSize;
    }

    @Override
    public void serialize(ASWriter writer) throws IOException {
        this.commandName.serialize(writer);
        this.transactionId.serialize(writer);
        for (AMF0Type argument : this.arguments) {
            argument.serialize(writer);
        }
    }

    /**
     * @apiNote You MUST use a limited reader, otherwise you will get an infinite
     *          loop or garbage data.
     */
    public static RTMPMessageCommand0 parse(ASReader reader) throws IOException {
        String0 commandName = AMF0Type.parse(reader);
        Number0 transactionId = AMF0Type.parse(reader);

        List<AMF0Type> arguments = new LinkedList<>();
        while (true) {
            try {
                arguments.add(AMF0Type.parse(reader));
            } catch (EndOfStreamException e) {
                break;
            }
        }

        return new RTMPMessageCommand0(commandName, transactionId, Collections.unmodifiableList(arguments));
    }

    @Override
    public final String toString() {
        return String.format("RTMPMessageCommand0[%s(%s)]", this.commandName.value(), this.arguments);
    }

}

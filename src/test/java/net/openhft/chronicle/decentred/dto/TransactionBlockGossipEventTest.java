package net.openhft.chronicle.decentred.dto;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.time.UniqueMicroTimeProvider;
import net.openhft.chronicle.decentred.util.KeyPair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransactionBlockGossipEventTest {


    @Test
    public void marshalUnmarshal() {
        final KeyPair kp = new KeyPair(1);

        final Bytes bytes = Bytes.allocateElasticDirect(1000);

        final TransactionBlockGossipEvent expected = new TransactionBlockGossipEvent()
            .messageType(0xFFF1)
            .protocol(17)
            .timestampUS(UniqueMicroTimeProvider.INSTANCE.currentTimeMicros())
            .blockNumber(42)
            .chainAddress(43)
            ;

        expected.addressToBlockNumberMap().justPut(1, 16);
        expected.addressToBlockNumberMap().justPut((192L << 56) + (168L << 48) + (1L << 40) + (147L << 32)+ (10000L << 16), 17); // 192.168.1.147:10000
        expected.sign(kp.secretKey);

        System.out.println("expected = " + expected);
        System.out.println(expected.toHexString());

        expected.writeMarshallable(bytes);

        //bytes.readPosition(0);
        TransactionBlockGossipEvent actual = new TransactionBlockGossipEvent();
        actual.readMarshallable(bytes);

        final String actualString = actual.toString();

        int length = actualString.indexOf("addressToBlockNumberMap: {");

        assertEquals(expected.toString().substring(0, length), actualString.substring(0, length));

        assertTrue(actualString.contains("\"192.168.1.147:10000\": 17"));
        assertTrue(actualString.contains("\"0.0.0.0:0:1\": 16"));

    }


}

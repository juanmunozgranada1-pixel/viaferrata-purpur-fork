package com.viaferrata.fork.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ViaFerrataPipelineEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        // Leemos el ID del paquete sin avanzar el índice del buffer original
        int readerIndex = msg.readerIndex();
        int packetId = readVarInt(msg);

        // Si es un paquete de animación de swing o interacción en 1.7.10
        if (packetId == 0x0A || packetId == 0x0B) { 
            // Reutilizamos el buffer de salida directo de Netty (ByteBuf Pooling)
            out.writeBytes(msg);
            return;
        }

        // Restablecemos el índice para el resto del pipeline si no requiere bypass rápido
        msg.readerIndex(readerIndex);
        out.writeBytes(msg);
    }

    private int readVarInt(ByteBuf buffer) {
        int i = 0;
        int maxRead = Math.min(5, buffer.readableBytes());
        for (int j = 0; j < maxRead; j++) {
            byte b = buffer.readByte();
            i |= (b & 0x7F) << (j * 7);
            if ((b & 0x80) != 128) {
                return i;
            }
        }
        return i;
    }
}